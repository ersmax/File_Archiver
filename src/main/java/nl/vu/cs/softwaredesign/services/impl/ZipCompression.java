package nl.vu.cs.softwaredesign.services.impl;

import nl.vu.cs.softwaredesign.services.Compressible;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipCompression implements Compressible {

    public static int compressLvl = 5;
    public static String ZIP_EXTENSION = ".zip";


    public void compress(List<File> files, String archiveName, String destinationPath) {
        String outputPath = destinationPath + "\\" + archiveName + ZIP_EXTENSION;

        try (FileOutputStream fos = new FileOutputStream(outputPath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            zos.setLevel(compressLvl);

            for (File file : files) {
                if (file.exists()) {
                    if (file.isDirectory()) {
                        zipDirectory(file, file.getName(), zos);
                    } else {
                        zipFile(file, file.getName(), zos);
                    }
                } else {
                    throw new FileNotFoundException(String.format("File with path '%s' not found", file.getAbsolutePath()));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void decompress(String archivePath, String extractionPath) throws FileNotFoundException {
        File archiveFile = new File(archivePath);
        if (!archiveFile.exists() || !archiveFile.isFile()) {
            throw new FileNotFoundException("Unable to extract an archive. Archive file does not exist for path '" + archivePath + "'");
        }

        File extractionDir = new File(extractionPath);
        if (!extractionDir.exists()) {
            extractionDir.mkdirs(); // Create extraction directory if it doesn't exist
        }

        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(archiveFile))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String entryName = entry.getName();
                File entryFile = new File(extractionDir, entryName);
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
                        while ((length = zipInputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }
                    }
                }
                zipInputStream.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException("Exception occurred while trying to extract an archive.");
        }
    }

    @Override
    public String getTargetPathExtension() {
        return ZIP_EXTENSION;
    }

    private static void zipDirectory(File directory, String baseName, ZipOutputStream zos) throws IOException {
        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            // If directory is empty, create an empty entry
            zos.putNextEntry(new ZipEntry(baseName + "/"));
            zos.closeEntry();
        } else {
            for (File file : files) {
                if (file.isDirectory()) {
                    zipDirectory(file, baseName + "/" + file.getName(), zos);
                } else {
                    zipFile(file, baseName + "/" + file.getName(), zos);
                }
            }
        }
    }


    private static void zipFile(File file, String entryName, ZipOutputStream zos) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(entryName);
            zos.putNextEntry(zipEntry);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
        }
    }
}
