package com.example.logintask;

public class User {
    private String email,name;
    private String emergencyContact;

    private User(){

    }

    public User(String email, String name, String emergencyContact) {
        this.email = email;
        this.name = name;
        this.emergencyContact = emergencyContact;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }
}
