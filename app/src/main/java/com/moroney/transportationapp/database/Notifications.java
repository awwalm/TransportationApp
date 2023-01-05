package com.moroney.transportationapp.database;

public class Notifications
{

    String dateTime;
    String from;
    String fromExactNameEmail;
    String ticketID;
    String type;
    String destination;
    String origin;

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getFromExactNameEmail() {
        return fromExactNameEmail;
    }

    public void setFromExactNameEmail(String fromExactNameEmail) {
        this.fromExactNameEmail = fromExactNameEmail;
    }

    public String getTicketID() {
        return ticketID;
    }

    public void setTicketID(String ticketID) {
        this.ticketID = ticketID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public Notifications(String dateTime, String from, String fromExactNameEmail, String ticketID, String type, String destination, String origin) {
        this.dateTime = dateTime;
        this.from = from;
        this.fromExactNameEmail = fromExactNameEmail;
        this.ticketID = ticketID;
        this.type = type;
        this.destination = destination;
        this.origin = origin;
    }

    public Notifications() {
    }



}
