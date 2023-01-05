package com.moroney.transportationapp.database;

public class Ticket {
    private String userID;
    private String placeOfOrigin;
    private String placeOfDestination;
    private String date;
    private String time;
    private String ticketID;
    private String ticketType;
    private String qrCodeUrl;
    private String bookedOn;

    public Ticket() {
    }

    public Ticket(String userID, String placeOfOrigin, String placeOfDestination, String date, String time, String ticketID, String ticketType, String bookedOn) {
        this.userID = userID;
        this.placeOfOrigin = placeOfOrigin;
        this.placeOfDestination = placeOfDestination;
        this.date = date;
        this.time = time;
        this.ticketID = ticketID;
        this.ticketType = ticketType;
        this.bookedOn = bookedOn;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPlaceOfOrigin() {
        return placeOfOrigin;
    }

    public void setPlaceOfOrigin(String placeOfOrigin) {
        this.placeOfOrigin = placeOfOrigin;
    }

    public String getPlaceOfDestination() {
        return placeOfDestination;
    }

    public void setPlaceOfDestination(String placeOfDestination) {
        this.placeOfDestination = placeOfDestination;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTicketID() {
        return ticketID;
    }

    public void setTicketID(String ticketID) {
        this.ticketID = ticketID;
    }

    public String getTicketType() {
        return ticketType;
    }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public String getBookedOn() {
        return bookedOn;
    }

    public void setBookedOn(String bookedOn) {
        this.bookedOn = bookedOn;
    }
}