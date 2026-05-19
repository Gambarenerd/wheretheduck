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

  if (!["ok", "muto", "revenge", "dismissed"].includes(response)) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "response must be ok, muto, revenge, or dismissed"
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

  // 2b. If muted, save mute to Firestore so sendStarnazzo can check it
  if (response === "muto" && muteDurationMinutes) {
    const muteUntil = new Date(Date.now() + muteDurationMinutes * 60 * 1000);
    await db
      .collection("users")
      .doc(userId)
      .collection("muted")
      .doc(alert.fromUserId)
      .set({
        muteUntil: admin.firestore.Timestamp.fromDate(muteUntil),
        durationMinutes: muteDurationMinutes,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
      });
  }

  // 3. Notify the sender
  const responderDoc = await db.collection("users").doc(userId).get();
  const responderName = responderDoc.exists
    ? responderDoc.data()!.displayName || "Qualcuno"
    : "Qualcuno";

  const senderDoc = await db.collection("users").doc(alert.fromUserId).get();
  if (senderDoc.exists) {
    const senderToken = senderDoc.data()!.fcmToken;
    if (senderToken) {
      let title = "";
      let body = "";

      switch (response) {
        case "ok":
          title = "OK!";
          body = `${responderName} ha visto il tuo starnazzo!`;
          break;
        case "muto":
          title = "Non mi rompere!";
          body = `${responderName} ti ha mutato per ${muteDurationMinutes || 1} minuti`;
          break;
        case "revenge":
          title = "REVENGE!";
          body = `${responderName} ti ha restituito lo starnazzo!`;
          break;
        case "dismissed":
          title = "Chiuso";
          body = `${responderName} ha chiuso lo starnazzo`;
          break;
      }

      await sendFcmMessage(
        senderToken,
        {
          type: "starnazzo_response",
          alertId,
          response,
          fromDisplayName: responderName,
        },
        title,
        body
      );
    }
  }

  return { success: true };
});
