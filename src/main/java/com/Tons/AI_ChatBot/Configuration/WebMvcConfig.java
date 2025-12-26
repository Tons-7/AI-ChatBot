package com.Tons.AI_ChatBot.Configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig {

    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        Path upload_dir = Paths.get("uploads");
        String upload_path = upload_dir.toFile().getAbsolutePath();

        registry
                .addResourceHandler("/uploads/**")
                .addResourceLocations("file: " + upload_path + "/")
                .setCachePeriod(3600);
    }
}
