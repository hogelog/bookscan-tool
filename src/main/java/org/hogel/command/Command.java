package org.hogel.command;

import com.google.inject.Injector;
import lombok.Getter;
import org.hogel.command.job.AbstractJob;
import org.hogel.command.job.DownloadAllJob;
import org.hogel.command.job.Job;
import org.hogel.command.job.OptimizeAllJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Command {
    DOWNLOAD_ALL("download_all", DownloadAllJob.class),
    OPTIMIZE_ALL("optimize_all", OptimizeAllJob.class),
    ;

    private static final Logger LOG = LoggerFactory.getLogger(Command.class);

    @Getter
    final String commandName;

    @Getter
    final Class<? extends Job> jobClass;

    Command(String commandName, Class<? extends AbstractJob> jobClass) {
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
