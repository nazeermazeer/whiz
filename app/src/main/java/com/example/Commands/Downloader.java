package com.example.Commands;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

    public static final void getURLS(String url) {
        try {
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .timeout(10000)
                .get();

            Elements elements = doc.select("a[href]");
            for (Element element : elements) {
                String absUrl = element.absUrl("href");
                downloadFile(absUrl, null);
            }
        } catch (IOException err) {
            throw new RuntimeException();
        }
    }
}
