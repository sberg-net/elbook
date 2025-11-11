# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

### Changed

### Fixed

### Security

## [2.16.0]

### Added
- integration der api version 1.12.8
- integration der fhir suche aus der übersicht der suchergebnisse

## [2.15.1]

### Fixed
- tokenvalidierung gefixt

## [2.15.0]

### Added
- integration der api version 1.12.8
- integration des fhir holderauth token handlings

## [2.14.0]

### Added
- tsp soap 1.6 schnittstelle integriert

## [2.13.2]

### Fixed
- Mailjob resilient
- synchronisieren der smcb einträge

## [2.13.1]

### Fixed
- Laden der Logeinträge resilient

## [2.13.0]

### Added
- Plausiregeln bei den Adressdaten berücksichtigt

## [2.12.2]

### Fixed
- beim Import der VZD-Einträge wird gecheckt, ob ein Zertifikat "notAfter" noch gültig ist

## [2.12.1]

### Fixed
- beim Import der VZD-Einträge wird gecheckt, ob ein Zertifikat "notAfter" noch gültig ist

## [2.12.0]

### Added
- lanr und providedBy ergänzt
- apache beanutils version upgrade

## [2.11.0]

### Added
- add API 1.12.7
- vzd entry uploading bugfixes

## [2.10.0]

### Changed
- refactor App Environment Variablen (siehe README.md ##update auf Version 2.10.x)
- Image läuft per Default im NonRoot Modus
- Container: alle persistenten elbook daten (z.B. logs & daten) wurden nach /elbook verschoben

## [2.9.4]

### Fixed
- Batchvorgang -> Synchronisierung mit den Tsp's angepasst, Orgeinheiten bei Apotheken

## [2.9.3]

### Fixed
- Batchvorgang -> Synchronisierung mit den Tsp's angepasst, Orgeinheiten bei Apotheken

## [2.9.2]

### Fixed
- Batchvorgang -> Synchronisierung mit den Tsp's angepasst, Orgeinheiten bei Apotheken

## [2.9.1]

### Fixed
- Batchvorgang -> Synchronisierung mit den Tsp's angepasst, Orgeinheiten bei Apotheken

## [2.9.0]

### Changed
- Batchvorgang -> Synchronisierung mit den Tsp's angepasst, Orgeinheiten bei Apotheken 

## [2.8.1]

### Fixed
- BugFix - Auswerten der Berechtigungsmatrix

## [2.8.0]

### Changed
- Glossar erweitert um Spezialisierungen/Fachrichtungen
- BugFix - Swagger

## [2.7.1]

### Fixed
- Glossar bugfixes

## [2.7.0]

### Changed
- Glossar erweitert um TelematikId-Patterns
- VZD-Sucheinträge im Bereich der TelematikID, Holder und ProfessionOID mit dem Glossar verlinkt

## [2.6.7]

### Security
* update spring-boot-starter-parent auf 3.4.3
* update jasperreports auf 6.21.3
* update flyway-mysql auf 11.3.4
* update gson auf 2.12.1
* update okhttp auf 4.9.2
* update logging-interceptor auf 4.9.2
* update json auf 20250107
* entfernen ungenutztes maven repository

## [2.6.6]

### Fixed
- Suche nach VZD-Einträgen angepasst
- Fixen des Ladens nach Vorgangsnummer von HBA/SMCB-Anträgen

## [2.6.5]

### Fixed
- Erweiterung des Glossars um TelematikID-Bildungsregel -> https://wiki.gematik.de/display/DIRSERV/Telematik-ID+Mapping

## [2.6.4]

### Fixed
- Cleaner Job für die BatchJob's integriert

## [2.6.3]

### Fixed
- Anpassung des Glossars

## [2.6.2]

### Fixed
- Cleaner Job für die BatchJob's integriert

## [2.6.1]

### Fixed
- Holderattributverwaltung

## [2.6.0]

### Changed
- Holderattributverwaltung
- Import von VZD-Objekten angepasst bzgl. Holder-Attribut

## [2.5.2]

### Fixed
- Holderattributverwaltung

## [2.5.1]

### Fixed
- Holderattributverwaltung

## [2.5.0]

### Added
- Umstieg auf Bootstrap 5

## [2.4.0]

### Added
- HoldarAttr - Verwaltung über die Oberfläche aufrufbar

## [2.3.0]

### Added
- Glossar erweitert um ProfessionOID und Holder

### Fixed
- diverse BugFixes rund um Holder-Attr-Change und Stammdatenimport

## [2.2.0]

### Added
- Integration des Glosars: Eingabe einer TelematikID -> umfassende Informationen
  - das Glossar ist auch per Rest-API abfragbar -> alles im öffentlichen Bereich
- API integriert, um Holder-Attribute im Bulk-Modus für viele Objekte zu setzen 

## [2.1.0]

### Added
- Pflege von TSP's / QVDA's
- Abgleich der VZD-Einträge erweitert um Abgleich der HBA- und SMCB-Kartendateninformationen für die Zertifikate

### Changed
- Update des Mysql-JDBC-Treibers 

## [2.0.2]

### Fixed
- Menüpunkt "Logeinträge" für Nutzer der 3.Ebene sichtbar und nutzbar
- Auswertung der Rechtematrix -> Bearbeiten von VZD-Einträgen
- Anzeige des QR-Codes für die 2-Faktor-Authentifizierung
- fehlende Container Parameter der README hinzugefügt

## [2.0.1]

### Fixed
- FIX beim null-check der Suche nach den KIM-Adressen in den Fachdaten

## [2.0.0]

### Changed
- Update aller Komponenten
- Umstellung auf openSource EUPL 1.2
- Änderungen für die Rest-API-Version 1.12.5
