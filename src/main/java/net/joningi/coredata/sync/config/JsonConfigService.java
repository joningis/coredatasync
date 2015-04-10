/*
 * Copyright 2015 Azazo
 *
 */
package net.joningi.coredata.sync.config;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class JsonConfigService implements ConfigService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonConfigService.class);
    private static final String DEFAULT_CONFIG_FILE = "Settings.json";
    private Map data;

    public JsonConfigService() throws IOException, ConfigException {
        this(DEFAULT_CONFIG_FILE);
    }

    public JsonConfigService(final String filename) throws IOException, ConfigException {

        final Gson gson = new Gson();

        try {
            URL resource = getClass().getResource("/" + filename);
            if (resource == null) {
                throw new IOException("The input json file does not exist");
            }
            Path path = Paths.get(resource.toURI());

            this.data = gson.fromJson(new InputStreamReader(new FileInputStream(path.toFile()),
                    Charset.forName("UTF-8")), Map.class);
        } catch (IOException ex) {
            LOGGER.error("Unable to read json config file: " + ExceptionUtils.getStackTrace(ex));
            throw ex;
        } catch (JsonSyntaxException e) {
            LOGGER.error("The config file is not a valid json file: "
                    + ExceptionUtils.getStackTrace(e));
            throw e;
        } catch (URISyntaxException e) {
            LOGGER.error("Unable to locate the json config file: "
                    + ExceptionUtils.getStackTrace(e));
            throw new ConfigException("Unable to locate the json config file", e);
        }
    }

    @Override
    public String getString(final String value) {
        if (this.data.containsKey(value)) {
            return this.data.get(value).toString();
        }
        return null;
    }

    /**
     * Function to get boolean value for key from the a config file.
     * If the key is not found false is returned
     *
     * @param key The name of the property
     * @return true if key is set to true in config file.
     * false otherwise.
     */
    @Override
    public Boolean getBoolean(final String key) {
        return this.data.containsKey(key) && this.data.get(key).toString().equalsIgnoreCase("true");
    }

    @Override
    public Integer getInteger(final String value) {
        if (this.data.containsKey(value)) {
            return Integer.valueOf(this.data.get(value).toString());
        }
        return null;
    }

    @Override
    public boolean contains(final String key) {
        return this.data.containsKey(key);
    }

    @Override
    public Object get(final String value) {
        return this.data.get(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getAsList(final String value) {
        return (List<T>) this.data.get(value);
    }
}
