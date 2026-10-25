package com.smt.refactor.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", 400);
        body.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getDefaultMessage()).findFirst().orElse("参数错误");
        Map<String, Object> body = new HashMap<>();
        body.put("code", 400);
        body.put("message", msg);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleDenied(AccessDeniedException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", 403);
        body.put("message", "无权限");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOther(Exception e) {
        e.printStackTrace();
        Map<String, Object> body = new HashMap<>();
        body.put("code", 500);
        body.put("message", "服务器内部错误");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
