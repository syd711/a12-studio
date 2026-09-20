---
name: properties-bundles-mixed-encoding
description: messages*.properties / validation-messages*.properties mix raw UTF-8 and \uXXXX escapes - only ever append lines, never re-encode or rewrite the file
metadata:
  type: feedback
---

The four bundles (`a12-studio-ui/.../messages.properties` + `_de`, `a12-studio-models-validation/.../validation-messages.properties` + `_de`) contain both raw UTF-8 (comments with box-drawing characters, many German umlauts) and `ä`-style escapes, in the same file, and several of them carry uncommitted work of the user. On 2026-09-20 a "normalize everything to escapes" pass over them turned a 58-line addition into a 1,000-line diff; it was recovered only because every pre-existing line could be restored byte-for-byte from `git show HEAD:<file>` (matching by key / unescaped text).

**Why:** the mixed state is the repo's convention; a whole-file rewrite is unreviewable and can clobber the user's uncommitted edits (so `git checkout` is not an undo either). Also `grep -P` is unreliable in this Git Bash locale ("supports only unibyte and UTF-8 locales"), so it cannot be used to decide whether a file has non-ASCII text - use Python and decode explicitly.

**How to apply:** add new keys by appending (Edit tool, or a Python append that opens with `newline=''` and does not touch existing text); write German umlauts as `\uXXXX` in new lines (ASCII-only additions are safe in both encodings). After editing, `git diff --stat` on the file must show insertions only. Related tooling trap: backslashes inside Bash heredocs passed through this tool get collapsed (`\\u00e4` arrives as `ä` and Python then turns it into the real character) - put scripts that need literal backslashes in a file via the Write tool and use `chr(92)`.
