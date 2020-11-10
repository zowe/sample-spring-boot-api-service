/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.zos;

import org.springframework.util.ResourceUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LibExtractor {

    public static class LibExtractionError extends RuntimeException {
        private static final long serialVersionUID = 2453905334387687806L;

        public LibExtractionError(String message, IOException e) {
            super(message, e);
        }
    }

    private static final int BUFFER_SIZE = 4096;

    private final LibLoader libLoader = new LibLoader();

    public void extractLibrary(String libraryName, String targetDir) {
        String filename = "lib/lib" + libraryName + ".so";
        Path targetPath = Paths.get(targetDir, libLoader.libraryFileName(libraryName));
        System.out.printf("Extracting %s to %s%n", filename, targetPath);  // NOSONAR
        try {
            URL url = ResourceUtils.getURL(ResourceUtils.CLASSPATH_URL_PREFIX + filename);
            try (InputStream inputStream = url.openStream();
                 FileOutputStream fos = new FileOutputStream(targetPath.toString());
                 BufferedOutputStream outputStream = new BufferedOutputStream(fos)
        ) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int lengthRead;
                while ((lengthRead = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, lengthRead);
                }
            }
        } catch (IOException e) {
            throw new LibExtractionError(String.format("Error extracting library %s to %s", libraryName, targetDir), e);
        }
    }

    public static void main(String[] args) {  // NOSONAR
        LibExtractor ex = new LibExtractor();
        if (args.length == 2) {
            ex.extractLibrary(args[0], args[1]);
        } else {
            System.err.println("No arguments provided. Expected: libName targetDir");  // NOSONAR
        }
    }
}
