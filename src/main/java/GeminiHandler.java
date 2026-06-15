/**
 *
 * @copyright 2020 Wolfgang Hauptfleisch <dev@augmentedlogic.com>
 * Apache Licence Version 2.0
 * This file is part of mollyb
 *
 **/
package com.augmentedlogic.mollyb;

/**
 * Interface for handlers set up for dynamic content
 **/
public interface GeminiHandler {

    /**
     * the method called when the handler is executed
     *
     * @param request the Resquest object
     * @param response the Response object
     * @return the Response object
     *
     */
    public Response handle(Request request, Response response);

}

