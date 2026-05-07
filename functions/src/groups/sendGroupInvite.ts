import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { sendFcmMessage } from "../util/fcmSender";

export const sendGroupInvite = functions.https.onCall(async (data, context) => {
  // 1. Verify auth
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Must be logged in");
  }

  const { groupId, email } = data;
  if (!groupId || !email) {
    throw new functions.https.HttpsError("invalid-argument", "groupId and email required");
  }

  const db = admin.firestore();
  const senderId = context.auth.uid;

  // 2. Verify sender is admin of the group
  const memberDoc = await db
    .collection("groups").doc(groupId)
    .collection("members").doc(senderId)
    .get();

  if (!memberDoc.exists || memberDoc.data()?.role !== "admin") {
    throw new functions.https.HttpsError("permission-denied", "Only admins can invite");
  }

  // 3. Find user by email
  const usersQuery = await db
    .collection("users")
    .where("email", "==", email.toLowerCase().trim())
    .limit(1)
    .get();

  if (usersQuery.empty) {
    return { status: "user_not_found", inviteId: "" };
  }

  const invitedUserDoc = usersQuery.docs[0];
  const invitedUser = invitedUserDoc.data();
  const invitedUserId = invitedUserDoc.id;

  // 4. Check if already a member
  const existingMember = await db
    .collection("groups").doc(groupId)
    .collection("members").doc(invitedUserId)
    .get();

  if (existingMember.exists) {
    return { status: "already_member", inviteId: "" };
  }

  // 5. Check if already invited (pending)
  const existingInvite = await db
    .collection("groups").doc(groupId)
    .collection("invites")
    .where("invitedUserId", "==", invitedUserId)
    .where("status", "==", "pending")
    .limit(1)
    .get();

  if (!existingInvite.empty) {
    return { status: "already_invited", inviteId: "" };
  }

  // 6. Get sender info and group info
  const senderDoc = await db.collection("users").doc(senderId).get();
  const senderData = senderDoc.data();
  const groupDoc = await db.collection("groups").doc(groupId).get();
  const groupData = groupDoc.data();

  // 7. Create invite
  const inviteRef = db
    .collection("groups").doc(groupId)
    .collection("invites").doc();

  await inviteRef.set({
    groupId,
    invitedEmail: email.toLowerCase().trim(),
    invitedUserId,
    invitedDisplayName: invitedUser.displayName || "",
    invitedBy: senderId,
    invitedByDisplayName: senderData?.displayName || "",
    groupName: groupData?.name || "",
    status: "pending",
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    respondedAt: null,
  });

  // 8. Send FCM to invited user
  if (invitedUser.fcmToken) {
    await sendFcmMessage(
      invitedUser.fcmToken,
      {
        type: "group_invite",
        groupId,
        inviteId: inviteRef.id,
        groupName: groupData?.name || "",
      },
      "Nuovo invito!",
      `${senderData?.displayName || "Qualcuno"} ti ha invitato in ${groupData?.name || "un gruppo"}`
    );
  }

  return { status: "sent", inviteId: inviteRef.id };
});
