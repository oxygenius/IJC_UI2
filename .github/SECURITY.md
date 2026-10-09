# Security Policy

## Supported Versions

Only the latest release is supported with security updates.

| Version | Supported |
|---------|-----------|
| Latest  | ✅ Yes    |
| Older   | ❌ No     |

## Reporting a Vulnerability

This application handles personal data of (youth) chess club members. If you discover a security vulnerability, please report it responsibly so it can be addressed before public disclosure.

### How to Report

Please report security issues via [GitHub Security Advisories](https://github.com/oxygenius/IJC_UI2/security/advisories/new).

Alternatively, you may contact the maintainer directly:

- **Email**: Open a private issue via the [Security tab](https://github.com/oxygenius/IJC_UI2/security) on GitHub.

### What to Include

When reporting a vulnerability, please provide:

- A clear description of the issue
- Steps to reproduce the problem
- Potential impact (e.g., data exposure, unauthorized access)
- Any suggested fixes or mitigations, if applicable

### What to Expect

- **Acknowledgment**: You will receive a confirmation within 7 days of reporting.
- **Assessment**: The maintainer will evaluate the report and determine the severity.
- **Resolution**: If the issue is confirmed, a fix will be developed and released as soon as possible.
- **Disclosure**: After a fix is released, the vulnerability may be discussed publicly, with credit to the reporter (unless anonymity is requested).

## Security Considerations

This application stores sensitive data locally:

- **Player data**: Names, KNSB numbers, and game history are stored in `status.json` and `db/spelers.odb`.
- **Encrypted credentials**: Passwords (e.g., for the club website API) are stored encrypted in `keystore.ks`.
- **No telemetry**: The application does not send data over the internet unless explicitly triggered by a user action (e.g., publishing results to the club website).

Users are advised to:

- Keep `configuratie.json`, `status.json`, `keystore.ks`, and `db/` private and never commit them to version control.
- Back up the data directory regularly.
- Download the application only from [official releases](https://github.com/oxygenius/IJC_UI2/releases).
