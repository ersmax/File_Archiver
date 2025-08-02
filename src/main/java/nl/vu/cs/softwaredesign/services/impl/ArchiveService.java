package nl.vu.cs.softwaredesign.services.impl;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import nl.vu.cs.softwaredesign.models.Archive;
import nl.vu.cs.softwaredesign.models.Directory;
import nl.vu.cs.softwaredesign.models.Element;
import nl.vu.cs.softwaredesign.models.RegularFile;
import nl.vu.cs.softwaredesign.services.Compressible;
import nl.vu.cs.softwaredesign.services.Encryptable;
import nl.vu.cs.softwaredesign.utils.FileUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Service that handles business logic for Archive related functionalities.
 * Factory design pattern
 * - the createArchiveFromPath method acts as a factory method for creating Archive objects based on the provided path.
 * It abstracts the instantiation logic of Archive objects
 */
public class ArchiveService {

    private final Compressible compressible;
    private final Compressible tarCompressible;

    private final Compressible sevenzCompressible;

    private final Compressible gzipCompressible;

    private final Encryptable encryptionService;
    private final Compressible lzoCompressible;

    private final Compressible bzip2Compressible;


    /**
     * Dependency Injection design pattern
     */
    public ArchiveService(Compressible compressible, Compressible tarCompressible, Compressible sevenzCompressible, Compressible gzipCompressible, Encryptable encryptionService, Compressible lzoCompressible, Compressible bzip2Compressible) {
        this.compressible = compressible;
        this.tarCompressible = tarCompressible;
        this.sevenzCompressible = sevenzCompressible;
        this.gzipCompressible = gzipCompressible;
        this.encryptionService = encryptionService;
        this.lzoCompressible = lzoCompressible;
        this.bzip2Compressible = bzip2Compressible;
    }

