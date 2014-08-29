package org.hogel.bookscantool.command.job;

import com.google.inject.Inject;
import org.hogel.bookscan.BookscanClient;
import org.hogel.bookscan.model.OptimizedBook;
import org.hogel.bookscantool.bookscan.BookscanDownloader;
import org.hogel.bookscantool.config.BookscanToolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DownloadOptimizedBooksJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadOptimizedBooksJob.class);

    @Inject
    BookscanToolConfig config;

    @Inject
    BookscanClient bookscanClient;

    @Inject
    BookscanDownloader downloader;

    @Override
    public void run() throws Exception {
        LOG.info("Start: downloading optimized books");
        List<OptimizedBook> books = bookscanClient
            .fetchOptimizedBooks()
            .get();

        long wait = config.getWait();
        for (OptimizedBook book : books) {
            downloader.download(book);
            Thread.sleep(wait);
        }
        LOG.info("Finish: downloading optimized books");
    }
}
