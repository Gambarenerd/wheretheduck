import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

export const deleteGroup = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Must be logged in");
  }

  const { groupId } = data;
  if (!groupId) {
    throw new functions.https.HttpsError("invalid-argument", "groupId required");
  }

  const db = admin.firestore();
  const userId = context.auth.uid;

  // Verify user is admin of the group
  const memberDoc = await db
    .collection("groups").doc(groupId)
    .collection("members").doc(userId)
    .get();

  if (!memberDoc.exists || memberDoc.data()?.role !== "admin") {
    throw new functions.https.HttpsError(
      "permission-denied",
      "Only group admin can delete the group"
    );
  }

  // Delete all subcollections then the group document
  const batch = db.batch();

  // Delete members
  const members = await db
    .collection("groups").doc(groupId)
    .collection("members").get();
  members.docs.forEach((doc) => batch.delete(doc.ref));

  // Delete invites
  const invites = await db
    .collection("groups").doc(groupId)
    .collection("invites").get();
  invites.docs.forEach((doc) => batch.delete(doc.ref));

  // Delete group document
  batch.delete(db.collection("groups").doc(groupId));

  await batch.commit();

  return { success: true };
});
