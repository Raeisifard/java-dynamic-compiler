package com.ceramic.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.springframework.stereotype.Component;

/**
 * @author: B_Raeisifard
 * @date: 16/November/2024
 * @mail: Behnam.Raeisifard@gmail.com
 */
@Component
public class CoreVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startPromise) throws Exception{
        System.out.println("Core verticle has been started!");
        startPromise.complete();
    }
}