# Health Sync App - Deployment Guide

Complete guide for deploying to Google Play Store using GitHub Actions.

## Package Information

**Package Name:** `io.github.hitoshura25.healthsyncapp`

## Prerequisites Checklist

Before your first deployment, ensure ALL items are complete:

### 1. Play Console Setup
- [ ] Google Play Developer account active ($25 one-time fee)
- [ ] Service account created in Google Cloud Console
- [ ] Service account JSON key downloaded and stored securely
- [ ] Play Developer API enabled
- [ ] Service account linked to Play Console
- [ ] Service account has "Release to production" permission
- [ ] **First manual upload completed** (Google requirement)

**Guide:** See `distribution/PLAY_CONSOLE_SETUP.md`

**Validation:**
```bash
python3 scripts/validate-playstore.py \
  ~/path/to/service-account.json \
  io.github.hitoshura25.healthsyncapp
```

### 2. GitHub Secrets
- [ ] SERVICE_ACCOUNT_JSON (entire JSON file contents)
- [ ] SIGNING_KEY_STORE_BASE64 (base64-encoded keystore)
- [ ] SIGNING_KEY_ALIAS (from KEYSTORE_INFO.txt)
- [ ] SIGNING_STORE_PASSWORD (from KEYSTORE_INFO.txt)
- [ ] SIGNING_KEY_PASSWORD (from KEYSTORE_INFO.txt)

**Guide:** See `distribution/GITHUB_SECRETS.md`

**Add Secrets:**
1. Go to: Repository → Settings → Secrets and variables → Actions
2. Click "New repository secret"
3. Add each secret

### 3. GitHub Environment
- [ ] "production" environment created
- [ ] Required reviewers configured (you and/or team members)

**Setup:**
1. Go to: Repository → Settings → Environments
2. Click "New environment"
3. Name: `production`
4. Enable "Required reviewers"
5. Add reviewers
6. Click "Save protection rules"

---

## Deployment Workflows

### Internal Testing (Automatic)

**When:** Automatically on every push to `main` or `develop`

**What:** Deploys to internal testing track (up to 100 testers)

**How:**
```bash
git add .
git commit -m "feat: new feature"
git push origin main
```

**Review Time:** Instant (< 1 minute)

**Monitor:** Actions → Deploy to Internal Testing

---

### Beta Testing (Manual)

**When:** Manual trigger when ready for wider testing

**What:** Deploys to alpha or beta track

**How:**
1. Go to: Actions → Deploy to Beta Testing
2. Click "Run workflow"
3. Select:
   - Track: "beta" (or "alpha" for smaller group)
   - Rollout: "100" (or lower for staged)
4. Click "Run workflow"

**Review Time:** < 1 day typically

**Best Practice:**
- Use alpha (10-50 users) for pre-beta
- Use beta (100-1000+ users) for broader testing
- Run beta for 1-2 weeks minimum
- Monitor crash reports and feedback

---

### Production (Approval Required)

**When:** Ready for public release

**What:** Deploys to production with staged rollout

**How (Recommended - via Git Tag):**
```bash
# Update version in app/build.gradle.kts
# versionCode = 2  (increment)
# versionName = "1.0.1"

# Update release notes
vim distribution/whatsnew/en-US/whatsnew

# Commit changes
git add app/build.gradle.kts distribution/whatsnew/
git commit -m "chore: bump version to 1.0.1"

# Create and push tag
git tag v1.0.1
git push origin main
git push origin v1.0.1

# Approve in GitHub Actions tab
# Go to: Actions → Deploy to Production → Approve
```

**Review Time:** 1-7 days (first submission), then hours

**Default Rollout:** 5% (staged)

---

## Staged Rollout Process

### Recommended Schedule

**Day 1: Deploy at 5%**
```bash
git tag v1.0.0
git push origin v1.0.0
# Approve in GitHub Actions
```

**Monitor for 24-48 hours:**
- Crash-free rate > 99.5%
- ANR rate < 0.5%
- No critical user feedback

**Day 2: Increase to 20%**
1. Go to: Actions → Manage Production Rollout
2. Click "Run workflow"
3. Select action: "increase"
4. Enter percentage: "20"
5. Approve

**Monitor for 24 hours**

**Day 3: Increase to 50%**
1. Same as above, percentage: "50"
2. Monitor for 24 hours

**Day 5: Complete to 100%**
1. Actions → Manage Production Rollout
2. Select action: "complete"
3. Approve

---

## Emergency Procedures

### Critical Bug Detected

**1. Halt Rollout Immediately:**
```
Actions → Manage Production Rollout
Select action: "halt"
Click "Run workflow" → Approve
```

**2. Fix the Bug:**
```bash
git checkout -b hotfix/critical-bug
# Make fix
git commit -m "fix: critical bug in production"
git push origin hotfix/critical-bug
```

