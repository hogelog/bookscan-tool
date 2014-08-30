package org.hogel.bookscantool.command.job;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.glacier.transfer.UploadResult;
import com.google.inject.Inject;
import org.hogel.bookscantool.config.BookscanToolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GlacierBackupJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(GlacierBackupJob.class);

    @Inject
    BookscanToolConfig config;

    @Inject
    AWSCredentials credentials;

    @Override
    public void run() throws Exception {
        AmazonGlacierClient client = new AmazonGlacierClient(credentials);
        client.setEndpoint(config.getGlacierEndpoint());

        Path downloadDirectoryPath = Paths.get(config.getDownloadDirectory());
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(downloadDirectoryPath)) {
            ArchiveTransferManager transferManager = new ArchiveTransferManager(client, credentials);
            String vault = config.getGlacierVault();
            for (Path path : directoryStream) {
                String fileName = path.getFileName().toString();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String description = dateFormat.format(new Date()) + " bookscan backup";

                LOG.info("Backup: {}", fileName);
                UploadResult result = transferManager.upload(vault, description, path.toFile());
                LOG.info("Uploaded: {} {}", result.getArchiveId(), fileName);
            }
        }
    }
}
