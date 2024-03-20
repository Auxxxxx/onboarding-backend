package com.example.onboardingservice.web.config;

import com.example.onboardingservice.service.JsonParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
//@EnableMethodSecurity(securedEnabled = true)
public class WebConfig implements WebMvcConfigurer {
    private final JsonParserService jsonParserService;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new RequestDataArgumentResolver(jsonParserService));
        argumentResolvers.add(new VersionArgumentResolver());
    }
}
