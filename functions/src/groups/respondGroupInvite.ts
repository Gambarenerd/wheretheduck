import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { sendFcmMessage } from "../util/fcmSender";

export const respondGroupInvite = functions.https.onCall(async (data, context) => {
  // 1. Verify auth
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Must be logged in");
  }

  const { groupId, inviteId, accepted } = data;
  if (!groupId || !inviteId || accepted === undefined) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "groupId, inviteId, and accepted required"
    );
  }

  const db = admin.firestore();
  const userId = context.auth.uid;

  // 2. Read invite and verify ownership
  const inviteRef = db
    .collection("groups").doc(groupId)
    .collection("invites").doc(inviteId);
  const inviteDoc = await inviteRef.get();

  if (!inviteDoc.exists) {
    throw new functions.https.HttpsError("not-found", "Invite not found");
  }

  const invite = inviteDoc.data()!;

  if (invite.invitedUserId !== userId) {
    throw new functions.https.HttpsError("permission-denied", "Not your invite");
  }

  if (invite.status !== "pending") {
    throw new functions.https.HttpsError("failed-precondition", "Invite already responded");
  }

  // 3. Get user info
  const userDoc = await db.collection("users").doc(userId).get();
  const userData = userDoc.data();

  if (accepted) {
    // 4a. Accept: update invite + add member
    await inviteRef.update({
      status: "accepted",
      respondedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    await db
      .collection("groups").doc(groupId)
      .collection("members").doc(userId)
      .set({
        userId: userId,
        displayName: userData?.displayName || "",
        photoUrl: userData?.photoUrl || "",
        role: "member",
        joinedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

    // Notify admin
    const adminDoc = await db.collection("users").doc(invite.invitedBy).get();
    const adminData = adminDoc.data();
    if (adminData?.fcmToken) {
      await sendFcmMessage(
        adminData.fcmToken,
        {
          type: "invite_accepted",
          groupId,
          userId,
        },
        "Invito accettato!",
        `${userData?.displayName || "Qualcuno"} ha accettato l'invito a ${invite.groupName || "il gruppo"}!`
      );
    }

    return { status: "accepted" };
  } else {
    // 4b. Reject: update invite
    await inviteRef.update({
      status: "rejected",
      respondedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    // Notify admin
    const adminDoc = await db.collection("users").doc(invite.invitedBy).get();
    const adminData = adminDoc.data();
    if (adminData?.fcmToken) {
      await sendFcmMessage(
        adminData.fcmToken,
        {
          type: "invite_rejected",
          groupId,
          userId,
        },
        "Invito rifiutato",
        `${userData?.displayName || "Qualcuno"} ha rifiutato l'invito a ${invite.groupName || "il gruppo"}`
      );
    }

    return { status: "rejected" };
  }
});
