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
package net.sberg.elbook.tspcmpts;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sberg.elbook.common.ICommonConstants;
import net.sberg.elbook.common.StringUtils;
import net.sberg.elbook.jdbc.DaoDescriptorClass;
import net.sberg.elbook.jdbc.DaoDescriptorElement;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

@Data
@DaoDescriptorClass
@Component
@Slf4j
public class Tsp {

    @DaoDescriptorElement(notNull = true)
    private int id;

    @DaoDescriptorElement(notNull = true)
    private int mandantId;

    @DaoDescriptorElement(notNull = true)
    private EnumTspName tspName = EnumTspName.BUNDESDRUCKEREI;

    @DaoDescriptorElement(notNull = true)
    private String hbaUri;

    @DaoDescriptorElement(notNull = true)
    private String smcbUri;

    @DaoDescriptorElement(notNull = true)
    private String keystorePass;

    @DaoDescriptorElement(notNull = true)
    private EnumKeystoreType keystoreType = EnumKeystoreType.PKCS12;

    @DaoDescriptorElement(notNull = true)
    private String keystoreFile;

    @DaoDescriptorElement(notNull = true)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime angelegtAm;

    @DaoDescriptorElement(notNull = true)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime geaendertAm;

    public void decrypt(String[] keys) throws Exception {
        String content = new String(Base64.getDecoder().decode(getKeystorePass().getBytes()));
        setKeystorePass(StringUtils.xor(content, keys));
    }

    public void encrypt(String[] keys) throws Exception {
        setKeystorePass(new String(Base64.getEncoder().encode(StringUtils.xor(getKeystorePass(), keys).getBytes())));
    }

    public TspProperties create(EnumAntragTyp antragTyp) {
        TspProperties tspProperties = new TspProperties();
        tspProperties.setQvdas(new ArrayList<>());

        QvdaProperties qvdaProperties = new QvdaProperties();
        qvdaProperties.setName(tspName.getQvda().name());
        qvdaProperties.setConnection(new HashMap<>());

        ConnectionProperties connectionProperties = new ConnectionProperties();
        connectionProperties.setDefaultUri(antragTyp.equals(EnumAntragTyp.HBA)?hbaUri:smcbUri);
        connectionProperties.setKeystorePass(keystorePass);
        connectionProperties.setKeystoreType(keystoreType.name());
        connectionProperties.setKeystoreFileInClasspath(false);
        connectionProperties.setKeystoreFile(createKeystoreDirectory() + keystoreFile);

        qvdaProperties.getConnection().put(antragTyp, connectionProperties);

        tspProperties.getQvdas().add(qvdaProperties);

        return tspProperties;
    }

    public String createKeystoreDirectory() {
        return ICommonConstants.BASE_DIR + "tsps" + File.separator + getMandantId() + File.separator + getTspName() + File.separator;
    }
}
