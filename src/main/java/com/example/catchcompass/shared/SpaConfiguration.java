package com.example.catchcompass.shared;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Serves the built React app, and hands unknown paths back to it.
 *
 * <p>The problem this solves: React Router owns routes like /catches/3, but the
 * server has never heard of them. Clicking a link works because the router
 * handles it in the browser, but refreshing that page asks the server directly
 * and gets a 404. Returning index.html instead lets React boot and read the URL
 * itself.
 */
@Configuration
public class SpaConfiguration implements WebMvcConfigurer {

    private static final String[] SERVER_OWNED_PREFIXES = {"api/", "actuator/"};

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {

                    @Override
                    protected Resource getResource(String resourcePath, Resource location)
                            throws IOException {

                        Resource requested = location.createRelative(resourcePath);
                        if (requested.exists() && requested.isReadable()) {
                            return requested;
                        }

                        // Never answer for the server's own namespaces. Without
                        // this, a typo like /api/catchez would return the HTML
                        // page with a 200, and the client would try to parse a
                        // web page as JSON.
                        for (String prefix : SERVER_OWNED_PREFIXES) {
                            if (resourcePath.startsWith(prefix)) {
                                return null;
                            }
                        }

                        return new ClassPathResource("/static/index.html");
                    }
                });
    }
}
