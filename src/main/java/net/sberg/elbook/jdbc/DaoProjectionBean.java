package net.sberg.elbook.jdbc;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DaoProjectionBean {
    private List<String> properties;
    private Class result;
    private boolean atomar;
}
