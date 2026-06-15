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


    public static final String VERSION = "0.7";

    private String bind_to = "localhost";
    private int port = 8080;
    private boolean debug = false;
    private boolean is_embedded = true;
    private int backlog = 100;
    private int so_timeout = 5000;
    private String webroot = null;
    private String access_log = null;
    private String debug_log = null;
    private LinkedHashMap<String, Object> handlers = new LinkedHashMap<String, Object>();
    private String keystore = null;
    private String keystore_password = null;

    public MollybService(String bind_to, int port) {
        this.port = port;
        this.bind_to = bind_to;
    }

    public void addHandler(String path, Object handler) {
        this.handlers.put(path, handler);
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public void setAccessLog(String access_log) {
        this.access_log = access_log;
    }

    public void setDebugLog(String debug_log) {
        this.debug_log = debug_log;
    }

    public void setWebroot(String webroot) {
        this.webroot = webroot;
    }

    public void setKeystore(String keystore) {
        this.keystore = keystore;
    }

    public void setKeystorePassword(String keystore_password) {
        this.keystore_password = keystore_password;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setEmbedded(boolean is_embedded) {
        this.is_embedded = is_embedded;
    }

    public void setTimeout(int so_timeout) {
        this.so_timeout = so_timeout;
    }


    public void start() throws Exception {

        try {

            String keystore = this.keystore;
            String password = this.keystore_password;

            KeyStore keystore_object = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore_object.load(new FileInputStream(keystore), password.toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keystore_object, password.toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            TrustManagerFactory factory = TrustManagerFactory.getInstance(
                                              TrustManagerFactory.getDefaultAlgorithm());
            factory.init(keystore_object);
            sslContext.init(keyManagerFactory.getKeyManagers(), factory.getTrustManagers(), null);

            SSLServerSocketFactory ssfactory = sslContext.getServerSocketFactory();
            ServerSocket listener = ssfactory.createServerSocket(this.port, 5000, InetAddress.getByName(this.bind_to));
            SSLServerSocket sslListener = (SSLServerSocket) listener;
            sslListener.setNeedClientAuth(false);
            sslListener.setEnabledProtocols(new String[] {"TLSv1.2", "TLSv1.3"});

            // we leave that for later implementation
            // sslListener.setWantClientAuth(true);
            if(this.access_log != null) {
                LogTool.setAccessLog(this.access_log);
            }
            if(this.debug_log != null) {
                LogTool.setDebugLog(this.debug_log);
            }


            while (true) {
                int thread_count = java.lang.Thread.activeCount();
                Runnable runnable =  new ServiceThread(sslListener.accept(), this.so_timeout, this.webroot, this.handlers, this.is_embedded, this.debug);
                Thread thread = new Thread(runnable);
                thread.start();
            }


        } catch (IOException e) {
            throw e;
        }

    }

}

