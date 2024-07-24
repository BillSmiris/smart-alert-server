package org.unipi.mpsp2343.SmartAlert.configuration.security;

import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.unipi.mpsp2343.SmartAlert.controller.AuthorizationController;

import java.util.Collection;
import java.util.List;

//Authentication principal class for firebase authentication tokens. Provides getters for some important and commonly used user info in the app.
public class FirebaseAuthenticationToken extends AbstractAuthenticationToken {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthenticationToken.class);
    private FirebaseToken firebaseToken;
    private String idToken;

    public FirebaseAuthenticationToken(Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
    }

    public FirebaseAuthenticationToken(
            String idToken, FirebaseToken firebaseToken, List<GrantedAuthority> authorities) {
        super(authorities);
        this.idToken = idToken;
        this.firebaseToken = firebaseToken;
    }

    @Override
    public Object getCredentials() {
        return idToken;
    }

    @Override
    public Object getPrincipal() {
        return firebaseToken.getUid();
    }

    public String getEmail() {
        return this.firebaseToken.getEmail();
    }
}