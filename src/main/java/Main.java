/**
 *
 * @copyright 2020 Wolfgang Hauptfleisch <dev@augmentedlogic.com>
 * Apache Licence Version 2.0
 * This file is part of mollyb
 *
 **/
package com.augmentedlogic.mollyb;

import java.net.*;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Properties;


import com.augmentedlogic.mollyb.*;

public class Main {

    public static void main( String[] args ) {

        int port = 1965;
        String bind = "localhost";
        String webroot = null;

        try {
            String configfile = null;

            // no config file
            if (args.length == 0) {
                System.out.println("No config file given. Not starting.");
                System.exit(0);
            }

            configfile = args[0];

            ConfigParser cp = new ConfigParser(configfile);
            port = cp.getInt("service", "port", 7777);
            bind = cp.getString("service", "bind", "localhost");
            webroot = cp.getString("service", "webroot", null);
            String logfile = cp.getString("service", "logfile", null);
            String keyfile = cp.getString("security", "keystore", null);
            String password = cp.getString("security", "password", null);

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
                props.setProperty("mollyb.keyfile", keyfile);
            }

            // keystore password is required
            if(password == null) {
                System.out.println("No keystore password given. Not starting.");
                System.exit(0);
            } else {
                props.setProperty("mollyb.password", password);
            }


            // only if logfile is set
            if(logfile != null) {
                props.setProperty("mollyb.logfile", logfile);
            }

            // all is good
            System.out.println("\n");
            System.out.println("Starting mollyb at " + bind + ":" + port);


        } catch(Exception e) {
            System.out.println(e);
        }


        MollybService ms = new MollybService(bind, port);
        ms.setBacklog(4096);
        ms.setDebug(false);
        ms.setWebroot(webroot);
        try {
            ms.start();
        } catch(Exception e) {
            System.out.println(e);
        }
    }

}

