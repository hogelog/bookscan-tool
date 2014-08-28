package org.hogel.bookscan;

import com.google.inject.Inject;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.hogel.bookscan.model.Book;
import org.hogel.bookscan.model.OptimizedBook;
import org.hogel.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class BookscanDownloader {
    private static final Logger LOG = LoggerFactory.getLogger(BookscanDownloader.class);

    private static final String USER_AGENT = "bookscan-tool-downloader";

    @Inject
    Config config;

    @Inject
    BookscanClient bookscanClient;

    OkHttpClient client = new OkHttpClient();

    @Inject
    public BookscanDownloader() {
    }

    public void download(Book book) throws IOException {
        String downloadUrl = book.createDownloadUrl();
        download(downloadUrl, book.getFilename());
    }

    public void download(OptimizedBook book) throws IOException {
        String downloadUrl = book.createDownloadUrl();
        download(downloadUrl, book.getFilename());
    }

    public void download(String downloadUrl, String filename) throws IOException {
        LOG.info("Start: downloading {}", filename);

        String downloadDirectory = config.getDownloadDirectory();

        Path downloadDirectoryPath = Paths.get(downloadDirectory);
        if (Files.notExists(downloadDirectoryPath)) {
            Files.createDirectory(downloadDirectoryPath);
        } else if (!Files.isDirectory(downloadDirectoryPath)) {
            throw new IOException(downloadDirectoryPath + " is not directory");
        }
        Path downloadPath = Paths.get(downloadDirectory, filename);
        if (Files.exists(downloadPath)) {
            LOG.info("{} is already exists", downloadPath);
            return;
        }

        Request request = createRequest(downloadUrl);
        Response response = client.newCall(request).execute();
        int responseCode = response.code();
        if (responseCode != 200) {
            LOG.error("Unknown Response: {}", response.code());
            return;
        }

        LOG.info("Response: 200, downloading...");
        try (InputStream downloadStream = response.body().byteStream()) {
            Files.copy(downloadStream, downloadPath);
        }

        LOG.info("Finish: downloading {}", filename);
    }

    private Request createRequest(String downloadUrl) {
        Map<String, String> cookies = bookscanClient.getCookies();
        return new Request.Builder()
            .url(downloadUrl)
            .addHeader("Cookie", packCookies(cookies))
            .addHeader("User-Agent", USER_AGENT)
            .get()
            .build();
    }

    private String packCookies(Map<String, String> cookies) {
        StringBuilder cookieBuilder = new StringBuilder();
        for (String name : cookies.keySet()) {
            if (cookieBuilder.length() > 0) {
                cookieBuilder.append("; ");
            }
            cookieBuilder.append(name).append("=").append(cookies.get(name));
        }
        return cookieBuilder.toString();
    }
}
