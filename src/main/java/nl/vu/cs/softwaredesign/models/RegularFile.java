package nl.vu.cs.softwaredesign.models;

import nl.vu.cs.softwaredesign.utils.enums.FType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;

/**
 * Represents a regular file in the file system.
 */
public class RegularFile extends Element {
    private FType type;

    /**
     * Constructs a regular file with the specified attributes.
     *
     * @param name         the name of the file
     * @param dateCreation the creation date of the file
     * @param size         the size of the file
     * @param parent       the parent element (if exists)
     */
    public RegularFile(String name, Date dateCreation, long size, Element parent, FType type) {
        super(name, dateCreation, size, parent);
        this.type = type;
    }

    /**
     * Returns the type of the file.
     */
    public FType getType() {
        return type;
    }

    public void setType(FType type) {
        this.type = type;
    }
}
