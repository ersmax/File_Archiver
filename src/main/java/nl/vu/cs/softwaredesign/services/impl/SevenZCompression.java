package nl.vu.cs.softwaredesign.services.impl;

import nl.vu.cs.softwaredesign.services.Compressible;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;

import java.io.IOException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class SevenZCompression implements Compressible {
    public static String SEVENZ_EXTENSION = ".7z";

    @Override
    public void compress(List<File> files, String archiveName, String destinationPath) {
        String outputPath = destinationPath + File.separator + archiveName + SEVENZ_EXTENSION;

        try (FileOutputStream fos = new FileOutputStream(outputPath);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             SevenZOutputFile sevenzos = new SevenZOutputFile(new File(outputPath))) {

            for (File file : files) {
                if (file.exists()) {
                    addFileToSevenZ(file, "", sevenzos);
                } else {
                    throw new FileNotFoundException(String.format("File with path '%s' not found", file.getAbsolutePath()));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void decompress(String archivePath, String extractionPath) {
        try {
            try (SevenZFile sevenZFile = new SevenZFile(new File(archivePath))) {
                ArchiveEntry entry;
                while ((entry = sevenZFile.getNextEntry()) != null) {
                    File entryFile = new File(extractionPath, entry.getName());
                    if (entry.isDirectory()) {
                        entryFile.mkdirs();
                    } else {
                        File parentDir = entryFile.getParentFile();
                        if (!parentDir.exists()) {
                            parentDir.mkdirs();
                        }
                        try (FileOutputStream outputStream = new FileOutputStream(entryFile)) {
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = sevenZFile.read(buffer)) > 0) {
                                outputStream.write(buffer, 0, length);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while decompressing 7z file", e);
        }
    }

    @Override
    public String getTargetPathExtension() {
        return SEVENZ_EXTENSION;
    }

    private void addFileToSevenZ(File file, String parent, SevenZOutputFile sevenzos) throws IOException {
        String entryName = parent + file.getName();
        SevenZArchiveEntry entry = sevenzos.createArchiveEntry(file, entryName);
        sevenzos.putArchiveEntry(entry);

        if (file.isFile()) {
            try (FileInputStream fis = new FileInputStream(file);
                 BufferedInputStream bis = new BufferedInputStream(fis)) {
                byte[] buffer = new byte[1024];
                int count;
                while ((count = bis.read(buffer)) != -1) {
                    sevenzos.write(buffer, 0, count);
                }
                sevenzos.closeArchiveEntry();
            }
        } else if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    addFileToSevenZ(child, entryName + File.separator, sevenzos);
                }
            }
        }
    }
}
