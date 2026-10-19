# WASIL Merchant App — Integration Audit

**Audit branch:** `ops/wasil-integration-audit`  
**Base:** `fix/wasil-supabase-auth-review`  
**Production status:** Not approved for release. `main` is unchanged.

## Verified in repository
- Android app uses `SupabaseRoleAuthenticator(context, "merchant")` in the entry flow.
- Sign-in/restored sessions are checked against the server-owned profile role and active status before access is granted.
- The app invokes Supabase-backed view-model actions for account activation, store categories, store creation/onboarding, refresh, and sign-out.
- Build configuration reads Supabase URL, publishable key, and Google Web OAuth client ID from local properties. No service-role key should be embedded in the app.

## Not yet verified
- End-to-end Google OAuth against the actual Supabase project.
- Merchant onboarding against live RLS policies, including creating a store and the admin approval/activation path.
- Build/test result for this audit branch.
- Cross-app order lifecycle with customer, driver, and admin.

## Release gates
1. Run Android build and unit tests for this branch.
2. Test sign-in, inactive/wrong-role rejection, session restore/refresh, sign-out, merchant store creation, and pending/approved store access against a disposable Supabase environment.
3. Verify the database has a safe admin approval path for `stores.is_active`; do not loosen client grants to work around a blocked approval flow.
4. Verify errors and loading states and ensure no secrets or tokens are logged.
5. Review findings before any merge. No live migration or merge to `main` has been performed by this audit.
