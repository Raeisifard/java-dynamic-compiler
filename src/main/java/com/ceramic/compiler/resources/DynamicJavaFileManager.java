package com.ceramic.compiler.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.List;

public class DynamicJavaFileManager extends MemoryFileManager {
    private static final Logger log = LoggerFactory.getLogger(DynamicJavaFileManager.class);
    private final DynamicClassLoader classLoader;
    private final List<MemoryByteCode> byteCodes = new ArrayList<>();


    public DynamicJavaFileManager(JavaFileManager fileManager, DynamicClassLoader classLoader) {
        super(fileManager, classLoader);
        this.classLoader = classLoader;

    }


    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {

       /*
        LocationAndKind key = new LocationAndKind(location, kind);
        if (classLoader.getRamFileSystem().containsKey(key)) {
            JavaFileObject  javaFileObject = classLoader.getRamFileSystem().get(key).get(className);
            if (javaFileObject != null) {
                return javaFileObject;
            }
        }
*/

        for (MemoryByteCode byteCode : byteCodes) {
            if (byteCode.getClassName().equals(className)) {
                return byteCode;
            }
        }

        try {
            MemoryByteCode innerClass = new MemoryByteCode(className);
            byteCodes.add(innerClass);
            classLoader.registerCompiledSource(innerClass);
            return innerClass;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }


        return null;
    }


    @Override
    public ClassLoader getClassLoader(Location location) {
        return classLoader;
    }
}
