package nl.vu.cs.softwaredesign.services.impl;

import nl.vu.cs.softwaredesign.services.Compressible;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorInputStream;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class LzoCompression implements Compressible {
    public static String LZO_EXTENSION = ".lzo";
    @Override
    public void compress(List<File> files, String archiveName, String destinationPath) {
        try {
            FileOutputStream fos = new FileOutputStream(destinationPath + File.separator + archiveName + LZO_EXTENSION);
            BlockLZ4CompressorOutputStream lz4Out = new BlockLZ4CompressorOutputStream(fos);

            ZipOutputStream zipOut = new ZipOutputStream(lz4Out);

            for (File file : files) {
                if (file.isDirectory()) {
                    compressDirectory(file, zipOut, "");
                } else {
                    compressFile(file, zipOut, "");
                }
            }

            zipOut.close();
            lz4Out.close();
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while compressing lzo file", e);
        }

    }

    private void compressDirectory(File directory, ZipOutputStream zipOut, String baseDir) throws IOException {
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                compressDirectory(file, zipOut, baseDir + directory.getName() + File.separator);
            } else {
                compressFile(file, zipOut, baseDir + directory.getName() + File.separator);
            }
        }
    }

    private void compressFile(File file, ZipOutputStream zipOut, String baseDir) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(baseDir + file.getName());
        zipOut.putNextEntry(zipEntry);

        IOUtils.copy(fis, zipOut);

        fis.close();
        zipOut.closeEntry();
    }

    @Override
    public void decompress(String archivePath, String extractionPath) {
        try {
            FileInputStream fis = new FileInputStream(archivePath);
            BlockLZ4CompressorInputStream lz4In = new BlockLZ4CompressorInputStream(fis);

            ZipInputStream zipIn = new ZipInputStream(lz4In);
            ZipEntry entry = zipIn.getNextEntry();

            while (entry != null) {
                File file = new File(extractionPath + File.separator + entry.getName());
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    File parentDir = file.getParentFile();
                    if (!parentDir.exists()) {
                        parentDir.mkdirs();
                    }

                    FileOutputStream fos = new FileOutputStream(file);
                    IOUtils.copy(zipIn, fos);
                    fos.close();
                }

                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }

            zipIn.close();
            lz4In.close();
            fis.close();
        }catch (IOException e) {
            throw new RuntimeException("Error occurred while decompressing lzo file", e);
        }

    }

    @Override
    public String getTargetPathExtension() {
        return LZO_EXTENSION;
    }
}
