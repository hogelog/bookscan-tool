package org.hogel.bookscantool.command.job;

import com.google.inject.Inject;
import org.hogel.bookscan.BookscanClient;
import org.hogel.bookscan.OptimizeOption;
import org.hogel.bookscan.exception.BookscanException;
import org.hogel.bookscan.model.Book;
import org.hogel.bookscan.model.OptimizedBook;
import org.hogel.bookscantool.config.BookscanToolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class OptimizeBooksJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(OptimizeBooksJob.class);

    private static final int PARALLEL_TUNING_COUNT = 5;

    @Inject
    BookscanToolConfig config;

    @Inject
    BookscanClient bookscanClient;

    @Override
    public void run() throws Exception {
        long wait = config.getWait();

        List<OptimizedBook> optimizedBooks = bookscanClient
            .fetchOptimizedBooks()
            .get();

        List<Book> books = bookscanClient
            .fetchBooks()
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
            return bookscanClient.fetchOptimizingBooks().get().size();
        } catch (BookscanException e) {
            LOG.error(e.getMessage(), e);
            return 0;
        }
    }
}
