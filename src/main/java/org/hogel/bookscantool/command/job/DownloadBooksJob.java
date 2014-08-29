package org.hogel.bookscantool.command.job;

import com.google.inject.Inject;
import org.hogel.bookscan.BookscanClient;
import org.hogel.bookscan.model.Book;
import org.hogel.bookscantool.bookscan.BookscanDownloader;
import org.hogel.bookscantool.config.BookscanToolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DownloadBooksJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadBooksJob.class);

    @Inject
    BookscanToolConfig config;

    @Inject
    BookscanClient bookscanClient;

    @Inject
    BookscanDownloader downloader;

    @Override
    public void run() throws Exception {
        LOG.info("Start: downloading books");
        List<Book> books = bookscanClient
            .fetchBooks()
            .get();

        long wait = config.getWait();
        for (Book book : books) {
            downloader.download(book);
            Thread.sleep(wait);
        }
        LOG.info("Finish: downloading books");
    }
}
