package org.hogel.bookscantool.command;

import com.google.inject.Injector;
import lombok.Getter;
import org.hogel.bookscantool.command.job.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Command {
    DOWNLOAD_BOOKS("download_books", DownloadBooksJob.class),
    OPTIMIZE_BOOKS("optimize_books", OptimizeBooksJob.class),
    DOWNLOAD_OPTIMIZED_BOOKS("download_optimized_books", DownloadOptimizedBooksJob.class),
    GLACIER_BACKUP("glacier_backup", GlacierBackupJob.class),
    ;

    private static final Logger LOG = LoggerFactory.getLogger(Command.class);

    @Getter
    final String commandName;

    @Getter
    final Class<? extends Job> jobClass;

    Command(String commandName, Class<? extends Job> jobClass) {
        this.commandName = commandName;
        this.jobClass = jobClass;
    }

    public Job createJob(Injector injector) {
        try {
            Job job = jobClass.newInstance();
            injector.injectMembers(job);
            return job;
        } catch (InstantiationException|IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static class UnknownCommandException extends Exception {
        public UnknownCommandException(String message) {
            super(message);
        }
    }

    public static Command of(String commandName) throws UnknownCommandException {
        for (Command command : Command.values()) {
            if (command.commandName.equals(commandName)) {
                return command;
            }
        }
        throw new UnknownCommandException("Unknown command: " + commandName);
    }
}
