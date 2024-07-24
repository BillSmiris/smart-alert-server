package org.unipi.mpsp2343.SmartAlert.model;

//Model that represents a report of an event.
public class Report {
    String comments; //Comments from the user that reported the event.
    String photoBase64; //Photo of the report encoded in base64
    String userEmail; //Email of the user that reported the event.

    public Report() {
    }

    public Report(String comments, String photoBase64, String userEmail) {
        this.comments = comments;
        this.photoBase64 = photoBase64;
        this.userEmail = userEmail;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getPhotoBase64() {
        return photoBase64;
    }

    public void setPhotoBase64(String photoBase64) {
        this.photoBase64 = photoBase64;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
