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
import java.util.UUID;


public class MollybToolkit {

    public static String sanitizeFilePath(String filePath) {
        return filePath.replaceAll("[\\.]{2,}", "");
    }


    public static Boolean fileExists(String filePathString) {
        File f = new File(filePathString);
        if(f.exists() && !f.isDirectory()) {
            return true;
        }
        return false;
    }

    // not in use yet
    static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded);
    }


    protected static String extractPath(URL url) throws Exception {
        String path = null;
        try {
            path = url.getPath();
        } catch(Exception e) {
            throw e;
        }
        return path;
    }

}


