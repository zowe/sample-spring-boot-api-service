package jarpatcher;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.ToString;

@ToString
public class ArchiveDefinition {
    @Getter
    private final Map<String, TestFile> files = new LinkedHashMap<>();

    public ArchiveDefinition addFiles(TestFile... testFiles) {
        for (TestFile file: testFiles) {
            files.put(file.getFilename(), file);
        }
        return this;
    }
}
