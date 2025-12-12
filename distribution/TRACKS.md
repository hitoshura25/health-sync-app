# Google Play Store Release Tracks

Complete guide to Play Store release tracks and workflow.

## Track Types

### 1. Internal Testing
**Audience:** Up to 100 testers (team only)
**Review:** None (instant, < 1 minute)
**Best for:** Daily builds, CI/CD, quick iterations
**Access:** Email-based tester list

**Use when:**
- Testing new features rapidly
- Verifying CI/CD pipeline
- Quick bug fix validation

### 2. Closed Testing (Beta)
**Audience:** Unlimited invited testers
**Review:** Typically < 1 day
**Best for:** Beta program, QA team, stakeholders
**Access:** Email list or shareable link

**Use when:**
- Extended testing with real users
- Collecting feedback before public release
- Testing with diverse devices/OS versions

### 3. Open Testing
**Audience:** Anyone with the link
**Review:** 1-7 days (first submission)
**Best for:** Public beta, community testing
**Access:** Public Play Store link

**Use when:**
- Large-scale public beta
- Community-driven feature testing
- Stress testing infrastructure

### 4. Production
**Audience:** All users (or staged rollout)
**Review:** 1-7 days (first submission, then hours)
**Best for:** Official releases
**Rollout:** Staged (5% → 10% → 50% → 100%)

**Use when:**
- Ready for public release
- All testing complete
- Version approved for general availability

## Recommended Workflow

```
Development
    ↓
Push to main → Internal Testing (auto)
    ↓
Test & validate
    ↓
Deploy to Beta → Closed Testing (manual)
    ↓
Beta feedback (1-2 weeks)
    ↓
Tag release → Production (manual approval)
    ↓
Staged Rollout:
  Day 1: 5%  → Monitor crash-free rate
  Day 2: 20% → Monitor ANR rate
  Day 3: 50% → Monitor user feedback
  Day 5: 100% → Complete rollout
```

## Promotion Between Tracks

**Internal → Closed:**
- Manual promotion in Play Console
- Or automated via GitHub Actions workflow

**Closed → Production:**
- Always requires manual approval
- Create GitHub release tag
- Triggers production deployment workflow

**Emergency Halt:**
If issues detected:
1. Run manage-rollout workflow with "halt" action
2. Fix issue
3. Deploy new version
4. Resume or start new rollout

## Best Practices

1. **Always test in Internal first**
   - Never skip to production
   - Catch obvious issues early

2. **Use Closed Testing for Beta**
   - Get real user feedback
   - Test on diverse devices
   - Minimum 1 week beta period

3. **Staged Production Rollouts**
   - Start at 5-10%
   - Monitor for 24-48 hours
   - Only increase if crash-free rate > 99%

4. **Monitor Key Metrics**
   - Crash-free rate (target: > 99.5%)
   - ANR rate (target: < 0.5%)
   - User ratings
   - Install/uninstall rates

5. **Have Rollback Plan**
   - Keep previous version available
   - Can halt rollout anytime
   - Can decrease rollout percentage
