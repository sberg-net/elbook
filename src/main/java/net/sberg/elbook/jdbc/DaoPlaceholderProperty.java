package net.sberg.elbook.jdbc;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class DaoPlaceholderProperty {
    @NonNull
    private String property;
    @NonNull
    private Object value;
}
