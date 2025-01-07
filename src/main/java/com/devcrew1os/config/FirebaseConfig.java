package com.devcrew1os.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account-key}")
    private String keyPath;

    public void initFirebase() {
        try {
            // extract SDK json
            InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream(keyPath.replace("classpath:", ""));
            if (serviceAccount == null) {
                throw new RuntimeException("Unable to find service account key file at " + keyPath);
            }

            // struct instance
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // check instance
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase has been initialized successfully.");
            } else {
                System.out.println("Firebase already initialized.");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize Firebase", e);
        }
    }
}
