package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
// 全局异常处理器，并且异常处理方法的返回值将以JSON格式直接写入响应体中
// 包括@ControllerAdvice(主要用来注解创建全局异常处理器)和@ResponseBody
// 集中处理所有控制器中抛出的异常，避免在每个方法中重复编写 try-catch
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 处理重复username异常   之所以能够返回json，是因为上面类前包含了@ResponseBody,会按键值对将对象变成json
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        // Duplicate entry 'zhangsan' for key 'employee.idx_username'
        String message = ex.getMessage();
        // 如果报错信息包含了Duplicate entry说明是这个异常
        if(message.contains("Duplicate entry")){
            String[] split = message.split(" ");
            // 提取出重复的username
            String username = split[2];

            // 将消息返回给前端  "已存在"
            String msg=username.substring(1,username.length()-1)+ MessageConstant.ALREADY_EXIST;
            return Result.error(msg);
        }else {
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }

}
