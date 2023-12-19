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
package net.sberg.elbook.kartendatentransfer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import net.sberg.elbook.jdbc.DaoDescriptorClass;
import net.sberg.elbook.jdbc.DaoDescriptorElement;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Data
@DaoDescriptorClass
@Component
public class KartendatenTransfer {

    @DaoDescriptorElement(notNull = true)
    private int id;

    @DaoDescriptorElement(notNull = true)
    private int count;

    @DaoDescriptorElement(notNull = true)
    private String aktivierungsCode;

    @DaoDescriptorElement(notNull = true)
    private int hashCode;

    @DaoDescriptorElement(notNull = true)
    private int mandantId;

    @DaoDescriptorElement(notNull = true)
    private String absender;

    @DaoDescriptorElement(notNull = true)
    private String empfaenger;

    @DaoDescriptorElement(notNull = true)
    private EnumTelematikAusweisHerausgeber empfaengerKartenherausgeber = EnumTelematikAusweisHerausgeber.AK3_11;

    @DaoDescriptorElement(notNull = true)
    private EnumTelematikAusweisHerausgeber absenderKartenherausgeber = EnumTelematikAusweisHerausgeber.AK3_11;

    @DaoDescriptorElement(notNull = true)
    private EnumAntragTyp kartenTyp;

    @DaoDescriptorElement(notNull = true)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime erstelltAm;

    @DaoDescriptorElement
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime gelesenAm;

    @DaoDescriptorElement
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime versendetAm;

    @DaoDescriptorElement(notNull = true)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime gueltigBis;
}
