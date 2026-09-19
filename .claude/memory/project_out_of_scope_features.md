---
name: out-of-scope-features
description: SME features decided (2026-09-19) as permanently out of scope for a12-studio - AI-assisted Document Model generation and the Model diff/compare editor
metadata:
  type: project
---

The user decided on 2026-09-19 that these SME features will **not** be implemented in a12-studio:

- **AI-assisted Document Model generation** (SME `client/src/modules/documentModel/ai/*`, built on `@com.mgmtp.ai.generation`).
- **Model diff/compare editor** (SME `hasModelDiffEditor`, settings/tree/typedef diff).

**Why:** explicit product decision by the user; not a "not yet" backlog item.

**How to apply:** don't propose, scope, or start work on either when doing SME gap analysis, TODO triage, or `docs/sme-reference-comparison.md` refreshes. List them as "Won't do", not "Missing" or "needs a decision". If a broader feature seems to depend on one of them, flag the dependency instead of building the piece.

Related: [[project-form-preview-real-rendering]] (a different, still-parked item).
