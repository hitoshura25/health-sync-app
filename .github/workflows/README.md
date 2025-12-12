# GitHub Actions Workflows - Play Store Deployment

Automated deployment workflows for Google Play Store with staged rollouts.

## Prerequisites

**CRITICAL:** Complete these before first deployment:

1. **GitHub Secrets** (see `distribution/GITHUB_SECRETS.md`):
   - SERVICE_ACCOUNT_JSON
   - SIGNING_KEY_STORE_BASE64
   - SIGNING_KEY_ALIAS
   - SIGNING_STORE_PASSWORD
   - SIGNING_KEY_PASSWORD

2. **GitHub Environment** (required for production):
   - Go to: Repository → Settings → Environments
   - Create "production" environment
   - Enable "Required reviewers"
   - Add yourself/team members as reviewers

3. **Play Console Setup**:
   - Service account created and linked
   - First manual upload completed (required by Google)
   - Release notes updated in `distribution/whatsnew/`

## Workflows Overview

### deploy-internal.yml
**Automatic deployment to internal testing track**

- **Trigger:** Push to `main` or `develop` branch
- **Track:** Internal testing (up to 100 testers)
- **Approval:** None (automatic)
- **Review:** Instant (< 1 minute)
- **Use:** Daily builds, CI/CD validation, quick iterations

**When it runs:**
```bash
git push origin main
# Automatically deploys to internal track
```

---

### deploy-beta.yml
**Manual deployment to alpha/beta testing tracks**

- **Trigger:** Manual only (workflow_dispatch)
- **Tracks:** Alpha or Beta (closed testing)
- **Approval:** Manual trigger is the approval
- **Rollout:** Configurable (5-100%)
- **Review:** Typically < 1 day
- **Use:** Beta testing, pre-production validation

**How to use:**
1. Go to: Actions → Deploy to Beta Testing
2. Click "Run workflow"
3. Select track: "beta" (or "alpha" for smaller group)
4. Set rollout: "100" (or lower for staged beta)
5. Click "Run workflow"

---

### deploy-production.yml
**Production deployment with staged rollout**

- **Trigger:** Git tag push (v*) or manual workflow_dispatch
- **Track:** Production
- **Approval:** **REQUIRED** via "production" environment
- **Rollout:** Staged (default 5%)
- **Review:** 1-7 days (first submission, then hours)
- **Use:** Official releases

**How to use:**

**Via Git Tag (Recommended):**
```bash
git tag v1.0.0
git push origin v1.0.0
# Then approve in GitHub Actions tab
```

**Via Manual Trigger:**
1. Go to: Actions → Deploy to Production
2. Click "Run workflow"
3. Set rollout percentage (default 5%)
4. Click "Run workflow"
5. Wait for approval request
6. Approve deployment

---

### manage-rollout.yml
**Control production rollout percentage**

- **Trigger:** Manual only
- **Actions:** increase, halt, resume, complete
- **Approval:** Required via "production" environment
- **Use:** Increase rollout, emergency halt, resume after fix

**How to use:**

**Increase rollout to 20%:**
1. Go to: Actions → Manage Production Rollout
2. Click "Run workflow"
3. Select action: "increase"
4. Enter percentage: "20"
5. Click "Run workflow"
6. Approve request

**Emergency halt:**
1. Actions → Manage Production Rollout
2. Select action: "halt"
3. Click "Run workflow"

**Resume after fix:**
1. Deploy new version with fix
2. Or select action: "resume" to resume current version

**Complete rollout to 100%:**
1. Actions → Manage Production Rollout
2. Select action: "complete"
3. Click "Run workflow"

---

## Deployment Workflow

Recommended deployment pipeline:

```
Development
    ↓
Push to main → Internal Testing (automatic)
    ↓
Test & validate (< 1 day)
    ↓
Manual trigger → Beta Testing (alpha: 10-50 users)
    ↓
Wider beta (1-2 weeks)
    ↓
Manual trigger → Beta Testing (beta: 100-1000+ users)
    ↓
Beta feedback & fixes (1-2 weeks)
    ↓
Git tag v*.*.* → Production (5% staged rollout)
    ↓
Monitor 24-48 hours
    ↓
Increase to 20% → Monitor
    ↓
Increase to 50% → Monitor
    ↓
Complete to 100%
```

## Beta Testing Best Practices

### 1. Use Alpha for Pre-Beta
- Test with small group (10-50 users)
- Catch major issues before wider beta
- Quick feedback loop
- Deploy via: Actions → Deploy to Beta Testing → track: "alpha"

### 2. Beta for Broader Testing
- Larger group (100-1000+ users)
- Diverse devices and OS versions
- Real-world usage patterns
- 1-2 week minimum period
- Deploy via: Actions → Deploy to Beta Testing → track: "beta"

### 3. Staged Beta Rollout
- Start at 50% for critical updates
- Monitor for 24 hours
- Increase to 100% if stable
- Can halt and fix if issues found

### 4. Collect Feedback
- Monitor crash reports in Play Console
- Review user feedback
- Check ANR (Application Not Responding) rate
- Address critical issues before production

---

## Production Deployment Best Practices

### 1. Always Test in Internal First
- Never skip to production
- Catch obvious issues early
- Verify signing and ProGuard configuration

