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
package net.sberg.elbook.common;

import java.time.*;

public class DateTimeUtils {
    public static java.time.OffsetDateTime convertFrom(String dateTime) {
        Instant instant = null;
        try {
            long ms = Long.valueOf(dateTime);
            instant = Instant.ofEpochMilli(ms);
        }
        catch (Exception e) {
            try {
                instant = Instant.parse(dateTime);
            }
            catch (Exception ee) {
                return OffsetDateTime.parse(dateTime);
            }
        }

        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        LocalDateTime now = LocalDateTime.now();
        ZoneOffset zoneOffSet = ZoneId.systemDefault().getRules().getOffset(now);

        return localDateTime.atOffset(zoneOffSet);
    }
}
