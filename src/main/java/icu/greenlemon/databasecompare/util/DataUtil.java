package icu.greenlemon.databasecompare.util;

import java.util.*;

/**
 * @ClassName: DataUtil
 * @Author: ChenYue
 * @Date: 2023/04/26
 */
public class DataUtil {
    public static final Map<String, Set<String>> newDatabaseMap = new HashMap<>();
    public static final Map<String, Set<String>> oldDatabaseMap = new HashMap<>();
    public static final Map<String, Map<String, Map<String, String>>> newTableMap = new HashMap<>();
    public static final Map<String, Map<String, Map<String, String>>> oldTableMap = new HashMap<>();
}
