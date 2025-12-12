# GitHub Secrets Setup

Add these secrets to your GitHub repository for automated deployment.

## Required Secrets

Go to: Repository → Settings → Secrets and variables → Actions → New repository secret

### 1. SERVICE_ACCOUNT_JSON

**Value:** Entire contents of the JSON file downloaded in service account setup

**How to add:**
1. Open the service account JSON file
2. Copy entire contents (including { and })
3. Paste as secret value
4. Click "Add secret"

### 2. SIGNING_KEY_STORE_BASE64

**Value:** Base64-encoded production keystore

**How to create:**
```bash
base64 -w 0 keystores/production-release.jks
# OR on macOS:
base64 -i keystores/production-release.jks
```

### 3. SIGNING_KEY_ALIAS

**Value:** `upload` (from KEYSTORE_INFO.txt)

### 4. SIGNING_STORE_PASSWORD

**Value:** Production keystore password (from KEYSTORE_INFO.txt)

### 5. SIGNING_KEY_PASSWORD

**Value:** Production key password (same as store password for PKCS12)

## Verification

After adding secrets:
1. Go to: Repository → Settings → Secrets and variables → Actions
2. Verify all 5 secrets are listed
3. Secrets are encrypted and cannot be viewed after creation
4. Use workflow runs to verify secrets work

## Security Notes

- Never log secret values
- Rotate SERVICE_ACCOUNT_JSON annually
- Keep KEYSTORE_INFO.txt secure (not in git)
- Use environment protection for production deployments
