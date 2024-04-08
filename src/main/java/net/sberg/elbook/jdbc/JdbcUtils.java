package net.sberg.elbook.jdbc;

import org.apache.commons.beanutils.PropertyUtils;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class JdbcUtils {

    public static final String getListAsStr(List l) throws Exception {
        String values = Arrays.toString(l.toArray());
        values = values.replaceAll("\\s", "");
        values = values.replaceAll("\\[", "");
        values = values.replaceAll("\\]", "");
        return values;
    }

    public static final StringBuilder getLikeStr(String value, String delimiter, String property) {
        StringBuilder result = new StringBuilder();
        result.append(" ( ");
        result.append(" " + property + " like '%" + delimiter + value + delimiter + "%' or ");
        result.append(" " + property + " = '" + value + "' or ");
        result.append(" " + property + " like '" + value + delimiter + "%' or ");
        result.append(" " + property + " like '%" + delimiter + value + "'");
        result.append(" ) ");
        return result;
    }

    public static final StringBuilder appendToWhereAndAddPlaceholder(
        StringBuilder builder,
        List<DaoPlaceholderProperty> placeholders,
        String str,
        String property,
        Object bean,
        boolean transformToLocalDateTime,
        String alternativePlaceholderPropertyName,
        String valueLikeTemplate,
        String booleanConcatStr
    ) throws Exception {
        Object value = PropertyUtils.getProperty(bean, property);
        if (value == null) {
            return builder;
        }
        return appendToWhereAndAddPlaceholder(
            builder,
            placeholders,
            str,
            value,
            transformToLocalDateTime,
            alternativePlaceholderPropertyName,
            valueLikeTemplate,
            booleanConcatStr
        );
    }

    public static final StringBuilder appendToWhereAndAddPlaceholder(
        StringBuilder builder,
        List<DaoPlaceholderProperty> placeholders,
        String str,
        Object value,
        boolean transformToLocalDateTime,
        String alternativePlaceholderPropertyName,
        String valueLikeTemplate,
        String booleanConcatStr
    ) throws Exception {
        builder = appendToWhere(builder, str, booleanConcatStr);
        if (transformToLocalDateTime) {
            LocalDateTime dateTime = new Timestamp((long) value).toLocalDateTime();
            value = dateTime;
        }
        if (valueLikeTemplate != null) {
            value = MessageFormat.format(valueLikeTemplate, value);
        }
        placeholders.add(new DaoPlaceholderProperty(alternativePlaceholderPropertyName, value));
        return builder;
    }

    public static final StringBuilder appendToWhere(StringBuilder builder, String str, String concatStr) {
        if (builder.length() > 0 && concatStr != null) {
            builder.append(" " + concatStr + " ");
        }
        builder.append(" ");
        builder.append(str);
        builder.append(" \n");
        return builder;
    }
}
