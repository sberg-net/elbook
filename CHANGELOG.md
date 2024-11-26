# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

### Changed

### Fixed

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
