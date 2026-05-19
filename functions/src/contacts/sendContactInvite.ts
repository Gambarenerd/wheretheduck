import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { sendFcmMessage } from "../util/fcmSender";

export const sendContactInvite = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Must be logged in");
  }

  const { email } = data;
  if (!email) {
    throw new functions.https.HttpsError("invalid-argument", "email required");
  }

  const db = admin.firestore();
  const senderId = context.auth.uid;
  const normalizedEmail = email.trim().toLowerCase();

  // 1. Get sender info
  const senderDoc = await db.collection("users").doc(senderId).get();
  if (!senderDoc.exists) {
    throw new functions.https.HttpsError("not-found", "Sender not found");
  }
  const sender = senderDoc.data()!;

  // 2. Find target user by email
  const usersSnapshot = await db
    .collection("users")
    .where("email", "==", normalizedEmail)
    .limit(1)
    .get();

  if (usersSnapshot.empty) {
    return { status: "user_not_found" };
  }

  const targetDoc = usersSnapshot.docs[0];
  const targetId = targetDoc.id;
  const target = targetDoc.data();

  // Can't add yourself
  if (targetId === senderId) {
    return { status: "cannot_add_self" };
  }

  // 3. Check if already contacts
  const existingContact = await db
    .collection("users").doc(senderId)
    .collection("contacts").doc(targetId)
    .get();

  if (existingContact.exists) {
    return { status: "already_contact" };
  }

  // 4. Check if invite already pending (in either direction)
  const [sentInvite, receivedInvite] = await Promise.all([
    db.collection("contact_invites")
      .where("fromUserId", "==", senderId)
      .where("toUserId", "==", targetId)
      .where("status", "==", "pending")
      .limit(1)
      .get(),
    db.collection("contact_invites")
      .where("fromUserId", "==", targetId)
      .where("toUserId", "==", senderId)
      .where("status", "==", "pending")
      .limit(1)
      .get(),
  ]);

  if (!sentInvite.empty) {
    return { status: "already_invited" };
  }

  // If they already invited us, auto-accept
  if (!receivedInvite.empty) {
    const existingInviteRef = receivedInvite.docs[0].ref;
    const batch = db.batch();

    // Accept the existing invite
    batch.update(existingInviteRef, {
      status: "accepted",
      respondedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    // Create bilateral contacts
    batch.set(db.collection("users").doc(senderId).collection("contacts").doc(targetId), {
      displayName: target.displayName || "",
      email: target.email || normalizedEmail,
      photoUrl: target.photoUrl || "",
      motto: target.motto || "",
      addedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
    batch.set(db.collection("users").doc(targetId).collection("contacts").doc(senderId), {
      displayName: sender.displayName || "",
      email: sender.email || "",
      photoUrl: sender.photoUrl || "",
      motto: sender.motto || "",
      addedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    await batch.commit();
    return { status: "auto_accepted" };
  }

  // 5. Create invite
  const inviteRef = db.collection("contact_invites").doc();
  await inviteRef.set({
    fromUserId: senderId,
    fromDisplayName: sender.displayName || "",
    fromEmail: sender.email || "",
    fromPhotoUrl: sender.photoUrl || "",
    toUserId: targetId,
    toEmail: target.email || normalizedEmail,
    status: "pending",
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  // 6. Send FCM notification
  if (target.fcmToken) {
    await sendFcmMessage(
      target.fcmToken,
      {
        type: "contact_invite",
        inviteId: inviteRef.id,
        fromUserId: senderId,
        fromDisplayName: sender.displayName || "Qualcuno",
      },
      "Nuovo contatto!",
      `${sender.displayName || "Qualcuno"} vuole aggiungerti come contatto`
    );
  }

  return { status: "sent" };
});
