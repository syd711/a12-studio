---
name: installed-sme-backend-and-client
description: The A12 installation ships a runnable SME backend (sme.jar) and the compiled SME client (static/); use them as black boxes for kernel-backed features and real rendering, and verify request shapes against the INSTALLED jar, not the SME source checkout
metadata:
  type: reference
---

`<installation>/bin/simple-model-editor/13.0.2/` (installation from `A12Settings`, e.g. `C:\workspace\a12\A12 Tools - 2026.06`) holds `sme.jar` (Spring Boot, 132 MB; `java -jar sme.jar --server.port=N`, ready in ~2 s with `<installation>/bin/java/<v>`), `static/` (the compiled SME client, ~134 MB: `index.html`, webpack chunks, fonts, `models/*.json` meta models) and the Electron shell. Both are used by a12-studio's Form Engine preview (`SmeInstallation`, `SmeBackend`), see [[project-form-preview-real-rendering]].

Endpoints used (all POST JSON, verified against 13.0.2): `/api/document-model/expand {documentModelId, documentModels[]}` -> `{documentModel, error}`; `/api/document-model/generate-validation-code {documentModel}` -> `{validationCode}`; `/api/document-model/generate-ad-hoc-test-input {documentModel (expanded), selectedElements[], partiallySelectedElements[]}` -> `{documentModel, validationCode}`; `/api/combination-model/expand {combinationModel, referencedModels[]}` -> `{documentModel, errors[]}`. Other controllers exist in the SME source (validate, migrate, move-element-with-refactoring, rule contradictions, print, mapping, structural mapping, transformer...) and are candidates for the "call the kernel out of process" strategy without any kernel dependency in a12-studio.

**Trap: the SME source checkout (`C:\workspace\sme`) can be at another revision than the installed jar** - e.g. its `CombModelRequestDTO` is `{modelId, documentModels, selectionModels, combinationModels}`, the installed one `{combinationModel, referencedModels}` (HTTP 400 "Parameter specified as non-null is null" in the backend log). Check the truth with `unzip sme.jar 'BOOT-INF/classes/<package>/*'` + `javap -p -cp BOOT-INF/classes <class>`, or grep the payload in `static/main.*.js`. The source is still the right map for *what exists*.

Client side: in the SME client, `window.name === "preview-window"` boots the Form Engine preview instead of the editor (`client/src/app/index.tsx`); the source of that preview is `moduleSupport/fmm` in the SME repo (public deps: `formengine-core`, `form-model-generator` on the community npm registry, see [[formengine-model-sources]] and [[a12-community-npm-and-maven-sources]]).
