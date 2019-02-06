package com.lokoproject.mailing.notification.template.element;

/**
 * @author Antonlomako. created on 22.01.2019.
 */
public class Link extends Text {
    private String destination;

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