### 2. Use Staged Rollouts
**Recommended rollout schedule:**
- Day 1: 5% (deploy via tag)
- Day 2: 20% (if crash-free rate > 99.5%)
- Day 3: 50% (if stable)
- Day 5: 100% (complete rollout)

### 3. Monitor Key Metrics
Before increasing rollout, verify:
- **Crash-free rate:** > 99.5%
- **ANR rate:** < 0.5%
- **User ratings:** Not declining
- **Install/uninstall rates:** Normal

### 4. Have Rollback Plan
If issues detected:
1. Halt rollout immediately (manage-rollout → halt)
2. Fix issue in new version
3. Deploy fixed version
4. Start new staged rollout

### 5. Emergency Response
**Critical issue detected:**
```bash
# 1. Halt rollout immediately
Actions → Manage Production Rollout → halt

# 2. Fix issue
git checkout -b hotfix/critical-bug
# ... make fix ...
git commit -m "fix: critical bug"
git push origin hotfix/critical-bug

# 3. Deploy fix
git tag v1.0.1
git push origin v1.0.1

# 4. New staged rollout starts at 5%
```

---

## Release Notes

Before each deployment, update release notes:

```bash
# Edit release notes for all locales
vim distribution/whatsnew/en-US/whatsnew
vim distribution/whatsnew/de-DE/whatsnew
# ... other locales ...

# Verify character count (max 500)
wc -m distribution/whatsnew/en-US/whatsnew

# Commit and push
git add distribution/whatsnew/
git commit -m "docs: update release notes for v1.0.0"
git push
```

**Release notes guidelines:**
- Maximum 500 characters
- Focus on user-visible changes
- List most important first
- Use bullet points
- Avoid technical jargon

---

## Version Management

**Version Code & Version Name:**

Update `app/build.gradle.kts`:
```kotlin
versionCode = 2  // Increment for each release
versionName = "1.0.1"  // Semantic versioning
```

**Recommended versioning:**
- Major: Breaking changes (1.0.0 → 2.0.0)
- Minor: New features (1.0.0 → 1.1.0)
- Patch: Bug fixes (1.0.0 → 1.0.1)

**Version code must always increase** (Google Play requirement).

---

## Troubleshooting

### "Environment not found"
**Cause:** "production" environment not created
**Fix:** Repository → Settings → Environments → Create "production"

### "Missing secrets"
**Cause:** Required secrets not configured
**Fix:** Add all 5 secrets (see `distribution/GITHUB_SECRETS.md`)

### "Package not found"
**Cause:** First manual upload not completed
**Fix:** Upload first version manually via Play Console

### "Permission denied"
**Cause:** Service account lacks permissions
**Fix:** Grant "Release" permission in Play Console → API access

### "Invalid rollout percentage"
**Cause:** Percentage not between 5-100
**Fix:** Use valid values: 5, 10, 20, 50, 100

### "Workflow failed on 'Deploy to Production'"
**Cause:** Various (check workflow logs)
**Fix:**
1. Check workflow logs in Actions tab
2. Verify all secrets are set correctly
3. Verify service account has permissions
4. Check Play Console for error details

---

## Monitoring & Alerts

**Play Console Metrics:**
- Go to: Play Console → Your App → Vitals
- Monitor:
  - Crash-free rate (target: > 99.5%)
  - ANR rate (target: < 0.5%)
  - User ratings
  - Install/uninstall trends

**GitHub Actions:**
- Monitor workflow runs in Actions tab
- Enable email notifications for failures
- Review artifact uploads (AAB, mapping files)

---

## Security Notes

**Keystore Security:**
- Keystore is decoded at build time, never committed
- Cleaned up after build (even on failure)
- Stored as base64 in GitHub Secrets

**Service Account Security:**
- JSON key stored in GitHub Secrets
- Never logged or exposed
- Minimum required permissions only
- Rotate annually

**Workflow Security:**
- Production requires manual approval
- Secrets are encrypted
- Artifacts retained based on track (30-365 days)

---

## First Deployment Checklist

Before your first production deployment:

- [ ] GitHub "production" environment created
- [ ] Required reviewers configured
- [ ] All 5 GitHub Secrets added
- [ ] Service account setup validated
- [ ] First manual upload completed in Play Console
- [ ] Release notes updated for all locales
- [ ] Version code incremented
- [ ] Tested in internal track
- [ ] Tested in beta track (optional but recommended)

---

## Quick Reference

| Action | Command | Approval | Track |
|--------|---------|----------|-------|
| Deploy to internal | `git push origin main` | None | internal |
| Deploy to alpha | Manual workflow | None | alpha |
| Deploy to beta | Manual workflow | None | beta |
| Deploy to production | `git tag v1.0.0 && git push origin v1.0.0` | **Required** | production |
| Increase rollout | Manual workflow | **Required** | production |
| Halt rollout | Manual workflow | **Required** | production |
| Complete rollout | Manual workflow | **Required** | production |

---

## Additional Resources

- **Play Console:** https://play.google.com/console/
- **Setup Guide:** `distribution/PLAY_CONSOLE_SETUP.md`
- **GitHub Secrets:** `distribution/GITHUB_SECRETS.md`
- **Release Tracks:** `distribution/TRACKS.md`
- **Release Notes:** `distribution/whatsnew/README.md`
