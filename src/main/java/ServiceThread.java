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
import javax.net.ssl.*;

/**
 * service thread thrown by MollybServer
 */
class ServiceThread implements Runnable {

    private final Socket socket;
    private int so_timeout = 20000;
    private boolean debug = false;
    private String webroot = null;
    private String custom_not_found = null;
    private Object handler = null;
    private LinkedHashMap<String, Object> handlers = null;
    private Boolean is_embedded = true;

    /**
     * service thread thrown by MollybServer
     */
    @SuppressWarnings("unchecked")
    public ServiceThread(Socket socket, int so_timeout, String webroot, LinkedHashMap handlers, String custom_not_found, Boolean is_embedded, boolean debug) {
        this.socket = socket;
        this.debug = debug;
        this.handlers = handlers;
        this.webroot = webroot;
        this.so_timeout = so_timeout;
        this.is_embedded = is_embedded;
        this.custom_not_found = custom_not_found;
    }


    /**
     * main thread to process socket input
     *
     */
    @Override
    public void run() {

        int response_status = 20;

        try {
            this.socket.setSoTimeout(this.so_timeout);

            // setting up for incoming stream
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()), 2048);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(
                                                  socket.getOutputStream(), StandardCharsets.UTF_8), true);
            OutputStream data_out = socket.getOutputStream();


            String final_path = null;
            String got_path = null;
            Boolean is_media = false;
            Boolean is_feed = false;

            // getting the remote_address
            String remote_address=(((InetSocketAddress) this.socket.getRemoteSocketAddress()).getAddress()).toString().replace("/","");


            Request request = new Request();


            String requestLine = in.readLine();
            String replaced = requestLine.replace("gemini:", "http:");
            request.processRequest(remote_address, replaced);

            if(this.debug == true) {
                new LogTool().debug("REMOTE: " + request.getRemoteAddress());
                new LogTool().debug("PATH: " + request.getPath());
                new LogTool().debug("QUERY: " + request.getQuery());
            }

            // the routing decision, only if run as embedded
            if(this.is_embedded == true) {
                String path_to_match = request.getPath();
                Iterator it = this.handlers.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    String handler_path = (String) pair.getKey();
                    if(MollybToolkit.matchHandler(handler_path, path_to_match) == true) {
                        this.handler = (Object) pair.getValue();
                        break;
                    }

                    //if(path_to_match.startsWith(test_path)) {
                    //    this.handler = (Object) pair.getValue();
                    //    break;
                    //}
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
                    new LogTool().debug("REQUEST: " + requestLine);
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


                if(this.handler != null) {

                    if(this.is_embedded == true) {
                        if(this.debug == true) {
                            new LogTool().debug("SERVER: Serving dynamic content");
                        }
                        Response dynamic_response = new Response();

                        try {
                            Response pass_response = new Response();
                            Class[] parameterTypes = new Class[] {Request.class, Response.class};
                            Object[] arguments = new Object[] { request, pass_response };
                            Method m = this.handler.getClass().getMethod("handle", parameterTypes);
                            dynamic_response = (Response) m.invoke(this.handler, arguments);
                            out.print(dynamic_response.getHeader() + "\r\n");
                            for( String line : dynamic_response.getBody()) {
                                out.print(line);
                            }

                        } catch(Exception e) {
                            Thread t = Thread.currentThread();
                            t.getUncaughtExceptionHandler().uncaughtException(t, e);
                        }
                    }

                } else if(!MollybToolkit.fileExists(final_path)) {
                    if(this.custom_not_found != null) {
                        if(this.debug == true) {
                           new LogTool().debug("RESPONSE: serving custom not found: " + this.custom_not_found);
                        }
                        Response response = new Response();
                        out.print("20 text/gemini\r\n");
                        ArrayList<String> payload = response.getStaticPayload(this.custom_not_found);
                        payload_size = response.getPayloadSize();
                        for( String line : payload) {
                            out.print(line);
                        }
                    } else {
                        out.print(Response.NOT_FOUND + "\r\n");
                    }
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
                    if(this.debug == true) {
                        new LogTool().debug("SERVER: Serving static content");
                    }

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
                new LogTool().write(remote_address + " " + response_status + " " + got_path + " " + payload_size);

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


            }

            this.socket.close();

        } catch (Exception e) {
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

