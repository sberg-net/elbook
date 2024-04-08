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
package net.sberg.elbook.batchjobcmpts;

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
@DaoDescriptorClass(transientBean = true)
@Component
public class BatchJobReduziert {

    @DaoDescriptorElement
    private int id;

    @DaoDescriptorElement
    private int mandantId;

    @DaoDescriptorElement
    private String mandantName;

    @DaoDescriptorElement
    private EnumBatchJobName batchJobName = EnumBatchJobName.STAMMDATEN_CERT_IMPORT;

    @DaoDescriptorElement
    private EnumBatchJobStatusCode statusCode;

    @DaoDescriptorElement
    private int anzahlDatensaetze;

    @DaoDescriptorElement
    private int anzahlDatensaetzeAbgearbeitet;

    @DaoDescriptorElement
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime gestartetAm;

    @DaoDescriptorElement
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime beendetAm;
}
