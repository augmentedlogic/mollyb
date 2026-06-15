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

import java.io.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Response {

    public static final String OK = "20 text/gemini";
    public static final String NOT_FOUND = "51 text/gemini";
    public static final String SERVER_ERROR = "40 text/gemini";
    public static final String BAD_REQUEST = "59 text/gemini";
    public static final String INPUT_REQUIRED = "10 text/gemini";
    public static final String SECURE_INPUT_REQUIRED = "11 text/gemini";

    public static final String MEDIA_JPEG = "image/jpeg";
    public static final String MEDIA_PNG = "image/png";
    public static final String MEDIA_GIF = "image/gif";

    private long filesize = 0;
    private String mimetype = "text/gemini";
    private int status_code = 20;
    private ArrayList<String> payload_lines = new ArrayList<String>();


    private long getFileSize(String filepath) {
        File file = new File(filepath);
        long length = file.length();
        return length;
    }

    public long getPayloadSize() {
        return this.filesize;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }


    public void setStatusCode(int status_code) {
        this.status_code = status_code;
    }


    public void addBody(String line) {
        this.payload_lines.add(line + "\r\n");
    }


    public int getStatusCode() {
        return this.status_code;
    }


    public String getMimetype() {
        return this.mimetype;
    }


    public ArrayList<String> getBody() {
        return this.payload_lines;
    }

    public ArrayList<String> getStaticPayload(String filepath) throws Exception {

        this.filesize = this.getFileSize(filepath);

        ArrayList<String> lines = new ArrayList<String>();
        try {

            InputStream is = new FileInputStream(filepath);
            Scanner sc = new Scanner(is, StandardCharsets.UTF_8.name());
            while (sc.hasNextLine()) {
                lines.add(sc.nextLine() + "\r\n");
            }
        } catch(Exception e) {
            // DEBUG ONLY
            new LogTool().debug("FILE NOT FOUND");
        }

        return lines;
    }

}
