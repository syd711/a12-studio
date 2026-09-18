---
name: feedback-web-build-compile-errors
description: "Recurring Java compile-error patterns on a12-studio (jspecify annotation placement on qualified/nested types, wrong local variable types) and how to avoid them - first seen in web-interface-generated code, later recurred in local Claude Code sessions too"
metadata:
  node_type: memory
  type: feedback
  originSessionId: 3bf942ba-4bc1-4610-8250-61f2db333346
  modified: 2026-09-06T13:44:53.397Z
---

Code generated through the Claude web interface for a12-studio has twice introduced the same class of compile error. Avoid these patterns when generating/editing Java here:

1. **TYPE_USE annotations (`@NonNull`/`@Nullable` from `org.jspecify.annotations`) on a qualified/nested type must go before the simple name, not before the outer class name.**
   Wrong: `@NonNull Outer.Inner field`
   Right: `Outer.@NonNull Inner field`
   This applies to record components, method return types, and parameters whenever the type is written as `Outer.Inner` (e.g. `AccessImportService.ColumnFieldType`). Getting this wrong on even one file causes a javac error that appears to abort annotation processing for the whole module — Lombok then fails to inject `log` fields (`@Slf4j`) project-wide, producing dozens of unrelated "symbol not found: log" errors that look like a much bigger problem than the real one-file annotation-placement bug. When diagnosing a wave of `log` "symbol not found" errors, look first for a jspecify annotation-placement error elsewhere in the same compile — fixing that one file resolves the rest.

2. **Don't declare a local variable with a narrower/wrong static type than the value being assigned.** Seen: `FontIcon accessIcon = withMenuIconStyle(WidgetFactory.createIcon(...))` where `withMenuIconStyle` returns `Node`, not `FontIcon` — a type mismatch. `WidgetFactory.createIcon(...)` returns `FontIcon`, but once passed through a `Node`-returning wrapper, the local variable must be typed `Node` (matching how every other call site in the same file uses the result directly as a `Node`, without an intermediate typed variable).

**Why:** Both errors originated from a build produced via the web interface (as opposed to local Claude Code sessions), suggesting that build path is more prone to introducing type-annotation-placement and return-type mismatches without local compiler feedback. **Update 2026-09-06:** the qualified-type annotation mistake (#1) also happened twice in a local Claude Code session (not just web-generated code) while writing new form-model editor panels — once on `java.util.function.BiPredicate<...>` written inline in a method signature, once on `HideConditionPanelController.MasterFieldScope` as a parameter type. Both times the fastest fix was importing the nested/generic type directly (`import ...HideConditionPanelController.MasterFieldScope;` then use plain `MasterFieldScope`) rather than moving the annotation per the `Outer.@NonNull Inner` rule — simpler and avoids re-triggering the mistake on the next edit. So: treat this as a general Java-in-this-repo risk, not a web-interface-specific one.

**How to apply:** Before writing `@NonNull`/`@Nullable` on any parameter/field/return type, check whether the type reference contains a `.` (a nested class like `Outer.Inner`) — if so, either import the inner type directly and annotate the plain name, or place the annotation as `Outer.@NonNull Inner`, never `@NonNull Outer.Inner`. After any Java edit in this repo (web-generated or not), run `./gradlew :<module>:compileJava` before moving on. If you see many unrelated "symbol not found: log" errors, search for jspecify `@NonNull`/`@Nullable` placement errors on qualified types first — that's very likely the true root cause. See [[a12-studio-conventions]] for the project's Lombok/jspecify usage conventions (compileOnly + annotationProcessor lombok 1.18.46, jspecify 1.0.0).
