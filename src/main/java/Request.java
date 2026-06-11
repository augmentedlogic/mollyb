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

public class Request {

    private String path = null;
    private String query_string = null;
    private String remote_address = null;

    protected void processRequest(String remote_address, String request_path) throws Exception {

        this.remote_address = remote_address;
        URL url = new URL(request_path);
        try {
            this.path = url.getPath();
            this.query_string = url.getQuery();
        } catch(Exception e) {
            throw e;
        }
    }

    protected String getPath() {
        return this.path;
    }


    protected String getQuery() {
        return this.query_string;
    }


    protected String getRemoteAddress() {
        return this.remote_address;
    }


    protected Boolean hasQuery() {
        if(this.query_string != null) {
            return true;
        }
        return false;
    }

}
