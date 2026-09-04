# Pushing the Owner's App Separately to a Different Repository

This repository provides two automated, zero-headache methods to extract the **AcademiaTrack Platform Owner Master App** into its own standalone Android project and push it to a completely different GitHub repository (e.g. `https://github.com/derekcrown001/AcademiaTrack-Owner`).

---

## Method 1: Using GitHub Actions (Automated, Zero-CLI)

You can push the Owner app directly from your browser using the included GitHub Actions workflow:

1. **Create your target repository** on GitHub (e.g. `AcademiaTrack-Owner` or `AcademiaTrack-Owner-Master`):
   - URL: `https://github.com/new`
   - Leave it empty (do not check "Add a README file").

2. **Generate a GitHub Personal Access Token (PAT)**:
   - Go to [GitHub Settings -> Developer Settings -> Personal Access Tokens (classic)](https://github.com/settings/tokens).
   - Click **Generate new token (classic)**.
   - Set Note to `AcademiaTrack Owner Repo Push`.
   - Check the **`repo`** scope (full control of repositories).
   - Click **Generate token** and copy it.

3. **Add Secret to this repository**:
   - Go to your main repository on GitHub -> **Settings** -> **Secrets and variables** -> **Actions**.
   - Click **New repository secret**.
   - Name: `OWNER_REPO_PAT`
   - Value: *(Paste your Personal Access Token)*

4. **Trigger the Workflow**:
   - Go to the **Actions** tab in this repository.
   - Click on **Push Owner App to Separate Repository** in the left sidebar.
   - Click **Run workflow**.
   - Set **Target GitHub Repository** (e.g. `derekcrown001/AcademiaTrack-Owner`).
   - Click the green **Run workflow** button!

The workflow will:
- Assemble the standalone project with its own `settings.gradle.kts` and `build.gradle.kts`.
- Set the application ID to `com.aistudio.academiatrack.owner`.
- Configure `OwnerMainActivity` as the primary launcher activity.
- Set up a standalone GitHub Actions workflow in that new repo to build `AcademiaTrack-Owner-Master.apk`.
- Push everything directly to your separate repository's `main` branch!

---

## Method 2: Using the CLI Script (1 Terminal Command)

If you have cloned this repository locally or are using the terminal:

### Over HTTPS:
```bash
bash scripts/push-owner-repo.sh https://github.com/derekcrown001/AcademiaTrack-Owner.git
```

### Over SSH:
```bash
bash scripts/push-owner-repo.sh git@github.com:derekcrown001/AcademiaTrack-Owner.git
```

### Export to Local Directory Only (to inspect or test):
```bash
bash scripts/push-owner-repo.sh --export-only ./standalone-owner-app
```

---

## What is in the Standalone Owner App?

The pushed repository will be a 100% independent Android Studio project with:
- **`settings.gradle.kts`**: Independent root project named `AcademiaTrack-Owner-Master`.
- **`app/build.gradle.kts`**: Standalone Application ID `com.aistudio.academiatrack.owner`.
- **`AndroidManifest.xml`**: Only executive entry points (`OwnerMainActivity` as launcher, `OwnerRemoteSyncService`, and telemetry).
- **Core Modules**:
  - `com.example.owner.*`: School provisioning, remote killswitch, account locking, global app settings.
  - `com.example.data.model.*`: Licensing configs, subscription tiers, payment audit logs, and metrics.
  - `com.example.owner.service.*`: Cloud Firestore synchronizer & broadcast service.
- **Dedicated CI/CD**: `.github/workflows/build-apk.yml` that compiles and releases `AcademiaTrack-Owner-Master.apk` on every push to that separate repository!
