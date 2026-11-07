package com.example.Commands;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
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

import com.example.Main;

public class Downloader {
    private static final Logger logger = LogManager.getLogger(Downloader.class);
    private String status = "";
    private int items = 0;
    private boolean stop = false;

    public final void downloadFile(String uri, Path path, Connection session) throws IOException {
        if (stop) {
            return;
        }

        try {
            Document doc = session.newRequest(uri).get();

            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            String content = doc.outerHtml();

            Files.writeString(path, content);
        } catch (HttpStatusException err) {
            logger.warn("{} error when downloading html file at url {}", err.getStatusCode(), err.getUrl(), err);
        }
    }

    public final List<String> downloadURLS(String url, String root, Path path, Connection session, List<String> urls) throws IOException {
        Document doc = session.newRequest(url).get();

        Elements elements = doc.select("a");
        for (Element element : elements) {
            if (stop) {
                return urls;
            }

            String abshref = element.attr("abs:href");
            String linkedurl, uripath;

            try {
                URI uri = new URI(abshref);
                uripath = uri.getPath();

                linkedurl = new URI(
                    uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uripath, uri.getQuery(), null
                ).toString();
            } catch (URISyntaxException err) {
                logger.warn("falling back to unstripped url for link {}", abshref, err);
                linkedurl = abshref;
                uripath = "";
            }

            uripath = uripath.replace("/3.14/", "");

            if (urls.contains(linkedurl)) {
                continue;
            }

            status += "imagining '" + uripath + "' located at " + linkedurl + "\n";
            urls.add(linkedurl);
            items++;

            if (linkedurl.contains(root)) {
                downloadFile(linkedurl, path.resolve(uripath), session);

                downloadURLS(linkedurl, root, path, session, urls);
            }
        }

        return urls;
    }

    public final void downloadWebsite(String url, Path path) {
        Connection session = Jsoup.newSession()
            .timeout(45 * 1000)
            .maxBodySize(0);

        List<String> urls = new ArrayList<>();

        Path root = Path.of(Main.getAppDirectory(), "python");
        try {
            Files.createDirectories(root);
        } catch (IOException err) {
            throw new RuntimeException(err);
        }

        try {
            urls = downloadURLS(url, url, path, session, urls);
        } catch (IOException e) {
            int retrycount = 0;
            logger.warn("retrying first retrival of website urls", e);
            try {
                urls = downloadURLS(url, url, path, session, urls);
            } catch (IOException err) {
                retrycount++;
                logger.warn("retrying website url retrieval... total retry count: {}", retrycount, err);
                if (retrycount >= 3) {
                    throw new RuntimeException(err);
                }
            }
        }
    }

    public final String getStatus() {
        return status;
    }

    public final int getTotalItemCount() {
        return items;
    }

    public final void stopDownloads() {
        stop = true;
    }
}
