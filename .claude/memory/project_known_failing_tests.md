---
name: known-failing-tests-at-head
description: Two tests that already fail on clean main (2026-09-19, HEAD 5efdcdc4) - don't investigate them as regressions of unrelated work
metadata:
  type: project
---

On a clean checkout of `main` at 5efdcdc4 (verified 2026-09-19 with a throwaway `git worktree` at a short path such as `C:/wt_a12` - the scratchpad path is too long for this repo's fixtures on Windows), `./gradlew test` fails exactly two tests, both unrelated to Form Model work:

- `QueryValidatorsTest.sortFieldReferenceValidatorReportsMissingField` (`a12-studio-models-validation`) - expects 1 error, gets 0.
- `ApplicationGroupFeatureTest.prefixesEveryModelAndRewritesReferences` (`a12-studio-plugins/application-groups`) - assertion at line 111 (`App_QueryModel.json` expected to exist).

**Why:** both touch Query Model handling and were red before the "modeled but no UI" Form Model work, so a red `test` run with only these two is the baseline, not a regression.

**How to apply:** when a full run shows only these two, treat the run as green for your change; if either is later fixed, delete this memory. Any *other* red test is yours to look at. Related tooling: `FxTestSupport` (`a12-studio-ui/src/test/.../editors/formmodel/`) starts a JavaFX toolkit and stubs `Studio`'s static root controller/project so FXML + controllers can be driven in tests; such tests skip themselves without a display.
