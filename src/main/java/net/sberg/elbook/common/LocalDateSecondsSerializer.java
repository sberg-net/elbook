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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateSecondsSerializer extends LocalDateSerializer {

    public static final LocalDateSecondsSerializer INSTANCE = new LocalDateSecondsSerializer();

    protected LocalDateSecondsSerializer() {
        super();
    }

    protected LocalDateSecondsSerializer(LocalDateSerializer base,
                                  Boolean useTimestamp, DateTimeFormatter dtf, JsonFormat.Shape shape) {
        super(base, useTimestamp, dtf, shape);
    }

    public LocalDateSecondsSerializer(DateTimeFormatter formatter) {
        super(formatter);
    }

    @Override
    protected LocalDateSecondsSerializer withFormat(Boolean useTimestamp, DateTimeFormatter dtf, JsonFormat.Shape shape) {
        return new LocalDateSecondsSerializer(this, useTimestamp, dtf, shape);
    }

    @Override
    public void serialize(LocalDate date, JsonGenerator g, SerializerProvider provider) throws IOException
    {
        if (useTimestamp(provider)) {
            if (_shape == JsonFormat.Shape.NUMBER_INT) {
                g.writeNumber(Date.valueOf(date).getTime());
            } else {
                g.writeStartArray();
                _serializeAsArrayContents(date, g, provider);
                g.writeEndArray();
            }
        } else {
            g.writeString((_formatter == null) ? date.toString() : date.format(_formatter));
        }
    }

    @Override
    public void serializeWithType(LocalDate value, JsonGenerator g,
                                  SerializerProvider provider, TypeSerializer typeSer) throws IOException
    {
        WritableTypeId typeIdDef = typeSer.writeTypePrefix(g,
                typeSer.typeId(value, serializationShape(provider)));
        // need to write out to avoid double-writing array markers
        switch (typeIdDef.valueShape) {
            case START_ARRAY:
                _serializeAsArrayContents(value, g, provider);
                break;
            case VALUE_NUMBER_INT:
                g.writeNumber(value.toEpochDay());
                break;
            default:
                g.writeString((_formatter == null) ? value.toString() : value.format(_formatter));
        }
        typeSer.writeTypeSuffix(g, typeIdDef);
    }

    protected void _serializeAsArrayContents(LocalDate value, JsonGenerator g,
                                             SerializerProvider provider) throws IOException
    {
        g.writeNumber(value.getYear());
        g.writeNumber(value.getMonthValue());
        g.writeNumber(value.getDayOfMonth());
    }

    @Override
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException
    {
        SerializerProvider provider = visitor.getProvider();
        boolean useTimestamp = (provider != null) && useTimestamp(provider);
        if (useTimestamp) {
            _acceptTimestampVisitor(visitor, typeHint);
        } else {
            JsonStringFormatVisitor v2 = visitor.expectStringFormat(typeHint);
            if (v2 != null) {
                v2.format(JsonValueFormat.DATE);
            }
        }
    }

    @Override // since 2.9
    protected JsonToken serializationShape(SerializerProvider provider) {
        if (useTimestamp(provider)) {
            if (_shape == JsonFormat.Shape.NUMBER_INT) {
                return JsonToken.VALUE_NUMBER_INT;
            }
            return JsonToken.START_ARRAY;
        }
        return JsonToken.VALUE_STRING;
    }
}
