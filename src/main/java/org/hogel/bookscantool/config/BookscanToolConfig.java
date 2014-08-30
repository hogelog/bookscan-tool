package org.hogel.bookscantool.config;

import lombok.Data;
import org.hogel.config.Config;
import org.hogel.config.InvalidConfigException;
import org.hogel.config.annotation.Attribute;
import org.hogel.config.annotation.IntDefaultValue;
import org.hogel.config.annotation.StringDefaultValue;

import java.io.IOException;
import java.nio.file.Path;

@Data
public class BookscanToolConfig extends Config {
    @Attribute
    @IntDefaultValue(30_000)
    int timeout;

    @Attribute
    @IntDefaultValue(1_000)
    int wait;

    @Attribute
    String email;

    @Attribute
    String password;

    @Attribute(name = "download")
    @StringDefaultValue("downloads")
    String downloadDirectory;

    public BookscanToolConfig(Path path) throws IOException, InvalidConfigException {
        super(path);
    }
}
