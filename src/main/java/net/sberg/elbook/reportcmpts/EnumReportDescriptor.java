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
package net.sberg.elbook.reportcmpts;

public enum EnumReportDescriptor {
    eArztausweis("Absolute Angaben über Anzahl der VZD-Einträge bzgl. Arztausweise separiert nach Kartenherausgebern"),
    eApothekenausweis("Absolute Angaben über Anzahl der VZD-Einträge bzgl. Apothekenausweise separiert nach Kartenherausgebern"),
    eApothekerausweis("Absolute Angaben über Anzahl der VZD-Einträge bzgl. Apothekerausweise separiert nach Kartenherausgebern"),
    ePraxisausweis("Absolute Angaben über Anzahl der VZD-Einträge bzgl. Praxisausweise separiert nach Kartenherausgebern");

    private String hrText;
    private EnumReportDescriptor(String hrText) {
        this.hrText = hrText;
    }
    public String getHrText() {
        return hrText;
    }
}
