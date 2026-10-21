package com.example.Commands;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Downloader {
    public static final void downloadFile(String uri, Path path) {
        try {
            Document doc = Jsoup.connect(uri)
                                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                                .timeout(10000)
                                .get();

            String content = doc.outerHtml();

            Files.writeString(path, content);
        } catch (IOException err) {
            throw new RuntimeException(err);
        }
    }
}
