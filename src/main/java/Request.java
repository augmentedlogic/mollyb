/**
 *
 * @copyright 2020 Wolfgang Hauptfleisch <dev@augmentedlogic.com>
 * Apache Licence Version 2.0
 * This file is part of mollyb
 *
 **/
package com.augmentedlogic.mollyb;

import java.net.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.util.regex.*;
import static java.nio.file.Paths.get;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.net.URL;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 *
 **/
public class Request {

    private String path = null;
    private String query_string = null;
    private String remote_address = null;
    private ClientIdentity client_identity;

    /**
     * set the remote address and parse the request path
     *
     * @param remote_address the clients IP address
     * @param request_path the path as sent by the client
     * @throws Exception if URL is invalid
     **/
    protected void processRequest(String remote_address, String request_path) throws Exception {

        this.remote_address = remote_address;
        URL url = new URL(request_path);
        try {
            this.path = url.getPath();
            this.query_string = url.getQuery();
        } catch(Exception e) {
            new LogTool().error(LogTool.getLogPoint(), e);
        }
    }

    /**
     * set the client identity (not yet in use)
     *
     * @param client_identity the ClientIdentity object
     **/
    protected void setClientIdentity(ClientIdentity client_identity) {
        this.client_identity = client_identity;
    }

    /**
     * get the path from the requested URL
     *
     * @return the URL path
     **/
    public String getPath() {
        return this.path;
    }

    /**
     * get the query from the URL if one exists
     *
     * @return the query string
     **/
    public String getQuery() {

        try {
            if(this.query_string != null) {
                query_string = java.net.URLDecoder.decode(query_string, StandardCharsets.UTF_8.name());
            }
        } catch(Exception e) {
            new LogTool().error(LogTool.getLogPoint(), e);
        }
        return this.query_string;
    }

    /**
     * get the coients IP address
     *
     * @return the clients IP address
     **/
    public String getRemoteAddress() {
        return this.remote_address;
    }

    /**
     * check if a query exists (convinience method)
     *
     * @return true if there is a query string
     **/
    public Boolean hasQuery() {
        if(this.query_string != null) {
            return true;
        }
        return false;
    }

}
