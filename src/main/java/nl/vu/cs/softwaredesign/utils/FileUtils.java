package nl.vu.cs.softwaredesign.utils;

import nl.vu.cs.softwaredesign.models.CompressionData;
import nl.vu.cs.softwaredesign.utils.enums.FType;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorOutputStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;

public class FileUtils {

    public static CompressionData getCompressionData(List<File> files, String compressionType) {
        CompressionData compressionData = new CompressionData();
        long originalSize = 0;
        long compressedSize = 0;
        long compressionTime = 0;

        for (File file : files) {
            CompressionData fileData = getCompressionData(file,compressionType);
            originalSize += fileData.getOriginalSize();
            compressedSize += fileData.getCompressedSize();
            compressionTime += fileData.getCompressionTime();
        }

        compressionData.setOriginalSize(originalSize);
        compressionData.setCompressedSize(compressedSize);
        compressionData.setCompressionTime(compressionTime);

        return compressionData;
    }

    public static CompressionData getCompressionData(File file,String compressionType) {
        if (file.isFile()) {
            return getCompressionDataOneFile(file,compressionType);
        } else if (file.isDirectory()) {
            return getCompressionDataOneDirectory(file,compressionType);
        } else {
            throw new RuntimeException("Unsupported file type");
        }
    }

    public static CompressionData getCompressionDataOneDirectory(File file,String compressionType) {
        CompressionData compressionData = new CompressionData();
        compressionData.setOriginalSize(calculateSize(file));

        long startTime = System.currentTimeMillis();
        long compressedSize = 0;
        long compressionTime = 0;

        File[] files = file.listFiles();
        if (files != null) {
            for (File child : files) {
                CompressionData childData = getCompressionData(child,compressionType);
                compressedSize += childData.getCompressedSize();
                compressionTime += childData.getCompressionTime();
            }
        }

        long endTime = System.currentTimeMillis();
        compressionTime += Math.round((double)(endTime - startTime) / CompressionData.KB * compressionData.getOriginalSize());

        compressionData.setCompressedSize(compressedSize);
        compressionData.setCompressionTime(compressionTime);

        return compressionData;
    }
    public static CompressionData getCompressionDataOneFile(File file,String compressionType) {

        try {
            CompressionData compressionData = new CompressionData();
            compressionData.setOriginalSize(calculateSize(file));

            byte[] buffer = new byte[CompressionData.KB];
            FileInputStream fileInputStream = new FileInputStream(file);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            OutputStream outputStream = null;
            long startTime = System.currentTimeMillis();

            if (compressionType.equals("BZIP2")) {
                outputStream = new BZip2CompressorOutputStream(byteArrayOutputStream);

            } else if (compressionType.equals("GZIP")) {
                // GZip compression
                outputStream = new GZIPOutputStream(byteArrayOutputStream);
            }else if (compressionType.equals("LZO")) {
                // LZ4 compression
                outputStream = new BlockLZ4CompressorOutputStream(byteArrayOutputStream);
            }else if (compressionType.equals("ZIP")) {
                // LZ4 compression
                outputStream = new DeflaterOutputStream(byteArrayOutputStream);
            }

            else {
                return new CompressionData();

            }

            int bytesRead;
            if ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();

            byte[] arr = byteArrayOutputStream.toByteArray();
            long compressedSize = Math.round((double) arr.length / CompressionData.KB * compressionData.getOriginalSize());
            long endTime = System.currentTimeMillis();
            long compressionTime = Math.round((double)(endTime - startTime) / CompressionData.KB * compressionData.getOriginalSize());

            compressionData.setCompressedSize(compressedSize);
            compressionData.setCompressionTime(compressionTime);

            return compressionData;
        }catch (IOException e) {
            throw new RuntimeException("Error occurred while compressing file", e);
        }



    }

    public static long calculateSize(File file) {
        if (file.isFile()) {
            return file.length();
        } else if (file.isDirectory()) {
            long size = 0;
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    size += calculateSize(child);
                }
            }
            return size;
        } else {
            return 0; // Return 0 for unsupported files
        }
    }

    public static FType determineFType(File f) {
        if (f == null) {
            throw new RuntimeException("File is null");
        }
        String type;
        try {
            type = Files.probeContentType(f.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (type == null) {
            //type couldn't be determined, assume binary
            return FType.BINARY_FILE;
        }
        return type.startsWith("text") ? FType.ASCII_FILE : FType.BINARY_FILE;
    }

    public static Date getDateOfCreation(File file) {
        try {
            BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            return new Date(attributes.creationTime().toMillis());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Counts total number of files and folders in java.nio.File
     *
     * @param file - File or a Directory
     * @return total number of files and folders inside a java.nio.File
     */
    public static int countFileElements(File file) {
        if (file == null || !file.exists()) {
            return 0;
        }
        int count = 1;
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                count++;
                if (f.isDirectory()) {
                    count += countFiles(f);
                }
            }
        }
        return count;
    }

    private static int countFiles(File file) {
        if (file.isFile()) {
            return 1; // Return 1 if the input is a file
        } else if (file.isDirectory()) {
            int count = 0;
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    count += countFiles(child); // Recursively count files for each child
                }
            }
            return count;
        } else {
            return 0; // Return 0 for unsupported files
        }
    }

    /**
     * @param sourcePath path to a certain file or directory
     * @return the name of the file or directory from the path
     */
    public static String getFileNameFromPath(String sourcePath) {
        Path path = Paths.get(sourcePath);
        return path.getFileName().toString();
    }
}