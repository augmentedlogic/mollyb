/**
 *
 * @copyright 2020 Wolfgang Hauptfleisch <dev@augmentedlogic.com>
 * Apache Licence Version 2.0
 * This file is part of mollyb
 *
 **/
package com.augmentedlogic.mollyb;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Properties;

/**
 * reads the configuration file when using the standalone service
 */
public class Configuration {

    private Pattern _section  = Pattern.compile("\\s*\\[([^]]*)\\]\\s*");
    private Pattern _keyValue = Pattern.compile("\\s*([^=]*)=(.*)");
    private Map<String, Map<String, String>> _entries = new HashMap<>();
    private int port = 1965;
    private String bind = "localhost";
    private String webroot = null;
    private String keystore = null;
    private String keystore_password = null;
    private String key_password = null;
    private String custom_not_found = null;
    private Boolean debug = false;

    /**
    * reads the configuration file when using the standalone service
    *
    * @param path path to the configuration file
    * @throws IOException if config file does not exist
    */
    public Configuration(String path) throws IOException {
        load(path);

        this.port = this.getInt("service", "port", 7777);
        this.bind = this.getString("service", "bind", "localhost");
        webroot = this.getString("service", "webroot", null);
        String access_log = this.getString("service", "access_log", null);
        int debug = this.getInt("service", "debug", 0);
        if(debug == 1) {
            this.debug = true;
        }
        String debug_log = this.getString("service", "debug_log", null);
        String error_log = this.getString("service", "error_log", null);
        String keystore = this.getString("security", "keystore", null);
        String keystore_password = this.getString("security", "password", null);
        String key_password = this.getString("security", "key_password", null);
        this.custom_not_found = this.getString("service", "not_found", null);

        Properties props = System.getProperties();

        // check if webroot is null
        if(webroot == null) {
            System.out.println("Webroot is not set. Not starting.");
            System.exit(0);
        }

        // keystore is required
        if(keystore == null) {
            System.out.println("No keyfile given. Not starting.");
            System.exit(0);
        } else {
            this.keystore = keystore;
            props.setProperty("mollyb.keystore", keystore);
        }

        // keystore password is required
        if(keystore_password == null) {
            System.out.println("No keystore password given. Not starting.");
            System.exit(0);
        } else {
            this.keystore_password = keystore_password;
            props.setProperty("mollyb.password", keystore_password);
        }

        // only if logfiles are set
        if(access_log != null) {
            props.setProperty("mollyb.access_log", access_log);
        }
        if(debug_log != null) {
            props.setProperty("mollyb.debuglog", debug_log);
        }
        if(error_log != null) {
            props.setProperty("mollyb.errorlog", error_log);
        }

    }

    /**
     * get the port set in the config file
     *
     * @return port as an integer
     **/
    public int getPort() {
        return this.port;
    }

    /**
     * returns the bind address set in the config file
     *
     * @return bind address to bind to as string
     **/
    public String getBind() {
        return this.bind;
    }

    /**
     * returns if debug is set in the config file
     *
     * @return debug debug status as boolean
     **/
    public Boolean getDebug() {
        return this.debug;
    }


    /**
     * returns the webroot directory set in the config file
     *
     * @return webroot the path to the webroot directory
     *
     **/
    public String getWebroot() {
        return this.webroot;
    }

    /**
     * returns the keystore file set in the config file
     *
     * @return keystore the path to the keystore file
     **/
    public String getKeystore() {
        return this.keystore;
    }

    /**
     * returns the keystore password set in the config file
     *
     * @return keystore_password the password to access the keystore file
     **/
    public String getKeystorePassword() {
        return this.keystore_password;
    }

    /**
     * returns the keystore password set in the config file
     *
     * @return keystore_password the password to access the keystore file
     **/
    public String getKeyPassword() {
        return this.key_password;
    }


    /**
     * returns the file of the custom not found page set in the config file
     *
     * @return custom_not_found path to the custom not found page
     **/
    public String getCustomNotFound() {
        return this.custom_not_found;
    }

    /**
     * loads the configuration from ini file
     *
     * @param path the path to the config file as string
     * @throws IOException if file does not exist
     **/
    public void load(String path) throws IOException {
        try(BufferedReader br = new BufferedReader(new FileReader( path ))) {
            String line;
            String section = null;
            while((line = br.readLine()) != null) {
                Matcher m = _section.matcher(line);
                if(m.matches()) {
                    section = m.group(1).trim();
                } else if(section != null) {
                    m = _keyValue.matcher(line);
                    if(m.matches()) {
                        String key   = m.group(1).trim();
                        String value = m.group(2).trim();
                        Map< String, String > kv = _entries.get(section);
                        if(kv == null) {
                            _entries.put(section, kv = new HashMap<>());
                        }
                        kv.put(key, value);
                    }
                }
            }
        }
    }

    /**
     * returns a string parameter from the loaded configuration
     *
     * @param section the ini file section
     * @param key the key parameter from the ini file
     * @param default_value default value to be returned if value is null
     * @return the value found or the default
     *
     **/
    public String getString(String section, String key, String default_value) {
        Map< String, String > kv = _entries.get(section);
        if(kv == null) {
            return default_value;
        }
        return kv.get(key);
    }

    /**
     * returns an integer parameter from the loaded configuration
     * @param section the ini file section
     * @param key the key parameter from the ini file
     * @param default_value default value to be returned if value is null
     * @return the value found or the default
     *
     **/
    public int getInt(String section, String key, int default_value) {
        Map<String, String> kv = _entries.get(section);
        if(kv == null) {
            return default_value;
        }
        return Integer.parseInt(kv.get(key));
    }

}
