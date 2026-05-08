import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

export const removeContact = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Must be logged in");
  }

  const { contactUserId } = data;
  if (!contactUserId) {
    throw new functions.https.HttpsError("invalid-argument", "contactUserId required");
  }

  const db = admin.firestore();
  const userId = context.auth.uid;

  // 1. Delete bilateral contact relationship
  const batch = db.batch();

  batch.delete(
    db.collection("users").doc(userId).collection("contacts").doc(contactUserId)
  );
  batch.delete(
    db.collection("users").doc(contactUserId).collection("contacts").doc(userId)
  );

  // 2. Remove from caller's personal groups
  const callerGroups = await db
    .collection("users").doc(userId)
    .collection("groups")
    .get();

  for (const groupDoc of callerGroups.docs) {
    const groupData = groupDoc.data();
    const contactIds: string[] = groupData.contactIds || [];
    if (contactIds.includes(contactUserId)) {
      batch.update(groupDoc.ref, {
        contactIds: admin.firestore.FieldValue.arrayRemove(contactUserId),
      });
    }
  }

  // 3. Remove from the other user's personal groups
  const otherGroups = await db
    .collection("users").doc(contactUserId)
    .collection("groups")
    .get();

  for (const groupDoc of otherGroups.docs) {
    const groupData = groupDoc.data();
    const contactIds: string[] = groupData.contactIds || [];
    if (contactIds.includes(userId)) {
      batch.update(groupDoc.ref, {
        contactIds: admin.firestore.FieldValue.arrayRemove(userId),
      });
    }
  }

  await batch.commit();

  return { status: "removed" };
});
