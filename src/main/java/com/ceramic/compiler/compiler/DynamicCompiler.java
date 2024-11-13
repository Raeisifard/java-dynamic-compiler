package com.ceramic.compiler.compiler;

import com.ceramic.compiler.exceptions.ArgumentNullException;
import com.ceramic.compiler.exceptions.DynamicCompilerException;
import com.ceramic.compiler.exceptions.JavaCompilerNotFoundException;
import com.ceramic.compiler.loader.ClassPathBuilder;
import com.ceramic.compiler.resources.DynamicClassLoader;
import com.ceramic.compiler.resources.DynamicJavaFileManager;
import com.ceramic.compiler.resources.StringSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.Processor;
import javax.tools.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Stream;

public class DynamicCompiler {
    private static final Logger log = LoggerFactory.getLogger(DynamicCompiler.class);
    private final JavaCompiler javaCompiler;
    private final StandardJavaFileManager standardFileManager;
    private final List<String> options = new ArrayList<>();
    private final DynamicClassLoader classLoader;

    private final Collection<JavaFileObject> compilationUnits = new ArrayList<>();
    private final List<Diagnostic<? extends JavaFileObject>> errors = new ArrayList<>();
    private final List<Diagnostic<? extends JavaFileObject>> warnings = new ArrayList<>();
    private JavaCompiler.CompilationTask task;
    private List<Processor> processors;
    private String classpath;

    public DynamicCompiler() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public DynamicCompiler(ClassLoader classLoader) {
        this(classLoader, ToolProvider.getSystemJavaCompiler());
    }

    public DynamicCompiler(ClassLoader classLoader, JavaCompiler javaCompiler) {
        this(classLoader, javaCompiler, false);
    }

    public DynamicCompiler(ClassLoader classLoader, JavaCompiler javaCompiler, boolean writeClassFile) {

        if (javaCompiler == null) {
            throw new JavaCompilerNotFoundException("Java Compiler not found. JDK is required");
        }

        options.add("-Xlint:unchecked");
        URL[] urls = new URL[0];
        if (classLoader instanceof URLClassLoader) {
            this.classpath = new ClassPathBuilder().getClassPath((URLClassLoader) classLoader);
        } else {
            this.classpath = new ClassPathBuilder().getClassPath();
            urls = Stream.of(classpath.split(ClassPathBuilder.pathSeparator)).map(path -> {
                try {
                    return new URL(path);
                } catch (MalformedURLException ignored) {
                }
                return null;
            }).filter(Objects::nonNull).toArray(URL[]::new);
        }

        this.javaCompiler = javaCompiler;
        this.standardFileManager = javaCompiler.getStandardFileManager(null, null, null);
        this.classLoader = new DynamicClassLoader(urls, classLoader, writeClassFile);

    }

    public void addSource(String className, String source) {
        addSource(new StringSource(className, source));
    }

    public void addSource(File sourceFile) {
        Iterable<? extends JavaFileObject> compilationUnitsIterable =
                standardFileManager.getJavaFileObjectsFromFiles(Collections.singletonList(sourceFile));
        for (JavaFileObject javaFileObject : compilationUnitsIterable) {
            addSource(javaFileObject);

        }
    }

    public void addSource(JavaFileObject javaFileObject) {
        compilationUnits.add(javaFileObject);
    }

    /**
     * <p> possibility to add class and jar into classpath
     *
     * @param path String
     */
    public void addClasspath(URL path) throws Exception {
        if (path == null) {
            throw new ArgumentNullException("path is empty");
        }
        File file = new File(path.getFile());
        if (file.exists()) {
            classpath = classpath + ClassPathBuilder.pathSeparator + file.getAbsolutePath();
            log.debug(file.getAbsolutePath());
            this.classLoader.addURL(path);

        }

    }

    /**
     * <p> possibility to add javac options
     *
     * @param compilerOption      javac option key
     * @param compilerOptionVaule javac option value
     */
    public void addCompilerOption(DynamicCompilerOption compilerOption, String compilerOptionVaule) throws ArgumentNullException {
        if (compilerOption == null) {
            throw new ArgumentNullException("Option key is empty");
        }
        options.add(compilerOption.toString());
        options.add(compilerOptionVaule);
    }

