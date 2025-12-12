# Google Play Console Setup

Complete guide for setting up service account and API access.

## Step 1: Create Google Cloud Project

1. Go to: https://console.cloud.google.com/
2. Click "Select a project" → "New Project"
3. Name: "Android App Deployment"
4. Click "Create"
5. Wait for project creation (30 seconds)

## Step 2: Create Service Account

1. In Cloud Console, go to: IAM & Admin → Service Accounts
2. Click "Create Service Account"
3. Name: `playstore-deploy`
4. Description: "Automated Play Store deployment"
5. Click "Create and Continue"
6. Skip role assignment (click "Continue")
7. Click "Done"

## Step 3: Create Service Account Key

1. Find your service account in the list
2. Click ⋮ (three dots) → "Manage keys"
3. Click "Add Key" → "Create new key"
4. Select "JSON"
5. Click "Create"
6. **CRITICAL:** Save the downloaded JSON file securely
   - Store in password manager
   - Never commit to git
   - This is your only copy!

## Step 4: Enable Play Developer API

1. In Cloud Console, go to: APIs & Services → Library
2. Search: "Google Play Android Developer API"
3. Click on it
4. Click "Enable"
5. Wait for activation (30 seconds)

## Step 5: Link to Play Console

1. Go to: https://play.google.com/console/
2. Select your app
3. Go to: Setup → API access
4. Click "Link a Google Cloud project"
5. Select your project from dropdown
6. Click "Link"

## Step 6: Grant Service Account Access

1. Still in Play Console → API access
2. Find your service account in "Service accounts" section
3. Click "Grant access"
4. Check: "Release to production, exclude devices, and use Play App Signing"
5. Click "Apply"
6. Click "Invite user"

## Step 7: Verify Setup

Service account email format:
`playstore-deploy@PROJECT_ID.iam.gserviceaccount.com`

✅ Checklist:
- [ ] Service account created
- [ ] JSON key downloaded and stored securely
- [ ] Play Developer API enabled
- [ ] Cloud project linked to Play Console
- [ ] Service account has "Release" permission
- [ ] Permissions have propagated (wait 5-10 minutes)

## Security Notes

🔒 **Service Account JSON:**
- Contains sensitive credentials
- Store in password manager
- Never commit to version control
- Rotate keys annually
- One key per environment (dev/prod)

🔒 **Permissions:**
- Grant minimum required permissions only
- Review access logs regularly
- Revoke unused accounts
- Use 2FA on Google account

## Validation

After completing setup, validate your configuration:

```bash
# Install required packages
pip install -r scripts/requirements-playstore.txt

# Run validation
python3 scripts/validate-playstore.py \
  ~/path/to/service-account.json \
  io.github.hitoshura25.healthsyncapp
```

Expected output:
```
✅ Service account JSON is valid
✅ Successfully connected to Play Developer API
✅ Can access package: io.github.hitoshura25.healthsyncapp
✅ All validations passed!
```

If validation fails, check:
- Service account has "Release" permission in Play Console
- Play Developer API is enabled
- Package name matches exactly
- Waited 5-10 minutes for permissions to propagate
