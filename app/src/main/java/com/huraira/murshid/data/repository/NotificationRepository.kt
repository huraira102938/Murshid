package com.huraira.murshid.data.repository

/**
 * Abstracts sending a push notification to app users. The dummy implementation is a no-op
 * stub — real FCM sending is wired up in Prompt 2.
 */
interface NotificationRepository {
    suspend fun send(title: String, body: String): Result<Unit>
}

class DummyNotificationRepository : NotificationRepository {
    override suspend fun send(title: String, body: String): Result<Unit> {
        // TODO(Prompt 2): send via Firebase Cloud Messaging (topic or all-devices broadcast).
        return Result.success(Unit)
    }
}