    public static boolean isJava9OrAbove() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.8")) {
            return false;
        }
        int majorVersion = Integer.parseInt(version.split("\\.")[0]);
        return majorVersion >= 9;
    }

    public Map<String, Class<?>> build() throws DynamicCompilerException {

        errors.clear();
        warnings.clear();

        if (!classpath.trim().isEmpty()) {
            options.add("-classpath");
            options.add(classpath);
        }

        try {
            if (isJava9OrAbove()) {
                importBaseModule();
            }
        } catch (Exception ignored) {
        }

        JavaFileManager fileManager = new DynamicJavaFileManager(standardFileManager, classLoader);

        DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
        task = javaCompiler.getTask(null, fileManager, collector, options, null, compilationUnits);
        if (processors != null)
            task.setProcessors(processors);

        try {

            if (!compilationUnits.isEmpty()) {
                boolean result = task.call();

                if (!result || collector.getDiagnostics().size() > 0) {

                    for (Diagnostic<? extends JavaFileObject> diagnostic : collector.getDiagnostics()) {
                        switch (diagnostic.getKind()) {
                            case NOTE:
                            case MANDATORY_WARNING:
                            case WARNING:
                                warnings.add(diagnostic);
                                break;
                            case OTHER:
                            case ERROR:
                            default:
                                errors.add(diagnostic);
                                break;
                        }

                    }

                    if (!errors.isEmpty()) {
                        throw new DynamicCompilerException("Compilation Error", errors);
                    }
                }
            }

            return classLoader.getClasses();
        } catch (ClassFormatError | ClassNotFoundException e) {
            throw new DynamicCompilerException(e);
        } finally {
            compilationUnits.clear();

        }


    }

    private void importBaseModule() {
        options.add(DynamicCompilerOption.ADD_EXPORTS.toString());
        options.add("java.base/java.lang=ALL-UNNAMED");
        options.add(DynamicCompilerOption.ADD_EXPORTS.toString());
        options.add("java.base/java.util=ALL-UNNAMED");
        options.add(DynamicCompilerOption.ADD_EXPORTS.toString());
        options.add("java.base/java.io=ALL-UNNAMED");
        options.add(DynamicCompilerOption.ADD_EXPORTS.toString());
        options.add("java.base/java.net=ALL-UNNAMED");
       /* options.add(DynamicCompilerOption.ADD_EXPORTS.toString());
        options.add("java.base/java.lang.invoke=ALL-UNNAMED");
        options.add(DynamicCompilerOption.ADD_EXPORTS.toString());
        options.add("java.base/java.lang.reflect=ALL-UNNAMED");
        options.add(DynamicCompilerOption.ADD_EXPORTS.toString());
        options.add("java.base/java.util.concurrent=ALL-UNNAMED");
        options.add(DynamicCompilerOption.ADD_EXPORTS.toString());
        options.add("java.base/java.util.stream=ALL-UNNAMED");
        options.add(DynamicCompilerOption.ADD_EXPORTS.toString());
        options.add("java.base/java.nio=ALL-UNNAMED");
        options.add(DynamicCompilerOption.ADD_EXPORTS.toString());
        options.add("java.base/java.nio.file=ALL-UNNAMED");
        options.add(DynamicCompilerOption.ADD_EXPORTS.toString());
        options.add("java.base/java.security=ALL-UNNAMED");*/
    }


    private List<String> diagnosticToString(List<Diagnostic<? extends JavaFileObject>> diagnostics) {

        List<String> diagnosticMessages = new ArrayList<>();

        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
            diagnosticMessages.add("line: " + diagnostic.getLineNumber() + ", message: " + diagnostic.getMessage(Locale.US));
        }

        return diagnosticMessages;

    }

    public List<String> getErrors() {
        return diagnosticToString(errors);
    }

    public List<String> getWarnings() {
        return diagnosticToString(warnings);
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public String getClasspath() {
        return classpath;
    }

    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }

    public JavaCompiler.CompilationTask getTask() {
        return task;
    }

    public void setProcessors(List<Processor> processors) {
        this.processors = processors;
    }
}
