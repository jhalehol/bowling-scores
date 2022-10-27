package com.jobsity.bowling.score.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Component
public class ResourceHelper {

    public String getFileReportContent(final String resourceName) {
        final String resourcePath = getFileResourceAbsolutePath(resourceName);
        final Path path = Paths.get(resourcePath);
        final StringBuilder content = new StringBuilder();
        try (Stream<String> stream = Files.lines(path)) {
            stream.forEach(content::append);
        } catch (IOException e) {
            log.error("Unable to read properly the resource ", e);
        }

        return content.toString();
    }

    public String getFileResourceAbsolutePath(final String resourceName) {
        final ClassLoader classLoader = getClass().getClassLoader();
        final File resourceFile = new File(Objects.requireNonNull(classLoader.getResource(resourceName)).getFile());
        return resourceFile.getAbsolutePath();
    }

}
