import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

export const deleteAccount = functions.https.onCall(async (_data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Must be logged in");
  }

  const db = admin.firestore();
  const userId = context.auth.uid;

  // 1. Get user's contacts and remove bilateral relationships
  const contactsSnap = await db
    .collection("users").doc(userId)
    .collection("contacts")
    .get();

  for (const contactDoc of contactsSnap.docs) {
    const contactId = contactDoc.id;

    // Remove this user from the other user's contacts
    await db
      .collection("users").doc(contactId)
      .collection("contacts").doc(userId)
      .delete();

    // Remove this user from the other user's groups
    const otherGroups = await db
      .collection("users").doc(contactId)
      .collection("groups")
      .get();

    for (const groupDoc of otherGroups.docs) {
      const contactIds: string[] = groupDoc.data().contactIds || [];
      if (contactIds.includes(userId)) {
        await groupDoc.ref.update({
          contactIds: admin.firestore.FieldValue.arrayRemove(userId),
        });
      }
    }
  }

  // 2. Delete user's subcollections: contacts, groups, alerts
  const subcollections = ["contacts", "groups", "alerts"];
  for (const sub of subcollections) {
    const snap = await db
      .collection("users").doc(userId)
      .collection(sub)
      .get();
    const batch = db.batch();
    snap.docs.forEach((doc) => batch.delete(doc.ref));
    if (snap.docs.length > 0) {
      await batch.commit();
    }
  }

  // 3. Delete pending invites (sent and received)
  const sentInvites = await db
    .collection("contactInvites")
    .where("fromUserId", "==", userId)
    .get();
  const receivedInvites = await db
    .collection("contactInvites")
    .where("toUserId", "==", userId)
    .get();

  const inviteBatch = db.batch();
  sentInvites.docs.forEach((doc) => inviteBatch.delete(doc.ref));
  receivedInvites.docs.forEach((doc) => inviteBatch.delete(doc.ref));
  if (sentInvites.docs.length > 0 || receivedInvites.docs.length > 0) {
    await inviteBatch.commit();
  }

  // 4. Delete profile photo from Storage
  try {
    const bucket = admin.storage().bucket();
    await bucket.file(`profile_photos/${userId}.jpg`).delete();
  } catch (_e) {
    // Photo may not exist, ignore
  }

  // 5. Delete user document from Firestore
  await db.collection("users").doc(userId).delete();

  // 6. Delete user from Firebase Auth
  await admin.auth().deleteUser(userId);

  return { status: "deleted" };
});
