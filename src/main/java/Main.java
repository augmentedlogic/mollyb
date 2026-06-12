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

public class Main {

    public static void main( String[] args ) {

        ConfigParser cp = null;

        try {
            String configfile = null;

            // no config file
            if (args.length == 0) {
                System.out.println("No config file given. Not starting.");
                System.exit(0);
            }

            configfile = args[0];
            cp = new ConfigParser(configfile);

            // all is good
            System.out.println("\n");
            System.out.println("Starting mollyb " + MollybService.VERSION + " at " + cp.getBind() + ":" + cp.getPort());


        } catch(Exception e) {
            System.out.println(e);
        }


        MollybService ms = new MollybService(cp.getBind(), cp.getPort());
        ms.setBacklog(4096);
        ms.setDebug(false);
        ms.setWebroot(cp.getWebroot());
        try {
            ms.start();
        } catch(Exception e) {
            System.out.println(e);
        }
    }

}

