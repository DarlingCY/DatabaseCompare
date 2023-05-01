package icu.greenlemon.databasecompare.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @ClassName: CommonService
 * @Author: ChenYue
 * @Date: 2022/11/01
 */
@Service
@Slf4j
public class CompareService {
    @Value("${newDatabase.url}")
    private String newDatabaseUrl;
    @Value("${newDatabase.user}")
    private String newDatabaseUser;
    @Value("${newDatabase.pass}")
    private String newDatabasePass;
    @Value("${oldDatabase.url}")
    private String oldDatabaseUrl;
    @Value("${oldDatabase.user}")
    private String oldDatabaseUser;
    @Value("${oldDatabase.pass}")
    private String oldDatabasePass;

    private final Set<String> newDataBaseSet = new HashSet<>();
    private final Set<String> oldDataBaseSet = new HashSet<>();
    private final Map<String, Set<String>> newDataBaseMap = new HashMap<>();
    private final Map<String, Set<String>> oldDataBaseMap = new HashMap<>();

    private final Map<String, Map<String, Map<String, String>>> newTableMap = new HashMap<>();
    private final Map<String, Map<String, Map<String, String>>> oldTableMap = new HashMap<>();

    @SneakyThrows
//    @PostConstruct
    public void init() {
        log.info("程序开始执行==>" + LocalDateTime.now());
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection newDatabaseConnection = DriverManager.getConnection(newDatabaseUrl, newDatabaseUser, newDatabasePass);
        Connection oldDatabaseConnection = DriverManager.getConnection(oldDatabaseUrl, oldDatabaseUser, oldDatabasePass);
        DatabaseMetaData newMetaData = newDatabaseConnection.getMetaData();
        DatabaseMetaData oldMetaData = oldDatabaseConnection.getMetaData();

        getData(newMetaData, oldMetaData);
        log.info("程序开始对比==>" + LocalDateTime.now());

        log.info("==========数据库判断开始==========");
        //共同拥有的数据库
        List<String> bothContainsDataBase = new ArrayList<>();
        newDataBaseSet.forEach(item -> {
            if (oldDataBaseSet.contains(item)) {
                bothContainsDataBase.add(item);
            }
        });
        bothContainsDataBase.forEach(newDataBaseSet::remove);
        bothContainsDataBase.forEach(oldDataBaseSet::remove);
        if (!newDataBaseSet.isEmpty()) {
            log.info("新增了数据库↓");
            newDataBaseSet.forEach(log::info);
        }
        if (!oldDataBaseSet.isEmpty()) {
            log.info("删除了数据库↓");
            oldDataBaseSet.forEach(log::info);
        }

        log.info("==========表判断开始==========");
        //每个数据库中共同存在的表
        Map<String, List<String>> bothContainsTableMap = new HashMap<>();
        bothContainsDataBase.forEach(dataBase -> {
            //共同拥有的表
            List<String> bothContainsTable = new ArrayList<>();
            Set<String> newTables = newDataBaseMap.get(dataBase);
            Set<String> oldTables = oldDataBaseMap.get(dataBase);
            newTables.forEach(table -> {
                if (oldTables.contains(table)) {
                    bothContainsTable.add(table);
                }
            });
            bothContainsTable.forEach(newTables::remove);
            bothContainsTable.forEach(oldTables::remove);
            bothContainsTableMap.put(dataBase, bothContainsTable);
            if (!newTables.isEmpty()) {
                log.info(dataBase + "中新增了表↓");
                newTables.forEach(log::info);
            }
            if (!oldTables.isEmpty()) {
                log.info(dataBase + "中删除了表↓");
                oldTables.forEach(log::info);
            }
        });

        log.info("==========字段判断开始==========");
        newTableMap.forEach((key, newFieldMap) -> {
            String dataBaseName = key.split("\\.")[0];
            String tableName = key.split("\\.")[1];
            List<String> bothContainsTable = bothContainsTableMap.get(dataBaseName);
            if (null != bothContainsTable && bothContainsTable.contains(tableName)) {
                Map<String, Map<String, String>> oldFieldMap = oldTableMap.get(key);
                Set<String> keySet = new HashSet<>();
                keySet.addAll(oldFieldMap.keySet());
                keySet.addAll(newFieldMap.keySet());
                keySet.forEach(item -> {
                    Map<String, String> oldField = oldFieldMap.get(item);
                    if (null == oldField) {
                        log.info(dataBaseName + "的" + tableName + "新增了字段==>" + item);
                        return;
                    }
                    Map<String, String> newField = newFieldMap.get(item);
                    if (null == newField) {
                        log.info(dataBaseName + "的" + tableName + "删除了字段==>" + item);
                        return;
                    }
                    StringBuilder str = new StringBuilder();
                    if (!oldField.get("fieldType").equals(newField.get("fieldType"))) {
                        str.append("字段类型变化：")
                                .append(oldField.get("fieldType"))
                                .append("==>")
                                .append(newField.get("fieldType"));
                    }
                    if (!oldField.get("fieldSize").equals(newField.get("fieldSize"))) {
                        str.append("字段大小变化：")
                                .append(oldField.get("fieldSize"))
                                .append("==>")
                                .append(newField.get("fieldSize"));
                    }
                    if (!oldField.get("fieldDigits").equals(newField.get("fieldDigits"))) {
                        str.append("字段小数变化：")
                                .append(oldField.get("fieldDigits"))
                                .append("==>")
                                .append(newField.get("fieldDigits"));
                    }
                    if (!oldField.get("fieldIsNull").equals(newField.get("fieldIsNull"))) {
                        str.append("字段判空变化：")
                                .append(oldField.get("fieldIsNull"))
                                .append("==>")
                                .append(newField.get("fieldIsNull"));
                    }
                    if (!oldField.get("fieldRemarks").equals(newField.get("fieldRemarks"))) {
                        str.append("字段备注变化：")
                                .append(oldField.get("fieldRemarks"))
                                .append("==>")
                                .append(newField.get("fieldRemarks"));
                    }
                    if (str.length() > 0) {
                        log.info(dataBaseName + "的" + tableName + "的" + item + "字段有变化");
                        log.info(str.toString());
                    }
                });
            }
        });
        log.info("程序结束执行↓" + LocalDateTime.now());
    }

