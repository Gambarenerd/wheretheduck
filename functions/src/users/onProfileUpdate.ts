import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

export const onProfileUpdate = functions.firestore
  .document("users/{userId}")
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();
    const userId = context.params.userId;

    // Check if any relevant profile field changed
    const fieldsToSync = ["displayName", "photoUrl", "motto"];
    const anyChanged = fieldsToSync.some((f) => before[f] !== after[f]);

    if (!anyChanged) return;

    // Sync ALL relevant fields (not just the changed ones) to self-heal stale data
    const updates: Record<string, string> = {};
    for (const field of fieldsToSync) {
      updates[field] = after[field] || "";
    }

    // Get MY contacts list (contacts are bilateral, so these users also have me)
    const db = admin.firestore();
    const myContacts = await db
      .collection("users")
      .doc(userId)
      .collection("contacts")
      .get();

    if (myContacts.empty) return;

    const batch = db.batch();

    for (const contactDoc of myContacts.docs) {
      const contactUserId = contactDoc.id;
      const ref = db
        .collection("users")
        .doc(contactUserId)
        .collection("contacts")
        .doc(userId);
      batch.update(ref, updates);
    }

    await batch.commit();

    functions.logger.info(
      `Synced ${Object.keys(updates).join(", ")} for user ${userId} to ${myContacts.size} contacts`
    );
  });
