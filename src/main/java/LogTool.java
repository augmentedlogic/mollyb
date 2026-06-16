/**
 *
 * @copyright 2020 Wolfgang Hauptfleisch <dev@augmentedlogic.com>
 * Apache Licence Version 2.0
 * This file is part of mollyb
 *
 **/
package com.augmentedlogic.mollyb;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.File;
import java.util.Properties;

/**
 * writes the access, debug and error log
 **/
public class LogTool {


    /**
     * default date format used
     **/
    private static final String DEFAULT_DATE_FORMAT = "dd MMM yyyy HH:mm:ss,SSS";
    /**
     * access log file path
     **/
    private String logfile = null;

    /**
     * writes the access, debug and error log
     **/
    public LogTool() {

    }

    /**
     * set the access log file path
     *
     * @param path the path to the access log file
     *
     **/
    public static void setAccessLog(String path) {
        Properties props = System.getProperties();
        props.setProperty("mollyb.logfile", path);
    }

    /**
     * set the debug log file path
     *
     * @param path the path to the debug log file
     *
     **/
    public static void setDebugLog(String path) {
        Properties props = System.getProperties();
        props.setProperty("mollyb.debuglog", path);
    }

    /**
     * set the error log file path
     *
     * @param path the path to the error log file
     *
     **/
    public static void setErrorLog(String path) {
        Properties props = System.getProperties();
        props.setProperty("mollyb.errorlog", path);
    }

    /**
     * get the point at which the log entry was written
     * currently not used
     *
     * @return returns the point where the error ocurred
     **/
    public static String getLogPoint() {
        return Thread.currentThread().getStackTrace()[2].getClassName() + File.separator + Thread.currentThread().getStackTrace()[2].getMethodName();
    }


    /**
     * writes a new line to the log
     *
     * @param target_logfile path to the log file to write to
     * @param msg the message added to the log file
     *
     **/
    private void writeTo(String target_logfile, String msg)  {

        Properties systemProperties = System.getProperties();
        String date_format = systemProperties.getProperty("mollyb.log.dateformat");
        if(date_format == null) {
            date_format = LogTool.DEFAULT_DATE_FORMAT;
        }

        Date curDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat(date_format);
        String DateToStr = format.format(curDate);
        String logmsg = DateToStr + " " + msg + "\n";


        PrintWriter printWriter = null;
        File file = new File(target_logfile);

        try {
            if (!file.exists()) file.createNewFile();
            printWriter = new PrintWriter(new FileOutputStream(target_logfile, true));
            printWriter.write(logmsg);
        } catch (IOException e) {
            new LogTool().error(LogTool.getLogPoint(), e);
        } finally {
            if (printWriter != null) {
                printWriter.flush();
                printWriter.close();
            }
        }


    }


    /**
     * wrapper for writing to the access log
     *
     * @param msg the message added to the log file
     **/
    public void write(String msg) {
        Properties systemProperties = System.getProperties();
        String access_log = systemProperties.getProperty("mollyb.logfile");
        if(access_log != null) {
            this.writeTo(access_log, msg);
        }
    }


    /**
     * wrapper for writing to the debug log
     *
     * @param msg the message added to the log file
     **/
    public void debug(String msg) {
        Properties systemProperties = System.getProperties();
        String debug_log = systemProperties.getProperty("mollyb.debuglog");
        if(debug_log != null) {
            this.writeTo(debug_log, msg);
        } else {
            System.out.println(msg);
        }
    }


    /**
     * wrapper for writing to the error log
     *
     * @param logpoint the method where the error occurred
     * @param emsg the message added to the log file
     **/
    public void error(String logpoint, Exception emsg)  {
        Properties systemProperties = System.getProperties();
        String error_log = systemProperties.getProperty("mollyb.errorlog");
        if(error_log != null) {
            this.writeTo(error_log, logpoint + " : " + emsg.getMessage());
        } else {
            System.out.println(emsg);
        }
    }

}

