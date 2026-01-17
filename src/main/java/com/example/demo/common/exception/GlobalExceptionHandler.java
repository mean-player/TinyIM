package com.example.demo.common.exception;

import com.example.demo.DTO.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<String> handleValid(MethodArgumentNotValidException e){
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ":" + f.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数校验异常: {}", msg);
        return ApiResponse.fail(400, msg);
    }

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<String> handleBusiness(BusinessException e){
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<String> handleRuntime(RuntimeException e){
        log.error("运行时异常: ", e);
        return ApiResponse.fail(500, "服务器内部错误");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<String> handleException(Exception e){
        log.error("未知异常: ", e);
        return ApiResponse.fail(500, "未知错误");
    }
}
