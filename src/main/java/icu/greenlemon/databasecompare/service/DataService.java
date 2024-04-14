package icu.greenlemon.databasecompare.service;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import icu.greenlemon.databasecompare.util.DataUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.*;

/**
 * @ClassName: CommonService
 * @Author: ChenYue
 * @Date: 2022/11/01
 */
@Service
@Slf4j
public class DataService {
    @Value("${sourceDatabase.host}")
    private String sourceDatabaseHost;
    @Value("${sourceDatabase.port}")
    private Integer sourceDatabasePort;
    @Value("${sourceDatabase.user}")
    private String sourceDatabaseUser;
    @Value("${sourceDatabase.password}")
    private String sourceDatabasePassword;
    @Value("${sourceDatabase.useSshTunnel}")
    private Boolean sourceUseSshTunnel;
    @Value("${sourceDatabase.sshHost}")
    private String sourceSshHost;
    @Value("${sourceDatabase.sshUser}")
    private String sourceSshUser;
    @Value("${sourceDatabase.sshPassword}")
    private String sourceSshPassword;
    @Value("${sourceDatabase.sshPort}")
    private Integer sourceSshPort;

    @Value("${targetDatabase.host}")
    private String targetDatabaseHost;
    @Value("${targetDatabase.port}")
    private Integer targetDatabasePort;
    @Value("${targetDatabase.user}")
    private String targetDatabaseUser;
    @Value("${targetDatabase.password}")
    private String targetDatabasePassword;
    @Value("${targetDatabase.useSshTunnel}")
    private Boolean targetUseSshTunnel;
    @Value("${targetDatabase.sshHost}")
    private String targetSshHost;
    @Value("${targetDatabase.sshUser}")
    private String targetSshUser;
    @Value("${targetDatabase.sshPassword}")
    private String targetSshPassword;
    @Value("${targetDatabase.sshPort}")
    private Integer targetSshPort;
    private static final List<String> excludeDatabase = Arrays.asList("performance_schema", "information_schema", "sys", "mysql");
    private static boolean isInitializedSshTunnel = false;

    @SneakyThrows
    @PostConstruct
    public void init() {
        log.info("==>开始初始化");
        String sourceDatabaseUrl = "jdbc:mysql://" + sourceDatabaseHost + ":" + sourceDatabasePort + "/";
        String targetDatabaseUrl = "jdbc:mysql://" + targetDatabaseHost + ":" + targetDatabasePort + "/";
        if (!isInitializedSshTunnel) {
            if (targetUseSshTunnel) {
                JSch targetJSch = new JSch();
                Session tagetSession = targetJSch.getSession(targetSshUser, targetSshHost, targetSshPort);
                tagetSession.setPassword(targetSshPassword);
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                tagetSession.setConfig(config);
                tagetSession.connect();
                tagetSession.setPortForwardingL(targetDatabasePort, targetDatabaseHost, targetDatabasePort);
            }
            if (sourceUseSshTunnel) {
                JSch sourceJSch = new JSch();
                Session sourceSession = sourceJSch.getSession(sourceSshUser, sourceSshHost, sourceSshPort);
                sourceSession.setPassword(sourceSshPassword);
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                sourceSession.setConfig(config);
                sourceSession.connect();
                sourceSession.setPortForwardingL(sourceDatabasePort, sourceDatabaseHost, sourceDatabasePort);
            }
            isInitializedSshTunnel = true;
        }
        Class.forName("com.mysql.cj.jdbc.Driver");
        log.info("==>开始建立源数据连接");
        Connection sourceDatabaseConnection = DriverManager.getConnection(sourceDatabaseUrl, sourceDatabaseUser, sourceDatabasePassword);
        log.info("==>源数据连接建立成功");
        log.info("==>开始建立目标数据连接");
        Connection tagetDatabaseConnection = DriverManager.getConnection(targetDatabaseUrl, targetDatabaseUser, targetDatabasePassword);
        log.info("==>目标数据连接建立成功");
        log.info("==>数据连接建立成功");
        log.info("==>开始获取源数据");
        DataUtil.sourceDatabaseAllDataMap = getAllData(sourceDatabaseConnection);
        log.info("==>源数据获取完成");
        log.info("==>开始获取目标数据");
        DataUtil.tagetDatabaseAllDataMap = getAllData(tagetDatabaseConnection);
        log.info("==>目标数据获取成功");
        sourceDatabaseConnection.close();
        tagetDatabaseConnection.close();
        log.info("==>开始处理数据库");
        databaseHandle();
        log.info("==>数据库处理完成");
        log.info("==>开始处理表");
        tableHandle();
        log.info("==>表处理完成");
        log.info("==>开始处理字段");
        fieldHandle();
        log.info("==>字段处理完成");
        log.info("初始化完成");
    }

    private void databaseHandle() {
        Set<String> sourceDatabaseSet = new HashSet<>(DataUtil.sourceDatabaseAllDataMap.keySet());
        Set<String> targetDatabaseSet = new HashSet<>(DataUtil.tagetDatabaseAllDataMap.keySet());

        Set<String> bothDatabaseSet = DataUtil.getBothSet(sourceDatabaseSet, targetDatabaseSet);

        bothDatabaseSet.forEach(sourceDatabaseSet::remove);
        bothDatabaseSet.forEach(targetDatabaseSet::remove);

        DataUtil.deletedDatabase = targetDatabaseSet;
        DataUtil.bothDatabase = bothDatabaseSet;
        DataUtil.addedDatabase = sourceDatabaseSet;
    }

