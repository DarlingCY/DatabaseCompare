package icu.greenlemon.databasecompare.handler;

import icu.greenlemon.databasecompare.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @ClassName: GlobalExceptionHandler
 * @Author: ChenYue
 * @Date: 2023/05/02
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public <T> R<T> exceptionHandler(Exception e) {
        log.error(e.getMessage(), e);
        return R.failed("全局异常捕获,错误原因>>>" + e.getMessage());
    }
}
