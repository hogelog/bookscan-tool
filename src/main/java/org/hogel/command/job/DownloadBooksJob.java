package org.hogel.command.job;

import com.google.inject.Inject;
import org.hogel.bookscan.BookscanClient;
import org.hogel.bookscan.BookscanDownloader;
import org.hogel.bookscan.model.Book;
import org.hogel.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DownloadBooksJob extends AbstractJob {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadBooksJob.class);

    @Inject
    Config config;

    @Inject
    BookscanClient bookscanClient;

    @Inject
    BookscanDownloader downloader;

    @Override
    public void run() throws Exception {
        LOG.info("Start: downloading books");
        List<Book> books = bookscanClient
            .fetchBooks()
            .timeout(config.getTimeout())
            .get();

        long wait = config.getWait();
        for (Book book : books) {
            downloader.download(book);
            Thread.sleep(wait);
        }
        LOG.info("Finish: downloading books");
    }
}
