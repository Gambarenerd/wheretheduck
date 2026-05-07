import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { sendFcmMessage } from "../util/fcmSender";
import { checkDailyCount } from "../rateLimit/rateLimiter";

const FREE_DAILY_LIMIT = 10;
const FREE_BROADCAST_DAILY_LIMIT = 1;

const DEFAULT_ANIMALS: Record<string, string> = {
  light: "cricket",
  medium: "duck",
  heavy: "goose",
};

export const sendBroadcastStarnazzo = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Must be logged in");
  }

  const { groupId, level, animalType } = data;
  if (!groupId || !level) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "groupId and level required"
    );
  }

  const db = admin.firestore();
  const senderId = context.auth.uid;

  // 1. Get sender info
  const senderDoc = await db.collection("users").doc(senderId).get();
  if (!senderDoc.exists) {
    throw new functions.https.HttpsError("not-found", "Sender not found");
  }
  const sender = senderDoc.data()!;

  // 2. Verify sender is member
  const senderMember = await db
    .collection("groups").doc(groupId)
    .collection("members").doc(senderId)
    .get();

  if (!senderMember.exists) {
    throw new functions.https.HttpsError("permission-denied", "Not a member");
  }

  // 3. Check plan limits (disabled for testing)
  // const plan = sender.plan || "free";
  // if (plan === "free") {
  //   const dailyCount = await checkDailyCount(senderId);
  //   if (dailyCount >= FREE_DAILY_LIMIT) {
  //     return {
  //       broadcastId: "",
  //       alertIds: [],
  //       failedMembers: [],
  //       status: "plan_limited",
  //     };
  //   }
  // }

  // 4. Get all members except sender
  const membersSnapshot = await db
    .collection("groups").doc(groupId)
    .collection("members")
    .get();

  const members = membersSnapshot.docs.filter((doc) => doc.id !== senderId);

  if (members.length === 0) {
    return {
      broadcastId: "",
      alertIds: [],
      failedMembers: [],
      status: "sent",
    };
  }

  const resolvedAnimal = animalType || DEFAULT_ANIMALS[level] || "duck";
  const broadcastId = db.collection("alerts").doc().id;
  const alertIds: string[] = [];
  const failedMembers: string[] = [];

  // 5. Send to each member
  for (const memberDoc of members) {
    const memberId = memberDoc.id;

    // Get receiver info
    const receiverDoc = await db.collection("users").doc(memberId).get();
    if (!receiverDoc.exists) {
      failedMembers.push(memberId);
      continue;
    }
    const receiver = receiverDoc.data()!;

    // Create alert
    const alertRef = db.collection("alerts").doc();
    await alertRef.set({
      fromUserId: senderId,
      fromDisplayName: sender.displayName || "",
      toUserId: memberId,
      toDisplayName: receiver.displayName || "",
      groupId,
      broadcastId,
      starnazzoLevel: level,
      animalType: resolvedAnimal,
      status: "sending",
      response: null,
      muteDuration: null,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      deliveredAt: null,
      respondedAt: null,
    });

    // Send FCM
    let delivered = false;
    if (receiver.fcmToken) {
      const senderName = sender.displayName || "Qualcuno";
      delivered = await sendFcmMessage(
        receiver.fcmToken,
        {
          type: "starnazzo",
          alertId: alertRef.id,
          fromUserId: senderId,
          fromDisplayName: senderName,
          level,
          animalType: resolvedAnimal,
          groupId,
          broadcastId,
        },
        "STARNAZZO A TUTTI!",
        `${senderName} ha starnazzato tutto il gruppo! (${resolvedAnimal})`
      );
    }

    if (delivered) {
      await alertRef.update({
        status: "delivered",
        deliveredAt: admin.firestore.FieldValue.serverTimestamp(),
      });
      alertIds.push(alertRef.id);
    } else {
      await alertRef.update({ status: "failed" });
      failedMembers.push(memberId);
    }
  }

  const status = failedMembers.length === 0
    ? "sent"
    : alertIds.length > 0
      ? "partial"
      : "error";

  return { broadcastId, alertIds, failedMembers, status };
});
