---
name: project-form-preview-real-rendering
description: The Form Model / ad hoc preview renders with the real Form Engine (built 2026-09-25) by serving the INSTALLED SME client bundle + backend; how it works, what was learned, what is still missing
metadata:
  node_type: memory
  type: project
  modified: 2026-09-25
---

**Status: built 2026-09-25** (the earlier plan here, "resolve fmm-support licensing first", is obsolete - and its claim that `fmm-support` is private with no local source was wrong: it is the workspace package `moduleSupport/fmm` of the SME repo at `C:\workspace\sme`, wrapping the public community-npm `@com.mgmtp.a12.formengine/formengine-core`).

**How it works** (code in `a12-studio-ui/.../preview/` and `.../previewapp/`; full write-up in `docs/sme-reference-comparison.md`, "Form Engine preview"):
- SME's preview window = the SME client bundle booted with `window.name === "preview-window"`; it asks `window.opener` for data via `postMessage` (`request-initial-data` -> `set-initial-data {formModel, documentModel, validationCode, ...}`, edits as `update-formModel`/`update-models`).
- a12-studio's `PreviewServer` serves the installed bundle (`<A12 installation>/bin/simple-model-editor/<v>/static`, `SmeInstallation`) under `/sme/` with `form-engine-bootstrap.js` injected (sets the window name, fakes `window.opener`, polls `/fe/{session}/data`). Nothing of mgm's JS is copied into the repo.
- The models come from `FormModelPreviewSession` / `AdHocTestPreviewSession`; expansion, combination expansion, validation-code generation and ad hoc reduction are done by the installed backend `sme.jar` (`SmeBackend`, started on demand with the bundled JRE, ~2 s). No kernel dependency in a12-studio, so TODO "Open decision 1" no longer blocks this.
- The wireframe (`preview.html`, `ApplicationModelPreviewService`, `FormModelPreviewService`) remains as fallback and for the Application Model preview.

**Why it matters / traps:** the Form Engine needs `subHeaderBox` and `footerBox` in the content (studio omits them when empty -> "Json is no valid FormModel!"; the preview adds them to its copy). The preview only shows a generic error text; to see the real one, stub `window.__REDUX_DEVTOOLS_EXTENSION__` in a throwaway page to capture the store and read `activities[..].dataHolders[0].error`. See [[installed-sme-backend-and-client]] for the backend contract drift.

**Still missing** (TODO.md, "Form Engine preview"): Theme and Data menus (no `.theme`/sample documents offered), ad hoc for Additive Document Models, alignment of `FormScreenGenerator` with SME's `form-model-generator`. Related: [[project-ad-hoc-testing-done]].
