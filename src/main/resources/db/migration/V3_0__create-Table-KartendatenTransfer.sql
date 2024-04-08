/*
 *  Copyright (C) 2023 sberg it-systeme GmbH
 *
 *  Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the
 *  European Commission - subsequent versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

CREATE TABLE IF NOT EXISTS `KartendatenTransfer` (
    `id` INT(10) NOT NULL,
    `hashCode` INT(10) NOT NULL,
    `count` INT(10) NOT NULL,
    `mandantId` INT(10) NOT NULL,
    `absender` VARCHAR (100) NOT NULL,
    `empfaenger` VARCHAR (100) NOT NULL,
    `empfaengerKartenherausgeber` VARCHAR(10) NOT NULL,
    `kartenTyp` VARCHAR (30) NOT NULL,
    `aktivierungsCode` VARCHAR (100) NOT NULL,
    `erstelltAm` DATETIME NOT NULL,
    `gueltigBis` DATETIME NOT NULL,
    `gelesenAm` DATETIME NULL,
    `versendetAm` DATETIME NULL,
    PRIMARY KEY (id),
    CONSTRAINT `FK_KartendatenTransfer_Mandant` FOREIGN KEY (`mandantId`) REFERENCES `Mandant`(`id`)
    )
    COLLATE='utf8_general_ci'
    ENGINE=InnoDB
;