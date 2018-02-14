package com.adyen.mirakl.config;

public abstract class MiraklProperties {

    private String miraklEnvUrl;
    private String miraklApiKey;


    public String getMiraklEnvUrl() {
        return miraklEnvUrl;
    }

    public void setMiraklEnvUrl(final String miraklEnvUrl) {
        this.miraklEnvUrl = miraklEnvUrl;
    }

    public String getMiraklApiKey() {
        return miraklApiKey;
    }

    public void setMiraklApiKey(final String miraklApiKey) {
        this.miraklApiKey = miraklApiKey;
    }

}
