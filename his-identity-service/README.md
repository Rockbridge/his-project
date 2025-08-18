# HIS Identity Service

Der Identity-/Auth-Service stellt Benutzer- und Rollenverwaltung bereit und vergibt kontextbezogene JWTs.

## Hauptaufgaben
- **Authentifizierung** über LDAP und Datenbank-Fallback.
- **Benutzer- & Rollenverwaltung** mit hierarchischem Rollenmodell.
- **Kontextverwaltung** (aktive Rolle, Station/Fachbereich, Organisation, Mandant, Standort).
- **Token-Ausgabe**: Ausstellung von signierten JWTs, die Benutzer- und Kontextinformationen enthalten.
- **Kontextwechsel** durch erneutes Anmelden bzw. expliziten Wechsel-Endpunkt.
- **Audit-Logging** aller Login- und Berechtigungsänderungen.

## Architektur
```
[Client] → [API Gateway] → [Identity Service]
                          ↘ PostgreSQL (Schema: his_auth)
                          ↘ LDAP Server
```

- **Controller-Layer**: REST-APIs für Login, Benutzer- und Rollenverwaltung, Kontextwechsel sowie CSV-Import/-Export.
- **Service-Layer**: Business-Logik für Benutzerpflege, Rollenauflösung und Token-Erstellung.
- **Repository-Layer**: Zugriff auf PostgreSQL-Schema `his_auth` (JPA).
- **Security-Layer**: Spring Security mit LDAP-Bind und OAuth2 Authorization Server zur JWT-Erzeugung.
- **Audit-Layer**: Persistiert Aktionen (Benutzer, Rolle, Kontext, Aktion, Patient?, Zeit, Notfallflag).

## Ports und Konfiguration
- Default-Port: `8083`
- Health-Endpoint: `/actuator/health`
- Docker-Image basiert auf `eclipse-temurin:21`.

## Zukunft
- Erweiterung um zusätzliche Kontextparameter und feinere Policies.
- Integration einer externen Policy-Engine (z.B. OPA) für flexible Zugriffsregeln.
