## Authentication decision: Google first, phone later

- First launch sign-in method: Google via native Android Credential Manager, exchanging the Google ID token for a Supabase Auth session.
- Phone/SMS OTP is deferred. Do not present it as an available sign-in option in the current release.
- Each Android application needs its own Android OAuth client in Google Cloud using that app's exact applicationId and SHA-1 signing fingerprint (debug and release fingerprints differ). Create a Web OAuth client too; configure the Web client ID and secret in Supabase Auth > Providers > Google. The Web client ID is used by the native Credential Manager request.
- Add `GOOGLE_WEB_CLIENT_ID` to local `local.properties` only. Do not commit OAuth client secrets. The Supabase publishable key is safe for client apps; service-role/secret keys are never safe in Android.
- Add the Google provider's redirect/configuration and verify the ID-token exchange, session persistence/refresh, sign-out, and role-based access before calling Google login complete.
- New Google accounts receive the default customer role. Merchant, driver, and admin access must be granted only through a trusted approval/provisioning process, never from client-provided metadata.
- Required provider setup: https://supabase.com/docs/guides/auth/social-login/auth-google?platform=android

# WASIL Supabase setup (Android)

1. Open the project in Android Studio.
2. In the project root, open your local `local.properties` file (do not commit it).
3. Add these two properties, using the project's URL and publishable key from Supabase:

```properties
SUPABASE_URL=https://YOUR_PROJECT_REF.supabase.co
SUPABASE_PUBLISHABLE_KEY=sb_publishable_REPLACE_WITH_YOUR_KEY
```

4. Sync Gradle. The app reads these values into BuildConfig at build time.
5. In Supabase, enable Phone Auth and configure an SMS provider before testing OTP.

Security:
- Never put a `service_role` key or a `secret` key in an Android app.
- The publishable key is not an authorization boundary. Enable RLS and create restrictive policies on every exposed table.
- Keep calls on an IO coroutine/dispatcher. Do not call synchronous OkHttp requests from the main thread.
- This PR adds a low-level REST/auth foundation only. It does not yet replace existing mock/Firebase repositories or connect screens; those need role-specific migrations and tests.
