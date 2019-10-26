/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package jarpatcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import lombok.ToString;

public class JarPatcher {
    private static final Logger logger = Logger.getLogger(JarPatcher.class.getName());

    @ToString
    public class CompareResult {
        Set<String> created = new HashSet<>();
        Set<String> deleted = new HashSet<>();
        Set<String> changed = new HashSet<>();

        public boolean archivesAreSame() {
            return created.isEmpty() && deleted.isEmpty() && changed.isEmpty();
        }

        public boolean isFileChangedOrCreated(String filename) {
            return changed.contains(filename) || created.contains(filename);
        }
    }

    private static final String DELETED = "_deleted_-";

    public int run(String[] args) {
        try {
            if (args[0].equals("diff")) {
                String oldPath = args[1];
                String newPath = args[2];
                String patchPath = args[3];
                String patcherPath = (args.length >= 5) ? args[4] : null;
                boolean areSame = createPatch(oldPath, newPath, patchPath, patcherPath);
                if (areSame) {
                    return 1;
                }
            } else if (args[0].equals("patch")) {
                String targetPath = args[1];
                String patchPath = args[2];
                String ignoredPrefix = (args.length >= 4) ? args[3] : null;
                applyPatch(targetPath, patchPath, ignoredPrefix);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    public CompareResult compare(String path1, String path2) throws ZipException, IOException {
        logger.info(String.format("Comparing %s to %s", path1, path2));
        ZipFile zipFile1 = new ZipFile(new File(path1));
        ZipFile zipFile2 = new ZipFile(new File(path2));

        Set<String> files1 = setFromEntryNames(zipFile1);
        Set<String> files2 = setFromEntryNames(zipFile2);
        logger.info(String.format("Files in %s: %s", path1, files1));
        logger.info(String.format("Files in %s: %s", path2, files2));

        CompareResult result = new CompareResult();
        Set<String> inBoth = files1.stream().distinct().filter(files2::contains).collect(Collectors.toSet());
        for (String filename : inBoth) {
            if (!compareEntries(zipFile1.getEntry(filename), zipFile2.getEntry(filename))) {
                printEntry(zipFile1, zipFile1.getEntry(filename));
                printEntry(zipFile2, zipFile2.getEntry(filename));
                result.changed.add(filename);
            }
        }
        result.deleted.addAll(files1.stream().distinct().filter(f -> !files2.contains(f)).collect(Collectors.toSet()));
        result.created.addAll(files2.stream().distinct().filter(f -> !files1.contains(f)).collect(Collectors.toSet()));

        zipFile1.close();
        zipFile2.close();
        return result;
    }

    private void printEntry(ZipFile f, ZipEntry entry) {
        logger.info(String.format("zipFile=%s file=%s size=%d crc=%d dir=%b comment=%s method=%s", f.getName(),
                entry.getName(), entry.getSize(), entry.getCrc(), entry.isDirectory(), entry.getComment(),
                entry.getMethod()));
    }

    private boolean compareEntries(ZipEntry entry1, ZipEntry entry2) {
        return (entry1.isDirectory() == entry2.isDirectory()) && (entry1.getSize() == entry2.getSize())
                && (entry1.getName().equals(entry2.getName())) && (entry1.getCrc() == entry2.getCrc()
                        && (entry1.isDirectory() || (entry1.getMethod() == entry2.getMethod())));
    }

    private Set<String> setFromEntryNames(ZipFile zipFile) {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        Set<String> set = new HashSet<>();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            set.add(entry.getName());
        }
        return set;
    }

    private void addChangedOrCreatedFiles(String newPath, CompareResult result, ZipOutputStream zipOut)
            throws IOException {
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(newPath));
        ZipEntry inEntry = zipIn.getNextEntry();
        while (inEntry != null) {
            String filename = inEntry.getName();
            if (result.isFileChangedOrCreated(filename)) {
                ZipEntry patchEntry = new ZipEntry(filename);
                copyZipEntryAttributes(inEntry, patchEntry);
                zipOut.putNextEntry(patchEntry);
                if (!inEntry.isDirectory()) {
                    copyStream(zipIn, zipOut);
                }
                zipOut.closeEntry();
            }
            inEntry = zipIn.getNextEntry();
        }
        zipIn.close();
    }

    private void copyStream(InputStream zipIn, OutputStream zipOut) throws IOException {
        byte[] buffer = new byte[4096];
        int len;
        while ((len = zipIn.read(buffer)) > 0) {
            zipOut.write(buffer, 0, len);
        }
    }

    private void addDeletedEntries(Set<String> deletedFiles, ZipOutputStream zipOut) throws IOException {
        for (String filename : deletedFiles) {
            ZipEntry patchEntry = new ZipEntry(DELETED + filename);
            zipOut.putNextEntry(patchEntry);
            zipOut.closeEntry();
        }
    }

    private void addFiles(String fromPath, ZipOutputStream zipOut, String ignoredPath) throws IOException {
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(fromPath));
        ZipEntry inEntry = zipIn.getNextEntry();
        while (inEntry != null) {
            String filename = inEntry.getName();
            if (!filename.startsWith(ignoredPath)) {
                ZipEntry patchEntry = new ZipEntry(filename);
                zipOut.putNextEntry(patchEntry);
                if (!inEntry.isDirectory()) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zipIn.read(buffer)) > 0) {
                        zipOut.write(buffer, 0, len);
                    }
                }
                zipOut.closeEntry();
            }
            inEntry = zipIn.getNextEntry();
        }
        zipIn.close();
    }

    public boolean createPatch(String oldPath, String newPath, String patchPath, String patcherPath)
            throws ZipException, IOException {
        CompareResult result = compare(oldPath, newPath);
        logger.info(String.format("Comparing %s and %s: %s", oldPath, newPath, result));

        FileOutputStream fos = new FileOutputStream(patchPath);
        ZipOutputStream zipOut = new ZipOutputStream(fos);

        addChangedOrCreatedFiles(newPath, result, zipOut);
        addDeletedEntries(result.deleted, zipOut);
        if (patcherPath != null) {
            addFiles(patcherPath, zipOut, "META-INF");
        }

        zipOut.close();
        fos.close();

        return result.archivesAreSame();
    }

    private void createDirectories(String filename, ZipOutputStream zipOut, Set<String> createdDirectories)
            throws IOException {
        Path dir = Paths.get(filename).getParent();
        String dirname = (dir == null) ? null : (dir.toString() + "/");
        if ((dirname != null) && !createdDirectories.contains(dirname)) {
            createDirectories(dirname, zipOut, createdDirectories);
            ZipEntry zipEntry = new ZipEntry(dirname);
            zipOut.putNextEntry(zipEntry);
            zipOut.closeEntry();
            createdDirectories.add(dirname);
        }
    }

    public void applyPatch(String targetPath, String patchPath, String ignoredPath) throws IOException {
        String originalTargetPath = targetPath + "-original";
        Files.copy(Paths.get(targetPath), Paths.get(originalTargetPath), StandardCopyOption.REPLACE_EXISTING);
        FileOutputStream fos = new FileOutputStream(targetPath.toString());
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        Set<String> deletedOrPatchedNames = new HashSet<>();

        ZipFile zipPatch = new ZipFile(patchPath);
        Enumeration<? extends ZipEntry> entries = zipPatch.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String filename = entry.getName();

            if (filename.startsWith(DELETED)) {
                String realFilename = filename.substring(DELETED.length());
                logger.info("Deleting: " + realFilename);
                deletedOrPatchedNames.add(realFilename);
            } else if ((ignoredPath != null) && (filename.startsWith(ignoredPath))) {
                logger.info("Ignoring: " + filename);
            } else {
                logger.info("Patching: " + filename);
                deletedOrPatchedNames.add(filename);
                writeEntry(zipOut, deletedOrPatchedNames, zipPatch, entry, filename);
            }
        }
        zipPatch.close();

        ZipFile zipIn = new ZipFile(originalTargetPath);
        entries = zipIn.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String filename = entry.getName();

            if (!deletedOrPatchedNames.contains(filename)) {
                logger.info("Keeping: " + filename);
                deletedOrPatchedNames.add(filename);
                writeEntry(zipOut, deletedOrPatchedNames, zipIn, entry, filename);
            }
        }
        zipIn.close();

        zipOut.close();
        fos.close();
        Files.delete(Paths.get(originalTargetPath));
    }

    private void writeEntry(ZipOutputStream zipOut, Set<String> createdDirectories, ZipFile zipPatch, ZipEntry entry,
            String filename) throws IOException {
        createDirectories(filename, zipOut, createdDirectories);
        ZipEntry zipEntry = new ZipEntry(filename);
        copyZipEntryAttributes(entry, zipEntry);
        InputStream inputStream = zipPatch.getInputStream(entry);
        zipOut.putNextEntry(zipEntry);
        copyStream(inputStream, zipOut);
        zipOut.closeEntry();
    }

    private void copyZipEntryAttributes(ZipEntry inEntry, ZipEntry outEntry) {
        outEntry.setMethod(inEntry.getMethod());
        outEntry.setComment(inEntry.getComment());
        if (inEntry.getMethod() == ZipEntry.STORED) {
            outEntry.setCrc(inEntry.getCrc());
            outEntry.setCompressedSize(inEntry.getCompressedSize());
        }
    }

    public static void main(String[] args) {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s %n");
        System.exit(new JarPatcher().run(args));
    }
}
