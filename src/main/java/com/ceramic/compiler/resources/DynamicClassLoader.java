package com.ceramic.compiler.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.JavaFileObject;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class DynamicClassLoader extends URLClassLoader {
    private static final Logger log = LoggerFactory.getLogger(DynamicClassLoader.class);
    private final Map<LocationAndKind, Map<String, JavaFileObject>> ramFileSystem = new HashMap<>();
    private final Map<String, MemoryByteCode> byteCodes = new HashMap<>();
    private final boolean writeClassFile;

    public DynamicClassLoader(URL[] urls, ClassLoader classLoader, boolean writeClassFile) {
        super(urls, classLoader);
        this.writeClassFile = writeClassFile;
    }

    public void addURL(URL url) {
        super.addURL(url);
    }

    public void registerCompiledSource(MemoryByteCode byteCode) {
        byteCodes.put(byteCode.getClassName(), byteCode);
    }


    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        MemoryByteCode byteCode = byteCodes.get(name);
        if (byteCode == null) {
            return super.findClass(name);
        }

        if (writeClassFile) {
            writeClass(name, byteCode.getByteCode());
        }

        return super.defineClass(name, byteCode.getByteCode(), 0, byteCode.getByteCode().length);
    }

    private void writeClass(String name, byte[] byteCode) {
        try {
            final char PKG_SEPARATOR = '.';
            final char DIR_SEPARATOR = '/';
            final String CLASS_FILE_SUFFIX = ".class";
            String classFileName = name.replace(PKG_SEPARATOR, DIR_SEPARATOR) + CLASS_FILE_SUFFIX;
            URL ClassFilePath = this.getResource(classFileName);
            if (ClassFilePath != null) {
                FileOutputStream outputStream = new FileOutputStream(ClassFilePath.getFile());
                outputStream.write(byteCode);
                outputStream.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    public Map<String, Class<?>> getClasses() throws ClassNotFoundException {
        Map<String, Class<?>> classes = new HashMap<>();
        for (MemoryByteCode byteCode : byteCodes.values()) {
            classes.put(byteCode.getClassName(), findClass(byteCode.getClassName()));
        }
        return classes;
    }


    public Map<LocationAndKind, Map<String, JavaFileObject>> getRamFileSystem() {
        return ramFileSystem;
    }
}
