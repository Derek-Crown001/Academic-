#!/usr/bin/env bash
# ==============================================================================
# AcademiaTrack - Standalone Owner Master App Exporter & Git Push Script
# ==============================================================================
# This script extracts the executive Platform Owner Master app from the
# AcademiaTrack monorepo into an independent, standalone Android project
# and pushes it to a separate GitHub repository.
#
# Usage:
#   bash scripts/push-owner-repo.sh <target_git_url> [branch]
#
# Examples:
#   bash scripts/push-owner-repo.sh https://github.com/derekcrown001/AcademiaTrack-Owner.git
#   bash scripts/push-owner-repo.sh git@github.com:derekcrown001/AcademiaTrack-Owner.git main
#   bash scripts/push-owner-repo.sh --export-only ./standalone-owner-app
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

TARGET_REMOTE="$1"
TARGET_BRANCH="${2:-main}"

if [ -z "${TARGET_REMOTE}" ]; then
  echo "======================================================================"
  echo "AcademiaTrack: Standalone Owner Master App Exporter"
  echo "======================================================================"
  echo "Usage:"
  echo "  bash scripts/push-owner-repo.sh <target_git_url> [branch]"
  echo "  bash scripts/push-owner-repo.sh --export-only [destination_directory]"
  echo ""
  echo "Example:"
  echo "  bash scripts/push-owner-repo.sh https://github.com/derekcrown001/AcademiaTrack-Owner.git"
  echo "======================================================================"
  exit 1
fi

EXPORT_ONLY=false
if [ "${TARGET_REMOTE}" = "--export-only" ]; then
  EXPORT_ONLY=true
  OUTPUT_DIR="${2:-${ROOT_DIR}/standalone-owner-app}"
else
  OUTPUT_DIR="${RUNNER_TEMP:-/tmp}/standalone-owner-app-$(date +%s)"
fi

echo "==> Preparing standalone Owner Master Android project in: ${OUTPUT_DIR}"
rm -rf "${OUTPUT_DIR}"
mkdir -p "${OUTPUT_DIR}"

# 1. Copy Root Gradle Configuration
echo "==> Setting up root Gradle structure..."
cp "${ROOT_DIR}/build.gradle.kts" "${OUTPUT_DIR}/"
cp "${ROOT_DIR}/gradle.properties" "${OUTPUT_DIR}/"
if [ -f "${ROOT_DIR}/.env.example" ]; then
  cp "${ROOT_DIR}/.env.example" "${OUTPUT_DIR}/"
fi
if [ -f "${ROOT_DIR}/firestore.rules" ]; then
  cp "${ROOT_DIR}/firestore.rules" "${OUTPUT_DIR}/"
fi

# Copy gradle wrapper & version catalog
mkdir -p "${OUTPUT_DIR}/gradle"
if [ -d "${ROOT_DIR}/gradle" ]; then
  cp -r "${ROOT_DIR}/gradle/"* "${OUTPUT_DIR}/gradle/"
fi

