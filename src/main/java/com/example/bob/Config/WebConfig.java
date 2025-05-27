package com.example.bob.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/profileImages/**")
                .addResourceLocations("file:uploads/profileImages/");

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/uploads/resume/");

        registry.addResourceHandler("/uploads/projectFiles/**")
                .addResourceLocations("file:./uploads/projectFiles/");

    }


}
