package com.example.bob.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/profileImages/**")
                .addResourceLocations("file:uploads/profileImages/");

        registry.addResourceHandler("/uploads/resumeFiles/**")
                .addResourceLocations("file:uploads/resumeFiles/");

        registry.addResourceHandler("/uploads/projectFiles/**")
                .addResourceLocations("file:./uploads/projectFiles/");

        registry.addResourceHandler("/uploads/chat/**")
                .addResourceLocations("file:uploads/chat/");

        registry.addResourceHandler("/uploads/boardFiles/**")
                .addResourceLocations("file:uploads/boardFiles/");

    }

    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.mediaType("wasm", MediaType.valueOf("application/wasm"));
    }
}
