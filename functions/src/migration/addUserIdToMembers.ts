import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

/**
 * One-time migration: adds `userId` field to all member documents
 * where it's missing. The userId equals the document ID.
 * Call once, then remove.
 */
export const migrateAddUserIdToMembers = functions.https.onCall(async (_data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Must be logged in");
  }

  const db = admin.firestore();
  const groupsSnap = await db.collection("groups").get();
  let updated = 0;

  for (const groupDoc of groupsSnap.docs) {
    const membersSnap = await groupDoc.ref.collection("members").get();
    for (const memberDoc of membersSnap.docs) {
      const data = memberDoc.data();
      if (!data.userId) {
        await memberDoc.ref.update({ userId: memberDoc.id });
        updated++;
      }
    }
  }

  return { updated };
});