    private void tableHandle() {
        DataUtil.bothDatabase.forEach(databaseName -> {
            Map<String, Map<String, Map<String, String>>> sourceTableMap = DataUtil.sourceDatabaseAllDataMap.get(databaseName);
            Map<String, Map<String, Map<String, String>>> tagetTableMap = DataUtil.tagetDatabaseAllDataMap.get(databaseName);

            Set<String> sourceTableSet = new HashSet<>(sourceTableMap.keySet());
            Set<String> targetTableSet = new HashSet<>(tagetTableMap.keySet());

            Set<String> bothTableSet = DataUtil.getBothSet(sourceTableSet, targetTableSet);

            bothTableSet.forEach(sourceTableSet::remove);
            bothTableSet.forEach(targetTableSet::remove);

            if (!targetTableSet.isEmpty()) {
                DataUtil.deletedTable.put(databaseName, targetTableSet);
            }
            if (!sourceTableSet.isEmpty()) {
                DataUtil.addedTable.put(databaseName, sourceTableSet);
            }
            if (!sourceTableSet.isEmpty()) {
                DataUtil.bothTable.put(databaseName, bothTableSet);
            }
        });
    }

    public void fieldHandle() {
        DataUtil.bothTable.forEach((databaseName, tableSet) -> {
            tableSet.forEach(tableName -> {
                Map<String, Map<String, String>> sourceFieldMap = DataUtil.sourceDatabaseAllDataMap.get(databaseName).get(tableName);
                Map<String, Map<String, String>> tagetFieldMap = DataUtil.tagetDatabaseAllDataMap.get(databaseName).get(tableName);

                Set<String> sourceFieldSet = new HashSet<>(sourceFieldMap.keySet());
                Set<String> targetFieldSet = new HashSet<>(tagetFieldMap.keySet());

                Set<String> bothFieldSet = DataUtil.getBothSet(sourceFieldSet, targetFieldSet);

                bothFieldSet.forEach(sourceFieldSet::remove);
                bothFieldSet.forEach(targetFieldSet::remove);

                if (!sourceFieldSet.isEmpty()) {
                    Map<String, Set<String>> addedFieldMap = DataUtil.addedField.getOrDefault(databaseName, new HashMap<>());
                    addedFieldMap.put(tableName, sourceFieldSet);
                    DataUtil.addedField.put(databaseName, addedFieldMap);
                }
                if (!targetFieldSet.isEmpty()) {
                    Map<String, Set<String>> deletedFieldMap = DataUtil.deletedField.getOrDefault(databaseName, new HashMap<>());
                    deletedFieldMap.put(tableName, targetFieldSet);
                    DataUtil.deletedField.put(databaseName, deletedFieldMap);
                }

                bothFieldSet.forEach(fieldName -> {
                    Map<String, String> sourcePropertiesMap = sourceFieldMap.get(fieldName);
                    Map<String, String> targetPropertiesMap = tagetFieldMap.get(fieldName);
                    sourcePropertiesMap.forEach((key, sourceValue) -> {
                        String targetValue = targetPropertiesMap.get(key);
                        if (!sourceValue.equals(targetValue)) {
                            Map<String, Map<String, Map<String, String>>> tableMap = DataUtil.modifiedDatabaseAllDataMap.getOrDefault(databaseName, new HashMap<>());
                            Map<String, Map<String, String>> fieldMap = tableMap.getOrDefault(tableName, new HashMap<>());
                            Map<String, String> propertiesMap = fieldMap.getOrDefault(fieldName, new HashMap<>());
                            propertiesMap.put("after" + key, sourceValue);
                            propertiesMap.put("before" + key, targetValue);
                            fieldMap.put(fieldName, propertiesMap);
                            tableMap.put(tableName, fieldMap);
                            DataUtil.modifiedDatabaseAllDataMap.put(databaseName, tableMap);
                        }
                    });
                });
            });
        });
    }

    @SneakyThrows
    private Map<String, Map<String, Map<String, Map<String, String>>>> getAllData(Connection connection) {
        Map<String, Map<String, Map<String, Map<String, String>>>> allDataMap = new HashMap<>();
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet newRs = metaData.getColumns(null, "%", "%", "%");
        while (newRs.next()) {
            String databaseName = newRs.getString("TABLE_CAT");
            if (!excludeDatabase.contains(databaseName)) {
                String tableName = newRs.getString("TABLE_NAME");
                String fieldName = newRs.getString("COLUMN_NAME");
                Map<String, Map<String, Map<String, String>>> tableMap = allDataMap.getOrDefault(databaseName, new HashMap<>());
                Map<String, Map<String, String>> fieldMap = tableMap.getOrDefault(tableName, new HashMap<>());
                Map<String, String> propertiesMap = fieldMap.getOrDefault(fieldName, new HashMap<>());
                propertiesMap.put("FieldType", newRs.getString("TYPE_NAME"));
                propertiesMap.put("FieldSize", newRs.getString("COLUMN_SIZE"));
                propertiesMap.put("FieldDigits", newRs.getString("DECIMAL_DIGITS") == null ? "" : newRs.getString("DECIMAL_DIGITS"));
                propertiesMap.put("FieldIsNull", newRs.getString("NULLABLE"));
                propertiesMap.put("FieldRemarks", newRs.getString("REMARKS"));
                fieldMap.put(fieldName, propertiesMap);
                tableMap.put(tableName, fieldMap);
                allDataMap.put(databaseName, tableMap);
            }
        }
        return allDataMap;
    }
}
