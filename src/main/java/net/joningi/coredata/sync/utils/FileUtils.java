/*
 * Copyright 2015 Azazo
 *
 */
package net.joningi.coredata.sync.utils;

import java.nio.file.Path;

public class FileUtils {
    public static String removeFileEnding(final String name) {
        if(name.contains(".")) {
            return name.substring(0,name.lastIndexOf("."));
        } else {
            return name;
        }

    }

    public static boolean isTempFile(final Path fileName) {
        if(fileName.startsWith("~")) {
            return true;
        }
        return false;
    }
}
