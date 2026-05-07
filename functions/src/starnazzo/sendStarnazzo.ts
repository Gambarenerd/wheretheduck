import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { sendFcmMessage } from "../util/fcmSender";
import { checkRateLimit, checkDailyCount } from "../rateLimit/rateLimiter";

const FREE_DAILY_LIMIT = 10;

const DEFAULT_ANIMALS: Record<string, string> = {
  light: "cricket",
  medium: "duck",
  heavy: "goose",
};

export const sendStarnazzo = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Must be logged in");
  }

  const { toUserId, groupId, level, animalType } = data;
  if (!toUserId || !groupId || !level) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "toUserId, groupId, and level required"
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

  // 2. Verify both are members of the group
  const [senderMember, receiverMember] = await Promise.all([
    db.collection("groups").doc(groupId).collection("members").doc(senderId).get(),
    db.collection("groups").doc(groupId).collection("members").doc(toUserId).get(),
  ]);

  if (!senderMember.exists || !receiverMember.exists) {
    throw new functions.https.HttpsError(
      "permission-denied",
      "Both users must be members of the group"
    );
  }

  // 3. Check plan limits (disabled for testing)
  // const plan = sender.plan || "free";
  // if (plan === "free") {
  //   const dailyCount = await checkDailyCount(senderId);
  //   if (dailyCount >= FREE_DAILY_LIMIT) {
  //     return {
  //       alertId: "",
  //       status: "plan_limited",
  //       upgradeReason: "daily_limit_reached",
  //     };
  //   }
  // }

  // 4. Check rate limit (disabled for testing)
  // const rateCheck = await checkRateLimit(senderId, toUserId);
  // if (!rateCheck.allowed) {
  //   return {
  //     alertId: "",
  //     status: "rate_limited",
  //     retryAfterSeconds: rateCheck.retryAfterSeconds,
  //   };
  // }

  // 5. Get receiver info
  const receiverDoc = await db.collection("users").doc(toUserId).get();
  if (!receiverDoc.exists) {
    throw new functions.https.HttpsError("not-found", "Receiver not found");
  }
  const receiver = receiverDoc.data()!;

  // 6. Resolve animal type
  const resolvedAnimal = animalType || DEFAULT_ANIMALS[level] || "duck";

  // 7. Create alert document
  const alertRef = db.collection("alerts").doc();
  await alertRef.set({
    fromUserId: senderId,
    fromDisplayName: sender.displayName || "",
    toUserId,
    toDisplayName: receiver.displayName || "",
    groupId,
    broadcastId: null,
    starnazzoLevel: level,
    animalType: resolvedAnimal,
    status: "sending",
    response: null,
    muteDuration: null,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    deliveredAt: null,
    respondedAt: null,
  });

  // 8. Send FCM
  let delivered = false;
  console.log("Receiver fcmToken:", receiver.fcmToken ? "EXISTS" : "MISSING", "receiverId:", toUserId);
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
      },
      "STARNAZZO!",
      `${senderName} ti ha starnazzato! (${resolvedAnimal})`
    );
  }

  // 9. Update status
  if (delivered) {
    await alertRef.update({
      status: "delivered",
      deliveredAt: admin.firestore.FieldValue.serverTimestamp(),
    });
  } else {
    await alertRef.update({ status: "failed" });
  }

  return {
    alertId: alertRef.id,
    status: delivered ? "sent" : "error",
  };
});
