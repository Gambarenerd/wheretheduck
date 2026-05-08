import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { sendFcmMessage } from "../util/fcmSender";

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

  // 2. Get personal group to find contactIds
  const groupDoc = await db
    .collection("users").doc(senderId)
    .collection("groups").doc(groupId)
    .get();

  if (!groupDoc.exists) {
    throw new functions.https.HttpsError("not-found", "Group not found");
  }

  const groupData = groupDoc.data()!;
  const contactIds: string[] = groupData.contactIds || [];

  if (contactIds.length === 0) {
    return {
      broadcastId: "",
      alertIds: [],
      failedMembers: [],
      status: "sent",
    };
  }

  // 3. Verify all are still contacts
  const validContactIds: string[] = [];
  for (const contactId of contactIds) {
    const contactDoc = await db
      .collection("users").doc(senderId)
      .collection("contacts").doc(contactId)
      .get();
    if (contactDoc.exists) {
      validContactIds.push(contactId);
    }
  }

  const resolvedAnimal = animalType || DEFAULT_ANIMALS[level] || "duck";
  const broadcastId = db.collection("alerts").doc().id;
  const alertIds: string[] = [];
  const failedMembers: string[] = [];

  // 4. Send to each contact
  for (const contactId of validContactIds) {
    const receiverDoc = await db.collection("users").doc(contactId).get();
    if (!receiverDoc.exists) {
      failedMembers.push(contactId);
      continue;
    }
    const receiver = receiverDoc.data()!;

    // Create alert
    const alertRef = db.collection("alerts").doc();
    await alertRef.set({
      fromUserId: senderId,
      fromDisplayName: sender.displayName || "",
      toUserId: contactId,
      toDisplayName: receiver.displayName || "",
      groupId: null,
      broadcastId,
      starnazzoLevel: level,
      animalType: resolvedAnimal,
      status: "sending",
      response: null,
      muteDuration: null,
      isRevenge: false,
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
      failedMembers.push(contactId);
    }
  }

  const status = failedMembers.length === 0
    ? "sent"
    : alertIds.length > 0
      ? "partial"
      : "error";

  return { broadcastId, alertIds, failedMembers, status };
});
