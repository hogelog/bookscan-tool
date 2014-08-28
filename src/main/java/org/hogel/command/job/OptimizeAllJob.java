package org.hogel.command.job;

import com.google.inject.Inject;
import org.hogel.bookscan.BookscanClient;
import org.hogel.bookscan.OptimizeOption;
import org.hogel.bookscan.exception.BookscanException;
import org.hogel.bookscan.model.Book;
import org.hogel.bookscan.model.OptimizedBook;
import org.hogel.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class OptimizeAllJob extends AbstractJob {
    private static final Logger LOG = LoggerFactory.getLogger(OptimizeAllJob.class);

    private static final int PARALLEL_TUNING_COUNT = 5;

    @Inject
    Config config;

    @Inject
    BookscanClient bookscanClient;

    @Override
    public void run() throws Exception {
        long wait = config.getWait();

        List<OptimizedBook> optimizedBooks = bookscanClient
            .fetchOptimizedBooks()
            .timeout(config.getTimeout())
            .get();

        List<Book> books = bookscanClient
            .fetchBooks()
            .timeout(config.getTimeout())
            .get();

        // TODO: load optimize option from config file
        OptimizeOption option = new OptimizeOption();
        option.addFlag(OptimizeOption.Flag.COVER);
        option.addFlag(OptimizeOption.Flag.BOLD);
        option.addFlag(OptimizeOption.Flag.WHITE);

        option.addType(OptimizeOption.Type.KINDLEP);

        for (Book book : books) {
            if (isOptimized(book, optimizedBooks)) {
                LOG.info("{} is already optimized", book.getFilename());
                continue;
            }
            while (countOptimizingBooks() > PARALLEL_TUNING_COUNT) {
                LOG.info("waiting optimizing...", book.getFilename());
                Thread.sleep(wait);
            }
            LOG.info("Optimize: {}", book.getFilename());
            bookscanClient
                .requestBookOptimize(book, option)
                .timeout(config.getTimeout())
                .get();
            Thread.sleep(wait);
        }
    }

    private boolean isOptimized(Book book, List<OptimizedBook> optimizedBooks) {
        String bookName = book.getFilename();
        for (OptimizedBook optimizedBook : optimizedBooks) {
            if (optimizedBook.getFilename().endsWith(bookName)) {
                return true;
            }
        }
        return false;
    }

    private int countOptimizingBooks() {
        try {
            return bookscanClient.fetchOptimizingBooks().timeout(config.getTimeout()).get().size();
        } catch (BookscanException e) {
            LOG.error(e.getMessage(), e);
            return 0;
        }
    }
}
