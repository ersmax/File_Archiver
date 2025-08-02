package nl.vu.cs.softwaredesign.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public interface Compressible {

    /**
     * Ranges between 0 - 9. Higher value means more complex compression of the files.
     */
    int compressLvl = 3; // Default value

    void compress(List<File> files, String archiveName, String destinationPath);

    void decompress(String archivePath, String extractionPath) throws FileNotFoundException;

    /**
     * The purpose of this method is to return a string that represents an extension.
     * (e.g ".zip", ".rar")
     */
    String getTargetPathExtension();
}
