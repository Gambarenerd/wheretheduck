import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { sendFcmMessage } from "../util/fcmSender";

export const respondStarnazzo = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Must be logged in");
  }

  const { alertId, response, muteDurationMinutes } = data;
  if (!alertId || !response) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "alertId and response required"
    );
  }

  if (!["arrivo", "muto", "dismissed"].includes(response)) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "response must be arrivo, muto, or dismissed"
    );
  }

  const db = admin.firestore();
  const userId = context.auth.uid;

  // 1. Get the alert
  const alertRef = db.collection("alerts").doc(alertId);
  const alertDoc = await alertRef.get();
  if (!alertDoc.exists) {
    throw new functions.https.HttpsError("not-found", "Alert not found");
  }

  const alert = alertDoc.data()!;
  if (alert.toUserId !== userId) {
    throw new functions.https.HttpsError(
      "permission-denied",
      "Only the recipient can respond"
    );
  }

  // 2. Update alert with response
  const updateData: Record<string, unknown> = {
    response,
    respondedAt: admin.firestore.FieldValue.serverTimestamp(),
  };
  if (response === "muto" && muteDurationMinutes) {
    updateData.muteDuration = muteDurationMinutes;
  }
  await alertRef.update(updateData);

  // 3. Notify the sender if response is "arrivo"
  if (response === "arrivo") {
    const responderDoc = await db.collection("users").doc(userId).get();
    const responderName = responderDoc.exists
      ? responderDoc.data()!.displayName || "Qualcuno"
      : "Qualcuno";

    const senderDoc = await db.collection("users").doc(alert.fromUserId).get();
    if (senderDoc.exists) {
      const senderToken = senderDoc.data()!.fcmToken;
      if (senderToken) {
        await sendFcmMessage(
          senderToken,
          {
            type: "starnazzo_response",
            alertId,
            response: "arrivo",
            fromDisplayName: responderName,
          },
          "ARRIVO!",
          `${responderName} sta arrivando!`
        );
      }
    }
  }

  return { success: true };
});
