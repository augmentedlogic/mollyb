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

/**
 * service class that initates the SSL connection and throws the service thread
 **/
public class MollybService {

    /**
     * server build version
     **/
    public static final String VERSION = "0.7.2";

    private String bind_to = "localhost";
    private int port = 8080;
    private boolean debug = false;
    private boolean is_embedded = true;
    private int backlog = 100;
    private int so_timeout = 5000;
    private String webroot = null;
    private String access_log = null;
    private String debug_log = null;
    private String error_log = null;
    private String custom_not_found = null;
    private LinkedHashMap<String, Object> handlers = new LinkedHashMap<String, Object>();
    private String keystore = null;
    private String keystore_password = null;
    private String key_password = null;

    /**
     * service class that initates the SSL connection and throws the service thread
     *
     * @param bind_to the address to bind the listener to
     * @param port the port to listen to
     **/
    public MollybService(String bind_to, int port) {
        this.port = port;
        this.bind_to = bind_to;
    }

    /**
     * connect a handler to a URI path
     *
     * @param path the URI path to connect the handler to
     * @param handler the GeminiHandler object
     */
    public void addHandler(String path, Object handler) {
        this.handlers.put(path, handler);
    }

    /**
     * sets the socket backlog
     *
     * @param backlog the socket backlog in bytes
     */
    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    /**
     * set the path to the access log file
     *
     * @param access_log the path to the access log
     */
    public void setAccessLog(String access_log) {
        this.access_log = access_log;
    }

    /**
     * set the path to the debug log file
     *
     * @param debug_log the path to the debug log
     */
    public void setDebugLog(String debug_log) {
        this.debug_log = debug_log;
    }

    /**
     * set the path to the error log file
     *
     * @param error_log the path to the debug log
     */
    public void setErrorLog(String error_log) {
        this.error_log = error_log;
    }

    /**
     * set the path to a custom not found file
     *
     * @param custom_not_found the path to the static file
     */
    public void setCustomNotFound(String custom_not_found) {
        this.custom_not_found = custom_not_found;
    }

    /**
     * set the path to the directory files are served from
     *
     * @param webroot path to the directory
     */
    public void setWebroot(String webroot) {
        this.webroot = webroot;
    }

    /**
     * set the path to the keystore file
     *
     * @param keystore path to the keystore (.jks) file
     */
    public void setKeystore(String keystore) {
        this.keystore = keystore;
    }

    /**
     * set the password for the keystore
     *
     * @param keystore_password the keystore password
     */
    public void setKeystorePassword(String keystore_password) {
        this.keystore_password = keystore_password;
    }

    /**
     * enable debugging
     *
     * @param debug set true to enable debugging
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * indicate if mollyb is used as a library
     *
     * @param is_embedded indicate if mollyb is used as a library
     */
    protected void setEmbedded(boolean is_embedded) {
        this.is_embedded = is_embedded;
    }

    /**
     * set the server socket timeout
     *
     * @param so_timeout the socket timeout value
     */
    public void setTimeout(int so_timeout) {
        this.so_timeout = so_timeout;
    }

    /**
     * start the service
     *
     * @throws Exception if spawning the thread fails
     */
    public void start() throws Exception {

        try {

            String keystore = this.keystore;
            String password = this.keystore_password;
            if(this.key_password == null) {
                this.key_password = this.keystore_password;
            }

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
            if(this.error_log != null) {
                LogTool.setErrorLog(this.error_log);
            }


            while (true) {
                int thread_count = java.lang.Thread.activeCount();
                Runnable runnable =  new ServiceThread(sslListener.accept(), this.so_timeout, this.webroot, this.handlers, this.custom_not_found, this.is_embedded, this.debug);
                Thread thread = new Thread(runnable);
                thread.start();
            }


        } catch (IOException e) {
            new LogTool().error(LogTool.getLogPoint(), e);
        }

    }

}

