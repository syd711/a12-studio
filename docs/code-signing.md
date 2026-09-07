# Code Signing Setup

A12-Studio signs all Windows executables (launcher EXEs + installer) in CI using a self-signed certificate.
This reduces false positives from endpoint security tools (e.g. Sophos Intercept X "Lockdown") and gives
Windows a consistent, identifiable publisher for the application.

> **Note:** A self-signed certificate does not establish SmartScreen trust. For SmartScreen reputation,
> a commercially issued OV or EV certificate from a CA (e.g. SSL.com, DigiCert) is required.

---

## One-time Setup

### 1. Generate the certificate (Windows only)

Run the following script on a Windows machine:

```powershell
.\scripts\generate-codesign-cert.ps1
```

The script will:
- Create a self-signed SHA-256 code signing certificate valid for 10 years
- Export it as a password-protected `.pfx` file
- Print the base64-encoded `.pfx` to the console

### 2. Add GitHub Secrets

Go to **Settings → Secrets and variables → Actions** in the repository and add:

| Secret name             | Value                                          |
|-------------------------|------------------------------------------------|
| `CODESIGN_PFX_BASE64`   | The base64 string printed by the script        |
| `CODESIGN_PFX_PASSWORD` | The password you chose when running the script |

### 3. Delete the local .pfx file

```powershell
Remove-Item .\a12-studio-codesign.pfx
```

The certificate is now stored exclusively in GitHub Secrets. The private key never leaves your machine
unencrypted.

---

## How it works in CI

The Windows packaging job (`workflow.yml`) runs two signing steps:

1. **Sign EXE wrappers** — signs `A12-Studio.exe` and `A12-Studio-Server.exe` *before* Inno Setup
   packages them into the installer.
2. **Sign Installer EXE** — signs the final `A12-Studio-Full-Installer-*.exe` produced by Inno Setup.

Both steps are skipped automatically if the `CODESIGN_PFX_BASE64` secret is not set, so the build
remains functional without signing configured.

Signing uses `signtool.exe` (part of the Windows SDK, pre-installed on `windows-latest` runners)
with a DigiCert RFC 3161 timestamp so signatures remain valid after the certificate expires.

---

## Upgrading to a commercial certificate later

Replace the two GitHub Secrets with the new certificate's base64/password. No workflow changes needed.
