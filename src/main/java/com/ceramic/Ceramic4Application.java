package com.ceramic;

import com.ceramic.aop.VerticleAOP;
import com.ceramic.compiler.compiler.DynamicCompiler;
import com.ceramic.compiler.compiler.DynamicCompilerOption;
import com.ceramic.compiler.exceptions.ArgumentNullException;
import com.ceramic.compiler.exceptions.DynamicCompilerException;
import com.ceramic.config.Config;
import com.ceramic.verticle.CoreVerticle;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.util.Map;

@SpringBootApplication
public class Ceramic4Application implements CommandLineRunner {
    @Autowired
    VerticleAOP verticleAOP;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private Vertx vertx;
    @Autowired
    private DynamicCompiler dynamicCompiler;
    @Autowired
    private ClassLoader classLoader;
    @Autowired
    private CoreVerticle coreVerticle;

    public static void main(String[] args) {
        SpringApplication.run(Ceramic4Application.class, args);
    }

    @PostConstruct
    public void deployVerticle() throws DynamicCompilerException, ArgumentNullException, InstantiationException, IllegalAccessException {

    }

    @Override
    public void run(String... args) throws Exception {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.setParent(applicationContext);
        //AnnotationConfigServletWebServerApplicationContext ctx =  applicationContext;
        //Vertx vertx = Vertx.vertx();
        //vertx.deployVerticle(coreVerticle);
        File f = new File("temp/v1234567890/SampleVerticle.java");
        //URL url = classLoader.getResource(f.getAbsolutePath());
        //assert url != null;

        dynamicCompiler.setClasspath(System.getProperty("java.class.path"));
        dynamicCompiler.addCompilerOption(DynamicCompilerOption.ADD_EXPORTS, "java.base/java.lang.invoke=ALL-UNNAMED");
        dynamicCompiler.addCompilerOption(DynamicCompilerOption.ADD_EXPORTS, "java.base/java.lang.reflect=ALL-UNNAMED");
        dynamicCompiler.addCompilerOption(DynamicCompilerOption.ADD_EXPORTS, "java.base/java.util.concurrent=ALL-UNNAMED");
        dynamicCompiler.addCompilerOption(DynamicCompilerOption.ADD_EXPORTS, "java.base/java.util.stream=ALL-UNNAMED");
        dynamicCompiler.addCompilerOption(DynamicCompilerOption.ADD_EXPORTS, "java.base/java.nio=ALL-UNNAMED");
        dynamicCompiler.addCompilerOption(DynamicCompilerOption.ADD_EXPORTS, "java.base/java.nio.file=ALL-UNNAMED");
        dynamicCompiler.addCompilerOption(DynamicCompilerOption.ADD_EXPORTS, "java.base/java.security=ALL-UNNAMED");
        dynamicCompiler.addCompilerOption(DynamicCompilerOption.ADD_EXPORTS, "java.base/java.lang.annotation=ALL-UNNAMED");
        dynamicCompiler.addSource(f);
        //dynamicCompiler.setProcessors(Arrays.asList(new ConfigurationMetadataAnnotationProcessor()));
        Map<String, Class<?>> compiled = dynamicCompiler.build();
        Class<?> JavaFileClass = compiled.get("v1234567890.SampleVerticle");
        ctx.register(Config.class);
        ctx.register(JavaFileClass);

        //ProxyFactory factory = new ProxyFactory(JavaFileClass);
        //factory.addAdvice(new LoggingAdvice());
        //factory.getProxy();
        ctx.registerAlias(Character.toLowerCase(JavaFileClass.getSimpleName().charAt(0)) + JavaFileClass.getSimpleName().substring(1), JavaFileClass.getSimpleName());
        ctx.refresh();
        Verticle verticle = (Verticle) ctx.getBean(JavaFileClass.getSimpleName());
        vertx.deployVerticle(verticle).onComplete(res -> {
            if (res.succeeded()) {
                System.out.println("Deployment id is: " + res.result());
            } else {
                System.out.println("Deployment failed!");
            }
        });
    }
}
