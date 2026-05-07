import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

export const removeMember = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Must be logged in");
  }

  const { groupId, memberId } = data;
  if (!groupId || !memberId) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "groupId and memberId required"
    );
  }

  const db = admin.firestore();
  const userId = context.auth.uid;
  const isLeavingSelf = userId === memberId;

  if (!isLeavingSelf) {
    // Only admin can remove others
    const callerDoc = await db
      .collection("groups").doc(groupId)
      .collection("members").doc(userId)
      .get();

    if (!callerDoc.exists || callerDoc.data()?.role !== "admin") {
      throw new functions.https.HttpsError(
        "permission-denied",
        "Only group admin can remove members"
      );
    }
  }

  // Verify target is actually a member
  const targetDoc = await db
    .collection("groups").doc(groupId)
    .collection("members").doc(memberId)
    .get();

  if (!targetDoc.exists) {
    throw new functions.https.HttpsError("not-found", "Member not found");
  }

  // Don't allow removing the last admin
  if (targetDoc.data()?.role === "admin") {
    const allMembers = await db
      .collection("groups").doc(groupId)
      .collection("members").get();

    const adminCount = allMembers.docs.filter(
      (doc) => doc.data().role === "admin"
    ).length;

    if (adminCount <= 1) {
      throw new functions.https.HttpsError(
        "failed-precondition",
        "Cannot remove the last admin. Delete the group or assign another admin first."
      );
    }
  }

  await db
    .collection("groups").doc(groupId)
    .collection("members").doc(memberId)
    .delete();

  return { success: true };
});
