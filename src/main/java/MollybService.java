/**
 *
 * @copyright 2020 Wolfgang Hauptfleisch <dev@augmentedlogic.com>
 * Apache Licence Version 2.0
 * This file is part of mollyb
 *
 **/
package com.augmentedlogic.mollyb;

import java.nio.channels.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.lang.reflect.*;


import javax.net.ssl.SSLContext;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;

import java.security.KeyStore;
import javax.net.ssl.*;
import java.util.Properties;


public class MollybService {


    public static final String VERSION = "0.5";

    private String bind_to = "localhost";
    private int port = 8080;
    private boolean debug = false;
    private int backlog = 100;
    private int so_timeout = 5000;
    private String webroot = null;

    public MollybService(String bind_to, int port) {
        this.port = port;
        this.bind_to = bind_to;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public void setWebroot(String webroot) {
        this.webroot = webroot;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setTimeout(int so_timeout) {
        this.so_timeout = so_timeout;
    }

    public void start() throws Exception {


        try {

            Properties systemProperties = System.getProperties();
            String keyfile = systemProperties.getProperty("mollyb.keyfile");
            String password = systemProperties.getProperty("mollyb.password");

            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(new FileInputStream(keyfile), password.toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keystore, password.toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            TrustManagerFactory factory = TrustManagerFactory.getInstance(
                                              TrustManagerFactory.getDefaultAlgorithm());
            factory.init(keystore);
            sslContext.init(keyManagerFactory.getKeyManagers(), factory.getTrustManagers(), null);

            SSLServerSocketFactory ssfactory = sslContext.getServerSocketFactory();
            ServerSocket listener = ssfactory.createServerSocket(this.port, 5000, InetAddress.getByName(this.bind_to));
            SSLServerSocket sslListener = (SSLServerSocket) listener;
            sslListener.setNeedClientAuth(false);
            sslListener.setEnabledProtocols(new String[] {"TLSv1.2", "TLSv1.3"});


            while (true) {
                int thread_count = java.lang.Thread.activeCount();
                Runnable runnable =  new ServiceThread(sslListener.accept(), this.so_timeout, this.webroot, this.debug);
                Thread thread = new Thread(runnable);
                thread.start();
            }


        } catch (IOException e) {
            throw e;
        }

    }

}

