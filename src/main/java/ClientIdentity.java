/**
 *
 * @copyright 2020 Wolfgang Hauptfleisch <dev@augmentedlogic.com>
 * Apache Licence Version 2.0
 * This file is part of mollyb
 *
 **/
package com.augmentedlogic.mollyb;

import java.util.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

import javax.security.cert.*;
import javax.net.ssl.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

/**
 *  This is for future identity handling, not yet in use
 *
 **/
class ClientIdentity {

    private X509Certificate peer_certificate;
    private String name = null;

    public void parse(SSLSession session) throws Exception {
        try {
            this.peer_certificate = (X509Certificate) session.getPeerCertificates()[0];


            try {
                peer_certificate.checkValidity();
                this.name = this.peer_certificate.getSubjectX500Principal().getName();

            } catch (CertificateExpiredException e) {
                new LogTool().error(LogTool.getLogPoint(), e);
            }


        } catch (SSLPeerUnverifiedException e) {
            this.peer_certificate = null;
            new LogTool().error(LogTool.getLogPoint(), e);
        }

    }

    public String getName() {
        return this.name;
    }

}
