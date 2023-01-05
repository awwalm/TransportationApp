package com.moroney.transportationapp.database;


public class User
{
    public String
            userID,
            name,
            email,
            groupCode,
            gender,
            favoriteCity,
            deviceToken,
            dpUrl;

    public User() { }

    public User(String userID, String name, String email, String groupCode, String gender, String favoriteCity, String deviceToken, String dpUrl) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.groupCode = groupCode;
        this.gender = gender;
        this.favoriteCity = favoriteCity;
        this.deviceToken = deviceToken;
        this.dpUrl = dpUrl;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getFavoriteCity() {
        return favoriteCity;
    }

    public void setFavoriteCity(String favoriteCity) {
        this.favoriteCity = favoriteCity;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getDpUrl() {
        return dpUrl;
    }

    public void setDpUrl(String dpUrl) {
        this.dpUrl = dpUrl;
    }
}