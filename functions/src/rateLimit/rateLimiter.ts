import * as admin from "firebase-admin";

const HOURLY_LIMIT = 5; // max starnazzi per hour to same person
const DAILY_LIMIT = 10; // max starnazzi per day (free plan)
const COOLDOWN_SECONDS = 30; // min seconds between starnazzi to same person

interface RateLimitResult {
  allowed: boolean;
  retryAfterSeconds?: number;
  reason?: string;
}

export async function checkRateLimit(
  senderId: string,
  receiverId: string
): Promise<RateLimitResult> {
  const db = admin.firestore();
  const docId = `${senderId}_${receiverId}`;
  const ref = db.collection("rateLimits").doc(docId);
  const doc = await ref.get();
  const now = admin.firestore.Timestamp.now();

  if (!doc.exists) {
    // First starnazzo to this person, create record
    await ref.set({
      hourlyCount: 1,
      dailyCount: 1,
      lastStarnazzoAt: now,
      windowStart: now,
    });
    return { allowed: true };
  }

  const data = doc.data()!;
  const lastStarnazzo = data.lastStarnazzoAt as admin.firestore.Timestamp;
  const windowStart = data.windowStart as admin.firestore.Timestamp;

  // Check cooldown
  const secondsSinceLast = now.seconds - lastStarnazzo.seconds;
  if (secondsSinceLast < COOLDOWN_SECONDS) {
    return {
      allowed: false,
      retryAfterSeconds: COOLDOWN_SECONDS - secondsSinceLast,
      reason: "cooldown",
    };
  }

  // Check if window has expired (1 hour)
  const hoursSinceWindow = (now.seconds - windowStart.seconds) / 3600;
  let hourlyCount = data.hourlyCount || 0;
  let dailyCount = data.dailyCount || 0;

  if (hoursSinceWindow >= 1) {
    // Reset hourly counter
    hourlyCount = 0;
  }

  // Reset daily counter if new day
  const lastDate = new Date(windowStart.seconds * 1000).toDateString();
  const nowDate = new Date(now.seconds * 1000).toDateString();
  if (lastDate !== nowDate) {
    dailyCount = 0;
  }

  // Check hourly limit
  if (hourlyCount >= HOURLY_LIMIT) {
    return {
      allowed: false,
      retryAfterSeconds: Math.ceil(3600 - (now.seconds - windowStart.seconds)),
      reason: "hourly_limit",
    };
  }

  // Update counters
  await ref.update({
    hourlyCount: hourlyCount + 1,
    dailyCount: dailyCount + 1,
    lastStarnazzoAt: now,
    windowStart: hoursSinceWindow >= 1 ? now : windowStart,
  });

  return { allowed: true };
}

export async function checkDailyCount(senderId: string): Promise<number> {
  const db = admin.firestore();
  const now = new Date();
  const startOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate());

  const alerts = await db
    .collection("alerts")
    .where("fromUserId", "==", senderId)
    .where("createdAt", ">=", admin.firestore.Timestamp.fromDate(startOfDay))
    .count()
    .get();

  return alerts.data().count;
}
