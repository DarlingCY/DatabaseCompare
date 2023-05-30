package icu.greenlemon.databasecompare.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.Set;

/**
 * @ClassName: DatabaseData
 * @Author: ChenYue
 * @Date: 2023/05/30
 */
@Data
@Accessors(chain = true)
public class DatabaseData {
    private Map<String, Set<String>> databaseMap;
    private Map<String, Map<String, Map<String, String>>> tableMap;
}
