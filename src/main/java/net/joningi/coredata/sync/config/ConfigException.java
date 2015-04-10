/*
 * Copyright 2015 Azazo
 *
 */
package net.joningi.coredata.sync.config;

public class ConfigException extends Exception {
    public ConfigException(final String msg) {
        super(msg);
    }

    public ConfigException(final String msg, final Exception exception) {
        super(msg, exception);
    }
}
