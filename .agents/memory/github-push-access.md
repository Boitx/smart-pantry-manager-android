---
name: GitHub push access
description: GitHub source-control authorization may not be usable in this project even when binding reports success.
---

In this project, GitHub showed an account-level connection, but `addIntegration` reported it was already added while `listConnections` returned no usable connection and Git push was rejected. The approved source push did not reach GitHub.

**Why:** The integration status and git CLI authentication did not agree after the conversation moved into the project.

**How to apply:** Before a public push, confirm credentials are usable from the current project. If authentication fails, do not repeat the push blindly or assume the remote changed; have the user reconnect GitHub in this project and then retry.
