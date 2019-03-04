package org.smarthata.service.message;

public class SmarthataMessage {

    public static final String SOURCE_MQTT = "MQTT";
    public static final String SOURCE_TM = "TM";
    public static final String SOURCE_CRON = "CRON";

    private String path;

    private String text;

    private String source;

    private boolean retained = false;

    public SmarthataMessage() {
    }

    public SmarthataMessage(String path, String text, String source) {
        this.path = path;
        this.text = text;
        this.source = source;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isRetained() {
        return retained;
    }

    public void setRetained(boolean retained) {
        this.retained = retained;
    }

    @Override
    public String toString() {
        return "SmarthataMessage{" +
                "path='" + path + '\'' +
                ", text='" + text + '\'' +
                ", source='" + source + '\'' +
                ", retained=" + retained +
                '}';
    }
}
