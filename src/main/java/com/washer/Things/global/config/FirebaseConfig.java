//package com.washer.Things.global.config;
//
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.firebase.FirebaseApp;
//import com.google.firebase.FirebaseOptions;
//import jakarta.annotation.PostConstruct;
//import org.springframework.context.annotation.Configuration;
//
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//
//@Configuration
//public class FirebaseConfig {
//
//    @PostConstruct
//    public void initialize() {
//        try (InputStream serviceAccount = getClass().getClassLoader()
//                .getResourceAsStream("washer-b7c5a-firebase-adminsdk-fbsvc-97d813a8cd.json")) {
//
//            if (serviceAccount == null) {
//                throw new RuntimeException("firebase-service-account 파일을 찾을 수 없습니다.");
//            }
//
//            FirebaseOptions options = FirebaseOptions.builder()
//                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                    .build();
//
//            if (FirebaseApp.getApps().isEmpty()) {
//                FirebaseApp.initializeApp(options);
//            }
//        } catch (IOException e) {
//            throw new RuntimeException("Firebase 초기화 실패", e);
//        }
//    }
//}
