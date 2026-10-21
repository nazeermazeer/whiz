package com.example.Commands;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Downloader {
    private static final Logger logger = LogManager.getLogger(Downloader.class);

    public static final void downloadFile(String uri, Path path) {
        try {
            Document doc = Jsoup.connect(uri)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .referrer("https://google.com")
                .timeout(10000)
                .get();

            String content = doc.outerHtml();

            Files.writeString(path, content);
        } catch (HttpStatusException err) {
            logger.warn("{} error when downloading html file at url {}", err.getStatusCode(), err.getUrl(), err);
        } catch (IOException err) {
            throw new RuntimeException(err);
        }
    }

    public static final void getURLS(String url, Path path) {
        try {
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .referrer("https://google.com")
                .timeout(10000)
                .get();

            Elements elements = doc.select("a[href]");
            for (Element element : elements) {
                String absUrl = element.absUrl("href");
                
                String location = doc.location(); 
                String name = Paths.get(location).getFileName().toString();
                downloadFile(absUrl, path.resolve(name));
            }
        } catch (IOException err) {
            throw new RuntimeException();
        }
    }
}
