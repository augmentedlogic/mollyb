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
    private String custom_not_found = null;
    private Boolean debug = false;

    /**
    * reads the configuration file when using the standalone service
    *
    * @param path path to the configuration file
    * @throws IOException if the file does not exist
    *
    */
    public Configuration(String path) throws IOException {
        load(path);

        this.port = this.getInt("service", "port", 7777);
        this.bind = this.getString("service", "bind", "localhost");
        webroot = this.getString("service", "webroot", null);
        String logfile = this.getString("service", "logfile", null);
        int debug = this.getInt("service", "debug", 0);
        if(debug == 1) {
            this.debug = true;
        }
        String debug_log = this.getString("service", "debug_log", null);
        String keyfile = this.getString("security", "keystore", null);
        String password = this.getString("security", "password", null);
        this.custom_not_found = this.getString("service", "not_found", null);

        Properties props = System.getProperties();

        // check if webroot is null
        if(webroot == null) {
            System.out.println("Webroot is not set. Not starting.");
            System.exit(0);
        }

        // keyfile/keystore is required
        if(keyfile == null) {
            System.out.println("No keyfile given. Not starting.");
            System.exit(0);
        } else {
            this.keystore = keyfile;
            props.setProperty("mollyb.keyfile", keyfile);
        }

        // keystore password is required
        if(password == null) {
            System.out.println("No keystore password given. Not starting.");
            System.exit(0);
        } else {
            this.keystore_password = password;
            props.setProperty("mollyb.password", password);
        }

        // only if logfile is set
        if(logfile != null) {
            props.setProperty("mollyb.logfile", logfile);
        }
        if(debug_log != null) {
            props.setProperty("mollyb.debuglog", debug_log);
        }

    }

    public int getPort() {
        return this.port;
    }

    public String getBind() {
        return this.bind;
    }

    public Boolean getDebug() {
        return this.debug;
    }

    public String getWebroot() {
        return this.webroot;
    }

    public String getKeystore() {
        return this.keystore;
    }

    public String getKeystorePassword() {
        return this.keystore_password;
    }

    public String getCustomNotFound() {
        return this.custom_not_found;
    }


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

    public String getString(String section, String key, String defaultvalue) {
        Map< String, String > kv = _entries.get(section);
        if(kv == null) {
            return defaultvalue;
        }
        return kv.get(key);
    }

    public int getInt(String section, String key, int defaultvalue) {
        Map<String, String> kv = _entries.get(section);
        if(kv == null) {
            return defaultvalue;
        }
        return Integer.parseInt(kv.get(key));
    }

}
