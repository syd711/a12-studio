# generate-codesign-cert.ps1
# Generates a self-signed code signing certificate and exports it as a .pfx file.
# Run this ONCE locally on Windows, then add the base64-encoded result as a GitHub Secret.
#
# Usage:
#   .\scripts\generate-codesign-cert.ps1
#
# After running:
#   1. Copy the output of "Base64 for GitHub Secret" into a GitHub Secret named CODESIGN_PFX_BASE64
#   2. Set a strong password and add it as GitHub Secret CODESIGN_PFX_PASSWORD
#   3. You can delete the local .pfx file afterwards — the cert is valid for 10 years

param(
    [string]$CertSubject   = "CN=A12-Studio, O=a12-studio.dev, C=DE",
    [string]$PfxOutputPath = ".\a12-studio-codesign.pfx",
    [int]$ValidityYears    = 10
)

$ErrorActionPreference = "Stop"

Write-Host "=== A12-Studio Code Signing Certificate Generator ===" -ForegroundColor Cyan
Write-Host ""

# Prompt for password
$Password = Read-Host -AsSecureString "Enter a strong password for the .pfx file"
$PasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($Password)
)

Write-Host ""
Write-Host "Generating self-signed certificate..." -ForegroundColor Yellow

# Create the certificate in the current user's certificate store
$cert = New-SelfSignedCertificate `
    -Subject $CertSubject `
    -Type CodeSigningCert `
    -KeyUsage DigitalSignature `
    -FriendlyName "A12-Studio Code Signing" `
    -NotAfter (Get-Date).AddYears($ValidityYears) `
    -CertStoreLocation "Cert:\CurrentUser\My" `
    -HashAlgorithm SHA256

Write-Host "Certificate created: $($cert.Thumbprint)" -ForegroundColor Green

# Export to .pfx
Export-PfxCertificate -Cert $cert -FilePath $PfxOutputPath -Password $Password | Out-Null
Write-Host "Exported to: $PfxOutputPath" -ForegroundColor Green

# Convert to base64 for GitHub Secret
$pfxBytes   = [System.IO.File]::ReadAllBytes((Resolve-Path $PfxOutputPath))
$pfxBase64  = [System.Convert]::ToBase64String($pfxBytes)

Write-Host ""
Write-Host "=== Base64 for GitHub Secret (CODESIGN_PFX_BASE64) ===" -ForegroundColor Cyan
Write-Host $pfxBase64
Write-Host ""
Write-Host "=== Next Steps ===" -ForegroundColor Cyan
Write-Host "1. Go to: https://github.com/syd711/a12-studio/settings/secrets/actions"
Write-Host "2. Add secret:  CODESIGN_PFX_BASE64  (paste the base64 string above)"
Write-Host "3. Add secret:  CODESIGN_PFX_PASSWORD  (the password you just entered)"
Write-Host "4. Delete the local .pfx file: Remove-Item '$PfxOutputPath'"
Write-Host ""
Write-Host "Certificate valid until: $((Get-Date).AddYears($ValidityYears).ToString('yyyy-MM-dd'))" -ForegroundColor Green
