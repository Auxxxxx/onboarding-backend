package com.example.onboardingservice;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.example.onboardingservice.model.Manager;
import com.example.onboardingservice.model.User;
import com.example.onboardingservice.service.AuthenticationService;
import com.example.onboardingservice.service.UserService;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;
import java.util.Optional;


@SpringBootApplication
@Slf4j
public class OnboardingServiceApplication extends SpringBootServletInitializer {
    @Value("${storage.credentials.key}")
    private String key;
    @Value("${storage.credentials.secret}")
    private String secret;
    @Value("${manager.credentials.email}")
    private String managerEmail;
    @Value("${manager.credentials.password}")
    private String managerPassword;

    public static void main(String[] args) {
        SpringApplication.run(OnboardingServiceApplication.class, args);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ApplicationListener<ContextRefreshedEvent> applicationListener() {
        return event -> {
            ApplicationContext applicationContext = event.getApplicationContext();
            applicationContext.getBean(RequestMappingHandlerMapping.class).getHandlerMethods()
                    .forEach((a, b) -> log.info(b.toString()));
        };
    }

    @Bean
    public AmazonS3 amazonS3() {
        AWSCredentials credentials = new BasicAWSCredentials(key, secret);
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(
                        new AmazonS3ClientBuilder.EndpointConfiguration(
                                "storage.yandexcloud.net", "ru-central1"
                        )
                )
                .build();
    }

    public @Bean OpenAPI noteAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("OnboardingService API")
                                .description("A CRUD API for the Onboarding Service")
                );
    }

    @Bean
    public CommandLineRunner commandLineRunner(UserService userService,
                                               AuthenticationService authenticationService) {
        return args -> {
            try {
                Optional<User> existingManager = userService.findByEmailOptional(managerEmail);
                if (existingManager.isEmpty()) {
                    userService.save(Manager.builder()
                            .email(managerEmail)
                            .password(passwordEncoder().encode(managerPassword))
                            .build());
                }
                //authenticationService.signIn("bill_edwards@gmail.com", "cookie123");
                authenticationService.register("Bill Edwards", "bill_edwards@gmail.com", "cookie123");
                authenticationService.signIn("bill_edwards@gmail.com", "cookie123");
                userService.updateClient(
                        "bill_edwards@gmail.com",
                        "Bob edwards",
                        List.of("default1", "default2", "default3", "default4", "default5", "default6"),
                        List.of("first steps", "common client", "partner"),
                        1L);
            } catch (Exception e) {
                log.error("error_on_command_line_runner");
                log.error(e.getMessage() + " " + e.getCause());
            }
        };
    }

}
