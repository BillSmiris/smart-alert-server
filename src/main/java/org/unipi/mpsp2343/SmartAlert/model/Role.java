package org.unipi.mpsp2343.SmartAlert.model;

//Model for representing the user roles in the db.
public class Role {
    String userId;
    String role;

    public Role() {
    }

    public Role(String userId, String role) {
        this.userId = userId;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
