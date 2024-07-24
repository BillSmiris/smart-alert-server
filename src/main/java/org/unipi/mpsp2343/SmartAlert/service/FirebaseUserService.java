package org.unipi.mpsp2343.SmartAlert.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.unipi.mpsp2343.SmartAlert.dtos.auth.*;
import org.unipi.mpsp2343.SmartAlert.utils.AuthUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class FirebaseUserService {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseUserService.class);
    @Value("${firebase.api.key}")
    private String firebaseApiKey;

    @Value("${firebase.verification-url}")
    private String verificationUrl;

    private final FirebaseAuth firebaseAuth;
    private final RestTemplate restTemplate;
    private final AuthUtils authUtils;

    public FirebaseUserService(FirebaseAuth firebaseAuth, AuthUtils authUtils) {
        this.firebaseAuth = firebaseAuth;
        this.authUtils = authUtils;
        this.restTemplate = new RestTemplate();
    }

    //Logs in the user. The function returns various errors for different failure scenarios. The error text is used in the app as a string resource
    // key to display human readable text in the selected language.
    public LoginResponse login(LoginRequest loginRequest) {
        //Sets up the login request in firebase and captures the retrieved user data
        FirebaseAuthRequest firebaseAuthRequest = new FirebaseAuthRequest(loginRequest.getEmail(), loginRequest.getPassword());

        FirebaseAuthResponse firebaseAuthResponse;
        try {
            firebaseAuthResponse = restTemplate.postForObject(verificationUrl + firebaseApiKey, firebaseAuthRequest, FirebaseAuthResponse.class);
        }
        catch (HttpClientErrorException e){
            try {
                String bodyString = e.getResponseBodyAsString();
                String errorMessage = "LOGIN_ERROR_" + (new ObjectMapper().readValue(bodyString.substring(12, bodyString.length() - 3), FirebaseAuthError.class)).getMessage();
                throw new RuntimeException(errorMessage);
            }
            catch (JsonProcessingException ex) {
                throw new RuntimeException("LOGIN_ERROR_UNEXPECTED_ERROR");
            }
        }

        if (firebaseAuthResponse == null || firebaseAuthResponse.getIdToken() == null) {
            throw new RuntimeException("LOGIN_ERROR_UNEXPECTED_ERROR");
        }

        UserRecord user;
        try {
            user = firebaseAuth.getUserByEmail(loginRequest.getEmail());
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("LOGIN_ERROR_UNEXPECTED_ERROR");
        }

        if (user.isDisabled()) {
            throw new RuntimeException("LOGIN_ERROR_ACCOUNT_DEACTIVATED");
        }

        String role;
        try {
            role = authUtils.getUserRole(user.getUid());
        } catch (Exception e) {
            throw new RuntimeException("LOGIN_ERROR_UNEXPECTED_ERROR");
        }

        if(!user.isEmailVerified()){
            throw new RuntimeException("LOGIN_ERROR_NOT_VERIFIED");
        }

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAuthToken(firebaseAuthResponse.getIdToken());
        loginResponse.setUserId(firebaseAuthResponse.getLocalId());
        loginResponse.setEmail(firebaseAuthResponse.getEmail());
        loginResponse.setRefreshToken(firebaseAuthResponse.getRefreshToken());
        loginResponse.setExpiresIn(firebaseAuthResponse.getExpiresIn());
        loginResponse.setRole(role);
        return loginResponse;
    }

    //Signs ups a use.
    public void signup(SignupRequest signupRequest) {
        try {
            //Creates a new user in firebase with the provided info
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(signupRequest.getEmail())
                    .setPassword(signupRequest.getPassword());



            UserRecord newUser = this.firebaseAuth.createUser(request);

            //Create a role for the user. Users that sign up through the API are assigned the USER role
            CompletableFuture<Void> createRoleFuture = authUtils.createUserRole(newUser.getUid(), "ROLE_USER");

            try {
                createRoleFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("SIGNUP_ERROR_UNEXPECTED_ERROR");
            }

            //A verification email is sent to the new user.
            authUtils.sendVerificationEmail(newUser.getUid());

        } catch (FirebaseAuthException | IllegalArgumentException e) {

            String authError = e.getMessage();
            String error = "UNEXPECTED_ERROR";

            if(authError.contains("EMAIL_EXISTS")) {
                error = "EMAIL_ALREADY_EXISTS";
            }
            else if(authError.contains("password must be at least")) {
                error = "PASSWORD_LENGTH";
            }
            else if(authError.equals("email cannot be null or empty")){
                error = "EMAIL_EMPTY";
            }
            else if(authError.equals("password cannot be null or empty")){
                error = "PASSWORD_EMPTY";
            }

            throw new RuntimeException("SIGNUP_ERROR_" + error);
        }
    }
}
