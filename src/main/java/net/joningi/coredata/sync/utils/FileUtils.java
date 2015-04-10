/*
 * Copyright 2015 Azazo
 *
 */
package net.joningi.coredata.sync.utils;

public class FileUtils {
    public static String removeFileEnding(final String name) {
        if(name.contains(".")) {
            return name.substring(0,name.lastIndexOf("."));
        } else {
            return name;
        }

    }
}
