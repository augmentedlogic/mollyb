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


public class LogTool {


    private static final String DEFAULT_DATE_FORMAT = "dd MMM yyyy HH:mm:ss,SSS";
    private static final String DEFAULT_LOG_DIR = "." + File.separator;
    private String logfile = null;


    public LogTool() {

    }

    public static void setLogfile(String path) {
        if(!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        Properties props = System.getProperties();
        props.setProperty("mollyb.logfile", path);
    }

    /**
     * get the point at which the log entry was written
     * currently not used
     **/
    public static String getLogPoint() {
        return Thread.currentThread().getStackTrace()[2].getClassName() + File.separator + Thread.currentThread().getStackTrace()[2].getMethodName();
    }


    /**
     *
     **/
    private void writeTo(String msg)  {

        Properties systemProperties = System.getProperties();
        String date_format = systemProperties.getProperty("mollyb.log.dateformat");
        if(date_format == null) {
            date_format = LogTool.DEFAULT_DATE_FORMAT;
        }

        Date curDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat(date_format);
        String DateToStr = format.format(curDate);
        String logmsg = DateToStr + " " + msg + "\n";
        String access_log = systemProperties.getProperty("mollyb.logfile");

        if(access_log == null) {

            // we won't log or do anything if logfile is not set

        } else {

            PrintWriter printWriter = null;
            File file = new File(access_log);

            try {
                if (!file.exists()) file.createNewFile();
                printWriter = new PrintWriter(new FileOutputStream(access_log, true));
                printWriter.write(logmsg);
            } catch (IOException e) {
                System.out.println("can't write Logfile");
            } finally {
                if (printWriter != null) {
                    printWriter.flush();
                    printWriter.close();
                }
            }


        }

    }


    /**
     *
     **/
    public void write(String msg) {
        this.writeTo(msg);
    }


    /**
     *
     **/
    public void error(Exception emsg)  {
        this.writeTo(emsg.getMessage());
    }

}

