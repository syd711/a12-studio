; Inno Setup script for the A12 Studio full installer (server + UI).
; Ported/trimmed from a12-studio's inno-installer-studio-full.iss.
; Templated by Gradle's Copy + ReplaceTokens filter (see :a12-studio-ui:prepareInstallerScript) -
; @version@ below is an Ant-style token placeholder, not an Inno Setup preprocessor directive.

#define MyAppName "A12 Studio"
#define MyAppVersion "@version@"
#define MyAppPublisher "Matthias Faust"
#define MyAppURL "https://github.com/syd711/a12-studio"
#define MyAppExeName "A12-Studio.exe"

[Setup]
; NOTE: The value of AppId uniquely identifies this application. Do not use the same AppId value in installers for other applications.
AppId={{B3E2B6C0-6E4E-4B7B-9B7E-2B9D7B6C1A11}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName=C:\a12studio\A12-Studio\
DisableProgramGroupPage=yes
DisableDirPage=no
LicenseFile=..\..\..\LICENSE
OutputDir=../../../Output/A12-Studio
; SetupIconFile intentionally omitted - no .ico artwork exists yet, add one here once available.
OutputBaseFilename=A12-Studio-Full-Installer-{#MyAppVersion}
Compression=lzma
SolidCompression=yes
WizardStyle=modern

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Code]
function IsProcessRunning(const ProcessName: String): Boolean;
var
  WbemLocator: Variant;
  WbemServices: Variant;
  WbemObjectSet: Variant;
begin
  Result := False;
  try
    WbemLocator := CreateOleObject('WbemScripting.SWbemLocator');
    WbemServices := WbemLocator.ConnectServer('', 'root\CIMV2', '', '');
    WbemObjectSet := WbemServices.ExecQuery(
      'SELECT Name FROM Win32_Process WHERE Name LIKE ''' + ProcessName + '''');
    Result := not VarIsNull(WbemObjectSet) and (WbemObjectSet.Count > 0);
  except
  end;
end;

function InitializeSetup(): Boolean;
var
  Msg: String;
begin
  Result := True;
  while IsProcessRunning('A12-Studio.exe') do
  begin
    Msg := 'A12 Studio is still running.' + #13#10 + #13#10 +
           'Please close A12-Studio.exe before continuing.' + #13#10 + #13#10 +
           'Click Retry to check again, or Cancel to abort the installation.';
    case MsgBox(Msg, mbError, MB_RETRYCANCEL) of
      IDCANCEL:
        begin
          Result := False;
          Exit;
        end;
    end;
  end;
end;

[Tasks]
Name: "desktopicon"; Description: "Create Desktop Icon for A12 Studio"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Dirs]
Name: "{app}"; Permissions: users-full

[Files]
Source: "..\..\..\Output\A12-Studio\{#MyAppExeName}"; DestDir: "{app}"; Flags: ignoreversion; Permissions: everyone-full
Source: "..\..\..\Output\A12-Studio\a12-studio-ui.jar"; DestDir: "{app}"; Flags: ignoreversion; Permissions: everyone-full
Source: "..\..\..\Output\A12-Studio\a12-studio-server.jar"; DestDir: "{app}"; Flags: ignoreversion; Permissions: everyone-full
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Icons]
Name: "{autoprograms}\A12-Studio\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

[UninstallDelete]
Type: files; Name: "{app}\*.log"
Type: files; Name: "{app}\*.bat"
Type: filesandordirs; Name: "{app}"
