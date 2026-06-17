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

/**
 * The response object handed over to, and returned from, the GeminiHandler
 **/
public class Response {

    /**
     * default gemini OK header
     **/
    public static final String OK = "20 text/gemini";
    /**
     * file not found header
     **/
    public static final String NOT_FOUND = "51 text/gemini";
    /**
     * server error header
     **/
    public static final String SERVER_ERROR = "40 text/gemini";
    /**
     * bad request header
     **/
    public static final String BAD_REQUEST = "59 text/gemini";
    /**
     * input required header
     **/
    public static final String INPUT_REQUIRED = "10 text/gemini";
    /**
     * secure input required header
     **/
    public static final String SECURE_INPUT_REQUIRED = "11 text/gemini";
    /**
     * JPEG mimetype
     **/
    public static final String MEDIA_JPEG = "image/jpeg";
    /**
     * PNG mimetype
     **/
    public static final String MEDIA_PNG = "image/png";
    /**
     * GIF mimetype
     **/
    public static final String MEDIA_GIF = "image/gif";

    private long filesize = 0;
    private String mimetype = "text/gemini";
    private String header = "20 text/gemini";
    private int status_code = 20;
    private ArrayList<String> payload_lines = new ArrayList<String>();


    /**
     * returns the filesize of static file served
     *
     * @param filepath path to the file
     * @return filesize
     */
    private long getFileSize(String filepath) {
        File file = new File(filepath);
        long length = file.length();
        return length;
    }

    /**
     * returns the payload size
     *
     * @return the size of the payload body to be served
     */
    public long getPayloadSize() {
        return this.filesize;
    }

    /**
     * set the media mimetype of the response
     *
     * @param mimetype the mimetype to be added
     */
    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    /**
     * set the status code of the response
     *
     * @param status_code the gemini status code
     */
    public void setStatusCode(int status_code) {
        this.status_code = status_code;
    }

    /**
     * set the header of the response
     *
     * @param header the response header
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * add to the body that will be resturned by the response
     *
     * @param body the text to be added to the body
     */
    public void addBody(String body) {
        String[] lines = body.split(System.getProperty("line.separator"));
        for(String line: lines) {
            this.payload_lines.add(line + "\r\n");
        }
    }

    /**
     * get the status code of the response
     *
     * @return the status code of the response
     */
    protected int getStatusCode() {
        return this.status_code;
    }

    /**
     * get the mimetype of the response
     *
     * @return the mimetype of the response, if any
     */
    protected String getMimetype() {
        return this.mimetype;
    }

    /**
     * get the header of the response
     *
     * @return the header of the response
     */
    protected String getHeader() {
        return this.header;
    }


    /**
     * get the body of the response
     *
     * @return an array with the lines of the response body
     */
    protected ArrayList<String> getBody() {
        return this.payload_lines;
    }

    /**
     * read a static file into an array for the response
     *
     * @param filepath the path to the file to be read
     * @throws Exception if file can't be read
     * @return an array with the lines of the response body
     */
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
            new LogTool().error(LogTool.getLogPoint(), e);
        }

        return lines;
    }

}
