/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.zos;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.util.ResourceUtils;

public class LibExtractor {
    private static final int BUFFER_SIZE = 4096;

    private LibLoader libLoader = new LibLoader();

    public void extractLibrary(String libraryName, String targetDir) {
        String filename = "lib/lib" + libraryName + ".so";
        Path targetPath = Paths.get(targetDir, libLoader.libraryFileName(libraryName));
        System.out.println(String.format("Extracting %s to %s", filename, targetPath));
        try {
            URL url = ResourceUtils.getURL(ResourceUtils.CLASSPATH_URL_PREFIX + filename);
            try (InputStream inputStream = url.openStream();
                    OutputStream outputStream = new FileOutputStream(targetPath.toString())) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int lengthRead;
                while ((lengthRead = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, lengthRead);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error extracting library %s to %s", libraryName, targetDir), e);
        }
    }

    public static void main(String[] args) {
        LibExtractor ex = new LibExtractor();
        if (args.length == 2) {
            ex.extractLibrary(args[0], args[1]);
        } else {
            System.err.println("No arguments provided. Expected: libName targetDir");
        }
    }
}
