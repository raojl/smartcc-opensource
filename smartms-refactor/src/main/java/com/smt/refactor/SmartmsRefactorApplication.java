package com.smt.refactor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SmartCC 重构启动类（阶段 0 基建）
 */
@SpringBootApplication
@MapperScan("com.smt.refactor.mapper")
public class SmartmsRefactorApplication {

    public static void main(String[] args) {
        // 兜底默认端口：部分沙箱/CI 环境会注入 SERVER__PORT=0（Spring 宽松绑定为 server.port=0 即随机端口），
        // 系统属性优先级高于环境变量，可覆盖之；生产可用 --server.port 或 -Dserver.port 覆盖本默认值。
        if (System.getProperty("server.port") == null) {
            System.setProperty("server.port", "8080");
        }
        SpringApplication.run(SmartmsRefactorApplication.class, args);
    }
}
