# elBook

## elbook springboot Docker Container

Docker Container für alle elBook Releases für AMD64 & ARM64 werden auf Docker Hub zur Verfügung gestellt.
https://hub.docker.com/r/sbergit/elbook

### Environment Variables

Folgenden Environment Variablen können am Container gesetzt werden. Werden sie nicht gesetzt ziehen die default Values.

| Name                                 | Beschreibung                                                                                                                            | Values                            | default              |
|--------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------|----------------------|
| DB_HOST                              | DB Hostname für elbook                                                                                                                  | *                                 | localhost            |
| DB_PORT                              | DB Port für elbook                                                                                                                      | *                                 | 3306                 |
| DB_NAME                              | DB Name für elbook                                                                                                                      | *                                 | elbook               |
| DB_USER                              | DB Username für den Zugriff auf die elbook DB                                                                                           | *                                 | elbook               |
| DB_PASSWORD                          | DB Password für den Zugriff auf die elbook DB                                                                                           | *                                 | elbook               |
| FLYWAY_USER                          | User für DB Init/Update (wenn DB root dann kann auch die DB inkl. Rechten angelegt)                                                     | *                                 | =DB_USER             |
| FLYWAY_PASSWORD                      | Passwort für Init/Update DB User                                                                                                        | *                                 | =DB_PASSWORD         |
| FLYWAY_CHECKSUM_REPAIR               | fixen der Checksummen für die DB Update SQL Sripte                                                                                      | true, flase                       | false                |
| ELBOOK_APP_KARTENDATENTRANS_BASE_URL | Base URL für den Kartendatentransfer                                                                                                    | *                                 | localhost            |
| **MAIL_HOST**                        | App Mail Settings: Hostname oder IP                                                                                                     | *                                 | notSet               |
| MAIL_PORT                            | App Mail Settings: SMTP Port                                                                                                            | int                               | 465                  |
| **MAIL_USERNAME**                    | App Mail Settings: Username für Mail Account                                                                                            | *                                 | notSet               |
| **MAIL_PASSWORD**                    | App Mail Settings: Passwort für Mail Account                                                                                            | *                                 | notSet               |
| MAIL_SSL_ENABLED                     | App Mail Settings: SSL nutzen                                                                                                           | true, false                       | true                 |
| DEFAULT_ADMIN_USER                   | default Admin_Nutzername                                                                                                                | *                                 | admin                |
| DEFAULT_ADMIN_PWD                    | default Admin_Passwort                                                                                                                  | *                                 | admin                |
| VZD_CRED_ENCRYPTION_KEYS             | Verschlüsselungskeys (kommagetrennte Zeichenketten)                                                                                     | *                                 | bravo,delta,tango,27 |
| APP_PROFILES                         | Profile der Spring App. Hängt von der Konfiguration der application.yaml ab.<br/>Es können mehrere getrennt durch Komma gesetzt werden. | dev, default                      | default              |
| TZ                                   | Timezone des Containers                                                                                                                 | tzdata zones (z.B. Europe/Berlin) | Europe/Berlin        |
| LOG_MAIL_ENABLE_ERROR_APPENDER       | Logging Mail Appender: An / Aus (restlichen Logging Mail Settings nur relevant wenn "aktive")                                           | true, false                       | false                |
| LOG_MAIL_SUBJECT_ADDON               | Logging Mail Appender: Subject Zusatz für RunTime Umgebung                                                                              | *                                 | notSet               |
| LOG_MAIL_SMTP_HOST                   | Logging Mail Appender: SMTP Hostname                                                                                                    | *                                 | notSet               |
| LOG_MAIL_SMTP_PORT                   | Logging Mail Appender: SMTP Port                                                                                                        | int                               | 25                   |
| LOG_MAIL_STARTTLS                    | Logging Mail Appender: StartTLS                                                                                                         | true, false                       | true                 |
| LOG_MAIL_USERNAME                    | Logging Mail Appender: SMTP Login Username                                                                                              | *                                 | notSet               |
| LOG_MAIL_PASSWORD                    | Logging Mail Appender: SMTP Login Passwort                                                                                              | *                                 | notSet               |
| LOG_MAIL_SEND_FROM                   | Logging Mail Appender: Mail Absender (FROM Address)                                                                                     | *                                 | notSet               |
| LOG_MAIL_SEND_TO                     | Logging Mail Appender: Mail Empfänger (TO Address)                                                                                      | *                                 | notSet               |
