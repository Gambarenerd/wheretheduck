import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { sendFcmMessage } from "../util/fcmSender";

export const respondContactInvite = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Must be logged in");
  }

  const { inviteId, accepted } = data;
  if (!inviteId || accepted === undefined) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "inviteId and accepted required"
    );
  }

  const db = admin.firestore();
  const userId = context.auth.uid;

  // 1. Get invite
  const inviteRef = db.collection("contact_invites").doc(inviteId);
  const inviteDoc = await inviteRef.get();
  if (!inviteDoc.exists) {
    throw new functions.https.HttpsError("not-found", "Invite not found");
  }

  const invite = inviteDoc.data()!;
  if (invite.toUserId !== userId) {
    throw new functions.https.HttpsError(
      "permission-denied",
      "Only the recipient can respond"
    );
  }

  if (invite.status !== "pending") {
    return { status: "already_responded" };
  }

  const fromUserId = invite.fromUserId;

  if (accepted) {
    // Get both user profiles
    const [fromUserDoc, toUserDoc] = await Promise.all([
      db.collection("users").doc(fromUserId).get(),
      db.collection("users").doc(userId).get(),
    ]);

    const fromUser = fromUserDoc.exists ? fromUserDoc.data()! : {};
    const toUser = toUserDoc.exists ? toUserDoc.data()! : {};

    const batch = db.batch();

    // Update invite status
    batch.update(inviteRef, {
      status: "accepted",
      respondedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    // Create bilateral contacts
    batch.set(
      db.collection("users").doc(fromUserId).collection("contacts").doc(userId),
      {
        displayName: toUser.displayName || "",
        email: toUser.email || "",
        photoUrl: toUser.photoUrl || "",
        addedAt: admin.firestore.FieldValue.serverTimestamp(),
      }
    );
    batch.set(
      db.collection("users").doc(userId).collection("contacts").doc(fromUserId),
      {
        displayName: fromUser.displayName || "",
        email: fromUser.email || "",
        photoUrl: fromUser.photoUrl || "",
        addedAt: admin.firestore.FieldValue.serverTimestamp(),
      }
    );

    await batch.commit();

    // Notify sender
    if (fromUser.fcmToken) {
      const responderName = toUser.displayName || "Qualcuno";
      await sendFcmMessage(
        fromUser.fcmToken,
        {
          type: "contact_accepted",
          fromDisplayName: responderName,
        },
        "Contatto accettato!",
        `${responderName} ha accettato il tuo invito`
      );
    }
  } else {
    // Reject
    await inviteRef.update({
      status: "rejected",
      respondedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    // Notify sender
    const fromUserDoc = await db.collection("users").doc(fromUserId).get();
    const toUserDoc = await db.collection("users").doc(userId).get();
    if (fromUserDoc.exists) {
      const fromUser = fromUserDoc.data()!;
      const responderName = toUserDoc.exists
        ? toUserDoc.data()!.displayName || "Qualcuno"
        : "Qualcuno";

      if (fromUser.fcmToken) {
        await sendFcmMessage(
          fromUser.fcmToken,
          {
            type: "contact_rejected",
            fromDisplayName: responderName,
          },
          "Invito rifiutato",
          `${responderName} ha rifiutato il tuo invito`
        );
      }
    }
  }

  return { status: accepted ? "accepted" : "rejected" };
});
