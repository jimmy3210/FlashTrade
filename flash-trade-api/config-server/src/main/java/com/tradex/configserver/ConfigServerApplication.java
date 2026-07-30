package com.tradex.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * FlashTrade 配置中心
 * <p>
 * 使用 Spring Cloud Config Server 统一管理各环境配置。
 * 支持两种配置源：
 * <ul>
 *   <li><b>native（本地文件系统）</b>：开发/测试环境，配置文件位于 config-repo/ 目录</li>
 *   <li><b>git</b>：生产环境，配置文件托管在 Git 仓库（通过环境变量 CONFIG_GIT_URI 指定）</li>
 * </ul>
 * 启动端口：8888
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
