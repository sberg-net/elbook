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
package net.sberg.elbook.mandantcmpts;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import net.sberg.elbook.common.StringUtils;
import net.sberg.elbook.jdbc.DaoDescriptorClass;
import net.sberg.elbook.jdbc.DaoDescriptorElement;
import net.sberg.elbook.vzdclientcmpts.TiVZDProperties;
import net.sberg.elbook.vzdclientcmpts.command.EnumStateOrProvinceName;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;

@Data
@DaoDescriptorClass
@Component
public class Mandant {

    public static final String KUERZEL_3_01 = "3-01";
    public static final String KUERZEL_3_02 = "3-02";
    public static final String KUERZEL_3_03 = "3-03";
    public static final String KUERZEL_3_04 = "3-04";
    public static final String KUERZEL_3_05 = "3-05";
    public static final String KUERZEL_3_06 = "3-06";
    public static final String KUERZEL_3_07 = "3-07";
    public static final String KUERZEL_3_08 = "3-08";
    public static final String KUERZEL_3_09 = "3-09";
    public static final String KUERZEL_3_10 = "3-10";
    public static final String KUERZEL_3_11 = "3-11";
    public static final String KUERZEL_3_12 = "3-12";
    public static final String KUERZEL_3_13 = "3-13";
    public static final String KUERZEL_3_14 = "3-14";
    public static final String KUERZEL_3_15 = "3-15";
    public static final String KUERZEL_3_16 = "3-16";
    public static final String KUERZEL_3_17 = "3-17";

    @DaoDescriptorElement(notNull = true)
    private int id;

    @DaoDescriptorElement
    private int mandantId;

    @DaoDescriptorElement(notNull = true)
    private String name = "";

    @DaoDescriptorElement(notNull = true)
    private String telematikKuerzel = "";

    @DaoDescriptorElement(notNull = true)
    private String mail = "";

    @DaoDescriptorElement(notNull = true)
    private String nutzername = "";

    @DaoDescriptorElement(notNull = true)
    private EnumSektor sektor = EnumSektor.APOTHEKE;

    @DaoDescriptorElement(notNull = true)
    private EnumStateOrProvinceName bundesland = EnumStateOrProvinceName.RLP;

    @DaoDescriptorElement(notNull = true)
    private String passwort = "";

    @DaoDescriptorElement(notNull = true)
    private String vzdResourceUri = "";

    @DaoDescriptorElement(notNull = true)
    private String vzdTokenUri = "";

    @DaoDescriptorElement
    private String vzdAuthId = "";

    @DaoDescriptorElement
    private String vzdAuthSecret = "";

    @DaoDescriptorElement
    private boolean bearbeitenEintraege = true;

    @DaoDescriptorElement
    private boolean bearbeitenNurFiltereintraege = false;

    @DaoDescriptorElement
    private boolean filternEintraege = true;

    @DaoDescriptorElement
    private int threadAnzahl = 3;

    @DaoDescriptorElement
    private boolean goldLizenz = false;

    @DaoDescriptorElement
    private boolean using2FA = false;

    @DaoDescriptorElement
    private String secret2FA = "";

    @DaoDescriptorElement
    private boolean superAdmin = false;

    @DaoDescriptorElement(notNull = true)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDate gueltigBis = LocalDate.now();

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
        if (getVzdAuthSecret() != null && !getVzdAuthSecret().trim().isEmpty()) {
            String content = new String(Base64.getDecoder().decode(getVzdAuthSecret().getBytes()));
            setVzdAuthSecret(StringUtils.xor(content, keys));
        }

        if (getVzdAuthId() != null && !getVzdAuthId().trim().isEmpty()) {
            String content = new String(Base64.getDecoder().decode(getVzdAuthId().getBytes()));
            setVzdAuthId(StringUtils.xor(content, keys));
        }
    }

    public void encrypt(String[] keys) throws Exception {
        if (getVzdAuthSecret() != null && !getVzdAuthSecret().trim().isEmpty()) {
            setVzdAuthSecret(new String(Base64.getEncoder().encode(StringUtils.xor(getVzdAuthSecret(), keys).getBytes())));
        }

        if (getVzdAuthId() != null && !getVzdAuthId().trim().isEmpty()) {
            setVzdAuthId(new String(Base64.getEncoder().encode(StringUtils.xor(getVzdAuthId(), keys).getBytes())));
        }
    }

    public TiVZDProperties createAndGetTiVZDProperties() {
        TiVZDProperties tiVZDProperties = new TiVZDProperties();
        tiVZDProperties.setAuthId(vzdAuthId);
        tiVZDProperties.setAuthSecret(vzdAuthSecret);
        tiVZDProperties.setResourceUri(vzdResourceUri);
        tiVZDProperties.setTokenUri(vzdTokenUri);
        tiVZDProperties.setDebugNetworkTraffic(false);
        return tiVZDProperties;
    }

    public boolean authInfoIsBlank() {
        return vzdAuthId.isEmpty() || vzdAuthSecret.isEmpty();
    }
}
