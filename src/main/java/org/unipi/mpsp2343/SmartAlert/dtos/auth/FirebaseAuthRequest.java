package org.unipi.mpsp2343.SmartAlert.dtos.auth;

//Model containing the required data to make an authentication request to firebase.
public class FirebaseAuthRequest {
    private String email;
    private String password;
    private boolean returnSecureToken;

    public FirebaseAuthRequest(String email, String password) {
        this.email = email;
        this.password = password;
        this.returnSecureToken = true;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isReturnSecureToken() {
        return returnSecureToken;
    }

    public void setReturnSecureToken(boolean returnSecureToken) {
        this.returnSecureToken = returnSecureToken;
    }

    @Override
    public String toString() {
        return "FirebaseAuthRequest{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", returnSecureToken=" + returnSecureToken +
                '}';
    }
}
