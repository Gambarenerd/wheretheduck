import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

export const unmuteSender = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Must be logged in");
  }

  const { mutedUserId } = data;
  if (!mutedUserId) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "mutedUserId required"
    );
  }

  const db = admin.firestore();
  const userId = context.auth.uid;

  const muteRef = db
    .collection("users")
    .doc(userId)
    .collection("muted")
    .doc(mutedUserId);

  const muteDoc = await muteRef.get();
  if (muteDoc.exists) {
    await muteRef.delete();
  }

  return { success: true };
});
