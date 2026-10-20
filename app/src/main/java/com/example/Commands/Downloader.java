package com.example.Commands;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Downloader {
    public static final void downloadFile(String uri, Path path) {
        try (InputStream in = URI.create(uri).toURL().openStream()) {
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException err) {
            throw new RuntimeException(err);
        }
    }
}
