package icu.greenlemon.databasecompare.controller;

import com.alibaba.fastjson.JSONObject;
import icu.greenlemon.databasecompare.bizservice.BizService;
import icu.greenlemon.databasecompare.service.DataService;
import icu.greenlemon.databasecompare.util.R;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@CrossOrigin
@RequestMapping("/compare")
public class CompareController {

    @Resource
    private BizService bizService;
    @Resource
    private DataService dataService;

    @GetMapping("/reloadCompare")
    public R<Boolean> reload() {
        dataService.init();
        return R.ok(true);
    }

    @GetMapping("/queryDatabase")
    public R<JSONObject> queryDatabase() {
        return bizService.queryDatabase();
    }

    @GetMapping("/queryTable")
    public R<JSONObject> queryTable() {
        return bizService.queryTable();
    }

    @GetMapping("/queryField")
    public R<JSONObject> queryField() {
        return bizService.queryField();
    }
}
