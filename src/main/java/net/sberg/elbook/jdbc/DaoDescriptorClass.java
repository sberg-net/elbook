package net.sberg.elbook.jdbc;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
@Qualifier
public @interface DaoDescriptorClass {
    String dbTable() default DaoDescriptorHelper.unknown;
    boolean dynamicTableName() default false;
    String primaryKey() default "id";
    boolean transientBean() default false;
}
