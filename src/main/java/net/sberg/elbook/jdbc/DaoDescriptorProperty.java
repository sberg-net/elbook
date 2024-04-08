package net.sberg.elbook.jdbc;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DaoDescriptorProperty {
    private String type = DaoDescriptorHelper.unknown;
    private Class typeClass;
    private String dbProperty = DaoDescriptorHelper.unknown;
    private boolean notNull = false;

    public static final int getSqlType(Class typeClass) throws Exception {
        if (typeClass.equals(Integer.class)) {
            return Types.INTEGER;
        } else if (typeClass.equals(LocalDate.class)) {
            return Types.DATE;
        } else if (typeClass.equals(LocalDateTime.class)) {
            return Types.TIMESTAMP;
        } else if (typeClass.equals(String.class)) {
            return Types.VARCHAR;
        } else if (typeClass.getSuperclass().equals(Enum.class)) {
            return Types.VARCHAR;
        } else if (typeClass.equals(Boolean.class)) {
            return Types.TINYINT;
        } else if (typeClass.equals(Double.class)) {
            return Types.DOUBLE;
        } else if (typeClass.equals(BigDecimal.class)) {
            return Types.DECIMAL;
        } else {
            throw new IllegalStateException("unknown sqlType for javaType: " + typeClass);
        }
    }
}
