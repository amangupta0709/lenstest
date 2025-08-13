package com.db.lenstest.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Configuration
@EnableReactiveMongoRepositories(
        basePackages = "com.db.lenstest.lensRepository",
        reactiveMongoTemplateRef = "reactiveMongoTemplate"
)
public class ReactiveMongoConfig {
    @Bean
    public MongoClient reactiveMongoClient() {
        try {
            TrustManager[] trustAllCertificates = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());

            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new com.mongodb.ConnectionString("mongodb://localhost:27017"))
                    .applyToSslSettings(builder -> builder.context(sslContext))
                    .build();

            return MongoClients.create(settings);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create MongoClient with custom SSL settings", e);
        }
    }

    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate(MongoClient reactiveMongoClient){
           return new ReactiveMongoTemplate(reactiveMongoClient, "lenstest");
    }
}
