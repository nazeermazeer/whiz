package com.example.Commands;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Downloader {
    private static final Logger logger = LogManager.getLogger(Downloader.class);

    public static final void downloadFile(String uri, Path path, Connection session) {
        try {
            Document doc = session.newRequest(uri).get();

            String content = doc.outerHtml();

            Files.writeString(path, content);
        } catch (HttpStatusException err) {
            logger.warn("{} error when downloading html file at url {}", err.getStatusCode(), err.getUrl(), err);
        } catch (IOException err) {
            throw new RuntimeException(err);
        }
    }

    public static final void getURLS(String url, Path path) throws IOException {
        Connection session = Jsoup.newSession()
            .timeout(45 * 1000)
            .maxBodySize(0);

        Document doc = session.newRequest(url).get();

        Elements elements = doc.select("a");
        for (Element element : elements) {
            String linkedurl = element.attr("abs:href");
            
            String name = Paths.get(linkedurl).getFileName().toString();

            downloadFile(linkedurl, path.resolve(name), session);
        }
    }

    public static final void downloadWebsite(String url, Path path) {
        try {
            getURLS(url, path);
        } catch (IOException e) {
            int retrycount = 0;
            try {
                getURLS(url, path);
            } catch (IOException err) {
                retrycount++;
                if (retrycount >= 3) {
                    throw new RuntimeException(err);
                }
            }
        }
    }
}
