import * as admin from "firebase-admin";

admin.initializeApp();

// Groups
export { sendGroupInvite } from "./groups/sendGroupInvite";
export { respondGroupInvite } from "./groups/respondGroupInvite";
export { deleteGroup } from "./groups/deleteGroup";
export { removeMember } from "./groups/removeMember";

// Starnazzo
export { sendStarnazzo } from "./starnazzo/sendStarnazzo";
export { sendBroadcastStarnazzo } from "./starnazzo/sendBroadcast";
export { respondStarnazzo } from "./starnazzo/respondStarnazzo";

// Migration (temporary)
export { migrateAddUserIdToMembers } from "./migration/addUserIdToMembers";
