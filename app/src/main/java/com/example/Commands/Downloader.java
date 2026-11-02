package com.example.Commands;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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

    public static final void downloadFile(String uri, Path path, Connection session) throws IOException {
        try {
            Document doc = session.newRequest(uri).get();

            String content = doc.outerHtml();

            Files.writeString(path, content);
        } catch (HttpStatusException err) {
            logger.warn("{} error when downloading html file at url {}", err.getStatusCode(), err.getUrl(), err);
        }
    }

    public static final List<String> getURLS(String url, Path path, Connection session) throws IOException {
        Document doc = session.newRequest(url).get();

        List<String> urls = new ArrayList<>();

        Elements elements = doc.select("a");
        for (Element element : elements) {
            String linkedurl = element.attr("abs:href");
            urls.add(linkedurl);

            if (linkedurl.contains("python")) {
                for (String recursiveurl : getURLS(url, path, session)) {
                    urls.add(recursiveurl);
                }
            }
        }

        return urls;
    }

    public static final void downloadWebsite(String url, Path path) {
        Connection session = Jsoup.newSession()
            .timeout(45 * 1000)
            .maxBodySize(0);

        List<String> urls = new ArrayList<>();

        try {
            urls = getURLS(url, path, session);
        } catch (IOException e) {
            int retrycount = 0;
            logger.warn("retrying first retrival of website urls", e);
            try {
                urls = getURLS(url, path, session);
            } catch (IOException err) {
                retrycount++;
                logger.warn("retrying website url retrieval... total retry count: {}", retrycount, err);
                if (retrycount >= 3) {
                    throw new RuntimeException(err);
                }
            }
        }

        for (String websiteurl : urls) {
            String name = Paths.get(websiteurl).getFileName().toString();
            try {
                downloadFile(websiteurl, path.resolve(name), session);
            } catch (IOException e) {
                int retrycount = 0;
                logger.warn("retrying initial download of website url {}", websiteurl, e);
                try {
                    downloadFile(websiteurl, path.resolve(name), session);
                } catch (IOException err) {
                    retrycount++;
                    logger.warn("retrying download of website... total retry count: {}", retrycount, err);
                    if (retrycount >= 3) {
                        throw new RuntimeException(err);
                    }
                }
            }
        }
    }
}
