---
name: bash-tool-heredoc-backslashes
description: In this environment's Bash tool a heredoc halves backslashes and expands \uXXXX escapes - never write regex/escape-heavy source through cat <<EOF, use the Write tool
metadata:
  type: reference
---

Seen 2026-09-25: `cat > File.java <<'EOF'` with `"\\d+"` produced `"\d+"` in the file (javac: "Unzulaessiges Escapezeichen"), `'\\'` became `'\'`, and a `§` in the source landed as a literal `§`. The quoted delimiter does not help, the tool rewrites the text before bash sees it. `python - <<'EOF'` scripts that only need plain text (no backslashes) worked fine.

**Why:** cost one failed compile of the typesetting regex helpers; the failure is silent until javac runs.

**How to apply:** for any file containing backslashes, regexes, `\n` in strings or `\uXXXX`, create it with the **Write** tool (literal), and edit with **Edit**. To append to the `messages*.properties` bundles (append-only, see [[properties-bundles-mixed-encoding]]): Write a snippet file into the scratchpad, then `cat snippet >> bundle` (byte-exact; raw UTF-8 umlauts are fine, the files mix raw UTF-8 and `\u` escapes). Check the target's line endings first (`grep -c $'\r' file`): `validation-messages*.properties` are CRLF, `messages*.properties` are LF; a Write-created snippet is LF, so convert the appended lines for the CRLF ones. Also: never start two `./gradlew` runs in one tree (a background run plus a second one fails with `NoSuchFileException ... in-progress-results-generic.bin`).
