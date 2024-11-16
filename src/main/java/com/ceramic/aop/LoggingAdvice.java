package com.ceramic.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: B_Raeisifard
 * @date: 16/November/2024
 * @mail: Behnam.Raeisifard@gmail.com
 */

public class LoggingAdvice  implements MethodInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAdvice.class);
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        logger.info("Before executing: " + invocation.getMethod().getName());
        Object result = invocation.proceed(); // Proceed to the original method
        logger.info("After executing: " + invocation.getMethod().getName());
        return result;
    }
}