# Security Policy

## Supported Versions

Trade360Lab is under active development.  
Security fixes are applied to the latest version of the main branch.

---

## Reporting a Vulnerability

If you discover a security vulnerability, **do not open a public GitHub issue**.

Instead, report it privately via Telegram: **@ba6kir**

Please include as much detail as possible:

- short description of the issue  
- affected component(s)  
- steps to reproduce  
- expected vs actual behavior  
- potential impact  
- logs, screenshots, or proof of concept (if available)

---

## Disclosure Policy

Please report vulnerabilities responsibly and avoid public disclosure until the issue has been reviewed and addressed.

All valid reports will be investigated based on severity and potential impact.

---

## Scope

Security reports are especially relevant for:

- authentication and authorization issues  
- exposure of API keys or secrets  
- insecure communication between services  
- unsafe execution of trading strategies  
- injection vulnerabilities (SQL, command, etc.)  
- database access control issues  
- sensitive data leakage (logs, API responses)  
- vulnerable dependencies with real impact  

---

## Security Practices

Trade360Lab is designed with security and isolation in mind:

- no hardcoded secrets  
- environment-based configuration  
- separation of frontend, orchestration, execution, and database layers  
- controlled handling of exchange credentials  
- basic dependency review and updates  

---

## Notes

Please avoid reporting vulnerabilities through public GitHub issues.

Responsible disclosure helps protect users, infrastructure, and research systems.
