package icu.greenlemon.databasecompare.bizservice;

import com.alibaba.fastjson2.JSONObject;
import icu.greenlemon.databasecompare.service.DataService;
import icu.greenlemon.databasecompare.util.DataUtil;
import icu.greenlemon.databasecompare.util.R;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @ClassName: BizService
 * @Author: ChenYue
 * @Date: 2023/04/26
 */
@Service
public class BizService {

    @Resource
    private DataService dataService;

    public R<JSONObject> getDatabaseDifference() {
        Set<String> newDatabaseSet = new HashSet<>(DataUtil.newDatabaseMap.keySet());
        Set<String> oldDatabaseSet = new HashSet<>(DataUtil.oldDatabaseMap.keySet());
        List<String> bothContainsDatabase = getBothContainsDatabase(newDatabaseSet, oldDatabaseSet);
        bothContainsDatabase.forEach(newDatabaseSet::remove);
        bothContainsDatabase.forEach(oldDatabaseSet::remove);
        JSONObject result = new JSONObject();
        result.put("addedDatabase", newDatabaseSet);
        result.put("deletedDatabase", oldDatabaseSet);
        result.put("commonDatabase", bothContainsDatabase);
        return R.ok(result);
    }

    public R<List<JSONObject>> getTableDifference() {
        Set<String> newDatabaseSet = new HashSet<>(DataUtil.newDatabaseMap.keySet());
        Set<String> oldDatabaseSet = new HashSet<>(DataUtil.oldDatabaseMap.keySet());
        List<String> bothContainsDatabase = getBothContainsDatabase(newDatabaseSet, oldDatabaseSet);
        Map<String, List<String>> bothContainsTableMap = getBothContainsTableMap();
        List<JSONObject> resultList = new ArrayList<>();
        bothContainsDatabase.forEach(dataBase -> {
            //共同拥有的表
            List<String> bothContainsTable = bothContainsTableMap.get(dataBase);
            Set<String> newTables = new HashSet<>(DataUtil.newDatabaseMap.get(dataBase));
            Set<String> oldTables = new HashSet<>(DataUtil.oldDatabaseMap.get(dataBase));
            bothContainsTable.forEach(newTables::remove);
            bothContainsTable.forEach(oldTables::remove);
            if (newTables.isEmpty() && oldTables.isEmpty()) {
                return;
            }
            JSONObject result = new JSONObject();
            result.put("databaseName", dataBase);
            result.put("addedTable", newTables);
            result.put("deletedTable", oldTables);
            resultList.add(result);
        });
        return R.ok(resultList);
    }

    public R<List<JSONObject>> getFieldDifference() {
        List<JSONObject> databaseList = new ArrayList<>();
        Map<String, List<String>> bothContainsTableMap = getBothContainsTableMap();
        bothContainsTableMap.forEach((dataBaseName, bothContainsTable) -> {
            List<JSONObject> tableList = new ArrayList<>();
            bothContainsTable.forEach(table -> {
                List<JSONObject> addedFieldList = new ArrayList<>();
                List<JSONObject> deletedFieldList = new ArrayList<>();
                List<JSONObject> updateFieldList = new ArrayList<>();
                Map<String, Map<String, String>> newFieldMap = new HashMap<>(DataUtil.newTableMap.get(dataBaseName + "." + table));
                Map<String, Map<String, String>> oldFieldMap = new HashMap<>(DataUtil.oldTableMap.get(dataBaseName + "." + table));
                Set<String> keySet = new HashSet<>();
                keySet.addAll(oldFieldMap.keySet());
                keySet.addAll(newFieldMap.keySet());
                keySet.forEach(item -> {
                    Map<String, String> oldField = oldFieldMap.get(item);
                    if (null == oldField) {
                        JSONObject addedField = JSONObject.parseObject(JSONObject.toJSONString(newFieldMap.get(item)));
                        addedFieldList.add(addedField);
                        return;
                    }
                    Map<String, String> newField = newFieldMap.get(item);
                    if (null == newField) {
                        JSONObject deletedField = JSONObject.parseObject(JSONObject.toJSONString(oldFieldMap.get(item)));
                        deletedFieldList.add(deletedField);
                        return;
                    }
                    if (!oldField.equals(newField)) {
                        JSONObject updateObj = new JSONObject();
                        Set<String> keys = new HashSet<>(oldField.keySet());
                        keys.forEach(key -> {
                            if ("fieldName".equals(key)) {
                                return;
                            }
                            if (oldField.getOrDefault(key, "").equals(newField.getOrDefault(key, ""))) {
                                oldField.remove(key);
                                newField.remove(key);
                            }
                        });
                        updateObj.put("oldField", oldField);
                        updateObj.put("newField", newField);
                        updateFieldList.add(updateObj);
                    }
                });
                if (addedFieldList.isEmpty() && deletedFieldList.isEmpty() && updateFieldList.isEmpty()) {
                    return;
                }
                JSONObject tableObj = new JSONObject();
                tableObj.put("tableName", table);
                tableObj.put("addedField", addedFieldList);
                tableObj.put("deletedField", deletedFieldList);
                tableObj.put("updateField", updateFieldList);
                tableList.add(tableObj);
            });
            if (tableList.isEmpty()) {
                return;
            }
            JSONObject databaseObj = new JSONObject();
            databaseObj.put("databaseName", dataBaseName);
            databaseObj.put("table", tableList);
            databaseList.add(databaseObj);
        });
        return R.ok(databaseList);
    }

    private List<String> getBothContainsDatabase(Set<String> newDatabaseSet, Set<String> oldDatabaseSet) {
        List<String> bothContainsDatabase = new ArrayList<>();
        newDatabaseSet.forEach(item -> {
            if (oldDatabaseSet.contains(item)) {
                bothContainsDatabase.add(item);
            }
        });
        return bothContainsDatabase;
    }

    private Map<String, List<String>> getBothContainsTableMap() {
        //数据库_数据表
        Map<String, List<String>> bothContainsTableMap = new HashMap<>();
        //共同拥有的库
        List<String> bothContainsDatabase = getBothContainsDatabase(new HashSet<>(DataUtil.newDatabaseMap.keySet()), new HashSet<>(DataUtil.oldDatabaseMap.keySet()));
        bothContainsDatabase.forEach(dataBase -> {
            //共同拥有的表
            List<String> bothContainsTable = new ArrayList<>();
            Set<String> newTables = new HashSet<>(DataUtil.newDatabaseMap.get(dataBase));
            Set<String> oldTables = new HashSet<>(DataUtil.oldDatabaseMap.get(dataBase));
            newTables.forEach(table -> {
                if (oldTables.contains(table)) {
                    bothContainsTable.add(table);
                }
            });
            bothContainsTableMap.put(dataBase, bothContainsTable);
        });
        return bothContainsTableMap;
    }

    public R<Boolean> overloadAndCompare() {
        dataService.init();
        return R.ok(true);
    }
}
