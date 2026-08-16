package com.example.catchcompass.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

/**
 * Where uploaded photos live on disk.
 *
 * <p>Deliberately a path outside {@code src/main/resources/static}, so photos are
 * never served as static content and can only be reached through a controller
 * that checks ownership first.
 */
@ConfigurationProperties(prefix = "catchcompass.storage")
public record StorageProperties(Path photoDirectory) {
}
