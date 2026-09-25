---
name: project-ad-hoc-testing-done
description: Document Model "Ad Hoc Testing" (SME Alt+T) was built 2026-09-25 on top of the installed SME backend + Form Engine preview; what exists and what does not
metadata:
  type: project
---

Deferred on 2026-09-20 ("skip the ad hoc testing but remember that this is still an open issue that will be done"), **built 2026-09-25** when the user asked for it together with the real Form Model preview ("add the ad-hoc testing action that can be found in the SME"). The blockers listed back then (kernel `createReducedDocumentModel`, compiled `validationCode`, a Form Engine renderer) turned out to be avoidable: the installed SME's backend does the first two over REST and its client bundle is the renderer, see [[project-form-preview-real-rendering]] and [[installed-sme-backend-and-client]].

What exists: toolbar button, element/root context menu entries and Alt+T in the Document Model tree (`DocumentModelActions#startAdHocTest`, `adHocTestElementIds`), `PreviewLauncher.openAdHocTest`, `AdHocTestPreviewSession` (selection + descendants, Include selects its expanded subtree, empty selection = whole model; Form Model via `FormScreenGenerator`), live-updates on edits. Bundle keys `document_model_tree.ad_hoc_testing[.tooltip]`. Tests: `FormEnginePreviewTest` (skips without an A12 installation), `DocumentModelTreeFxTest`.

Not done: Additive Document Models (button disabled), an element inside an Include/Attachment/Multi-Select stands for its whole group. **How to apply:** do not list ad hoc testing as an open SME gap any more; contrast with [[project-out-of-scope-features]] (the "won't do" ones).
