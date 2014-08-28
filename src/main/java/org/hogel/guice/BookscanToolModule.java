package org.hogel.guice;

import com.google.common.base.Strings;
import com.google.inject.Binder;
import com.google.inject.Module;
import org.hogel.bookscan.BookscanClient;
import org.hogel.bookscan.exception.BookscanException;
import org.hogel.config.Config;

import java.io.IOException;
import java.nio.file.Path;

public class BookscanToolModule implements Module {

    private final Config config;

    public BookscanToolModule(Path configPath) throws IOException {
        config = Config.loadConfig(configPath);
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(Config.class).toInstance(config);
        binder.bind(BookscanClient.class).toInstance(createBookscanClient());
    }

    private BookscanClient createBookscanClient() {
        BookscanClient bookscanClient = new BookscanClient();
        bookscanClient.setDefaultTimeout(config.getTimeout());
        try {
            String email = config.getEmail();
            String password = config.getPassword();
            if (Strings.isNullOrEmpty(email)) {
                throw new RuntimeException("login email is empty");
            }
            if (Strings.isNullOrEmpty(password)) {
                throw new RuntimeException("login password is empty");
            }
            bookscanClient.login(config.getEmail(), config.getPassword()).get();
        } catch (BookscanException e) {
            throw new RuntimeException(e);
        }
        return bookscanClient;
    }
}
