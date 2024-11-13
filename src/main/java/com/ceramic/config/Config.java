package com.ceramic.config;

import com.ceramic.compiler.compiler.DynamicCompiler;
import io.vertx.core.Vertx;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: B_Raeisifard
 * @date: 09/November/2024
 * @mail: Behnam.Raeisifard@gmail.com
 */

@Configuration
//@EnableAspectJAutoProxy
public class Config {
    @Bean
    public DynamicCompiler dynamicCompiler() {
        return new DynamicCompiler();
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