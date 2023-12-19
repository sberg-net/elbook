package net.sberg.elbook.jdbc;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class DaoDescriptorBean {
    private String name;
    private String dbTable;
    private String primaryKey;
    private Boolean transientBean = false;
    private Boolean dynamicTableName = false;
    private List<String> allProperties = new ArrayList<>();
    private List<String> allDbProperties = new ArrayList<>();
    private Map<String, String> dbPropertyMapping = new HashMap<>();
    private Map<String, DaoDescriptorProperty> properties = new HashMap<>();
}
