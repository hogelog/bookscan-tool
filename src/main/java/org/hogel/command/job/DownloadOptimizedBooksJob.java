package org.hogel.command.job;

import com.google.inject.Inject;
import org.hogel.bookscan.BookscanClient;
import org.hogel.bookscan.BookscanDownloader;
import org.hogel.bookscan.model.OptimizedBook;
import org.hogel.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DownloadOptimizedBooksJob extends AbstractJob {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadOptimizedBooksJob.class);

    @Inject
    Config config;

    @Inject
    BookscanClient bookscanClient;

    @Inject
    BookscanDownloader downloader;

    @Override
    public void run() throws Exception {
        LOG.info("Start: downloading optimized books");
        List<OptimizedBook> books = bookscanClient
            .fetchOptimizedBooks()
            .timeout(config.getTimeout())
            .get();

        long wait = config.getWait();
        for (OptimizedBook book : books) {
            downloader.download(book);
            Thread.sleep(wait);
        }
        LOG.info("Finish: downloading optimized books");
    }
}
