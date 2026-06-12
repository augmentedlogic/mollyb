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
import java.nio.file.Files;
import java.nio.file.Paths;
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
    // for future implementation
    private Object handler = null;
    private LinkedHashMap<String, Object> handlers = null;

    @SuppressWarnings("unchecked")
    public ServiceThread(Socket socket, int so_timeout, String webroot, LinkedHashMap handlers, boolean debug) {
        this.socket = socket;
        this.debug = debug;
        this.handlers = handlers;
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
            //BufferedOutputStream data_out = new BufferedOutputStream(socket.getOutputStream());
            OutputStream data_out = socket.getOutputStream();


            String requestLine = in.readLine();
            String final_path = null;
            String got_path = null;
            Boolean is_media = false;
            Boolean is_feed = false;
            // for future implementation
            Boolean custom_not_found = false;

            // setting the remote_address
            String remote_address=(((InetSocketAddress) this.socket.getRemoteSocketAddress()).getAddress()).toString().replace("/","");


            String replaced = requestLine.replace("gemini:", "http:");
            // DEBUG ONLY: for future implementation
            Request request = new Request();
            request.processRequest(remote_address, replaced);

            if(this.debug == true) {
                new LogTool().debug("PATH: " + request.getPath());
                new LogTool().debug("QUERY: " + request.getQuery());
                new LogTool().debug("REMOTE: " + request.getRemoteAddress());
            }

            // the routing decision
            String handler_path = request.getPath();
            Iterator it = this.handlers.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                String test_path = (String) pair.getKey();
                if(handler_path.startsWith(test_path)) {
                    this.handler = (Object) pair.getValue();
                    break;
                }
            }




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
                    new LogTool().debug(requestLine);
                }

                got_path = MollybToolkit.extractPath(new URL(replaced));
                got_path = MollybToolkit.sanitizeFilePath(got_path);

                long payload_size = 0;

                if(got_path.endsWith(".gmi")) {
                    final_path = this.webroot + got_path;
                } else if (MollybToolkit.isMediaFile(got_path) == true) {
                    is_media = true;
                    final_path = this.webroot + got_path;
                } else if (got_path.endsWith(".xml")) {
                    is_feed = true;
                    final_path = this.webroot + got_path;
                } else {
                    final_path = this.webroot + got_path + "/index.gmi";
                }

                // DEBUG ONLY
                if(this.debug == true) {
                    new LogTool().debug("LOOKUP:" + final_path);
                    new LogTool().debug("MEDIA: " + is_media);
                }




                Response dynamic_response = new Response();

                if(this.handler != null) {

                    try {
                        Response pass_response = new Response();
                        Class[] parameterTypes = new Class[] {Request.class, Response.class};
                        Object[] arguments = new Object[] { request, pass_response };
                        Method m = this.handler.getClass().getMethod("handle", parameterTypes);
                        dynamic_response = (Response) m.invoke(this.handler, arguments);
                        out.print(dynamic_response.getStatusCode() + " " + dynamic_response.getMimetype() + "\r\n");
                        for( String line : dynamic_response.getBody()) {
                            out.print(line);
                        }

                    } catch(Exception e) {
                        Thread t = Thread.currentThread();
                        t.getUncaughtExceptionHandler().uncaughtException(t, e);
                    }

                } else if(!MollybToolkit.fileExists(final_path)) {
                    if(custom_not_found == true) {
                        out.print(Response.OK + "\r\n");
                    } else {
                        out.print(Response.NOT_FOUND + "\r\n");
                    }
                    out.print("# Page Not Found\r\n");
                    response_status = 51;

                } else if(is_media == true) {
                    String media_mimetype = MollybToolkit.getMediaContentType(final_path);
                    String header = "20 " + media_mimetype + "\r\n";
                    data_out.write(header.getBytes(StandardCharsets.UTF_8));
                    Files.copy(Paths.get(final_path), data_out);
                } else if(is_feed == true) {
                    Response response = new Response();
                    //out.print(Response.OK + "\r\n");
                    out.print("20 text/xml\r\n");
                    ArrayList<String> payload = response.getStaticPayload(final_path);
                    payload_size = response.getPayloadSize();
                    for( String line : payload) {
                        out.print(line);
                    }
                } else {
                    Response response = new Response();
                    out.print(Response.OK + "\r\n");
                    ArrayList<String> payload = response.getStaticPayload(final_path);
                    payload_size = response.getPayloadSize();
                    for( String line : payload) {
                        out.print(line);
                    }
                }

                data_out.flush();
                out.flush();

                try {
                    data_out.close();
                } catch(Exception doe) {
                    if(this.debug == true) {
                        new LogTool().debug(doe.getMessage());
                    }
                }

                try {
                    out.close();
                } catch(Exception oe) {
                    if(this.debug == true) {
                        new LogTool().debug(oe.getMessage());
                    }
                }

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
                    new LogTool().debug(ex.getMessage());
                }

            }

        }

    }

}

