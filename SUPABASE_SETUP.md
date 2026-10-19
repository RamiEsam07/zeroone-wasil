# WASIL Merchant/store app — Supabase setup

This app is being reviewed on an isolated branch. Its draft review request is https://github.com/RamiEsam07/zeroone-wasil/pull/3. Do not merge until the customer/database migration review and all role-app checks are complete.

## Local configuration

In this repository's project root, create or edit the uncommitted `local.properties` file and add the same Supabase project configuration used by the customer app:

```properties
SUPABASE_URL=https://rtrtylhmsfapjlqzjodk.supabase.co
SUPABASE_PUBLISHABLE_KEY=sb_publishable_REPLACE_WITH_YOUR_PUBLISHABLE_KEY
GOOGLE_WEB_CLIENT_ID=REPLACE_WITH_YOUR_WEB_OAUTH_CLIENT_ID.apps.googleusercontent.com
```

Keep the publishable key and Web client ID in local configuration, not in committed files or chat. Never put a Supabase service-role/secret key in an Android app. Repeat this configuration in each local clone; a `local.properties` file is not shared between repositories or machines.

## Google OAuth and access provisioning

- Configure Google as an Auth provider in the same Supabase project; use the Web OAuth client ID above.
- Create an Android OAuth client for this app's exact package name: `com.aistudio.storeportal.mxkpqv`, and register its debug and release SHA-1 fingerprints in Google Cloud.
- Google sign-in creates/authenticates a Supabase session, but the app only admits active profiles whose server-owned role is `merchant`.
- Do not assign roles from Google metadata or client-side form fields. First bootstrap one admin through the trusted SQL Editor procedure documented in the customer/database repository. The admin provisions merchant/driver accounts; driver profiles are created through trusted provisioning. Merchant stores and products remain unavailable to customers until admin approval.
- Google login code, encrypted Android Keystore session storage, token refresh, sign-out and role gating are wired in the review branch; runtime login is not verified until the provider configuration is complete and tested on a device.
- Phone/SMS OTP and electronic payments are not enabled for this launch.

## Database and review boundary

The authoritative schema, RPCs, RLS grants, smoke tests and migration runbook are maintained in the customer/database repository: https://github.com/RamiEsam07/zeroone-wasil3/pull/2. Those migrations are review drafts and have not been applied to the live Supabase project.

