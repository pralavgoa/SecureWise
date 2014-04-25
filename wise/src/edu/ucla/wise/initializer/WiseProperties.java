/**
 * Copyright (c) 2014, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, 
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors 
 * may be used to endorse or promote products derived from this software without 
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.ucla.wise.initializer;

import edu.ucla.wise.shared.properties.AbstractWiseProperties;

public class WiseProperties extends AbstractWiseProperties {

    private static final long serialVersionUID = 1L;

    public static String EMAIL_FROM = "wise.email.from";
    public static String ALERT_EMAIL = "alert.email";
    public static String EMAIL_HOST = "email.host";
    public static String EMAIL_USERNAME = "SMTP_AUTH_USER";
    public static String EMAIL_PASSWORD = "SMTP_AUTH_PASSWORD";
    public static String ADMIN_SERVER = "admin.server";
    public static String IMAGES_PATH = "shared_image.path";
    public static String STYLES_PATH = "shared_style.path";

    public static String SSL_EMAIL = "email.ssl";

    public static String XML_LOC = "xml_root.path";

    private static final String SS_WIZARD_PROPERTIES_URL = "SS_WIZARD_PROPERTIES_URL";
    public static String SS_WIZARD_PASSWORD = "SS_WIZARD_PASSWORD";

    public WiseProperties(String fileName, String applicationName) {
        super(fileName, applicationName);
    }

    public String getStylesPath() {
        return this.getStringProperty(STYLES_PATH);
    }

    public String getImagesPath() {
        return this.getStringProperty(IMAGES_PATH);
    }

    public String getAdminServer() {
        return this.getStringProperty(ADMIN_SERVER);
    }

    public String getEmailUsername() {
        return this.getStringProperty(EMAIL_USERNAME);
    }

    public String getEmailPassword() {
        return this.getStringProperty(EMAIL_PASSWORD);
    }

    public String getEmailFrom() {
        return this.getStringProperty(EMAIL_FROM);
    }

    public String getAlertEmail() {
        return this.getStringProperty(ALERT_EMAIL);
    }

    public String getEmailHost() {
        return this.getStringProperty(EMAIL_HOST);
    }

    public boolean useSslEmail() {
        return "true".equalsIgnoreCase(this.getStringProperty(SSL_EMAIL));
    }

    public String getXmlRootPath() {
        return this.getStringProperty(XML_LOC);
    }

    public String getStudySpaceWizardPassword() {
        return this.getStringProperty(SS_WIZARD_PASSWORD);
    }

    public String getStudySpaceWizardParametersUrl() {
        return this.getStringProperty(SS_WIZARD_PROPERTIES_URL);
    }

}
