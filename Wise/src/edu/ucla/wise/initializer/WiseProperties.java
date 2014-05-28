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

import java.io.File;

import edu.ucla.wise.email.EmailProperties;
import edu.ucla.wise.shared.properties.AbstractWiseProperties;

public class WiseProperties extends AbstractWiseProperties implements EmailProperties {

    private static final long serialVersionUID = 1L;

    private static String EMAIL_FROM = "wise.email.from";
    private static String EMAIL_HOST = "email.host";
    private static String EMAIL_USERNAME = "email.authentication.username";
    private static String EMAIL_PASSWORD = "email.authentication.password";
    private static String ADMIN_SERVER = "admin.server";
    private static String IMAGES_PATH = "shared_image.path";
    private static String STYLES_PATH = "shared_style.path";
    private static String SSL_EMAIL = "email.ssl";
    private static String XML_LOC = "xml_root.path";
    private static final String SS_WIZARD_PROPERTIES_URL = "studyspacewizard.properties.url";
    private static final String EMAIL_AUTHENTICATION_PORT = "email.authentication.port";
    private static final String ALERT_EMAIL = "alert.email";
    private static String SS_WIZARD_PASSWORD = "studyspacewizard.response.encryption.key";
    private static final String SERVER_ROOT_URL = "server.rootURL";
    private static final String DB_BACKUP_PATH = "db_backup.path";
    private static final String DEFAULT_SHARED_FILES_LINKNAME = "default.sharedFiles_linkName";

    public WiseProperties(String fileName, String applicationName) {
        super(fileName, applicationName);
        if (!this.isValid()) {
            throw new IllegalArgumentException("The properties file is invalid");
        }
    }

    public boolean isValid() {
        boolean result = true;
        File xmlDir = new File(this.getXmlRootPath());
        result = result && xmlDir.isDirectory();
        return result;
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

    public String getEmailFrom() {
        return this.getStringProperty(EMAIL_FROM);
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

    public String getAlertEmail() {
        return this.getStringProperty(ALERT_EMAIL);
    }

    public String getServerRootUrl() {
        return this.getStringProperty(SERVER_ROOT_URL);
    }

    public String getDatabaseBackupPath() {
        return this.getStringProperty(DB_BACKUP_PATH);
    }

    public String getDefaultSharedFilesLinkName() {
        return this.getStringProperty(DEFAULT_SHARED_FILES_LINKNAME);
    }

    @Override
    public String getEmailUsername() {
        return this.getStringProperty(EMAIL_USERNAME);
    }

    @Override
    public String getEmailPassword() {
        return this.getStringProperty(EMAIL_PASSWORD);
    }

    @Override
    public String getEmailAuthenticationPort() {
        return this.getStringProperty(EMAIL_AUTHENTICATION_PORT);
    }

    @Override
    public String getEmailHost() {
        return this.getStringProperty(EMAIL_HOST);
    }

    @Override
    public boolean isUseSSL() {
        return "true".equalsIgnoreCase(this.getStringProperty(SSL_EMAIL));
    }

}
