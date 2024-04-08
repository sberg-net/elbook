package net.sberg.elbook.jdbc;

import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

public class DaoDescriptorHelper {

    public static final String properties = "properties";
    public static final String unknown = "_unknown_";

    private static final String insertTemplate = "insert into {0} ({1}) values ({2})";
    private static final String updateTemplate = "update {0} set {1} where {2}";
    private static final String deleteTemplate = "delete from {0} where {1}";
    private static final String selectMaxIdTemplate = "select max({0}) from {1}";
    private static final String selectSimpleTemplate = "select {0} from {1} where {2}";
    private static final String placeHolderTemplate = "{0} = ?";

    public String createInsertStatement(DaoDescriptorBean daoDescriptorBean, Optional<String> tableName) throws Exception {
        String projection = daoDescriptorBean.getAllDbProperties().stream().map(String::valueOf).collect(Collectors.joining(", "));
        String placeholders = daoDescriptorBean.getAllDbProperties().stream().map(o -> "?").collect(Collectors.joining(", "));
        return MessageFormat.format(insertTemplate, tableName.isEmpty()?daoDescriptorBean.getDbTable():tableName.get(), projection, placeholders);
    }

    public String createUpdateStatement(DaoDescriptorBean daoDescriptorBean, Optional<String> tableName) throws Exception {
        List<String> dbProperties = new ArrayList<>(daoDescriptorBean.getAllDbProperties());
        dbProperties.remove(daoDescriptorBean.getPrimaryKey());
        String placeholders = dbProperties.stream().map(o -> o + " = ?").collect(Collectors.joining(", "));
        return MessageFormat.format(updateTemplate, tableName.isEmpty()?daoDescriptorBean.getDbTable():tableName.get(), placeholders, MessageFormat.format(placeHolderTemplate, daoDescriptorBean.getPrimaryKey()));
    }

    public String createSelectMaxIdStatement(DaoDescriptorBean daoDescriptorBean, Optional<String> tableName) throws Exception {
        return MessageFormat.format(selectMaxIdTemplate, daoDescriptorBean.getPrimaryKey(), tableName.isEmpty()?daoDescriptorBean.getDbTable():tableName.get());
    }

    public String createSelectSimpleStatement(DaoProjectionBean daoProjectionBean, DaoDescriptorBean daoDescriptorBean, List<DaoPlaceholderProperty> placeholders) throws Exception {
        StringBuilder params = new StringBuilder("1=1");
        if (placeholders != null && !placeholders.isEmpty()) {
            params = new StringBuilder();
            DaoPlaceholderProperty daoPlaceholderProperty;
            DaoDescriptorProperty daoDescriptorProperty;
            for (DaoPlaceholderProperty placeholder : placeholders) {
                daoPlaceholderProperty = placeholder;
                daoDescriptorProperty = daoDescriptorBean.getProperties().get(daoPlaceholderProperty.getProperty());
                if (params.length() > 0) {
                    params.append(" and ");
                }
                params.append(MessageFormat.format(placeHolderTemplate, daoDescriptorProperty.getDbProperty()));
            }
        }
        String projection = "";
        if (daoProjectionBean == null) {
            projection = daoDescriptorBean.getAllDbProperties().stream().map(String::valueOf).collect(Collectors.joining(", "));
        } else {
            projection = daoProjectionBean.getProperties().stream().map(o -> daoDescriptorBean.getProperties().get(o).getDbProperty()).collect(Collectors.joining(", "));
        }

        return MessageFormat.format(selectSimpleTemplate, projection, daoDescriptorBean.getDbTable(), params.toString());
    }

    public String createDeleteStatement(DaoDescriptorBean daoDescriptorBean, Optional<String> tableName) throws Exception {
        return MessageFormat.format(deleteTemplate, tableName.isEmpty()?daoDescriptorBean.getDbTable():tableName.get(), MessageFormat.format(placeHolderTemplate, daoDescriptorBean.getPrimaryKey()));
    }

    public Map<String, DaoDescriptorBean> createBeanMap(List daoDescriptorPersitentClasses, List daoDescriptorTransientClasses) throws Exception {
        Map<String, DaoDescriptorBean> result = new HashMap<>();

        DaoDescriptorBean daoDescriptorBean;
        DaoDescriptorProperty daoDescriptorProperty;

        List daoDescriptorClasses = new ArrayList();
        daoDescriptorClasses.addAll(daoDescriptorPersitentClasses);
        daoDescriptorClasses.addAll(daoDescriptorTransientClasses);

        for (Object object : daoDescriptorClasses) {
            Class<?> aClass = object.getClass();
            daoDescriptorBean = new DaoDescriptorBean();
            daoDescriptorBean.setTransientBean(aClass.getAnnotation(DaoDescriptorClass.class).transientBean());
            daoDescriptorBean.setDynamicTableName(aClass.getAnnotation(DaoDescriptorClass.class).dynamicTableName());
            daoDescriptorBean.setDbTable(aClass.getAnnotation(DaoDescriptorClass.class).dbTable());
            daoDescriptorBean.setPrimaryKey(aClass.getAnnotation(DaoDescriptorClass.class).primaryKey());
            daoDescriptorBean.setName(aClass.getName());
            if (daoDescriptorBean.getDbTable().equals(unknown)) {
                daoDescriptorBean.setDbTable(aClass.getSimpleName());
            }

            result.put(daoDescriptorBean.getName(), daoDescriptorBean);

            Field[] fields = aClass.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field aField = fields[i];

                if (aField.getAnnotation(DaoDescriptorElement.class) == null) {
                    continue;
                }

                daoDescriptorProperty = new DaoDescriptorProperty();

                if (ClassUtils.primitiveToWrapper(aField.getType()) != null) {
                    daoDescriptorProperty.setTypeClass(ClassUtils.primitiveToWrapper(aField.getType()));
                } else {
                    daoDescriptorProperty.setTypeClass(ClassUtils.getClass(aField.getType().getName()));
                }

                daoDescriptorProperty.setType(daoDescriptorProperty.getTypeClass().getName());
                daoDescriptorProperty.setDbProperty(aField.getAnnotation(DaoDescriptorElement.class).dbProperty());
                daoDescriptorProperty.setNotNull(aField.getAnnotation(DaoDescriptorElement.class).notNull());

                if (daoDescriptorProperty.getDbProperty().equals(unknown)) {
                    daoDescriptorProperty.setDbProperty(aField.getName());
                }

                daoDescriptorBean.getAllProperties().add(aField.getName());
                daoDescriptorBean.getProperties().put(aField.getName(), daoDescriptorProperty);
                daoDescriptorBean.getAllDbProperties().add(daoDescriptorProperty.getDbProperty());
                daoDescriptorBean.getDbPropertyMapping().put(daoDescriptorProperty.getDbProperty(), aField.getName());

            }
        }

        return result;
    }
}
