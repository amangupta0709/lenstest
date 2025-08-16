package com.db.lenstest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/{spring:[^.]+}")
                .setViewName("forward:/index.html");
        registry.addViewController("/**/{spring:[^.]+}")
                .setViewName("forward:/index.html");
        registry.addViewController("/{spring:[^.]+}/**{rest:[^.]+}")
                .setViewName("forward:/index.html");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*");
    }
}

