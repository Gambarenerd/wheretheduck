import * as admin from "firebase-admin";

admin.initializeApp();

// Contacts
export { sendContactInvite } from "./contacts/sendContactInvite";
export { respondContactInvite } from "./contacts/respondContactInvite";
export { removeContact } from "./contacts/removeContact";

// Starnazzo
export { sendStarnazzo } from "./starnazzo/sendStarnazzo";
export { sendBroadcastStarnazzo } from "./starnazzo/sendBroadcast";
export { respondStarnazzo } from "./starnazzo/respondStarnazzo";
export { revengeStarnazzo } from "./starnazzo/revengeStarnazzo";
