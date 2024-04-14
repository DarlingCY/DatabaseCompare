package icu.greenlemon.databasecompare.util;

import icu.greenlemon.databasecompare.entity.DatabaseData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName: DataUtil
 * @Author: ChenYue
 * @Date: 2023/04/26
 */
public class DataUtil {
    public static Map<String, Map<String, Map<String, Map<String, String>>>> sourceDatabaseAllDataMap = new HashMap<>();
    public static Map<String, Map<String, Map<String, Map<String, String>>>> tagetDatabaseAllDataMap = new HashMap<>();
    public static Map<String, Map<String, Map<String, Map<String, String>>>> modifiedDatabaseAllDataMap = new HashMap<>();

    public static Set<String> addedDatabase;
    public static Set<String> deletedDatabase;
    public static Set<String> bothDatabase;

    public static Map<String, Set<String>> addedTable = new HashMap<>();
    public static Map<String, Set<String>> deletedTable = new HashMap<>();
    public static Map<String, Set<String>> bothTable = new HashMap<>();

    public static Map<String, Map<String, Set<String>>> addedField = new HashMap<>();
    public static Map<String, Map<String, Set<String>>> deletedField = new HashMap<>();

    public static Set<String> getBothSet(Set<String> sourceSet, Set<String> targetSet) {
        Set<String> bothSet = new HashSet<>();
        sourceSet.forEach(item -> {
            if (targetSet.contains(item)) {
                bothSet.add(item);
            }
        });
        return bothSet;
    }
}
