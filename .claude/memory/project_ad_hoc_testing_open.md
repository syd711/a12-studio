---
name: project-ad-hoc-testing-open
description: Document Model "ad hoc testing" (SME Alt+T reduced test model + preview) was deferred on 2026-09-20 but WILL be built later - do not treat it as won't-do
metadata:
  type: project
---

SME's Document Model **ad hoc testing** (select elements, backend builds a reduced Document+Validation model, popup preview renders a generated Form Model) is **deferred, not rejected** (user, 2026-09-20: "skip the ad hoc testing but remember that this is still an open issue that will be done"). It is the one item of TODO.md #13 that was not built.

**Why deferred:** it needs three things a12-studio does not have: (1) kernel `DocumentModelService.createReducedDocumentModel` (`internal` API; depends on the open mgm decision from the kernel spike, TODO #3), (2) compiled `validationCode` (kernel `generateValidationCode`), (3) a real Form Engine renderer plus a DM-to-Form-Model generator (SME's `fmm-support` is private; see [[project-form-preview-real-rendering]]). Details: "Ad hoc testing / live preview" row in `docs/sme-reference-comparison.md`.

**How to apply:** when someone lists open Document Model gaps or asks what is left in #13, include ad hoc testing as open/planned. Contrast with [[project-out-of-scope-features]] (AI generation, model diff): those are the "won't do" ones. Revisit once the kernel decision and the preview-engine licensing are answered.
