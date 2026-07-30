package com.tradex.flashtrade.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步执行器配置
 * <p>
 * 为券商 API 异步调用提供专用线程池，与业务线程池隔离，
 * 避免券商接口延迟影响主请求处理。
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 券商 API 专用线程池。
     * <ul>
     *   <li>核心线程数: 5</li>
     *   <li>最大线程数: 10</li>
     *   <li>队列容量: 100</li>
     *   <li>线程名前缀: broker-async-</li>
     * </ul>
     */
    @Bean(name = "brokerTaskExecutor")
    public Executor brokerTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("broker-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
