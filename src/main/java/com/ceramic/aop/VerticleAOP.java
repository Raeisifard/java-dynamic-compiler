package com.ceramic.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * @author: B_Raeisifard
 * @date: 16/November/2024
 * @mail: Behnam.Raeisifard@gmail.com
 */
@Aspect
@Component
public class VerticleAOP {
    @Before("execution(* io.vertx.core.AbstractVerticle+.start(..))")
    public void beforeStart(JoinPoint joinPoint){
        System.out.println("Before method start in verticle "+joinPoint.getSignature());
    }
    @Before("execution(* com.ceramic.verticle.CoreVerticle.start(..))")
    public void beforeCoreStart(JoinPoint joinPoint){
        System.out.println("Before method start in verticle "+joinPoint.getSignature());
    }
    @After("execution(* io.vertx.core.AbstractVerticle+.start(..))")
    public void afterStart(JoinPoint joinPoint){
        System.out.println("After method start in verticle "+joinPoint.getSignature());
    }

}