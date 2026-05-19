import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { sendFcmMessage } from "../util/fcmSender";

const DEFAULT_ANIMALS: Record<string, string> = {
  light: "cricket",
  medium: "duck",
  heavy: "goose",
};

export const sendStarnazzo = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Must be logged in");
  }

  const { toUserId, level, animalType } = data;
  if (!toUserId || !level) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "toUserId and level required"
    );
  }

  const db = admin.firestore();
  const senderId = context.auth.uid;

  // 1. Get sender info
  const senderDoc = await db.collection("users").doc(senderId).get();
  if (!senderDoc.exists) {
    throw new functions.https.HttpsError("not-found", "Sender not found");
  }
  const sender = senderDoc.data()!;

  // 2. Verify they are contacts
  const contactDoc = await db
    .collection("users").doc(senderId)
    .collection("contacts").doc(toUserId)
    .get();

  if (!contactDoc.exists) {
    throw new functions.https.HttpsError(
      "permission-denied",
      "Must be contacts to send starnazzo"
    );
  }

  // 3. Get receiver info
  const receiverDoc = await db.collection("users").doc(toUserId).get();
  if (!receiverDoc.exists) {
    throw new functions.https.HttpsError("not-found", "Receiver not found");
  }
  const receiver = receiverDoc.data()!;

  // 3b. Check if sender is muted by receiver
  const muteDoc = await db
    .collection("users")
    .doc(toUserId)
    .collection("muted")
    .doc(senderId)
    .get();

  if (muteDoc.exists) {
    const muteData = muteDoc.data()!;
    const muteUntil = muteData.muteUntil?.toDate();
    if (muteUntil && muteUntil > new Date()) {
      const remainingMs = muteUntil.getTime() - Date.now();
      const remainingMin = Math.ceil(remainingMs / 60000);
      return {
        alertId: null,
        status: "muted",
        remainingMinutes: remainingMin,
        message: `${receiver.displayName || "L'utente"} ti ha bloccato per ancora ${remainingMin} minuti`,
      };
    } else {
      // Mute expired, clean up
      await muteDoc.ref.delete();
    }
  }

  // 4. Resolve animal type
  const resolvedAnimal = animalType || DEFAULT_ANIMALS[level] || "duck";

  // 5. Create alert document
  const alertRef = db.collection("alerts").doc();
  await alertRef.set({
    fromUserId: senderId,
    fromDisplayName: sender.displayName || "",
    toUserId,
    toDisplayName: receiver.displayName || "",
    groupId: null,
    broadcastId: null,
    starnazzoLevel: level,
    animalType: resolvedAnimal,
    status: "sending",
    response: null,
    muteDuration: null,
    isRevenge: false,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    expiresAt: new Date(Date.now() + 14 * 24 * 60 * 60 * 1000),
    deliveredAt: null,
    respondedAt: null,
  });

  // 6. Send FCM
  let delivered = false;
  if (receiver.fcmToken) {
    const senderName = sender.displayName || "Qualcuno";
    delivered = await sendFcmMessage(
      receiver.fcmToken,
      {
        type: "starnazzo",
        alertId: alertRef.id,
        fromUserId: senderId,
        fromDisplayName: senderName,
        fromPhotoUrl: sender.photoUrl || "",
        level,
        animalType: resolvedAnimal,
      },
      "STARNAZZO!",
      `${senderName} ti ha starnazzato! (${resolvedAnimal})`
    );
  }

  // 7. Update status
  if (delivered) {
    await alertRef.update({
      status: "delivered",
      deliveredAt: admin.firestore.FieldValue.serverTimestamp(),
    });
  } else {
    await alertRef.update({ status: "failed" });
  }

  return {
    alertId: alertRef.id,
    status: delivered ? "sent" : "error",
  };
});
