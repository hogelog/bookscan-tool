package org.hogel;

import com.google.inject.Guice;
import com.google.inject.Injector;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.hogel.command.Command;
import org.hogel.command.job.Job;
import org.hogel.guice.BookscanToolModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    private static final String DEFAULT_CONFIG_PATH = "config.yaml";

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        OptionSet options = parseArgs(args);
        String configPath = options.has("config") ? (String) options.valueOf("config") : DEFAULT_CONFIG_PATH;

        try {
            Injector injector = createInjector(configPath);

            List<?> commandNames = options.nonOptionArguments();

            for (Object commandName : commandNames) {
                run(injector, commandName.toString());
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private static OptionSet parseArgs(String[] args) {
        OptionParser parser = new OptionParser();
        parser.accepts("config").withRequiredArg().ofType(String.class);
        return parser.parse(args);
    }

    private static Injector createInjector(String configPath) throws IOException {
        return Guice.createInjector(new BookscanToolModule(Paths.get(configPath)));
    }

    public static void run(Injector injector, String commandName) {
        try {
            Command command = Command.of(commandName);
            Job job = command.createJob(injector);
            job.run();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
