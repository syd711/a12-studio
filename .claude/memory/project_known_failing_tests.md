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

**Locked `build/` on Windows (seen 2026-09-19):** `:a12-studio-ui:processResources` can fail with `Failed to clean up stale outputs` / `Couldn't delete ...\a12-studio-ui\build\resources\main\...\Model-Overview.png ... used by another process` - something outside Gradle (the IDE, not a Java app of ours) holds an icon open, so no UI test can run in the real tree. `compileJava compileTestJava -x processResources -x processTestResources` still checks compilation. To actually run tests, mirror the tree to a short path and run there: `robocopy C:\workspace\a12-studio C:\wt5 /MIR /XD build .git .gradle .idea out Output node_modules /XF *.log` then `./gradlew test --continue --offline` in `C:\wt5` (robocopy exit code 1 = success). With a display the FX tests really run (75 UI tests, 0 skipped on 2026-09-19); baseline for the whole suite was 1183 tests, 6 skipped, exactly the two failures above (2026-09-20, after the Query aggregation work: 1345 tests, 9 skipped, still exactly those two).

**Debounced saves vs. `@TempDir`:** a JavaFX test that types into a field whose panel saves through a `Debouncer` (150 ms) can leave a pending save that fires after the test and races JUnit's temp-dir deletion, surfacing as `JUnitException: Failed to close extension context` on whichever test ran last - and only in a full run, not in isolation. Fix: an `@AfterEach` that calls the controller's `destroy()` (its `debouncer.shutdown()` cancels pending saves) and then flushes the FX queue (`FxTestSupport.onFx(() -> { })`); see `QueryAggregationPanelTest`.
