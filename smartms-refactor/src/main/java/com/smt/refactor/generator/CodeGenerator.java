package com.smt.refactor.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

/**
 * 阶段 0 代码生成器：扫描双库全部业务表生成 Entity + Mapper。
 *
 * 用法（在项目根目录）：
 *   mvn compile exec:java -Dexec.mainClass=com.smt.refactor.generator.CodeGenerator -Dexec.args="conf"
 *   mvn compile exec:java -Dexec.mainClass=com.smt.refactor.generator.CodeGenerator -Dexec.args="biz"
 *
 * 或直接 IDE 运行 main(args)（args[0]=conf|biz，缺省同时生成两库）。
 */
public class CodeGenerator {

    private static final String URL_PREFIX = "jdbc:mysql://127.0.0.1:3306/";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final String BASE_PKG = "com.smt.refactor";

    /** 表名前缀：生成类名时剥离，但 @TableName 保留全名 */
    private static final String[] TABLE_PREFIXES = {"cc_core_", "pbx_", "base_", "crm_", "im_", "tbl_"};

    public static void main(String[] args) {
        boolean genConf = true, genBiz = true;
        if (args != null && args.length > 0) {
            genConf = "conf".equalsIgnoreCase(args[0]);
            genBiz = "biz".equalsIgnoreCase(args[0]);
        }
        if (genConf) {
            generate("smartconf", "conf", "生成配置库(smartconf)");
        }
        if (genBiz) {
            generate("smartcc", "biz", "生成业务库(smartcc)");
        }
    }

    private static void generate(String dbName, String dsTag, String desc) {
        System.out.println("========== " + desc + " ==========");
        String url = URL_PREFIX + dbName + "?characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&zeroDateTimeBehavior=convertToNull&allowPublicKeyRetrieval=true";
        String outputDir = System.getProperty("user.dir") + "/src/main/java";
        String xmlDir = System.getProperty("user.dir") + "/src/main/resources/mapper/" + dsTag;

        FastAutoGenerator.create(url, USER, PASSWORD)
                .globalConfig(builder -> builder
                        .author("smartms-refactor")
                        .outputDir(outputDir)
                        .disableOpenDir()
                        .commentDate("yyyy-MM-dd")
                )
                .packageConfig(builder -> builder
                        .parent(BASE_PKG)
                        .moduleName("")          // 扁平包，Entity 统一放 entity
                        .entity("entity")
                        .mapper("mapper")
                        .pathInfo(Collections.singletonMap(OutputFile.xml, xmlDir))
                )
                .strategyConfig(builder -> builder
                        .addInclude(".*")        // 全部表
                        .addTablePrefix(TABLE_PREFIXES)   // 类名剥离前缀
                        .entityBuilder()
                        .enableLombok()
                        .enableTableFieldAnnotation()     // 字段加 @TableField（保留原名）
                        .enableRemoveIsPrefix()
                        .formatFileName("%s")
                        .mapperBuilder()
                        .enableBaseResultMap()
                        .formatMapperFileName("%sMapper")
                        .formatXmlFileName("%sMapper")
                        .enableMapperAnnotation()          // 加 @Mapper
                )
                .templateConfig(builder -> builder
                        .service(null)      // 阶段 0 不生成 Service/Controller
                        .serviceImpl(null)
                        .controller(null)
                )
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
        System.out.println("========== " + desc + " 完成 ==========");
    }
}
