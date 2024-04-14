package icu.greenlemon.databasecompare.bizservice;

import com.alibaba.fastjson.JSONObject;
import icu.greenlemon.databasecompare.util.DataUtil;
import icu.greenlemon.databasecompare.util.R;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BizService {

    public R<JSONObject> queryDatabase() {
        JSONObject result = new JSONObject();
        result.put("add", DataUtil.addedDatabase);
        result.put("delete", DataUtil.deletedDatabase);
        return R.ok(result);
    }

    public R<JSONObject> queryTable() {
        JSONObject result = new JSONObject();
        List<JSONObject> addedTables = new ArrayList<>();
        DataUtil.addedTable.forEach((databaseName, tables) -> {
            JSONObject addedTable = new JSONObject();
            addedTable.put("databaseName", databaseName);
            addedTable.put("tables", tables);
            addedTables.add(addedTable);
        });
        List<JSONObject> deletedTables = new ArrayList<>();
        DataUtil.deletedTable.forEach((databaseName, tables) -> {
            JSONObject deletedTable = new JSONObject();
            deletedTable.put("databaseName", databaseName);
            deletedTable.put("tables", tables);
            deletedTables.add(deletedTable);
        });
        result.put("add", addedTables);
        result.put("delete", deletedTables);
        return R.ok(result);
    }

    public R<JSONObject> queryField() {
        JSONObject result = new JSONObject();
        List<JSONObject> added = new ArrayList<>();
        DataUtil.addedField.forEach((databaseName, tables) -> {
            List<JSONObject> addedFields = new ArrayList<>();
            tables.forEach((tableName, fields) -> {
                JSONObject addedField = new JSONObject();
                addedField.put("tableName", tableName);
                addedField.put("fields", fields);
                addedFields.add(addedField);
            });
            JSONObject addedTable = new JSONObject();
            addedTable.put("databaseName", databaseName);
            addedTable.put("tables", addedFields);
            added.add(addedTable);
        });
        List<JSONObject> deleted = new ArrayList<>();
        DataUtil.deletedField.forEach((databaseName, tables) -> {
            List<JSONObject> deletedFields = new ArrayList<>();
            tables.forEach((tableName, fields) -> {
                JSONObject deletedField = new JSONObject();
                deletedField.put("tableName", tableName);
                deletedField.put("fields", fields);
                deletedFields.add(deletedField);
            });
            JSONObject deletedTable = new JSONObject();
            deletedTable.put("databaseName", databaseName);
            deletedTable.put("tables", deletedFields);
            deleted.add(deletedTable);
        });
        List<JSONObject> modified = new ArrayList<>();
        DataUtil.modifiedDatabaseAllDataMap.forEach((databaseName, tables) -> {
            List<JSONObject> modifiedFields = new ArrayList<>();
            tables.forEach((tableName, fields) -> {
                List<JSONObject> modifiedProperties = new ArrayList<>();
                fields.forEach((fieldName, data) -> {
                    JSONObject modifiedProperty = new JSONObject();
                    modifiedProperty.put("fieldName", fieldName);
                    modifiedProperty.put("data", data);
                    modifiedProperties.add(modifiedProperty);
                });
                JSONObject modifiedField = new JSONObject();
                modifiedField.put("tableName", tableName);
                modifiedField.put("fields", modifiedProperties);
                modifiedFields.add(modifiedField);
            });
            JSONObject modifiedTable = new JSONObject();
            modifiedTable.put("databaseName", databaseName);
            modifiedTable.put("tables", modifiedFields);
            modified.add(modifiedTable);
        });
        result.put("add", added);
        result.put("delete", deleted);
        result.put("modify", modified);
        return R.ok(result);
    }
}
