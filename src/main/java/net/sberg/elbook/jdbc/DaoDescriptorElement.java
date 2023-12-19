package net.sberg.elbook.jdbc;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DaoDescriptorElement {
    String dbProperty() default DaoDescriptorHelper.unknown;
    boolean notNull() default false;
}
