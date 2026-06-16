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

/**
 * main class and entry point when running mollyb as a standalone server
 **/
public class Main {

    /**
     * main method and entry point when running mollyb as a standalone server
     *
     * @param args command line arguments
     **/
    public static void main( String[] args ) {

        Configuration cp = null;

        try {
            String configfile = null;

            // no config file
            if (args.length == 0) {
                System.out.println("No config file given. Not starting.");
                System.exit(0);
            }

            configfile = args[0];
            cp = new Configuration(configfile);

            // all is good
            System.out.println("\n");
            System.out.println("Starting mollyb " + MollybService.VERSION + " at " + cp.getBind() + ":" + cp.getPort());

        } catch(Exception e) {
            new LogTool().error(LogTool.getLogPoint(), e);
        }

        MollybService ms = new MollybService(cp.getBind(), cp.getPort());
        ms.setBacklog(4096);
        ms.setDebug(cp.getDebug());
        ms.setEmbedded(false);
        ms.setWebroot(cp.getWebroot());
        ms.setKeystore(cp.getKeystore());
        ms.setKeystorePassword(cp.getKeystorePassword());
        ms.setCustomNotFound(cp.getCustomNotFound());
        try {
            ms.start();
        } catch(Exception e) {
            new LogTool().error(LogTool.getLogPoint(), e);
        }
    }

}

