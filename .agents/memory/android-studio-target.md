---
name: Native Android assignment target
description: Preserve the required Java Android platform instead of choosing a preview-friendly substitute.
---

Build this assignment as a native Java Android Studio project. Do not convert it to Expo or a web app to fit Replit's available preview artifact types.

**Why:** The assignment explicitly requires Java and Android Studio; a platform substitution would fail a core technical requirement.

**How to apply:** Keep the source in the Android Studio project, use local SQLite for persistence, and verify Android UI/build behavior in Android Studio with the SDK. Replit can run the standalone Java matching tests but currently has no Android SDK.
