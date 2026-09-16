# Postiz Mobile (Android scaffold)

A "bring your own instance" Android client for **Postiz** (the open-source
social-media scheduler, and specifically the
[`halleshubham/shacky-postiz`](https://github.com/halleshubham/shacky-postiz)
fork). Instead of hard-coding a server, the self-hoster types their own
**Server URL** and **API key** on first launch, and everything else in the
app is driven by [Postiz's Public API](https://docs.postiz.com/public-api/introduction).

## Stack

- Kotlin + Jetpack Compose (Material 3)
- Hilt for DI
- Retrofit + OkHttp + kotlinx.serialization
- Jetpack DataStore (Preferences) for storing the server URL / API key
- Navigation-Compose
- Coil for image loading

## How auth works

- On the Connect screen the user enters:
  - **Server URL** — e.g. `postiz.mycompany.com` or `192.168.1.20:5000` (a bare
    domain/IP is fine; the app adds `https://` and the right path).
  - **API key** — from their Postiz web app under *Settings → Developer*.
    An OAuth2 token (prefixed `pos_`) works the same way if you build out
    the [OAuth2 flow](https://docs.postiz.com/public-api/oauth) later.
  - A **"This is Postiz Cloud"** switch, off by default, since the base
    path differs: self-hosted is `https://{domain}/api/public/v1`, cloud is
    `https://api.postiz.com/public/v1`.
- `PostizApiProvider` builds the Retrofit client from those values at
  *runtime* (not compile time) and rebuilds it if they change.
- The Connect screen saves the values, then calls `GET /is-connected` to
  validate them; on failure it clears the half-saved session so the user
  isn't left in a broken state.
- Everything downstream just reads/writes through `PostizRepository`,
  which wraps calls in a `Resource<T>` (Loading/Success/Error) sealed class.

## What's implemented

- Connect / validate server screen
- Bottom-nav shell: **Posts**, **Channels** (integrations), **Settings**
- Posts list (`GET /posts`) with delete
- Create/schedule post (`POST /posts`): content, multi-select channels,
  optional image upload (`POST /upload`), post-now vs schedule
- Channels list (`GET /integrations`) with avatar, platform, group, disabled state
- Settings: view server/masked key, disconnect

## What's intentionally left as a TODO

This is a scaffold, not a finished app — the goal was full breadth (every
screen navigable, real network calls, a working auth flow) over depth on
any one feature:

- **Per-platform post settings.** The API requires `settings.__type` plus,
  for many platforms, extra fields (e.g. X's `who_can_reply_post`,
  Instagram's `post_type`). Right now `CreatePostViewModel` only sends
  `{"__type": "<platform>"}`, which works for platforms with no required
  extra settings (Bluesky, Mastodon, Threads, Telegram, etc.) but will be
  rejected by ones that need more. See
  https://docs.postiz.com/public-api/providers/<platform> per platform.
- **`PostDto` and the `GET /posts` query params** are a best-effort guess
  (I verified `is-connected`, `integrations`, the create-post payload, and
  the upload response directly against the docs; I did not fetch the full
  `openapi.json` for every endpoint). Pull
  `https://docs.postiz.com/public-api/openapi.json` and true these up
  before shipping.
- Real `DatePicker`/`TimePicker` instead of a raw ISO-8601 text field for
  scheduling.
- Pagination on posts/notifications, pull-to-refresh, analytics screens,
  customer/group filtering, delete-integration UI, notification list.
- The `network_security_config.xml` currently allows cleartext HTTP for
  *any* host, since self-hosted instances are often reached over plain
  HTTP/LAN before a reverse proxy is set up. Tighten this before a public
  release (e.g. Play Store will flag broad cleartext permission).
- Encrypt the stored API key (e.g. move from Preferences DataStore to
  `EncryptedSharedPreferences`/Android Keystore) rather than plaintext DataStore.
- No automated tests yet.

## Running it / getting an APK

**Option A — Android Studio (easiest, gives you a device install directly)**
1. Open the `PostizMobile/` folder in Android Studio (Iguana or newer).
   Studio will offer to add the Gradle wrapper automatically on sync —
   accept that (the wrapper jar itself isn't checked in here since it's a
   binary). Alternatively run `gradle wrapper` yourself if you have Gradle
   installed.
2. Let Gradle sync, then Run on a device/emulator, or
   *Build → Build Bundle(s)/APK(s) → Build APK(s)* for a standalone file.
3. On first launch, enter your Postiz server URL and API key.

**Option B — GitHub Actions (no local Android Studio needed)**
1. Push this folder to a GitHub repo.
2. `.github/workflows/build-apk.yml` (included) builds a debug APK on every
   push, or on demand via the "Run workflow" button.
3. Download the `postiz-mobile-debug-apk` artifact from the completed run
   and install it on a device (enable "install unknown apps" first).

Either way produces a **debug** APK — fine for testing, but before real
distribution you'll want a signed release build (a keystore + a
`signingConfigs` block in `app/build.gradle.kts`) and, per the TODOs above,
to tighten the cleartext-traffic config and encrypt the stored API key.

## Project layout

```
app/src/main/java/com/postiz/mobile/
  data/local/SessionManager.kt        # DataStore-backed server URL + token
  data/remote/PostizApiProvider.kt    # builds Retrofit at runtime from the session
  data/remote/PostizApiService.kt     # the Public API surface (Retrofit interface)
  data/remote/dto/Dtos.kt             # request/response models (see provenance notes)
  data/repository/PostizRepository.kt # Resource<T>-wrapped calls, error mapping
  ui/navigation/                      # root graph (Connect vs main app)
  ui/main/MainScaffold.kt             # bottom-nav shell
  ui/screens/connect/                 # server URL + API key entry
  ui/screens/posts/                   # list + create/schedule
  ui/screens/integrations/            # connected channels list
  ui/screens/settings/                # view/disconnect session
```
