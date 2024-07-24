package org.unipi.mpsp2343.SmartAlert.controller;

import com.google.firebase.auth.FirebaseAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.unipi.mpsp2343.SmartAlert.dtos.auth.LoginRequest;
import org.unipi.mpsp2343.SmartAlert.dtos.auth.LoginResponse;
import org.unipi.mpsp2343.SmartAlert.dtos.auth.SignupRequest;
import org.unipi.mpsp2343.SmartAlert.service.FirebaseUserService;

//Controller for handling requests related to authentication. No user login required for access.
@RestController
@RequestMapping("api/v1/public/auth")
public class AuthorizationController {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationController.class);
    private final FirebaseUserService firebaseUserService;

    public AuthorizationController(FirebaseUserService firebaseUserService) {
        this.firebaseUserService = firebaseUserService;
    }

    //Sings in a user
    @PostMapping("login")
    private ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = firebaseUserService.login(loginRequest);
            return ResponseEntity.ok().body(response);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    //Signs up (creates) a user
    @PostMapping("signup")
    private ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) {
        try {
            firebaseUserService.signup(signupRequest);
            return ResponseEntity.ok().body("");
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
