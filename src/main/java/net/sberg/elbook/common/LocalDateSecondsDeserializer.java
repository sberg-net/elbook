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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;

import java.io.IOException;
import java.sql.Date;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class LocalDateSecondsDeserializer extends LocalDateDeserializer {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public static final LocalDateSecondsDeserializer INSTANCE = new LocalDateSecondsDeserializer();

    protected LocalDateSecondsDeserializer() {
        this(DEFAULT_FORMATTER);
    }

    public LocalDateSecondsDeserializer(DateTimeFormatter dtf) {
        super(dtf);
    }

    /**
     * Since 2.10
     */
    public LocalDateSecondsDeserializer(LocalDateDeserializer base, DateTimeFormatter dtf) {
        super(base, dtf);
    }

    /**
     * Since 2.10
     */
    protected LocalDateSecondsDeserializer(LocalDateDeserializer base, Boolean leniency) {
        super(base, leniency);
    }

    @Override
    protected LocalDateDeserializer withDateFormat(DateTimeFormatter dtf) {
        return new LocalDateDeserializer(this, dtf);
    }

    @Override
    protected LocalDateDeserializer withLeniency(Boolean leniency) {
        return new LocalDateSecondsDeserializer(this, leniency);
    }

    @Override
    public LocalDate deserialize(JsonParser parser, DeserializationContext context) throws IOException
    {
        if (parser.hasToken(JsonToken.VALUE_STRING)) {
            String string = parser.getText().trim();
            try {
                long time = Long.valueOf(string);
                return new Date(time).toLocalDate();
            }
            catch (Exception e) {}
            if (string.length() == 0) {
                if (!isLenient()) {
                    return _failForNotLenient(parser, context, JsonToken.VALUE_STRING);
                }
                return null;
            }
            // as per [datatype-jsr310#37], only check for optional (and, incorrect...) time marker 'T'
            // if we are using default formatter
            DateTimeFormatter format = _formatter;
            try {
                if (format == DEFAULT_FORMATTER) {
                    // JavaScript by default includes time in JSON serialized Dates (UTC/ISO instant format).
                    if (string.length() > 10 && string.charAt(10) == 'T') {
                        if (string.endsWith("Z")) {
                            return LocalDateTime.ofInstant(Instant.parse(string), ZoneOffset.UTC).toLocalDate();
                        } else {
                            return LocalDate.parse(string, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        }
                    }
                }
                return LocalDate.parse(string, format);
            } catch (DateTimeException e) {
                return _handleDateTimeException(context, e, string);
            }
        }
        if (parser.isExpectedStartArrayToken()) {
            JsonToken t = parser.nextToken();
            if (t == JsonToken.END_ARRAY) {
                return null;
            }
            if (context.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
                    && (t == JsonToken.VALUE_STRING || t==JsonToken.VALUE_EMBEDDED_OBJECT)) {
                final LocalDate parsed = deserialize(parser, context);
                if (parser.nextToken() != JsonToken.END_ARRAY) {
                    handleMissingEndArrayForSingle(parser, context);
                }
                return parsed;
            }
            if (t == JsonToken.VALUE_NUMBER_INT) {
                int year = parser.getIntValue();
                int month = parser.nextIntValue(-1);
                int day = parser.nextIntValue(-1);

                if (parser.nextToken() != JsonToken.END_ARRAY) {
                    throw context.wrongTokenException(parser, handledType(), JsonToken.END_ARRAY,
                            "Expected array to end");
                }
                return LocalDate.of(year, month, day);
            }
            context.reportInputMismatch(handledType(),
                    "Unexpected token (%s) within Array, expected VALUE_NUMBER_INT",
                    t);
        }
        if (parser.hasToken(JsonToken.VALUE_EMBEDDED_OBJECT)) {
            return (LocalDate) parser.getEmbeddedObject();
        }
        // 06-Jan-2018, tatu: Is this actually safe? Do users expect such coercion?
        if (parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            if (!isLenient()) {
                return _failForNotLenient(parser, context, JsonToken.VALUE_STRING);
            }
            return new Date(parser.getLongValue()).toLocalDate();
        }
        return _handleUnexpectedToken(context, parser, "Expected array or string.");
    }

}
