import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { sendFcmMessage } from "../util/fcmSender";

const DEFAULT_ANIMALS: Record<string, string> = {
  light: "cricket",
  medium: "duck",
  heavy: "goose",
};

export const revengeStarnazzo = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Must be logged in");
  }

  const { alertId } = data;
  if (!alertId) {
    throw new functions.https.HttpsError("invalid-argument", "alertId required");
  }

  const db = admin.firestore();
  const userId = context.auth.uid;

  // 1. Get the original alert
  const originalRef = db.collection("alerts").doc(alertId);
  const originalDoc = await originalRef.get();
  if (!originalDoc.exists) {
    throw new functions.https.HttpsError("not-found", "Alert not found");
  }

  const original = originalDoc.data()!;

  // Only the recipient can revenge
  if (original.toUserId !== userId) {
    throw new functions.https.HttpsError(
      "permission-denied",
      "Only the recipient can revenge"
    );
  }

  // Prevent revenge chains
  if (original.isRevenge) {
    return { alertId: "", status: "cannot_revenge_revenge" };
  }

  const targetId = original.fromUserId; // revenge goes back to original sender

  // 2. Verify contact relationship
  const contactDoc = await db
    .collection("users").doc(userId)
    .collection("contacts").doc(targetId)
    .get();

  if (!contactDoc.exists) {
    throw new functions.https.HttpsError(
      "permission-denied",
      "Must be contacts to revenge"
    );
  }

  // 3. Update original alert with revenge response
  await originalRef.update({
    response: "revenge",
    respondedAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  // 4. Get user info
  const [senderDoc, targetDoc] = await Promise.all([
    db.collection("users").doc(userId).get(),
    db.collection("users").doc(targetId).get(),
  ]);

  if (!targetDoc.exists) {
    return { alertId: "", status: "target_not_found" };
  }

  const sender = senderDoc.exists ? senderDoc.data()! : {};
  const target = targetDoc.data()!;
  const level = original.starnazzoLevel || "medium";
  const resolvedAnimal = DEFAULT_ANIMALS[level] || "duck";

  // 5. Create revenge alert
  const revengeRef = db.collection("alerts").doc();
  await revengeRef.set({
    fromUserId: userId,
    fromDisplayName: sender.displayName || "",
    toUserId: targetId,
    toDisplayName: target.displayName || "",
    groupId: null,
    broadcastId: null,
    starnazzoLevel: level,
    animalType: resolvedAnimal,
    status: "sending",
    response: null,
    muteDuration: null,
    isRevenge: true,
    originalAlertId: alertId,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    expiresAt: new Date(Date.now() + 14 * 24 * 60 * 60 * 1000),
    deliveredAt: null,
    respondedAt: null,
  });

  // 6. Send FCM
  let delivered = false;
  if (target.fcmToken) {
    const senderName = sender.displayName || "Qualcuno";
    delivered = await sendFcmMessage(
      target.fcmToken,
      {
        type: "starnazzo",
        alertId: revengeRef.id,
        fromUserId: userId,
        fromDisplayName: senderName,
        fromPhotoUrl: sender.photoUrl || "",
        level,
        animalType: resolvedAnimal,
        isRevenge: "true",
      },
      "REVENGE STARNAZZO!",
      `${senderName} ti ha restituito lo starnazzo!`
    );
  }

  if (delivered) {
    await revengeRef.update({
      status: "delivered",
      deliveredAt: admin.firestore.FieldValue.serverTimestamp(),
    });
  } else {
    await revengeRef.update({ status: "failed" });
  }

  return {
    alertId: revengeRef.id,
    status: delivered ? "sent" : "error",
  };
});
