package org.smarthata.service.message;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class SmarthataMessage {

    private String path;

    private String text;

    private EndpointType source;
    private EndpointType destination;

    private boolean retained = false;

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
