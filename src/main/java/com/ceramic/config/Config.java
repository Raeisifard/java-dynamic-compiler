package com.ceramic.config;

import com.ceramic.compiler.compiler.DynamicCompiler;
import io.vertx.core.Vertx;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.EnableLoadTimeWeaving;

/**
 * @author: B_Raeisifard
 * @date: 09/November/2024
 * @mail: Behnam.Raeisifard@gmail.com
 */

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = false)
@EnableLoadTimeWeaving
public class Config {
    @Bean
    public DynamicCompiler dynamicCompiler() {
        return new DynamicCompiler(classLoader());
    }

    @Bean
    public Vertx vertx() {
        return Vertx.vertx();
    }

    @Bean
    public ClassLoader classLoader() {
        return getClass().getClassLoader();
    }
}