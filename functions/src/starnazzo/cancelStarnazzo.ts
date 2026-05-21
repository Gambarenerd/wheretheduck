import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { sendFcmMessage } from "../util/fcmSender";

export const cancelStarnazzo = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Must be logged in");
  }

  const { alertId } = data;
  if (!alertId) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "alertId required"
    );
  }

  const db = admin.firestore();
  const userId = context.auth.uid;

  const alertRef = db.collection("alerts").doc(alertId);
  const alertDoc = await alertRef.get();
  if (!alertDoc.exists) {
    throw new functions.https.HttpsError("not-found", "Alert not found");
  }

  const alert = alertDoc.data()!;

  // Only the sender can cancel
  if (alert.fromUserId !== userId) {
    throw new functions.https.HttpsError(
      "permission-denied",
      "Only the sender can cancel"
    );
  }

  // Only cancel if not already responded
  if (alert.response) {
    return { success: false, reason: "already_responded" };
  }

  // Update alert status
  await alertRef.update({
    status: "cancelled",
    cancelledAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  // Notify the receiver to stop
  const receiverDoc = await db.collection("users").doc(alert.toUserId).get();
  if (receiverDoc.exists) {
    const receiverToken = receiverDoc.data()!.fcmToken;
    if (receiverToken) {
      await sendFcmMessage(
        receiverToken,
        {
          type: "starnazzo_cancel",
          alertId,
        },
        "",
        ""
      );
    }
  }

  return { success: true };
});
