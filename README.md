# elBook

## elbook springboot Docker Container

### Environment Variables

Folgenden Environment Variablen können am Container gesetzt werden. Werden sie nicht gesetzt ziehen die default Values.

| Name                                 | Beschreibung                                                                                                                           | Values                            | default       |
|--------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------|---------------|
| ELBOOK-DB-HOST                       | DB Hostname für elbook                                                                                                                 | *                                 | localhost     |
| ELBOOK-DB-PORT                       | DB Port für elbook                                                                                                                     | *                                 | 3306          |
| ELBOOK-DB-NAME                       | DB Name für elbook                                                                                                                     | *                                 | elbook        |
| ELBOOK-DB-USER                       | DB Username für den Zugriff auf die elbook DB                                                                                          | *                                 | elbook        |
| ELBOOK-DB-PASSWORD                   | DB Password für den Zugriff auf die elbook DB                                                                                          | *                                 | elbook        |
| ELBOOK-DB-ROOTPASSWORD               | DB Password für den Zugriff auf die DB als root (z.B. für init via flyway)                                                             | *                                 | mrdata123!    |
| ELBOOK-APP-KARTENDATENTRANS-BASE-URL | Base URL für den Kartendatentransfer                                                                                                   | *                                 | localhost     |
| **MAIL-HOST**                        | App Mail Settings: Hostname oder IP                                                                                                    | *                                 | notSet        |
| MAIL-PORT                            | App Mail Settings: SMTP Port                                                                                                           | int                               | 465           |
| **MAIL-USERNAME**                    | App Mail Settings: Username für Mail Account                                                                                           | *                                 | notSet        |
| **MAIL-PASSWORD**                    | App Mail Settings: Passwort für Mail Account                                                                                           | *                                 | notSet        |
| MAIL-SSL-ENABLED                     | App Mail Settings: SSL nutzen                                                                                                          | true, false                       | true          |
| APP-PROFILES                         | Profile der Spring App. Hängt von der Konfiguration der application.yaml ab.<br/>Es können mehrere getrennt durch Komma gesetzt werden. | dev, default                      | default       |
| TZ                                   | Timezone des Containers                                                                                                                | tzdata zones (z.B. Europe/Berlin) | Europe/Berlin |
| LOG-MAIL-ENABLE-ERROR-APPENDER       | Logging Mail Appender: An / Aus (restlichen Logging Mail Settings nur relevant wenn "aktive")                                            | true, false                       | false         |
| LOG-MAIL-SUBJECT-ADDON               | Logging Mail Appender: Subject Zusatz für RunTime Umgebung                                                                             | *                                 | notSet        |
| LOG-MAIL-SMTP-HOST                   | Logging Mail Appender: SMTP Hostname                                                                                                   | *                                 | notSet        |
| LOG-MAIL-SMTP-PORT                   | Logging Mail Appender: SMTP Port                                                                                                       | int                               | 25            |
| LOG-MAIL-STARTTLS                    | Logging Mail Appender: StartTLS                                                                                                        | true, false                       | true          |
| LOG-MAIL-USERNAME                    | Logging Mail Appender: SMTP Login Username                                                                                             | *                                 | notSet        |
| LOG-MAIL-PASSWORD                    | Logging Mail Appender: SMTP Login Passwort                                                                                             | *                                 | notSet        |
| LOG-MAIL-SEND-FROM                   | Logging Mail Appender: Mail Absender (FROM Address)                                                                                    | *                                 | notSet        |
| LOG-MAIL-SEND-TO                     | Logging Mail Appender: Mail Empfänger (TO Address)                                                                                     | *                                 | notSet        |