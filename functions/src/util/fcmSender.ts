import * as admin from "firebase-admin";

interface FcmData {
  [key: string]: string;
}

export async function sendFcmMessage(
  token: string,
  data: FcmData,
  title?: string,
  body?: string
): Promise<boolean> {
  try {
    // Always send as data-only message so onMessageReceived is always called
    // (notification messages are handled automatically by Android when app is in background,
    // bypassing our custom handling)
    const fullData: FcmData = { ...data };
    if (title) fullData.title = title;
    if (body) fullData.body = body;

    const message: admin.messaging.Message = {
      token,
      data: fullData,
      android: {
        priority: "high" as const,
      },
    };

    const response = await admin.messaging().send(message);
    console.log("FCM send success, messageId:", response);
    return true;
  } catch (error: any) {
    console.error("FCM send error:", error.code, error.message);
    return false;
  }
}
