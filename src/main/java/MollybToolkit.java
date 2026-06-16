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

/**
 * various helper methods
 **/
public class MollybToolkit {

    /**
     * clean a path from unwanted elements
     *
     * @param file_path the path to clean
     * @return the cleaned path
     **/
    public static String sanitizeFilePath(String file_path) {
        return file_path.replaceAll("[\\.]{2,}", "");
    }

    /**
     * check if a static file is a media file
     *
     * @param path_to_file the file to check
     * @return boolean if file is media file or not
     */
    public static Boolean isMediaFile(String path_to_file) {
        if(path_to_file.toLowerCase().endsWith(".jpg")
                || path_to_file.toLowerCase().endsWith(".jpeg")
                || path_to_file.toLowerCase().endsWith(".png")
                || path_to_file.toLowerCase().endsWith(".gif")) {
            return true;
        }
        return false;
    }

    /**
    * get the mimetype of the media file
    *
    * @param path the path to the file to check
    * @throws IOException if file does not exist
    * @return the mimetype
    */
    public static String getMediaContentType(String path) throws IOException {
        String mime = null;
        try {
            mime = Files.probeContentType(Paths.get(path));
        } catch(Exception e) {
            new LogTool().error(LogTool.getLogPoint(), e);
        }
        return mime;
    }

    /**
     * check if a file exists
     *
     * @param path_to_file the file to check
     * @return false if file does not exist
     */
    public static Boolean fileExists(String path_to_file) {
        File f = new File(path_to_file);
        if(f.exists() && !f.isDirectory()) {
            return true;
        }
        return false;
    }

    /**
     * match a handler path to the url path
     *
     * @param handler_path the path set in addHandler which may contain a wildcard
     * @param path_to_match  the path extracted from the url
     * @return true when match was found
     **/
    protected static boolean matchHandler(String handler_path, String path_to_match) {

        if (handler_path.length() == 0 && path_to_match.length() == 0) {
            return true;
        }

        if (handler_path.length() > 1 && handler_path.charAt(0) == '*') {
            int i=0;
            while (i+1<handler_path.length() && handler_path.charAt(i+1) == '*') {
                i++;
                handler_path=handler_path.substring(i);
            }
        }

        if (handler_path.length() > 1 && handler_path.charAt(0) == '*' && path_to_match.length() == 0) {
            return false;
        }

        if ((handler_path.length() > 1 && handler_path.charAt(0) == '?') || (handler_path.length() != 0 && path_to_match.length() != 0 &&
                handler_path.charAt(0) == path_to_match.charAt(0))) {
            return matchHandler(handler_path.substring(1), path_to_match.substring(1));
        }

        if (handler_path.length() > 0 && handler_path.charAt(0) == '*') {
            return matchHandler(handler_path.substring(1), path_to_match) || matchHandler(handler_path, path_to_match.substring(1));
        }

        return false;
    }

    // not in use yet
    //static String readFile(String path) throws IOException {
    //    byte[] encoded = Files.readAllBytes(Paths.get(path));
    //    return new String(encoded);
    //}

    /**
     * extract the path from the URL
     *
     * @param url URL object
     * @throws Exception on invalid URL
     * @return the extracted path
     */
    protected static String extractPath(URL url) throws Exception {
        String path = null;
        try {
            path = url.getPath();
        } catch(Exception e) {
            new LogTool().error(LogTool.getLogPoint(), e);
        }
        return path;
    }

}


