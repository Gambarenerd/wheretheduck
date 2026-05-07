import * as admin from "firebase-admin";

admin.initializeApp();

// Groups
export { sendGroupInvite } from "./groups/sendGroupInvite";
export { respondGroupInvite } from "./groups/respondGroupInvite";

// Starnazzo
export { sendStarnazzo } from "./starnazzo/sendStarnazzo";
export { sendBroadcastStarnazzo } from "./starnazzo/sendBroadcast";

// Migration (temporary)
export { migrateAddUserIdToMembers } from "./migration/addUserIdToMembers";
