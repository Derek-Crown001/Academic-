# AcademiaTrack — Platform Owner Master Console

Standalone executive SaaS administration application for **AcademiaTrack**. This dedicated repository contains the Platform Owner Master Android application.

## Core Capabilities
- **Multi-Tenant School Provisioning**: Onboard new school tenants, assign custom school codes, and manage account credentials.
- **Remote Access Control & Lockout**: Instant remote killswitch and account lock capabilities for billing enforcement and security management.
- **Subscription & License Key Generation**: Cryptographic license key issuance for Monthly, Termly, and Annual tiers.
- **Global App Settings & Telemetry**: Over-the-air control for AI Gateways, maintenance mode flags, and live metrics monitoring.
- **Cloud Firestore Synchronizer**: Real-time broadcast and listener services to update all client school devices instantly.

## Building the APK
Run Gradle to build the standalone debug APK:
```bash
gradle :app:assembleDebug
```
The resulting binary will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

## CI / CD
Every push to `main` automatically triggers GitHub Actions to compile and publish the latest `AcademiaTrack-Owner-Master.apk` directly to GitHub Releases.
