package org.hogel.bookscantool.guice;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.google.common.base.Strings;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Singleton;
import org.hogel.bookscan.BookscanClient;
import org.hogel.bookscan.exception.BookscanException;
import org.hogel.bookscantool.config.BookscanToolConfig;
import org.hogel.config.InvalidConfigException;

import javax.inject.Provider;
import java.io.IOException;
import java.nio.file.Path;

public class BookscanToolModule implements Module {

    private final BookscanToolConfig config;

    public BookscanToolModule(Path configPath) throws IOException, InvalidConfigException {
        config = new BookscanToolConfig(configPath);
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(BookscanToolConfig.class).toInstance(config);
        binder.bind(BookscanClient.class).toProvider(BookscanClientProvider.class);
        binder.bind(AWSCredentials.class).toInstance(createAWSCredentials());
    }

    private AWSCredentials createAWSCredentials() {
        return new BasicAWSCredentials(config.getAwsAccessKey(), config.getAwsSecretKey());
    }

    @Singleton
    private static class BookscanClientProvider implements Provider<BookscanClient> {
        @Inject
        BookscanToolConfig config;

        @Override
        public BookscanClient get() {
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
}
