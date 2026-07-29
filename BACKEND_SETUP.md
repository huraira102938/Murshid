# Murshid — Backend Setup (Prompt 2)

This covers everything that had to be done *outside* the code — none of it can be run
from inside this repo, so walk through it once before the app will actually work.

## 1. Cloudflare R2 (images)

1. Cloudflare dashboard → R2 → **Create bucket** (e.g. `murshid-media`).
2. Bucket → Settings → enable public access via a custom domain, or use the `r2.dev`
   subdomain it gives you. That URL is `R2_PUBLIC_BASE_URL`.
3. R2 → **Manage R2 API Tokens** → create a token with **Object Read & Write** on that
   bucket. You get an Access Key ID + Secret Access Key — these are `R2_ACCESS_KEY_ID`
   / `R2_SECRET_ACCESS_KEY`.
4. Your account-scoped S3 endpoint is `https://<account_id>.r2.cloudflarestorage.com`
   — that's `R2_ENDPOINT`.
5. Copy `local.properties.example` → `local.properties` (already gitignored) and fill in
   all five `R2_*` values plus your `sdk.dir`.

## 2. Firebase

The project (`murshid-e3369`) already exists and `google-services.json` is already in
the repo, so:

1. Firebase Console → Firestore Database → **Create database** if you haven't already
   (Blaze plan, per the original brief).
2. Deploy the security rules + indexes from the repo root:
   ```
   npm install -g firebase-tools   # if you don't have it
   firebase login
   firebase deploy --only firestore:rules,firestore:indexes
   ```
3. **Read `firestore.rules` before deploying** — there's no user login in this app
   (Prompt 1 was explicit about that), so write access is currently open at the rules
   layer. The real gate is meant to be **Firebase App Check** (Play Integrity provider):
   Console → App Check → register the Android app → enforce App Check for Firestore.
   Do this before installing an admin-enabled build on any device that isn't yours.

## 3. Cloud Functions (for push notifications)

A mobile client can never safely hold FCM "send to everyone" credentials, so the actual
sending happens in `/functions`, triggered by Firestore writes the app already makes:

```
cd functions
npm install
firebase deploy --only functions
```

This deploys three triggers:
- `onNotificationRequestCreated` — the admin Notifications screen writes a doc to
  `notificationRequests`; this sends it to the `all_users` FCM topic.
- `onWallpaperCreated` / `onLibraryItemCreated` / `onUpdateCreated` — silent pings to the
  `content_updates` topic whenever new content is published, so devices can sync
  on-demand instead of polling.

## 4. Android build

Once `local.properties` is filled in, the app builds normally. First launch will:
- Subscribe the device to the `all_users` and `content_updates` FCM topics.
- Schedule a 12-hour periodic background sync (WorkManager).
- Stand up a 300MB Coil disk cache for images.

## Known scope notes (read this before assuming something's missing)

- **Pagination**: repositories fetch the most recent 24 active items per collection.
  There's no "load more" UI yet — that's a follow-up, not part of this pass.
- **Hard delete**: admin deletes are soft (`active: false`). Actual R2 object + Firestore
  doc cleanup is meant to run as a separate scheduled job — not included here, since it's
  explicitly called out in Prompt 2 as a later step ("simple scheduled cleanup path").
- **Category delete password** (`671245123`) is still a client-side constant. It's fine
  for a build-gated admin flow that never ships publicly, but should move server-side
  (a callable Cloud Function) before wider distribution — same reasoning as App Check
  above.
