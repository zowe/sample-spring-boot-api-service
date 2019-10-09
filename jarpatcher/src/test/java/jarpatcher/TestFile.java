package jarpatcher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class TestFile {
	private String filename;
    private String data;

    public TestFile(String filename) {
        this(filename, null);
    }
}
