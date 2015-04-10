/*
 * Copyright 2015 Azazo
 *
 */
package net.joningi.coredata.sync.config;

import java.util.List;

public interface ConfigService {
    /**
     * Method to get the property value back as string.
     *
     * @param value The name of the property
     * @return The String value of the input property. If value is not found,
     * the function return null.
     */
    String getString(String value);

    /**
     * Method to get the property value back as boolean.
     * The boolean returned represents the value true if the string argument is
     * not null and is equal, ignoring case, to the string "true". I value is
     * not found the function returns false.
     *
     * @param key The name of the property
     * @return The boolean value of that property (false if not found)
     */
    Boolean getBoolean(String key);

    /**
     * Method to get the property value back as int.
     *
     * @param value The name of the property
     * @return The int value of that property
     * @throws NumberFormatException if the value behind the property is not
     *                               of type int. If value not found the function returns null.
     */
    Integer getInteger(String value);

    /**
     * @param value
     * @return
     */
    boolean contains(String key);

    Object get(String value);

    @SuppressWarnings("unchecked")
    <T> List<T> getAsList(String value);
}
