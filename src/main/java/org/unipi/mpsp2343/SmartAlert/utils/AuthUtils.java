package org.unipi.mpsp2343.SmartAlert.utils;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import org.unipi.mpsp2343.SmartAlert.model.Role;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

/*--------------------------------------------------
 |This class contains various functions            |
 |that assist in user authentication/authorization.|
---------------------------------------------------*/
@Component
public class AuthUtils {
    @Value("${firebase.api.key}")
    private String firebaseApiKey;

    private final CollectionReference roles;

    public AuthUtils(Firestore firestore) {
        this.roles = firestore.collection("roles");
    }

    //This function sends a verification email to a user that has just signed up.
    public void sendVerificationEmail(String userId) {
        try {
            String customToken = FirebaseAuth.getInstance().createCustomToken(userId);
            String idToken = exchangeCustomTokenForIdToken(customToken);

            String requestUrl = "https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=" + firebaseApiKey;
            String requestBody = String.format("{\"requestType\":\"VERIFY_EMAIL\",\"idToken\":\"%s\"}", idToken);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForObject(requestUrl, requestBody, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Error sending verification email: " + e.getMessage(), e);
        }
    }

    //Retrieves an id token form firebase to send with a following request to identity toolkit
    private  String exchangeCustomTokenForIdToken(String customToken) {
        try {
            String requestUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithCustomToken?key=" + firebaseApiKey;
            String requestBody = String.format("{\"token\":\"%s\",\"returnSecureToken\":true}", customToken);
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> response = restTemplate.postForObject(requestUrl, requestBody, Map.class);
            if (response == null || !response.containsKey("idToken")) {
                throw new RuntimeException("Failed to exchange custom token for ID token");
            }

            return response.get("idToken").toString();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error exchanging custom token for ID token: " + e.getResponseBodyAsString(), e);
        }
    }

    //This function retrieves the role of a user. It is used during the log in process to get the role of the user.
    public String getUserRole(String userId) throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = new CompletableFuture<>();
        Query query = roles.whereEqualTo("userId", userId);

        ApiFuture<QuerySnapshot> querySnapshotApiFuture = query.get();
        querySnapshotApiFuture.addListener(() -> {
            try {
                QuerySnapshot querySnapshot = querySnapshotApiFuture.get();
                List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
                if (!documents.isEmpty()) {
                    Role role = documents.get(0).toObject(Role.class);
                    if (role != null) {
                        future.complete(role.getRole());
                    } else {
                        future.complete(null);
                    }
                } else {
                    future.complete(null);
                }
            } catch (InterruptedException | ExecutionException e) {
                future.completeExceptionally(e);
            }
        }, Executors.newSingleThreadExecutor());

        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw e;
        }
    }

    //This function creates a role entry in the database for a freshly created user.
    public CompletableFuture<Void> createUserRole(String userId, String role) {
        CompletableFuture<Void> future  = new CompletableFuture<>();
        DocumentReference newRole = roles.document();
        ApiFuture<WriteResult> writeResultApiFuture = newRole.set(new Role(userId, role));

        writeResultApiFuture.addListener(() -> {
            try {
                writeResultApiFuture.get();
                future.complete(null);
            } catch (InterruptedException | ExecutionException e) {
                future.completeExceptionally(e);
            }
        }, Executors.newSingleThreadExecutor());

        return future;
    }
}