    private Archive bundleArchive(List<File> files, String archiveName, String compressedPath, String password) {
        Archive archive = new Archive();
        List<Element> elements = new ArrayList<>();

        for (File sourceFile : files) {
            if (sourceFile.exists() && sourceFile.isDirectory()) {
                Directory directory = buildDirectory(sourceFile, null);
                elements.add(directory);
            } else if (sourceFile.exists() && sourceFile.isFile()) {
                RegularFile regularFile = buildRegularFile(sourceFile, null);
                elements.add(regularFile);
            } else {
                throw new RuntimeException("Invalid Path");
            }
        }
        try {
            Archive builtArchive = buildArchive(archive, archiveName, countFilesForArchiving(files), elements, compressedPath);
            if (!password.isEmpty()) encryptionService.encrypt(builtArchive.getLocation(), password);
            return builtArchive;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Archive createArchive(List<File> files, String archiveName, String destinationPath, String password) {
        this.compressible.compress(files, archiveName, destinationPath);
        String compressedPath = destinationPath + "\\" + archiveName + compressible.getTargetPathExtension();

        return bundleArchive(files, archiveName, compressedPath, password);
    }


    public Archive createTARArchive(List<File> files, String archiveName, String destinationPath, String password) {
        this.tarCompressible.compress(files, archiveName, destinationPath); // Use TAR-specific compress method
        String compressedPath = destinationPath + File.separator + archiveName + tarCompressible.getTargetPathExtension();

        return bundleArchive(files, archiveName, compressedPath, password);
    }

    public Archive createLzoArchive(List<File> files, String archiveName, String destinationPath, String password) {
        this.lzoCompressible.compress(files, archiveName, destinationPath); // Use TAR-specific compress method
        String compressedPath = destinationPath + File.separator + archiveName + tarCompressible.getTargetPathExtension();

        return bundleArchive(files, archiveName, compressedPath, password);
    }

    public Archive createBzip2Archive(List<File> files, String archiveName, String destinationPath, String password) {
        this.bzip2Compressible.compress(files, archiveName, destinationPath); // Use TAR-specific compress method
        String compressedPath = destinationPath + File.separator + archiveName + bzip2Compressible.getTargetPathExtension();

        return bundleArchive(files, archiveName, compressedPath, password);
    }


    public Archive create7ZArchive(List<File> files, String archiveName, String destinationPath, String password) {
        this.sevenzCompressible.compress(files, archiveName, destinationPath); // Use 7z-specific compress method
        String compressedPath = destinationPath + File.separator + archiveName + sevenzCompressible.getTargetPathExtension();

        return bundleArchive(files, archiveName, compressedPath, password);
    }

    public Archive createGzipArchive(List<File> files, String archiveName, String destinationPath, String password) {
        this.gzipCompressible.compress(files, archiveName, destinationPath); // Use Gzip-specific compress method
        String compressedPath = destinationPath + File.separator + archiveName + gzipCompressible.getTargetPathExtension();

        return bundleArchive(files, archiveName, compressedPath, password);
    }

    public List<String> getListOfEntriesFromZip(File zipFile) {
        List<String> entryList = new ArrayList<>();
        try (ZipFile archive = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = archive.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                entryList.add(entry.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entryList;
    }
    public void extractArchive(String archivePath, String extractionPath, String password) {
        String path = archivePath;
        try {
            if (!password.isEmpty()) {
                if (encryptionService.decrypt(archivePath, password)) {
                    path = archivePath.replace(".encrypted", "");
                }
                ;
            }
        } catch (Exception err) {
            System.out.println("Invalid password for " + archivePath + err);
            return;
        }
        try {
            String fileExtension = getFileExtension(path);

            System.out.println("Invalid password for " + archivePath + " - " + path + " - " + fileExtension);


            if ("zip".equalsIgnoreCase(fileExtension)) {
                this.compressible.decompress(path, extractionPath);
            } else if ("tar".equalsIgnoreCase(fileExtension)) {
                this.tarCompressible.decompress(path, extractionPath);
            } else if ("7z".equalsIgnoreCase(fileExtension)) {
                this.sevenzCompressible.decompress(path, extractionPath);
            } else if ("gz".equalsIgnoreCase(fileExtension)) {
                this.gzipCompressible.decompress(path, extractionPath);
            } else {
                System.out.println("Invalid compression format for file: " + archivePath);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return ""; // No extension found
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }


    private Directory buildDirectory(File directory, Directory parent) {
        if (directory == null) {
            throw new RuntimeException("Error when trying to build directory, directory is null");
        }
        Directory directoryElement = new Directory(directory.getName(), FileUtils.getDateOfCreation(directory), FileUtils.calculateSize(directory), parent);
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory()) {
                directoryElement.addChild(buildDirectory(file, directoryElement));
            } else if (file.isFile()) {
                directoryElement.addChild(buildRegularFile(file, directoryElement));
            }
        }
        return directoryElement;
    }

    private RegularFile buildRegularFile(File file, Directory parent) {
        if (file == null) {
            throw new RuntimeException("Error when trying to build create regular file, directory is null");
        }
        return new RegularFile(file.getName(), FileUtils.getDateOfCreation(file), file.length(), parent, FileUtils.determineFType(file));
    }

    /**
     * Creates and returns Archive object from the rootElement. Calculates the size and number of elements in that Archive.
     *
     * @param archive        archive that will be created
     * @param archiveName    name of the archive
     * @param elements       list of elements in the Archive
     * @param compressedPath path to the archive - consists of SOURCE_PATH.EXTENSION
     */
    private Archive buildArchive(Archive archive, String archiveName, int numOfElements, List<Element> elements, String compressedPath) throws FileNotFoundException {
        if (archive == null) {
            archive = new Archive();
        }

        File compressedFile = new File(compressedPath);
        if (!compressedFile.exists()) {
            throw new FileNotFoundException("Trying to create an archive for non existing file with path '" + compressedPath + "'");
        }

        archive.setnElem(numOfElements);
        archive.setSize(compressedFile.length());
        archive.setName(archiveName);
        archive.setLocation(compressedPath);
        archive.setElements(elements);
        archive.completeInsertion();

        return archive;
    }

    private int countFilesForArchiving(List<File> files) {
        AtomicInteger numOfFiles = new AtomicInteger();
        files.forEach(file -> numOfFiles.addAndGet(FileUtils.countFileElements(file)));
        return numOfFiles.get();
    }
}
