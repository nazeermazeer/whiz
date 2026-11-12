package com.example.Commands;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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

import dev.tamboui.widgets.chart.Dataset;
import dev.tamboui.widgets.chart.GraphType;

public class Downloader {
    private static final Logger logger = LogManager.getLogger(Downloader.class);
    private String status = "";
    private boolean stop = false;

    private LocalDateTime start = LocalDateTime.now();
    private int success = 0;
    private int snooze = 0;
    private int ignored = 0;
    private int comprehend = 0;

    private static double[][] data = new double[][] { {0, 0} };

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
            status += 
                "[red][italic]ignoring a url located [blue][link=" + err.getUrl() + 
                "]here[/blue][/link] as it returned a status code of " + err.getStatusCode() + " " + err.getMessage();
            ignored++;
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

            status += "imagining '" + uripath + "', which is located [blue][link=" + linkedurl + "]here[/link][/blue]\n";
            urls.add(linkedurl);
            success++;

            if (linkedurl.contains(root)) {
                downloadFile(linkedurl, path.resolve(uripath), session);

                downloadURLS(linkedurl, root, path, session, urls);
            }
        }

        status += "[green][bold]comprehending a url located [blue][link=" + url + "]here[/blue][/link], which has " +  elements.size() + " urls[/green][/bold]\n";
        comprehend++;
        return urls;
    }

    public final void downloadWebsite(String url, Path path) {
        start = LocalDateTime.now();

        Connection session = Jsoup.newSession()
            .timeout(30 * 1000)
            .maxBodySize(0)
            .onResponseProgress((processed, total, percent, response) -> {
                System.out.println("percent: " + percent + ", processed: " + processed + "/" + total);
            });

        List<String> urls = new ArrayList<>();

        Path root = Path.of(Main.getAppDirectory(), "python-3.14-docs");
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
            status += "[yellow][bold]snoozin' (attempt " + retrycount + "/3)[/yellow][/bold]\n";
            snooze++;
            try {
                urls = downloadURLS(url, url, path, session, urls);
            } catch (IOException err) {
                retrycount++;
                logger.warn("retrying website url retrieval... total retry count: {}", retrycount, err);
                status += "[yellow][bold]snoozin' (attempt " + retrycount + "/3)[/yellow][/bold]\n";
                snooze++;
                if (retrycount >= 3) {
                    throw new RuntimeException(err);
                }
            }
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(start, now);
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        status += "[bold]download finished![/bold]\n\n\n";
        status += "[bold]finished operation over the course of " + days + "d " + hours + "h " + minutes + "m " + seconds + "s[/bold]\n";
        status += " - [blue][bold]imagined " + success + " files[/blue][/bold]\n";
        status += " - [green][bold]comprehended " + comprehend + " urls[/green][/bold]\n";
        status += " - [yellow][bold]snoozed " + snooze + " times[/yellow][/bold]\n";
        status += " - [red][bold]ignored " + ignored + " entries[/red][/bold]\n";
        status += "\n";
    }

    public final String getStatus() {
        return status;
    }

    public final LocalDateTime getDownloadDateTime() {
        return start;
    }

    public final void stopDownloads() {
        stop = true;
    }

    public final Dataset.Builder getDatasetBuilder() {
        data = Arrays.copyOf(data, data.length + 1);
        data[data.length - 1] = new double[]{1, 2};

        Dataset.Builder dataset = Dataset.builder()
            .data(data);

        return dataset;
    }
}
