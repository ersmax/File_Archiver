package nl.vu.cs.softwaredesign.services.impl;

import nl.vu.cs.softwaredesign.services.Compressible;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.*;
import java.util.List;

public class TarCompression implements Compressible {

    public static String TAR_EXTENSION = ".tar";

    public void compress(List<File> files, String archiveName, String destinationPath) {
        String outputPath = destinationPath + File.separator + archiveName + TAR_EXTENSION;

        try (FileOutputStream fos = new FileOutputStream(outputPath);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             TarArchiveOutputStream taros = new TarArchiveOutputStream(bos)) {

            for (File file : files) {
                if (file.exists()) {
                    addFileToTar(file, "", taros);
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

        try (FileInputStream fis = new FileInputStream(archiveFile);
             BufferedInputStream bis = new BufferedInputStream(fis);
             TarArchiveInputStream taris = new TarArchiveInputStream(bis)) {

            TarArchiveEntry entry;
            while ((entry = taris.getNextTarEntry()) != null) {
                File entryFile = new File(extractionDir, entry.getName());

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
                        while ((length = taris.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, length);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Exception occurred while trying to extract an archive.");
        }
    }

    @Override
    public String getTargetPathExtension() {
        return TAR_EXTENSION;
    }

    private void addFileToTar(File file, String parent, TarArchiveOutputStream taros) throws IOException {
        String entryName = parent + file.getName();
        TarArchiveEntry tarEntry = new TarArchiveEntry(file, entryName);
        taros.putArchiveEntry(tarEntry);

        if (file.isFile()) {
            try (FileInputStream fis = new FileInputStream(file);
                 BufferedInputStream bis = new BufferedInputStream(fis)) {
                byte[] buffer = new byte[1024];
                int count;
                while ((count = bis.read(buffer)) != -1) {
                    taros.write(buffer, 0, count);
                }
                taros.closeArchiveEntry();
            }
        } else if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    addFileToTar(child, entryName + File.separator, taros);
                }
            }
        }
    }
}
