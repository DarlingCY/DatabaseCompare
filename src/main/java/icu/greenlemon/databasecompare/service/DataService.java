package icu.greenlemon.databasecompare.service;

import icu.greenlemon.databasecompare.entity.DatabaseData;
import icu.greenlemon.databasecompare.util.DataUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.*;
import java.util.*;

/**
 * @ClassName: CommonService
 * @Author: ChenYue
 * @Date: 2022/11/01
 */
@Service
@Slf4j
public class DataService {
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
    private static final List<String> excludeDatabase = Arrays.asList("performance_schema", "information_schema", "sys", "mysql");

    @SneakyThrows
    @PostConstruct
    public void init() {
        //校验配置信息完整性
        checkConfig();
        log.info("开始读取元数据");
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection newDatabaseConnection = DriverManager.getConnection(newDatabaseUrl, newDatabaseUser, newDatabasePass);
        Connection oldDatabaseConnection = DriverManager.getConnection(oldDatabaseUrl, oldDatabaseUser, oldDatabasePass);
        DatabaseMetaData newMetaData = newDatabaseConnection.getMetaData();
        DatabaseMetaData oldMetaData = oldDatabaseConnection.getMetaData();
        //读取元数据
        DataUtil.newDatabaseData = getData(newMetaData);
        DataUtil.oldDatabaseData = getData(oldMetaData);
        //数据处理
        log.info("元数据处理完成");
        newDatabaseConnection.close();
        oldDatabaseConnection.close();
    }

    private DatabaseData getData(DatabaseMetaData databaseMetaData) throws SQLException {
        //从元数据中获取到所有的表名
        Map<String,Set<String>> databaseMap=new HashMap<>();
        Map<String, Map<String, Map<String, String>>> tableMap=new HashMap<>();
        ResultSet newRs = databaseMetaData.getColumns(null, "%", "%", "%");
        while (newRs.next()) {
            String dataBaseName = newRs.getString("TABLE_CAT");
            if (!excludeDatabase.contains(dataBaseName)) {
                String tableName = newRs.getString("TABLE_NAME");
                String fieldName = newRs.getString("COLUMN_NAME");
                Map<String, String> field = new HashMap<>();
                field.put("fieldType", newRs.getString("TYPE_NAME"));
                field.put("fieldSize", newRs.getString("COLUMN_SIZE"));
                field.put("fieldDigits", newRs.getString("DECIMAL_DIGITS") == null ? "" : newRs.getString("DECIMAL_DIGITS"));
                field.put("fieldIsNull", newRs.getString("NULLABLE"));
                field.put("fieldRemarks", newRs.getString("REMARKS"));
                field.put("fieldName", fieldName);
                Set<String> tables = databaseMap.get(dataBaseName);
                if (null == tables) {
                    tables = new HashSet<>();
                    tables.add(tableName);
                    databaseMap.put(dataBaseName, tables);
                } else {
                    databaseMap.get(dataBaseName).add(tableName);
                }

                Map<String, Map<String, String>> fieldMap = tableMap.get(dataBaseName + "." + tableName);
                if (null == fieldMap) {
                    fieldMap = new HashMap<>();
                    fieldMap.put(fieldName, field);
                    tableMap.put(dataBaseName + "." + tableName, fieldMap);
                } else {
                    tableMap.get(dataBaseName + "." + tableName).put(fieldName, field);
                }
            }
        }
        DatabaseData databaseData=new DatabaseData();
        databaseData.setDatabaseMap(databaseMap)
                .setTableMap(tableMap);
        return databaseData;
    }

    private void checkConfig() {
        if (StringUtils.isBlank(newDatabaseUrl) || StringUtils.isBlank(newDatabaseUser) || StringUtils.isBlank(newDatabasePass) || StringUtils.isBlank(oldDatabaseUrl) || StringUtils.isBlank(oldDatabaseUser) || StringUtils.isBlank(oldDatabasePass)) {
            throw new RuntimeException("配置信息不完整，请检查配置信息");
        }
    }
}
