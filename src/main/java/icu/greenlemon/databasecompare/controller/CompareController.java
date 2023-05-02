package icu.greenlemon.databasecompare.controller;

import com.alibaba.fastjson2.JSONObject;
import icu.greenlemon.databasecompare.bizservice.BizService;
import icu.greenlemon.databasecompare.util.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName: CompareController
 * @Author: ChenYue
 * @Date: 2023/04/24
 */
@RestController
@RequestMapping("/compare")
public class CompareController {

    @Resource
    private BizService bizService;

    @GetMapping("/getDatabaseDifference")
    public R<JSONObject> getDatabaseDifference(){
        return bizService.getDatabaseDifference();
    }

    @GetMapping("/getTableDifference")
    public R<List<JSONObject>> getTableDifference(){
        return bizService.getTableDifference();
    }

    @GetMapping("/getFieldDifference")
    public R<List<JSONObject>> getFieldDifference(){
        return bizService.getFieldDifference();
    }

    @PostMapping("/overloadAndCompare")
    public R<Boolean> overloadAndCompare(){
        return bizService.overloadAndCompare();
    }
}
