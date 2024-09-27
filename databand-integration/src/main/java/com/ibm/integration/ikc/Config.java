/*
 * Copyright IBM Corp. 2024
 *
 * The following sample of source code ("Sample") is owned by International
 * Business Machines Corporation or one of its subsidiaries ("IBM") and is
 * copyrighted and licensed, not sold. You may use, copy, modify, and
 * distribute the Sample in any form without payment to IBM, for the purpose of
 * assisting you in the development of your applications.
 *
 * The Sample code is provided to you on an "AS IS" basis, without warranty of
 * any kind. IBM HEREBY EXPRESSLY DISCLAIMS ALL WARRANTIES, EITHER EXPRESS OR
 * IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. Some jurisdictions do
 * not allow for the exclusion or limitation of implied warranties, so the above
 * limitations or exclusions may not apply to you. IBM shall not be liable for
 * any damages you suffer as a result of using, copying, modifying or
 * distributing the Sample, even if IBM has been advised of the possibility of
 * such damages.
 */
package com.ibm.integration.ikc;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

    public static final String IKC_HOST = "IKC_HOST";
    public static final String IKC_IKC_BASIC_AUTH_TOKEN = "IKC_BASIC_AUTH_TOKEN";
    public static final String PROJECT_ID = "PROJECT_ID";
    public static final String DQ_ASSET_ID = "DQ_ASSET_ID";
    public static final String DATABAND_URL = "DATABAND_URL";
    public static final String DATABAND_TOKEN = "DATABAND_TOKEN";
    private static final String CONFIG_FILE = "config.properties";
    private static final Properties config = new Properties();

    private final static Config instance = new Config();

    private Config() {
        readConfig();
    }

    public static Config getInstance() {
        return instance;
    }

    public String getIkcHost() {
        return config.getProperty(IKC_HOST);
    }

    public String getBasicToken() {
        return config.getProperty(IKC_IKC_BASIC_AUTH_TOKEN);
    }

    public String getProjectId() {
        return config.getProperty(PROJECT_ID);
    }

    public String getDqAssetId() {
        return config.getProperty(DQ_ASSET_ID);
    }

    public String getDatabandUrl() {
        return config.getProperty(DATABAND_URL);
    }

    public String getDatabandToken() {
        return config.getProperty(DATABAND_TOKEN);
    }

    private void readConfig() {
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            config.load(fis);

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
