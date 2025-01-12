package com.devcrew1os.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.service-account-key}")
    private String keyPath;

    @PostConstruct
    public void initFirebase() {
        try {
            // extract SDK json
            InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream(keyPath);
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
                logger.info("[FirebaseConfig] Successfully initialized firebase app");
            } else {
                logger.info("[FirebaseConfig] Already initialized firebase app");
            }
        } catch (IOException e) {
            logger.error("[FirebaseConfig] Failed to initialize Firebase: {}", e.getMessage());
            throw new IllegalStateException("Failed to initialize Firebase", e);
        }
    }
}
