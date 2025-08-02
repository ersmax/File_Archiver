package nl.vu.cs.softwaredesign.services.impl;

import nl.vu.cs.softwaredesign.services.Compressible;

import java.io.*;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipCompression implements Compressible {

    public static String GZIP_EXTENSION = ".gz";

    @Override
    public void compress(List<File> files, String archiveName, String destinationPath) {
        String outputPath = destinationPath + File.separator + archiveName + GZIP_EXTENSION;
        try (FileOutputStream fos = new FileOutputStream(outputPath);
             GZIPOutputStream gzipOS = new GZIPOutputStream(fos)) {

            for (File file : files) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) != -1) {
                        gzipOS.write(buffer, 0, length);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Exception occurred during gzip compression.", e);
        }
    }

    @Override
    public void decompress(String archivePath, String extractionPath) {
        try (FileInputStream fis = new FileInputStream(archivePath);
             GZIPInputStream gzipIS = new GZIPInputStream(fis);
             BufferedInputStream bis = new BufferedInputStream(gzipIS)) {

            byte[] buffer = new byte[1024];
            int length;

            // Create a new file directly in the extraction path
            String extractedFilePath = extractionPath + File.separator + "decompressed_file"; // Change "decompressed_file" to the desired file name
            try (FileOutputStream fos = new FileOutputStream(extractedFilePath)) {
                while ((length = bis.read(buffer)) != -1) {
                    fos.write(buffer, 0, length);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Exception occurred during gzip decompression.", e);
        }
    }


    @Override
    public String getTargetPathExtension() {
        return GZIP_EXTENSION;
    }
}
