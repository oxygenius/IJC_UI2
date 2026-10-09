; Inno Setup-script voor de Windows-installer van IJC_UI2.
; Wordt aangeroepen vanuit Maven (mvn -Prelease,installer package); de waarden hieronder
; worden dan meegegeven met /D. Handmatig: ISCC.exe /DAppVersion=x.y.z.w /DAppImage=... IJC_UI2.iss

#ifndef AppVersion
  #error AppVersion ontbreekt (geef mee met /DAppVersion=...)
#endif
#ifndef AppImage
  #error AppImage ontbreekt: map met de jpackage app-image (IJC_UI2.exe + runtime)
#endif
#ifndef OutputDir
  #define OutputDir "..\Release"
#endif
#ifndef ProjectDir
  #define ProjectDir ".."
#endif

#define AppName "IJC_UI2"
#define AppTitle "IJC Indeling Interne Jeugd Competitie"
#define AppPublisher "Lars Dam"
#define AppUrl "https://github.com/oxygenius/IJC_UI2"

[Setup]
; Vaste AppId: nodig om bij een nieuwe versie over de bestaande installatie heen te installeren
AppId={{6F1C2A52-6A0E-4C43-9A43-1B8F4F0E1F01}
AppName={#AppTitle}
AppVersion={#AppVersion}
AppVerName={#AppTitle} {#AppVersion}
AppPublisher={#AppPublisher}
AppPublisherURL={#AppUrl}
AppSupportURL={#AppUrl}/issues
AppUpdatesURL={#AppUrl}/releases
VersionInfoVersion={#AppVersion}
VersionInfoProductName={#AppName}
VersionInfoProductVersion={#AppVersion}
VersionInfoDescription={#AppTitle} - installatie
; Installeren zonder beheerdersrechten, in %LOCALAPPDATA%\Programs\IJC_UI2
PrivilegesRequired=lowest
PrivilegesRequiredOverridesAllowed=dialog
DefaultDirName={autopf}\{#AppName}
DefaultGroupName={#AppTitle}
DisableProgramGroupPage=yes
LicenseFile={#ProjectDir}\LICENSE
OutputDir={#OutputDir}
OutputBaseFilename=IJC_UI2-setup-{#AppVersion}
Compression=lzma2/max
SolidCompression=yes
WizardStyle=modern
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
UninstallDisplayName={#AppTitle}
UninstallDisplayIcon={app}\{#AppName}.exe

[Languages]
Name: "dutch"; MessagesFile: "compiler:Languages\Dutch.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"

[Dirs]
; Werkmap voor configuratie.json, status.json, keystore.ks, db\ en de rondemappen.
; Staat buiten de programmamap, zodat gegevens bij een update of verwijdering bewaard blijven.
Name: "{userdocs}\{#AppName}"; Flags: uninsneveruninstall

[Files]
; overwritereadonly: installaties van 2.0.1.8 bevatten nog alleen-lezen bestanden
Source: "{#AppImage}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs overwritereadonly
; Sjablonen alleen neerzetten als ze er nog niet zijn: een aangepast sjabloon wordt niet overschreven
Source: "{#ProjectDir}\Leeg.docx"; DestDir: "{userdocs}\{#AppName}"; Flags: onlyifdoesntexist uninsneveruninstall skipifsourcedoesntexist
Source: "{#ProjectDir}\Template.xlsx"; DestDir: "{userdocs}\{#AppName}"; Flags: onlyifdoesntexist uninsneveruninstall skipifsourcedoesntexist
Source: "{#ProjectDir}\logging.properties"; DestDir: "{userdocs}\{#AppName}"; Flags: onlyifdoesntexist uninsneveruninstall
Source: "{#ProjectDir}\README.md"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#ProjectDir}\CHANGELOG.md"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#ProjectDir}\LICENSE"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
; WorkingDir is essentieel: het programma leest en schrijft zijn bestanden in de werkmap
Name: "{group}\{#AppTitle}"; Filename: "{app}\{#AppName}.exe"; WorkingDir: "{userdocs}\{#AppName}"
Name: "{group}\Gegevensmap {#AppName}"; Filename: "{userdocs}\{#AppName}"
Name: "{group}\{cm:UninstallProgram,{#AppTitle}}"; Filename: "{uninstallexe}"
Name: "{autodesktop}\{#AppTitle}"; Filename: "{app}\{#AppName}.exe"; WorkingDir: "{userdocs}\{#AppName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#AppName}.exe"; WorkingDir: "{userdocs}\{#AppName}"; Description: "{cm:LaunchProgram,{#AppTitle}}"; Flags: nowait postinstall skipifsilent
