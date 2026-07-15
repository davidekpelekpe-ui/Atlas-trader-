# Deployment Guide - Atlas Trader

## Pre-Deployment Checklist

- [ ] All tests passing (`./gradlew test`)
- [ ] No lint errors (`./gradlew lint`)
- [ ] Version code updated in `build.gradle.kts`
- [ ] Version name updated
- [ ] README.md updated
- [ ] CHANGELOG.md updated
- [ ] All secrets configured in `.env`
- [ ] ProGuard rules configured
- [ ] App icon assets updated

## Version Management

### Update Version

```kotlin
// In app/build.gradle.kts
android {
    defaultConfig {
        versionCode = 2          // Increment by 1
        versionName = "2.0.0"    // Use semantic versioning
    }
}
```

### Semantic Versioning
- **MAJOR.MINOR.PATCH** (e.g., 2.0.1)
- MAJOR: Breaking changes
- MINOR: New features (backward compatible)
- PATCH: Bug fixes

## Release Build Process

### 1. Prepare Release

```bash
# Create release branch
git checkout -b release/v2.0.0

# Update version numbers
# Update CHANGELOG.md
# Commit changes
git commit -m "chore: prepare release v2.0.0"
```

### 2. Build Release APK

```bash
# Set environment variables
export KEYSTORE_PATH="/path/to/keystore.jks"
export STORE_PASSWORD="your_password"
export KEY_PASSWORD="your_password"

# Build release
./gradlew clean assembleRelease

# Output location: app/build/outputs/apk/release/app-release.apk
```

### 3. Build Release AAB (for Play Store)

```bash
./gradlew bundleRelease

# Output location: app/build/outputs/bundle/release/app-release.aab
```

### 4. Test Release Build

```bash
# Install on device
adb install app/build/outputs/apk/release/app-release.apk

# Test all critical features
```

### 5. Sign & Upload

```bash
# Sign with your keystore (already done in build)
# Upload to Play Store via Play Console
```

## Keystore Management

### Generate Keystore (First Time Only)

```bash
keytool -genkey -v -keystore atlas-trader.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias upload
```

### Keystore Information

```bash
keytool -list -v -keystore atlas-trader.jks
```

### Backup Keystore

```bash
# Keep in safe location (NOT in git)
cp atlas-trader.jks ~/Backups/
chmod 600 ~/Backups/atlas-trader.jks
```

## Play Store Deployment

### 1. App Store Listing

- App name: "Atlas Trader"
- Short description: "AI-Powered Trade Analysis Platform"
- Full description: [See Play Store listing]
- Category: Finance
- Content rating: Medium Maturity

### 2. Upload to Play Store

1. Go to [Google Play Console](https://play.google.com/console)
2. Select "Atlas Trader"
3. Go to "Release" → "Production"
4. Click "Create new release"
5. Upload `app-release.aab`
6. Add release notes
7. Review and deploy

### 3. Rollout Strategy

- **Staged Rollout**: 10% → 25% → 50% → 100%
- Monitor crash reports and ratings
- Pause if critical issues found
- Roll back if necessary

## Monitoring Post-Deployment

### Track Metrics

- Crash rate (target: < 0.1%)
- ANR rate (target: < 0.1%)
- User ratings (target: > 4.5)
- Install rate
- Uninstall rate
- Session length

### Firebase Analytics

```kotlin
// Track custom events
FirebaseAnalytics.getInstance(context).logEvent("trade_analysis_opened", bundleOf())
```

### Crash Reporting

- Monitor Firebase Crashlytics
- Set up Slack notifications
- Create issues for critical crashes

## Rollback Procedure

If critical issues are discovered:

1. Pause Play Store rollout immediately
2. Create hotfix branch from release tag
3. Fix issues
4. Increment version code
5. Rebuild and redeploy

```bash
# Rollback to previous version
git checkout v1.0.0
git checkout -b hotfix/v1.0.1
# Fix issues
# Build and deploy
```

## Documentation After Release

- Update README.md with latest version
- Update CHANGELOG.md
- Create GitHub release with notes
- Announce on social media
- Send email notification to users

## Troubleshooting

### Build Fails

```bash
./gradlew clean
./gradlew assembleRelease --stacktrace
```

### Keystore Issues

```bash
# Verify keystore
keytool -list -v -keystore atlas-trader.jks

# Check signature
jarsigner -verify -verbose -certs app-release.apk
```

### Play Store Upload Fails

- Check AAB format
- Verify signing certificate
- Check bundle size limits
- Ensure all permissions are listed

## Release Checklist Template

```markdown
## Release v2.0.0

- [ ] Version bumped to 2.0.0
- [ ] CHANGELOG.md updated
- [ ] README.md updated
- [ ] All tests passing
- [ ] No lint warnings
- [ ] No ProGuard warnings
- [ ] APK built and tested
- [ ] AAB built
- [ ] Signed with correct keystore
- [ ] Play Store listing updated
- [ ] Release notes prepared
- [ ] Screenshots updated
- [ ] Rollout strategy decided
- [ ] Team notified
- [ ] Deployed to Play Store
- [ ] Monitored for crashes
```
