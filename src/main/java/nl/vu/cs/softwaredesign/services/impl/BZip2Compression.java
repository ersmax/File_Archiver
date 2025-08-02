package nl.vu.cs.softwaredesign.services.impl;

import nl.vu.cs.softwaredesign.services.Compressible;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.util.List;

public class BZip2Compression implements Compressible {
    public static String BZIP2_EXTENSION = ".tar.bz2";
    @Override
    public void compress(List<File> files, String archiveName, String destinationPath)  {
        try {
            File outputArchive = new File(destinationPath, archiveName + ".tar.bz2");

            try (TarArchiveOutputStream tarOut = new TarArchiveOutputStream(
                    new BZip2CompressorOutputStream(new FileOutputStream(outputArchive)))) {
                for (File file : files) {
                    TarArchiveEntry entry = tarOut.createArchiveEntry(file, file.getName());
                    tarOut.putArchiveEntry(entry);

                    // Write file content to TarArchiveOutputStream
                    try (InputStream in = new FileInputStream(file)) {
                        IOUtils.copy(in, tarOut);
                    }
                    tarOut.closeArchiveEntry();
                }
            }
        }catch (IOException e) {
            throw new RuntimeException("Error occurred while compressing bzip2 file", e);
        }

    }

    @Override
    public void decompress(String archivePath, String extractionPath){
        try {
            // Create input archive file
            File inputArchive = new File(archivePath);

            try (TarArchiveInputStream tarIn = new TarArchiveInputStream(
                    new BZip2CompressorInputStream(new FileInputStream(inputArchive)))) {
                ArchiveEntry entry;
                while ((entry = tarIn.getNextEntry()) != null) {
                    if (!tarIn.canReadEntryData(entry)) {
                        continue;
                    }

                    // Create output file
                    File outputFile = new File(extractionPath, entry.getName());

                    if (entry.isDirectory()) {
                        if (!outputFile.exists()) {
                            if (!outputFile.mkdirs()) {
                                throw new IOException("Failed to create directory: " + outputFile);
                            }
                        }
                    } else {
                        // Create parent directories for the file
                        File parent = outputFile.getParentFile();
                        if (!parent.exists()) {
                            if (!parent.mkdirs()) {
                                throw new IOException("Failed to create directory: " + parent);
                            }
                        }

                        // Write file content to output file
                        try (OutputStream out = new FileOutputStream(outputFile)) {
                            IOUtils.copy(tarIn, out);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while decompressing bzip2 file", e);
        }
    }

    @Override
    public String getTargetPathExtension() {
        return BZIP2_EXTENSION;
    }
}