**3. Deploy Fix:**
```bash
# Increment version
# versionCode = 3
# versionName = "1.0.2"

git tag v1.0.2
git push origin v1.0.2
# Approve deployment
```

**4. New staged rollout starts automatically at 5%**

---

## Release Notes Guidelines

Update release notes before each deployment:

**Location:** `distribution/whatsnew/{locale}/whatsnew`

**Format:**
- Maximum 500 characters
- Plain text (no markdown/HTML)
- UTF-8 encoding
- Focus on user-visible changes

**Example (Good):**
```
- New: Dark mode for easier nighttime reading
- Improved: App startup speed by 50%
- Fixed: Crash when syncing large datasets
- Updated: Better accessibility support
```

**Example (Avoid):**
```
- Refactored codebase
- Updated dependencies
- Various bug fixes
- Performance improvements
```

**Verify character count:**
```bash
wc -m distribution/whatsnew/en-US/whatsnew
```

---

## Version Management

**File:** `app/build.gradle.kts`

**Update before each release:**
```kotlin
defaultConfig {
    applicationId = "io.github.hitoshura25.healthsyncapp"
    versionCode = 2      // Must increase for each release
    versionName = "1.0.1" // Semantic versioning
}
```

**Version Code Rules:**
- MUST increase with every release
- Google Play requirement
- Cannot skip numbers
- Cannot decrease

**Version Name Best Practices:**
- Major: Breaking changes (1.0.0 → 2.0.0)
- Minor: New features (1.0.0 → 1.1.0)
- Patch: Bug fixes (1.0.0 → 1.0.1)

---

## Monitoring & Metrics

### Play Console Vitals

**Access:** Play Console → Your App → Vitals

**Key Metrics:**

1. **Crash-free rate**
   - Target: > 99.5%
   - Action if below: Halt rollout, investigate

2. **ANR (Application Not Responding) rate**
   - Target: < 0.5%
   - Action if high: Check for blocking operations

3. **User ratings**
   - Monitor for sudden drops
   - Review negative feedback

4. **Install/Uninstall rates**
   - Watch for unusual patterns

### GitHub Actions

**Monitor:**
- Actions tab for workflow runs
- Artifacts: AAB files, mapping files
- Enable email notifications for failures

---

## Troubleshooting

### "Workflow failed: Missing secrets"
**Solution:** Add all 5 required secrets (see GITHUB_SECRETS.md)

### "Workflow failed: Environment not found"
**Solution:** Create "production" environment in repository settings

### "Play Console: Permission denied"
**Solution:** Grant service account "Release to production" permission

### "Play Console: Package not found"
**Solution:** Complete first manual upload via Play Console

### "Invalid version code"
**Solution:** Version code must be higher than current production version

### "Release notes too long"
**Solution:** Keep under 500 characters, focus on top 3-4 changes

---

## First Deployment Walkthrough

### Step 1: Complete Prerequisites (1-2 hours)
1. Service account setup (30 min)
2. Add GitHub Secrets (10 min)
3. Create production environment (5 min)
4. First manual upload to Play Console (30 min)

### Step 2: Test in Internal Track (1 day)
```bash
git push origin main
# Verify deployment in Play Console
# Test with internal testers
```

### Step 3: Beta Testing (1-2 weeks)
```bash
# Via GitHub Actions: Deploy to Beta Testing
# Monitor feedback and crash reports
# Fix any issues
```

### Step 4: Production Deployment (1 week)
```bash
# Update version and release notes
git tag v1.0.0
git push origin v1.0.0
# Approve in GitHub Actions

# Day 1: 5% rollout
# Day 2: 20% rollout (if stable)
# Day 3: 50% rollout (if stable)
# Day 5: 100% rollout (complete)
```

---

## Quick Reference

| Action | Command | Approval | Track | Review Time |
|--------|---------|----------|-------|-------------|
| Internal | `git push origin main` | None | internal | < 1 min |
| Beta | Manual workflow | None | beta | < 1 day |
| Production | `git tag v1.0.0 && git push origin v1.0.0` | **Required** | production | 1-7 days (first), then hours |
| Increase rollout | Manual workflow | **Required** | production | Hours |
| Halt rollout | Manual workflow | **Required** | production | Immediate |

---

## Additional Resources

- **Workflows Guide:** `.github/workflows/README.md`
- **Play Console Setup:** `distribution/PLAY_CONSOLE_SETUP.md`
- **GitHub Secrets:** `distribution/GITHUB_SECRETS.md`
- **Release Tracks:** `distribution/TRACKS.md`
- **Release Notes:** `distribution/whatsnew/README.md`
- **Validation Script:** `scripts/validate-playstore.py`

---

## Support

**Play Console:** https://play.google.com/console/
**GitHub Actions:** Repository → Actions tab
**Local Validation:** `python3 scripts/validate-playstore.py`
