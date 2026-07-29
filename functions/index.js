const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { initializeApp } = require("firebase-admin/app");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

const ALL_USERS_TOPIC = "all_users";
const CONTENT_UPDATES_TOPIC = "content_updates";

/**
 * Admin's Notifications screen (in the Android app) never sends FCM directly — it just
 * writes a doc here. This function is the only thing with real "send to everyone"
 * credentials, which is why it lives server-side instead of in the APK.
 */
exports.onNotificationRequestCreated = onDocumentCreated(
  "notificationRequests/{requestId}",
  async (event) => {
    const snapshot = event.data;
    if (!snapshot) return;
    const data = snapshot.data();

    try {
      await getMessaging().send({
        topic: ALL_USERS_TOPIC,
        notification: {
          title: data.title,
          body: data.body,
        },
      });
      await snapshot.ref.update({ status: "sent", sentAt: Date.now() });
    } catch (error) {
      await snapshot.ref.update({ status: "failed", error: String(error) });
    }
  }
);

/**
 * Silent "something new was published" ping for each content collection, so the app can
 * sync on-demand instead of polling Firestore on every launch (see SyncWorker.kt /
 * MurshidMessagingService.kt on the client).
 */
function contentUpdateTrigger(collectionName) {
  return onDocumentCreated(`${collectionName}/{docId}`, async () => {
    await getMessaging().send({
      topic: CONTENT_UPDATES_TOPIC,
      data: { type: "content_update", collection: collectionName },
    });
  });
}

exports.onWallpaperCreated = contentUpdateTrigger("wallpapers");
exports.onLibraryItemCreated = contentUpdateTrigger("libraryItems");
exports.onUpdateCreated = contentUpdateTrigger("updates");
