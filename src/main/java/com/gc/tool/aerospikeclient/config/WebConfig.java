package com.gc.tool.aerospikeclient.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/connection");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(
            "/img/**",
            "/css/**",
            "/js/**")
            .addResourceLocations(
                "classpath:/static/img/",
                "classpath:/static/css/",
                "classpath:/static/js/");
    }
}
