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
import java.lang.Object;

class ServiceThread implements Runnable {

    private final Socket socket;
    private int so_timeout = 20000;
    private boolean debug = false;
    private String webroot = null;

    @SuppressWarnings("unchecked")
    public ServiceThread(Socket socket, int so_timeout, String webroot, boolean debug) {
        this.socket = socket;
        this.debug = debug;
        this.webroot = webroot;
        this.so_timeout = so_timeout;
    }


    @Override
    public void run() {


        int response_status = 20;

        try {
            this.socket.setSoTimeout(this.so_timeout);

            // setting up for incoming stream
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()), 2048);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(
                                                  socket.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedOutputStream dataOut = new BufferedOutputStream(socket.getOutputStream());


            String requestLine = in.readLine();
            String final_path = null;
            String got_path = null;

            // setting the remote_address
            String remote_address=(((InetSocketAddress) this.socket.getRemoteSocketAddress()).getAddress()).toString().replace("/","");

            if (requestLine == null || requestLine.length() == 0) {

                response_status = 59;
                got_path = "invalid";
                out.print(Response.BAD_REQUEST + "\r\n");

            } else if(requestLine.length() > 2048) {

                response_status = 59;
                got_path = "invalid";
                out.print(Response.BAD_REQUEST + "\r\n");

            } else if(!requestLine.startsWith("gemini:")) {

                response_status = 59;
                got_path = "invalid";
                out.print(Response.BAD_REQUEST + "\r\n");

            } else if(requestLine.contains(" ")) {

                response_status = 59;
                got_path = "invalid";
                out.print(Response.BAD_REQUEST + "\r\n");

            } else {
                // DEBUG ONLY
                if(this.debug == true) {
                    System.out.println(requestLine);
                }

                String replaced = requestLine.replace("gemini:", "http:");
                got_path = MollybToolkit.extractPath(new URL(replaced));
                got_path = MollybToolkit.sanitizeFilePath(got_path);

                long payload_size = 0;

                if(got_path.endsWith(".gmi")) {
                    final_path = this.webroot + got_path;
                } else {
                    final_path = this.webroot + got_path + "/index.gmi";
                }

                // DEBUG ONLY
                if(this.debug == true) {
                    System.out.println("LOOKUP:" + final_path);
                }

                if(!MollybToolkit.fileExists(final_path)) {
                    out.print(Response.NOT_FOUND + "\r\n");
                    out.print("# Page Not Found\r\n");
                    response_status = 51;
                } else {
                    Response response = new Response();
                    ArrayList<String> payload = response.getPayload(final_path);
                    payload_size = response.getPayloadSize();
                    out.print(Response.OK + "\r\n");
                    for( String line : payload) {
                        out.print(line);
                    }
                }

                out.flush();

                new LogTool().write(remote_address + " " + response_status + " " + got_path + " " + payload_size);

            }

            this.socket.close();

        } catch (Exception e) {
            // We don't actually want to throw an error here, instead we will log an invalid request in the future
            // DEBUG ONLY
            if(this.debug == true) {
                Thread t = Thread.currentThread();
                t.getUncaughtExceptionHandler().uncaughtException(t, e);
            }

            try {
                this.socket.close();
            } catch(Exception ex) {

                if(this.debug == true) {
                    System.out.println(ex);
                }

            }

        }

    }

}