    public void getData(DatabaseMetaData newDbMetaData, DatabaseMetaData oldDbMetaData) throws SQLException {
        //从元数据中获取到所有的表名
        ResultSet newRs = newDbMetaData.getColumns(null, "%", "%", "%");
        while (newRs.next()) {
            String dataBaseName = newRs.getString("TABLE_CAT");
            String tableName = newRs.getString("TABLE_NAME");
            String fieldName = newRs.getString("COLUMN_NAME");
            Map<String, String> field = new HashMap<>();
            field.put("fieldType", newRs.getString("TYPE_NAME"));
            field.put("fieldSize", newRs.getString("COLUMN_SIZE"));
            field.put("fieldDigits", newRs.getString("DECIMAL_DIGITS") == null ? "" : newRs.getString("DECIMAL_DIGITS"));
            field.put("fieldIsNull", newRs.getString("NULLABLE"));
            field.put("fieldRemarks", newRs.getString("REMARKS"));
            newDataBaseSet.add(dataBaseName);
            Set<String> tables = newDataBaseMap.get(dataBaseName);
            if (null == tables) {
                tables = new HashSet<>();
                tables.add(tableName);
                newDataBaseMap.put(dataBaseName, tables);
            } else {
                newDataBaseMap.get(dataBaseName).add(tableName);
            }

            Map<String, Map<String, String>> fieldMap = newTableMap.get(dataBaseName + "." + tableName);
            if (null == fieldMap) {
                fieldMap = new HashMap<>();
                fieldMap.put(fieldName, field);
                newTableMap.put(dataBaseName + "." + tableName, fieldMap);
            } else {
                newTableMap.get(dataBaseName + "." + tableName).put(fieldName, field);
            }
        }

        //从元数据中获取到所有的表名
        ResultSet oldRs = oldDbMetaData.getColumns(null, "%", "%", "%");
        while (oldRs.next()) {
            String dataBaseName = oldRs.getString("TABLE_CAT");
            String tableName = oldRs.getString("TABLE_NAME");
            String fieldName = oldRs.getString("COLUMN_NAME");
            Map<String, String> field = new HashMap<>();
            field.put("fieldType", oldRs.getString("TYPE_NAME"));
            field.put("fieldSize", oldRs.getString("COLUMN_SIZE"));
            field.put("fieldDigits", oldRs.getString("DECIMAL_DIGITS") == null ? "" : oldRs.getString("DECIMAL_DIGITS"));
            field.put("fieldIsNull", oldRs.getString("NULLABLE"));
            field.put("fieldRemarks", oldRs.getString("REMARKS"));
            oldDataBaseSet.add(dataBaseName);
            Set<String> tables = oldDataBaseMap.get(dataBaseName);
            if (null == tables) {
                tables = new HashSet<>();
                tables.add(tableName);
                oldDataBaseMap.put(dataBaseName, tables);
            } else {
                oldDataBaseMap.get(dataBaseName).add(tableName);
            }

            Map<String, Map<String, String>> fieldMap = oldTableMap.get(dataBaseName + "." + tableName);
            if (null == fieldMap) {
                fieldMap = new HashMap<>();
                fieldMap.put(fieldName, field);
                oldTableMap.put(dataBaseName + "." + tableName, fieldMap);
            } else {
                oldTableMap.get(dataBaseName + "." + tableName).put(fieldName, field);
            }
        }
    }
}
