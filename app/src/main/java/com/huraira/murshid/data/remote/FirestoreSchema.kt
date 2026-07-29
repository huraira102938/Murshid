package com.huraira.murshid.data.remote

/**
 * Central place for Firestore collection and field names so repositories don't scatter
 * string literals. Every content collection shares the same soft-delete convention:
 * documents are never hard-deleted from the client — `active` is flipped to false — so
 * devices with stale cached references never crash on a doc that vanished mid-session.
 * Real cleanup (R2 objects + Firestore doc) happens via a separate scheduled job.
 */
object FirestoreSchema {
    const val WALLPAPERS = "wallpapers"
    const val LIBRARY_ITEMS = "libraryItems"
    const val UPDATES = "updates"
    const val CATEGORIES = "categories"
    const val NOTIFICATION_REQUESTS = "notificationRequests"

    const val FIELD_ACTIVE = "active"
    const val FIELD_CREATED_AT = "createdAt"

    const val DEFAULT_PAGE_SIZE = 24L
}
