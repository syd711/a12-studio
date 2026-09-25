---
name: content-model-preview
description: Content Model editor's center is a JavaFX WebView running the installed SME client as the Content Engine "preview window" (built 2026-09-25); the WebView/bundle traps and how it is tested
metadata:
  type: project
---

The Content Model editor (`ContentModelEditorController`, layout tree | preview | properties) embeds `PreviewLauncher.registerContentPreview(item)`'s URL in a `WebView`. Same mechanism as the Form Engine preview ([[project-form-preview-real-rendering]]): `PreviewServer` serves the installed SME client under `/sme/?content=<session>` with `content-model-bootstrap.js` injected, `ContentModelPreviewSession` feeds it via `/cm/{session}/data`. Full write-up: "Content Model preview" in `docs/sme-reference-comparison.md`.

**Traps found (all cost time):**
- JavaFX 21 WebView has no IndexedDB and `window.indexedDB` is getter-only: install the stand-in with `Object.defineProperty`, never assignment (strict mode throws, and the bootstrap then dies silently before doing anything).
- The bundle's webpack export names are minified and `__webpack_require__.c` is not exposed; the kernel `DocumentServiceFactory` is found via `webpackChunk*.push([[id],{},req=>...])` + `req.m` factory sources containing `getDocumentModelSerializer(){return new`.
- The client re-renders on every `send-data`; resending on each poll makes the preview churn. Only send on change (or when it asks with `request-data`).
- WebView JS runs on the FX thread: a busy page freezes the studio and blocks `Platform.runLater`. To debug without a console use `engine.setOnAlert` + `alert("STEP ..")` markers in the bootstrap (works even while the page is stuck); `window.__previewErrors` holds script errors. The Chrome extension was not connected in this session.
- Tests: closing the last Stage triggers JavaFX implicit exit and kills the toolkit for later tests - `Platform.setImplicitExit(false)`; load `about:blank` before closing a WebView test stage.

**Not built:** themes and sample documents (`themeNames`/`documentIds` empty), saving edits made in the preview; the editor has no UI to set the `document-model-for-content-model` reference (only fixtures carry it).

Tests: `ContentModelPreviewTest` (session + real WebView rendering, skips without the SME installation/display), `ContentModelEditorPreviewTest` (FXML wiring).
