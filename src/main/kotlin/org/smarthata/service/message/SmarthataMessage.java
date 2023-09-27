package org.smarthata.service.message;


public class SmarthataMessage {

    public String path;

    public String text;

    public EndpointType source;
    public EndpointType destination;

    public boolean retained = false;

    public SmarthataMessage(String path, String text, EndpointType source) {
        this.path = path;
        this.text = text;
        this.source = source;
    }

    public SmarthataMessage(String path, String text, EndpointType source, EndpointType destination) {
        this.path = path;
        this.text = text;
        this.source = source;
        this.destination = destination;
    }

    public SmarthataMessage(String path, String text, EndpointType source, EndpointType destination, boolean retained) {
        this.path = path;
        this.text = text;
        this.source = source;
        this.destination = destination;
        this.retained = retained;
    }
}
