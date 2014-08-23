package org.hogel.config;

import com.google.common.base.Charsets;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Data
public class Config {
    private static final Logger LOG = LoggerFactory.getLogger(Config.class);

    @IntAttribute(name="timeout", defValue=30_000)
    int timeout;

    @IntAttribute(name="wait", defValue=1_000)
    int wait;

    @StringAttribute(name="email")
    String email;

    @StringAttribute(name="password")
    String password;

    @StringAttribute(name="download", defValue="downloads")
    String downloadDirectory;

    public Config(Map<String, Object> configMap) {
        Field[] fields = Config.class.getDeclaredFields();
        try {
            for (Field field : fields) {
                setFieldValue(field, configMap);
            }
        } catch (IllegalAccessException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void setFieldValue(Field field, Map<String, Object> configMap) throws IllegalAccessException {
        Annotation[] annotations = field.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof IntAttribute) {
                setFieldValue(field, configMap, (IntAttribute) annotation);
                return;
            } else if (annotation instanceof StringAttribute) {
                setFieldsValue(field, configMap, (StringAttribute) annotation);
                return;
            }
        }
    }

    private void setFieldValue(Field field, Map<String, Object> configMap, IntAttribute attribute) throws IllegalAccessException {
        String name = attribute.name();
        if (configMap.containsKey(name)) {
            field.set(this, configMap.get(name));
        } else {
            field.set(this, attribute.defValue());
        }
    }

    private void setFieldsValue(Field field, Map<String, Object> configMap, StringAttribute attribute) throws IllegalAccessException {
        String name = attribute.name();
        if (configMap.containsKey(name)) {
            field.set(this, configMap.get(name));
        } else {
            field.set(this, attribute.defValue());
        }
    }

    public static Config loadConfig(Path configPath) throws IOException {
        Yaml yaml = new Yaml();
        if (Files.notExists(configPath)) {
            throw new IOException("Cannot load config file: " + configPath.toFile().getPath());
        }
        try (Reader configReader = Files.newBufferedReader(configPath, Charsets.UTF_8))   {
            @SuppressWarnings("unchecked")
            Map<String, Object> configMap = (Map<String, Object>) yaml.load(configReader);
            return new Config(configMap);
        }
    }
}
