package net.sberg.elbook.jdbc;

import jakarta.annotation.PostConstruct;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class JdbcGenericDao {

    private static final Logger log = LoggerFactory.getLogger(JdbcGenericDao.class);

    @Autowired
    @Qualifier("jdbcElbook")
    private JdbcTemplate jdbcTemplate;

    @Autowired
    @DaoDescriptorClass
    List<Object> daoDescriptorPersitentClasses;

    @Autowired
    @DaoDescriptorClass(transientBean = true)
    List<Object> daoDescriptorTransientClasses;

    private DaoDescriptorHelper daoDescriptorHelper;
    private Map<String, DaoDescriptorBean> descrMap;
    private Map<String, Object> mutexMap;
    private Map<String, Integer> idMap;

    private static final String PROP_IGNORE = "_ignore_";

    @PostConstruct
    public void init() throws Exception {
        daoDescriptorHelper = new DaoDescriptorHelper();
        descrMap = daoDescriptorHelper.createBeanMap(daoDescriptorPersitentClasses, daoDescriptorTransientClasses);

        mutexMap = Collections.synchronizedMap(new HashMap<>());
        idMap = Collections.synchronizedMap(new HashMap<>());

        for (Iterator<String> iterator = descrMap.keySet().iterator(); iterator.hasNext(); ) {
            String beanName = iterator.next();
            DaoDescriptorBean daoDescriptorBean = descrMap.get(beanName);
            if (!daoDescriptorBean.getTransientBean() && !daoDescriptorBean.getDynamicTableName()) {
                mutexMap.put(beanName, new Object());
                idMap.put(beanName, getMaxId(daoDescriptorBean, Optional.empty()));
            }
        }

    }

    private void fillPreparedStatement(PreparedStatement ps, DaoDescriptorBean daoDescriptorBean, List dbProperties, Object entity, List<DaoPlaceholderProperty> placeholders) throws Exception {
        Object value;
        int sqlType;
        if (placeholders != null && !placeholders.isEmpty()) {
            for (int i = 0; i < placeholders.size(); i++) {
                DaoPlaceholderProperty daoPlaceholderProperty = placeholders.get(i);
                sqlType = DaoDescriptorProperty.getSqlType(daoPlaceholderProperty.getValue().getClass());
                if (sqlType == Types.TIMESTAMP && daoPlaceholderProperty.getValue().getClass().equals(LocalDateTime.class)) {
                    value = Timestamp.valueOf((LocalDateTime) daoPlaceholderProperty.getValue());
                } else if (sqlType == Types.DATE && daoPlaceholderProperty.getValue().getClass().equals(LocalDate.class)) {
                    value = Date.valueOf((LocalDate) daoPlaceholderProperty.getValue());
                } else if (daoPlaceholderProperty.getValue().getClass().getSuperclass().equals(Enum.class)) {
                    value = daoPlaceholderProperty.getValue().toString();
                } else {
                    value = daoPlaceholderProperty.getValue();
                }
                ps.setObject(i + 1, value, sqlType);
            }
        } else {
            String property;
            DaoDescriptorProperty daoDescriptorProperty;
            for (int i = 0; i < dbProperties.size(); i++) {
                property = daoDescriptorBean.getDbPropertyMapping().get(dbProperties.get(i));
                value = PropertyUtils.getProperty(entity, property);
                daoDescriptorProperty = daoDescriptorBean.getProperties().get(property);
                if (daoDescriptorProperty.isNotNull() && value == null) {
                    throw new IllegalStateException("error on inserting the entity: " + daoDescriptorBean.getName() + " property " + property + " must not null");
                }
                sqlType = DaoDescriptorProperty.getSqlType(daoDescriptorProperty.getTypeClass());
                if (value == null) {
                    ps.setNull(i + 1, sqlType);
                } else {
                    if (sqlType == Types.TIMESTAMP && daoDescriptorProperty.getTypeClass().equals(LocalDateTime.class)) {
                        value = Timestamp.valueOf((LocalDateTime) value);
                    } else if (sqlType == Types.DATE && daoDescriptorProperty.getTypeClass().equals(LocalDate.class)) {
                        value = Date.valueOf((LocalDate) value);
                    } else if (daoDescriptorProperty.getTypeClass().getSuperclass().equals(Enum.class)) {
                        value = value.toString();
                    }
                    ps.setObject(i + 1, value, sqlType);
                }
            }
        }
    }

    public Object selectOne(String entityName, DaoProjectionBean daoProjectionBean, List<DaoPlaceholderProperty> placeholders) throws Exception {
        List result = select(entityName, daoProjectionBean, placeholders);
        if (result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    public Object selectOne(String sql, String entityName, DaoProjectionBean daoProjectionBean, List<DaoPlaceholderProperty> placeholders) throws Exception {
        List result = select(sql, entityName, daoProjectionBean, placeholders);
        if (result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    private List select(String entityName, DaoProjectionBean daoProjectionBean, List<DaoPlaceholderProperty> placeholders) throws Exception {
        DaoDescriptorBean daoDescriptorBean = descrMap.get(entityName);
        String select = daoDescriptorHelper.createSelectSimpleStatement(daoProjectionBean, daoDescriptorBean, placeholders);
        List result = jdbcTemplate.query(select, new SelectPreparedStatementSetter(daoDescriptorBean, placeholders), new SelectRowMapper(daoProjectionBean, daoDescriptorBean));
        return result;
    }

    private List select(String sql, String entityName, DaoProjectionBean daoProjectionBean, List<DaoPlaceholderProperty> placeholders) throws Exception {
        DaoDescriptorBean daoDescriptorBean = descrMap.get(entityName);
        List result = jdbcTemplate.query(sql, new SelectPreparedStatementSetter(daoDescriptorBean, placeholders), new SelectRowMapper(daoProjectionBean, daoDescriptorBean));
        return result;
    }

    public List selectMany(String entityName, DaoProjectionBean daoProjectionBean, List<DaoPlaceholderProperty> placeholders) throws Exception {
        return select(entityName, daoProjectionBean, placeholders);
    }

    public List selectMany(String sql, String entityName, DaoProjectionBean daoProjectionBean, List<DaoPlaceholderProperty> placeholders) throws Exception {
        return select(sql, entityName, daoProjectionBean, placeholders);
    }

    @Transactional
    public Object insert(Object entity, Optional<String> tableName) throws Exception {
        DaoDescriptorBean daoDescriptorBean = descrMap.get(entity.getClass().getName());
        String insert = daoDescriptorHelper.createInsertStatement(daoDescriptorBean, tableName);

        String idProperty = daoDescriptorBean.getDbPropertyMapping().get(daoDescriptorBean.getPrimaryKey());
        Integer id = (Integer) PropertyUtils.getProperty(entity, idProperty);
        if (id <= 0) {
            id = getNextId(daoDescriptorBean, tableName);
            PropertyUtils.setProperty(entity, idProperty, id);
        }

        this.jdbcTemplate.update(insert, new InsertPreparedStatementSetter(daoDescriptorBean, entity));
        return entity;
    }

    @Transactional
    public void batchInsert(List<Object> entities, Optional<String> tableName) throws Exception {
        DaoDescriptorBean daoDescriptorBean = descrMap.get(entities.get(0).getClass().getName());
        String insert = daoDescriptorHelper.createInsertStatement(daoDescriptorBean, tableName);

        String idProperty = daoDescriptorBean.getDbPropertyMapping().get(daoDescriptorBean.getPrimaryKey());

        for (Iterator<Object> iterator = entities.iterator(); iterator.hasNext(); ) {
            Object entity = iterator.next();
            Integer id = (Integer) PropertyUtils.getProperty(entity, idProperty);
            if (id <= 0) {
                id = getNextId(daoDescriptorBean, tableName);
                PropertyUtils.setProperty(entity, idProperty, id);
            }
        }

        this.jdbcTemplate.batchUpdate(insert, new InsertBatchPreparedStatementSetter(daoDescriptorBean, entities));
    }

    private int getMaxId(DaoDescriptorBean daoDescriptorBean, Optional<String> tableName) throws Exception {
        String selectMaxId = daoDescriptorHelper.createSelectMaxIdStatement(daoDescriptorBean, tableName);
        Integer id = this.jdbcTemplate.queryForObject(selectMaxId, Integer.class);
        if (id == null) {
            id = 0;
        }
        return id;
    }

    public int getNextId(DaoDescriptorBean daoDescriptorBean, Optional<String> tableName) throws Exception {

        if (!tableName.isEmpty()) {
            synchronized (Integer.valueOf(tableName.get().hashCode())) {
                if (!mutexMap.containsKey(tableName.get())) {
                    mutexMap.put(tableName.get(), new Object());
                    idMap.put(tableName.get(), getMaxId(daoDescriptorBean, tableName));
                }
            }
        }

        synchronized (mutexMap.get(tableName.isEmpty()?daoDescriptorBean.getName():tableName.get())) {
            Integer id = idMap.get(tableName.isEmpty()?daoDescriptorBean.getName():tableName.get());
            id++;
            idMap.put(tableName.isEmpty()?daoDescriptorBean.getName():tableName.get(), id);
            return id;
        }
    }

    public int getNextId(String entityName, Optional<String> tableName) throws Exception {
        return getNextId(descrMap.get(entityName), tableName);
    }

    @Transactional
    public void delete(Object entity, Optional<String> tableName) throws Exception {
        DaoDescriptorBean daoDescriptorBean = descrMap.get(entity.getClass().getName());
        String idProperty = daoDescriptorBean.getDbPropertyMapping().get(daoDescriptorBean.getPrimaryKey());
        int id = (Integer) PropertyUtils.getProperty(entity, idProperty);
        delete(id, daoDescriptorBean, tableName);
    }

    private void delete(int id, DaoDescriptorBean daoDescriptorBean, Optional<String> tableName) throws Exception {
        String delete = daoDescriptorHelper.createDeleteStatement(daoDescriptorBean, tableName);

        this.jdbcTemplate.update(delete, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                try {
                    ps.setObject(1, id, Types.INTEGER);
                } catch (Exception e) {
                    log.error("error on deleting the entity: " + daoDescriptorBean.getName(), e);
                    throw new SQLException("error on deleting the entity: " + daoDescriptorBean.getName(), e);
                }
            }
        });
    }

    @Transactional
    public void delete(int id, String entityName, Optional<String> tableName) throws Exception {
        DaoDescriptorBean daoDescriptorBean = descrMap.get(entityName);
        delete(id, daoDescriptorBean, tableName);
    }

    @Transactional
    public void delete(String sql, List<DaoPlaceholderProperty> placeholders) throws Exception {
        jdbcTemplate.update(sql, new DeletePreparedStatementSetter(sql, placeholders));
    }

    @Transactional
    public Object update(Object entity, Optional<String> tableName) throws Exception {
        DaoDescriptorBean daoDescriptorBean = descrMap.get(entity.getClass().getName());
        String update = daoDescriptorHelper.createUpdateStatement(daoDescriptorBean, tableName);

        this.jdbcTemplate.update(update, new UpdatePreparedStatementSetter(daoDescriptorBean, entity, null));
        return entity;
    }

    @Transactional
    public void batchUpdate(List<Object> entities, Optional<String> tableName) throws Exception {
        DaoDescriptorBean daoDescriptorBean = descrMap.get(entities.get(0).getClass().getName());
        String update = daoDescriptorHelper.createUpdateStatement(daoDescriptorBean, tableName);
        this.jdbcTemplate.batchUpdate(update, new UpdateBatchPreparedStatementSetter(daoDescriptorBean, entities, null));
    }

    @Transactional
    public int update(String sql, String entityName, List<DaoPlaceholderProperty> placeholders) throws Exception {
        DaoDescriptorBean daoDescriptorBean = descrMap.get(entityName);
        return this.jdbcTemplate.update(sql, new UpdatePreparedStatementSetter(daoDescriptorBean, null, placeholders));
    }

    public Object deserializeEntity(String entityName, String[] properties, String[] values, Object[] formatters) throws Exception {
        DaoDescriptorBean daoDescriptorBean = descrMap.get(entityName);
        Object bean = Class.forName(entityName).getDeclaredConstructor().newInstance();

        for (int i = 0; i < properties.length; i++) {
            String property = properties[i];
            if (property.equals(PROP_IGNORE)) {
                continue;
            }
            DaoDescriptorProperty daoDescriptorProperty = daoDescriptorBean.getProperties().get(property);
            String value = values[i];
            Object formatter = formatters[i];

            if (value == null || value.trim().isEmpty()) {
                continue;
            }

            if (!value.trim().isEmpty() && daoDescriptorProperty.getTypeClass().equals(String.class)) {
                PropertyUtils.setProperty(bean, property, value);
            } else if (!value.trim().isEmpty() && daoDescriptorProperty.getTypeClass().equals(Integer.class)) {
                PropertyUtils.setProperty(bean, property, Integer.parseInt(value));
            } else if (!value.trim().isEmpty() && daoDescriptorProperty.getTypeClass().equals(BigDecimal.class)) {
                PropertyUtils.setProperty(bean, property, BigDecimal.valueOf(((NumberFormat) formatter).parse(value.replaceAll("\\.", ",")).doubleValue()));
            } else if (!value.trim().isEmpty() && daoDescriptorProperty.getTypeClass().equals(Boolean.class)) {
                if (value.equals("0") || value.equalsIgnoreCase("nein") || value.equalsIgnoreCase("false")) {
                    PropertyUtils.setProperty(bean, property, false);
                } else if (value.equals("1") || value.equalsIgnoreCase("ja") || value.equalsIgnoreCase("true")) {
                    PropertyUtils.setProperty(bean, property, true);
                }
            } else if (value != null && daoDescriptorProperty.getTypeClass().equals(LocalDateTime.class)) {
                PropertyUtils.setProperty(bean, property, new Timestamp(((SimpleDateFormat) formatter).parse(value).getTime()).toLocalDateTime());
            } else if (value != null && daoDescriptorProperty.getTypeClass().equals(LocalDate.class)) {
                PropertyUtils.setProperty(bean, property, new Date(((SimpleDateFormat) formatter).parse(value).getTime()).toLocalDate());
            } else if (value != null && daoDescriptorProperty.getTypeClass().getSuperclass().equals(Enum.class)) {
                PropertyUtils.setProperty(bean, property, Enum.valueOf(daoDescriptorProperty.getTypeClass(), value));
            } else {
                throw new IllegalStateException("class not supported: " + daoDescriptorProperty.getTypeClass());
            }
        }

        return bean;
    }

    private class UpdatePreparedStatementSetter implements PreparedStatementSetter {
        private final DaoDescriptorBean daoDescriptorBean;
        private final Object entity;
        private final List<DaoPlaceholderProperty> placeholders;

        private UpdatePreparedStatementSetter(DaoDescriptorBean daoDescriptorBean, Object entity, List<DaoPlaceholderProperty> placeholders) {
            this.daoDescriptorBean = daoDescriptorBean;
            this.entity = entity;
            this.placeholders = placeholders;
        }

        @Override
        public void setValues(PreparedStatement ps) throws SQLException {
            try {
                if (placeholders != null && !placeholders.isEmpty()) {
                    fillPreparedStatement(ps, daoDescriptorBean, null, null, placeholders);
                } else {
                    List<String> dbProperties = new ArrayList<>(daoDescriptorBean.getAllDbProperties());
                    dbProperties.remove(daoDescriptorBean.getPrimaryKey());
                    fillPreparedStatement(ps, daoDescriptorBean, dbProperties, entity, null);

                    String idProperty = daoDescriptorBean.getDbPropertyMapping().get(daoDescriptorBean.getPrimaryKey());
                    int id = (Integer) PropertyUtils.getProperty(entity, idProperty);
                    ps.setObject(dbProperties.size() + 1, id, Types.INTEGER);
                }
            } catch (Exception e) {
                log.error("error on updating the entity: " + daoDescriptorBean.getName(), e);
                throw new SQLException("error on updating the entity: " + daoDescriptorBean.getName(), e);
            }
        }
    }

    private class UpdateBatchPreparedStatementSetter implements BatchPreparedStatementSetter {
        private final DaoDescriptorBean daoDescriptorBean;
        private final List<Object> entities;
        private final List<DaoPlaceholderProperty> placeholders;

        private UpdateBatchPreparedStatementSetter(DaoDescriptorBean daoDescriptorBean, List<Object> entities, List<DaoPlaceholderProperty> placeholders) {
            this.daoDescriptorBean = daoDescriptorBean;
            this.entities = entities;
            this.placeholders = placeholders;
        }

        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            try {
                if (placeholders != null && !placeholders.isEmpty()) {
                    fillPreparedStatement(ps, daoDescriptorBean, null, null, placeholders);
                } else {
                    List<String> dbProperties = new ArrayList<>(daoDescriptorBean.getAllDbProperties());
                    dbProperties.remove(daoDescriptorBean.getPrimaryKey());
                    fillPreparedStatement(ps, daoDescriptorBean, dbProperties, entities.get(i), null);

                    String idProperty = daoDescriptorBean.getDbPropertyMapping().get(daoDescriptorBean.getPrimaryKey());
                    int id = (Integer) PropertyUtils.getProperty(entities.get(i), idProperty);
                    ps.setObject(dbProperties.size() + 1, id, Types.INTEGER);
                }
            } catch (Exception e) {
                log.error("error on updating the entity: " + daoDescriptorBean.getName(), e);
                throw new SQLException("error on updating the entity: " + daoDescriptorBean.getName(), e);
            }
        }

        @Override
        public int getBatchSize() {
            return entities.size();
        }
    }

    private class SelectRowMapper implements RowMapper<Object> {
        private final DaoDescriptorBean daoDescriptorBean;
        private final DaoProjectionBean daoProjectionBean;

        private SelectRowMapper(DaoProjectionBean daoProjectionBean, DaoDescriptorBean daoDescriptorBean) {
            this.daoDescriptorBean = daoDescriptorBean;
            this.daoProjectionBean = daoProjectionBean;
        }

        public Object mapRow(ResultSet rs, int rownum) throws SQLException {
            try {
                Object result;
                List<String> dbProperties;
                boolean resultIsMap = false;
                if (daoProjectionBean == null) {
                    result = Class.forName(daoDescriptorBean.getName()).newInstance();
                    dbProperties = daoDescriptorBean.getAllDbProperties();
                } else {
                    if (!daoProjectionBean.isAtomar()) {
                        resultIsMap = daoProjectionBean.getResult().equals(Map.class);
                        result = daoProjectionBean.getResult().equals(Map.class) ? new HashMap<>() : daoProjectionBean.getResult().newInstance();
                        if (daoDescriptorBean != null) {
                            dbProperties = daoProjectionBean.getProperties().stream().map(o -> daoDescriptorBean.getProperties().get(o).getDbProperty()).collect(Collectors.toList());
                        } else {
                            dbProperties = daoProjectionBean.getProperties();
                        }
                    } else {
                        return rs.getObject(1);
                    }
                }
                String dbProperty;
                String property;
                DaoDescriptorProperty daoDescriptorProperty;
                Object value;
                for (int i = 0; i < dbProperties.size(); i++) {

                    if (daoDescriptorBean == null) {
                        value = rs.getObject(i + 1);
                        if (value != null && value.getClass().equals(Timestamp.class)) {
                            value = ((Timestamp) value).toLocalDateTime();
                        } else if (value != null && value.getClass().equals(Date.class)) {
                            value = ((Date) value).toLocalDate();
                        }
                        property = dbProperties.get(i);
                    } else {
                        dbProperty = dbProperties.get(i);
                        property = daoDescriptorBean.getDbPropertyMapping().get(dbProperty);
                        daoDescriptorProperty = daoDescriptorBean.getProperties().get(property);

                        try {
                            value = rs.getObject(dbProperty);
                        } catch (SQLException e) {
                            if (daoDescriptorProperty.isNotNull()) {
                                throw e;
                            } else {
                                continue;
                            }
                        }

                        if (value != null && daoDescriptorProperty.getTypeClass().equals(LocalDateTime.class) && value instanceof Timestamp) {
                            value = ((Timestamp) value).toLocalDateTime();
                        } else if (value != null && daoDescriptorProperty.getTypeClass().equals(LocalDate.class) && value instanceof Date) {
                            value = ((Date) value).toLocalDate();
                        } else if (value != null && daoDescriptorProperty.getTypeClass().getSuperclass().equals(Enum.class)) {
                            value = Enum.valueOf(daoDescriptorProperty.getTypeClass(), value.toString());
                        } else if (value != null && value.getClass().equals(Double.class) && daoDescriptorProperty.getTypeClass().equals(BigDecimal.class)) {
                            value = BigDecimal.valueOf((Double) value);
                        }
                    }

                    if (value != null) {
                        if (resultIsMap) {
                            ((Map) result).put(property, value);
                        } else {
                            PropertyUtils.setProperty(result, property, value);
                        }
                    }
                }
                return result;
            } catch (Exception e) {
                log.error("error on selecting the entity: " + daoDescriptorBean.getName(), e);
                throw new SQLException("error on selecting the entity: " + daoDescriptorBean.getName(), e);
            }
        }
    }

    private class SelectPreparedStatementSetter implements PreparedStatementSetter {
        private final DaoDescriptorBean daoDescriptorBean;
        private final List<DaoPlaceholderProperty> placeholders;

        private SelectPreparedStatementSetter(DaoDescriptorBean daoDescriptorBean, List<DaoPlaceholderProperty> placeholders) {
            this.daoDescriptorBean = daoDescriptorBean;
            this.placeholders = placeholders;
        }

        @Override
        public void setValues(PreparedStatement ps) throws SQLException {
            try {
                if (placeholders != null && !placeholders.isEmpty()) {
                    fillPreparedStatement(ps, daoDescriptorBean, null, null, placeholders);
                }
            } catch (Exception e) {
                log.error("error on selecting the entity: " + daoDescriptorBean.getName(), e);
                throw new SQLException("error on selecting the entity: " + daoDescriptorBean.getName(), e);
            }
        }
    }

    private class DeletePreparedStatementSetter implements PreparedStatementSetter {
        private final List<DaoPlaceholderProperty> placeholders;
        private final String sql;

        private DeletePreparedStatementSetter(String sql, List<DaoPlaceholderProperty> placeholders) {
            this.sql = sql;
            this.placeholders = placeholders;
        }

        @Override
        public void setValues(PreparedStatement ps) throws SQLException {
            try {
                if (placeholders != null && !placeholders.isEmpty()) {
                    fillPreparedStatement(ps, null, null, null, placeholders);
                }
            } catch (Exception e) {
                log.error("error on deleting the entities 4 the sql: " + sql, e);
                throw new SQLException("error on deleting the entities 4 the sql: " + sql, e);
            }
        }
    }

    private class InsertPreparedStatementSetter implements PreparedStatementSetter {
        private final DaoDescriptorBean daoDescriptorBean;
        private final Object entity;

        private InsertPreparedStatementSetter(DaoDescriptorBean daoDescriptorBean, Object entity) {
            this.daoDescriptorBean = daoDescriptorBean;
            this.entity = entity;
        }

        @Override
        public void setValues(PreparedStatement ps) throws SQLException {
            try {
                fillPreparedStatement(ps, daoDescriptorBean, daoDescriptorBean.getAllDbProperties(), entity, null);
            } catch (Exception e) {
                log.error("error on inserting the entity: " + daoDescriptorBean.getName(), e);
                throw new SQLException("error on inserting the entity: " + daoDescriptorBean.getName(), e);
            }
        }
    }

    private class InsertBatchPreparedStatementSetter implements BatchPreparedStatementSetter {
        private final DaoDescriptorBean daoDescriptorBean;
        private final List<Object> entities;

        private InsertBatchPreparedStatementSetter(DaoDescriptorBean daoDescriptorBean, List<Object> entities) {
            this.daoDescriptorBean = daoDescriptorBean;
            this.entities = entities;
        }

        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            try {
                fillPreparedStatement(ps, daoDescriptorBean, daoDescriptorBean.getAllDbProperties(), entities.get(i), null);
            } catch (Exception e) {
                log.error("error on inserting the entity: " + daoDescriptorBean.getName(), e);
                throw new SQLException("error on inserting the entity: " + daoDescriptorBean.getName(), e);
            }
        }

        @Override
        public int getBatchSize() {
            return entities.size();
        }
    }
}
