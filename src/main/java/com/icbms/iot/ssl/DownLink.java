package com.icbms.iot.ssl;

/**
 * @Author: Cherry
 * @Date: 2021/1/10
 * @Desc: DownLink
 */
public class DownLink {
    private boolean confirmed;
    private String data;
    private String devEUI;
    private int fPort;
    String reference;

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDevEUI() {
        return devEUI;
    }

    public void setDevEUI(String devEUI) {
        this.devEUI = devEUI;
    }

    public int getfPort() {
        return fPort;
    }

    public void setfPort(int fPort) {
        this.fPort = fPort;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