# Create standalone settings.gradle.kts
cat << 'EOF' > "${OUTPUT_DIR}/settings.gradle.kts"
pluginManagement {
  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "AcademiaTrack-Owner-Master"

include(":app")
EOF

# 2. Copy App Module Structure
echo "==> Copying app module..."
mkdir -p "${OUTPUT_DIR}/app"
cp "${ROOT_DIR}/app/build.gradle.kts" "${OUTPUT_DIR}/app/build.gradle.kts"
if [ -f "${ROOT_DIR}/app/proguard-rules.pro" ]; then
  cp "${ROOT_DIR}/app/proguard-rules.pro" "${OUTPUT_DIR}/app/"
fi

# Modify app/build.gradle.kts for standalone owner app
sed -i 's/applicationId = "com.aistudio.academiatrack.app"/applicationId = "com.aistudio.academiatrack.owner"/g' "${OUTPUT_DIR}/app/build.gradle.kts"

# Copy source tree
echo "==> Copying source tree..."
mkdir -p "${OUTPUT_DIR}/app/src/main"
cp -r "${ROOT_DIR}/app/src/main/res" "${OUTPUT_DIR}/app/src/main/"

# Create custom strings.xml for Owner Master App
cat << 'EOF' > "${OUTPUT_DIR}/app/src/main/res/values/strings.xml"
<resources>
    <string name="app_name">AcademiaTrack Owner Master</string>
    <string name="owner_master_label">Owner Master Console</string>
    <string name="owner_sync_service_description">Platform Owner Remote Access &amp; Cloud Telemetry Service</string>
</resources>
EOF

# Copy java/kotlin code
mkdir -p "${OUTPUT_DIR}/app/src/main/java"
cp -r "${ROOT_DIR}/app/src/main/java/"* "${OUTPUT_DIR}/app/src/main/java/"

# Create standalone AndroidManifest.xml for the Owner App
cat << 'EOF' > "${OUTPUT_DIR}/app/src/main/AndroidManifest.xml"
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:name=".SchoolApplication"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.MyApplication">

        <!-- Primary Standalone Launcher: Owner Master Console -->
        <activity
            android:name="com.example.owner.OwnerMainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:taskAffinity="com.example.owner"
            android:launchMode="singleTask"
            android:icon="@mipmap/ic_launcher"
            android:theme="@style/Theme.MyApplication">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity
            android:name=".OwnerConsoleActivity"
            android:exported="true"
            android:label="AcademiaTrack Owner Master"
            android:taskAffinity="com.example.owner"
            android:launchMode="singleTask"
            android:theme="@style/Theme.MyApplication">
        </activity>

        <!-- Platform Owner Remote Control & Firestore Synchronization Service -->
        <service
            android:name="com.example.owner.service.OwnerRemoteSyncService"
            android:exported="false"
            android:description="@string/owner_sync_service_description">
            <intent-filter>
                <action android:name="com.example.owner.action.TOGGLE_ACCOUNT_LOCK" />
                <action android:name="com.example.owner.action.TOGGLE_MAINTENANCE" />
                <action android:name="com.example.owner.action.SYNC_SCHOOL_INSTANCE" />
                <action android:name="com.example.owner.action.BROADCAST_GLOBAL_MAINTENANCE" />
            </intent-filter>
        </service>

        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.provider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>
    </application>

</manifest>
EOF

# 3. Create standalone .gitignore
cat << 'EOF' > "${OUTPUT_DIR}/.gitignore"
*.iml
.gradle
/local.properties
/.idea/caches
/.idea/libraries
/.idea/modules.xml
/.idea/workspace.xml
/.idea/navEditor.xml
/.idea/assetWizardSettings.xml
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties
debug.keystore
*.apk
EOF

# 4. Create standalone GitHub Actions CI/CD for building the Owner APK
mkdir -p "${OUTPUT_DIR}/.github/workflows"
cat << 'EOF' > "${OUTPUT_DIR}/.github/workflows/build-apk.yml"
name: Build Owner Master APK

on:
  push:
    branches:
      - main
  workflow_dispatch:

permissions:
  contents: write

jobs:
  build:
    name: Build & Release Owner APK
    runs-on: ubuntu-latest

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up Java 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Set up Gradle 9.3.1
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: "9.3.1"

      - name: Create temporary debug keystore
        run: |
          keytool -genkey -v \
            -keystore "./debug.keystore" \
            -storepass android \
            -alias androiddebugkey \
            -keypass android \
            -keyalg RSA \
            -keysize 2048 \
            -validity 10000 \
            -dname "CN=Android Debug,O=Android,C=US"

      - name: Build Owner Master debug APK
        run: |
          gradle :app:assembleDebug --stacktrace --no-daemon

      - name: Prepare Release Asset
        id: prepare_asset
        run: |
          SRC_APK="app/build/outputs/apk/debug/app-debug.apk"
          TARGET_DIR="${RUNNER_TEMP}/release-asset"
          mkdir -p "${TARGET_DIR}"
          TARGET_APK="${TARGET_DIR}/AcademiaTrack-Owner-Master.apk"
          cp -f "${SRC_APK}" "${TARGET_APK}"
          echo "apk_path=${TARGET_APK}" >> "$GITHUB_OUTPUT"

      - name: Upload Artifact
        uses: actions/upload-artifact@v4
        with:
          name: AcademiaTrack-Owner-Master-APK
          path: app/build/outputs/apk/debug/app-debug.apk

      - name: Publish Owner APK to GitHub Releases
        continue-on-error: true
        env:
          GH_TOKEN: ${{ github.token }}
        run: |
          RELEASE_TAG="owner-v${{ github.run_number }}"
          gh release create "${RELEASE_TAG}" "${{ steps.prepare_asset.outputs.apk_path }}" \
            --title "AcademiaTrack Owner Master Release #${{ github.run_number }}" \
            --notes "Standalone executive Platform Owner Master Console APK with multi-tenant provisioning, remote access control, and telemetry switchboard." \
            --latest
EOF

# 5. Create Standalone README.md
cat << 'EOF' > "${OUTPUT_DIR}/README.md"
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
EOF

echo "==> Standalone project successfully assembled at: ${OUTPUT_DIR}"

if [ "${EXPORT_ONLY}" = true ]; then
  echo "==> Export complete! You can find the standalone project in: ${OUTPUT_DIR}"
  exit 0
fi

# 6. Initialize Git and Push to Target Remote
echo "==> Initializing Git repository and pushing to: ${TARGET_REMOTE}"
cd "${OUTPUT_DIR}"

git init
git checkout -b "${TARGET_BRANCH}"
git config user.name "AcademiaTrack Exporter"
git config user.email "bot@academiatrack.app"

git add .
git commit -m "feat: standalone AcademiaTrack Owner Master App project"

git remote add origin "${TARGET_REMOTE}"

echo "==> Pushing to ${TARGET_REMOTE} on branch ${TARGET_BRANCH}..."
git push -u origin "${TARGET_BRANCH}" --force

echo "======================================================================"
echo " SUCCESS: Owner Master App successfully pushed to ${TARGET_REMOTE}!"
echo "======================================================================"
