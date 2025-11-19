# Google Play Store Deployment Guide

This guide will help you set up automated deployment to the **Google Play Store Internal Testing track** (private, non-public release).

## Overview

The app uses GitHub Actions to automatically build and deploy to Google Play Store when code is pushed to specific branches or manually triggered.

## Prerequisites

Before you can deploy, you need to:

1. ✅ A Google Play Console developer account ($25 one-time fee)
2. ✅ Your app registered in the Play Console
3. ✅ An Android app signing keystore
4. ✅ A Google Cloud service account with Play Store access
5. ✅ GitHub repository secrets configured

## Step 1: Create an Android Keystore

If you don't already have a keystore, create one:

```bash
keytool -genkey -v -keystore release-keystore.jks \
  -alias health-sync-app \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

**Important:**
- Choose a strong password for both the keystore and key
- Store this keystore file securely (you'll need it for all future releases)
- **Never commit the keystore to git** (it's already in .gitignore)
- Save the passwords - you cannot recover them if lost

## Step 2: Convert Keystore to Base64

For GitHub Secrets, convert your keystore to base64:

```bash
# On Linux/macOS
base64 -i release-keystore.jks | tr -d '\n' > keystore.base64.txt

# On Windows (PowerShell)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release-keystore.jks")) | Out-File keystore.base64.txt
```

## Step 3: Create Google Play Console Service Account

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the **Google Play Android Developer API**
4. Create a Service Account:
   - Go to **IAM & Admin > Service Accounts**
   - Click **Create Service Account**
   - Name: `github-actions-play-store`
   - Click **Create and Continue**
   - Skip granting permissions (we'll do this in Play Console)
   - Click **Done**
5. Create a JSON key:
   - Click on the service account you just created
   - Go to **Keys** tab
   - Click **Add Key > Create New Key**
   - Choose **JSON** format
   - Download the JSON file (keep it secure!)

## Step 4: Grant Play Console Access

1. Go to [Google Play Console](https://play.google.com/console/)
2. Select your app
3. Go to **Setup > API access**
4. Link your Google Cloud project if not already linked
5. Under **Service Accounts**, find your service account
6. Click **Grant Access**
7. Set permissions:
   - **Account permissions:** View app information and download bulk reports
   - **App permissions:**
     - Releases: Create and edit releases
     - Choose your app and grant access

## Step 5: Configure GitHub Secrets

Add the following secrets to your GitHub repository:

**Settings > Secrets and variables > Actions > New repository secret**

| Secret Name | Value | How to Get It |
|-------------|-------|---------------|
| `KEYSTORE_BASE64` | Base64 encoded keystore | Content of `keystore.base64.txt` from Step 2 |
| `KEYSTORE_PASSWORD` | Keystore password | Password you set when creating keystore |
| `KEY_ALIAS` | Key alias | Alias you used (e.g., `health-sync-app`) |
| `KEY_PASSWORD` | Key password | Password you set for the key |
| `PLAY_STORE_SERVICE_ACCOUNT_JSON` | Service account JSON | Entire content of the JSON file from Step 3 |

**To add a secret:**
1. Go to your repository on GitHub
2. Click **Settings** > **Secrets and variables** > **Actions**
3. Click **New repository secret**
4. Enter the name and value
5. Click **Add secret**

## Step 6: Initial App Upload (First Time Only)

⚠️ **Important:** The first release MUST be uploaded manually through the Play Console.

1. Build a release AAB locally:
   ```bash
   export KEYSTORE_PASSWORD="your-password"
   export KEY_ALIAS="health-sync-app"
   export KEY_PASSWORD="your-key-password"

   # Copy your keystore to app directory
   cp /path/to/release-keystore.jks app/release-keystore.jks

   ./gradlew bundleRelease
   ```

2. Upload the AAB to Play Console:
   - Go to [Play Console](https://play.google.com/console/)
   - Select your app
   - Go to **Release > Testing > Internal testing**
   - Click **Create new release**
   - Upload `app/build/outputs/bundle/release/app-release.aab`
   - Add release notes
   - Click **Review release** > **Start rollout to Internal testing**

3. Clean up:
   ```bash
   rm app/release-keystore.jks
   ```

## Step 7: Automated Deployment

Once the initial release is done, automated deployment will work!

### Automatic Deployment

Push to these branches to trigger automatic deployment:
- `main` - Deploys to **internal** track
- `release/*` - Deploys to **internal** track

### Manual Deployment

You can also manually trigger deployment:

1. Go to **Actions** tab in your GitHub repository
2. Select **Deploy to Play Store Internal Testing**
3. Click **Run workflow**
4. Choose the track (internal, alpha, or beta)
5. Click **Run workflow**

## Understanding Play Store Tracks

| Track | Visibility | Purpose |
|-------|------------|---------|
| **Internal** | Private (invite only, up to 100 testers) | Quick testing, not public |
| **Alpha** | Private (invite only, unlimited testers) | Broader testing group |
| **Beta** | Can be open or closed testing | Pre-release testing |
| **Production** | Public | Live to all users |

**For your use case**, the **Internal** track is perfect - it's completely private and not accessible to the public.

## Workflow Configuration

The workflow file is located at `.github/workflows/deploy-internal.yml`

**What it does:**
1. Checks out your code
2. Sets up Java 17
3. Decodes the keystore from secrets
4. Builds a release AAB (Android App Bundle)
5. Deploys to Play Store internal track
6. Uploads the AAB as an artifact
7. Cleans up sensitive files

## Updating Version

Before each release, update the version in `app/build.gradle.kts`:

```kotlin
defaultConfig {
    versionCode = 2  // Increment this for each release
    versionName = "1.1"  // Update as needed
}
```

**Version rules:**
- `versionCode` must be an integer that increases with each release
- `versionName` is a string shown to users (e.g., "1.0", "1.1", "2.0-beta")

## Monitoring Deployments

1. **GitHub Actions**: Check the **Actions** tab in your repository
2. **Play Console**: Go to **Release > Testing > Internal testing** to see the status
3. **Release Timeline**: Deployments typically take 1-2 hours to become available

## Troubleshooting

### Build Fails: "Keystore was tampered with, or password was incorrect"
- Check that `KEYSTORE_PASSWORD` matches your keystore password
- Verify the base64 encoding didn't introduce extra characters

### API Error: "The caller does not have permission"
- Ensure service account has correct permissions in Play Console
- Wait a few minutes after granting permissions (can take time to propagate)

### Version Code Error: "Version code X has already been used"
- Increment `versionCode` in `app/build.gradle.kts`

### First Upload Fails
- The first release must be uploaded manually through Play Console
- After that, automated uploads will work

## Security Best Practices

✅ **DO:**
- Keep keystore and passwords secure
- Use GitHub Secrets for all sensitive data
- Rotate service account keys periodically
- Enable two-factor authentication on Google accounts

❌ **DON'T:**
- Commit keystore files to git
- Share keystore passwords in plain text
- Use the same keystore for different apps
- Store secrets in code or configuration files

## Support

For issues with:
- **GitHub Actions**: Check the Actions tab for detailed logs
- **Play Console API**: Review the Play Console API documentation
- **App signing**: Refer to Android documentation on app signing

## References

- [Google Play Console](https://play.google.com/console/)
- [Android App Signing](https://developer.android.com/studio/publish/app-signing)
- [Play Console API](https://developers.google.com/android-publisher)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
