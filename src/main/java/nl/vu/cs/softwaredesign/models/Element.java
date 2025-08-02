package nl.vu.cs.softwaredesign.models;

import java.util.Date;

/**
 * Composite design pattern
 * The Element, RegularFile, and Directory classes exhibit the Composite Pattern.
 * Element acts as the component interface defining operations for both file and directory elements.
 * RegularFile and Directory are the leaf and composite classes, respectively.
 */
public abstract class Element {
    public static int counterElem;
    private String name;
    private final Date dateCreation;
    private long size;
    private Element parent;

    /**
     * Constructs an element with the specified name, creation date, size, and parent element.
     * It increments the counterElem field each time we call a constructor.
     *
     * @param name         the name of the element
     * @param dateCreation the creation date of the element
     * @param size         the size of the element
     * @param parent       the parent element (if exists)
     */
    public Element(String name, Date dateCreation, long size, Element parent) {
        this.name = name;
        this.dateCreation = dateCreation;
        this.size = size;
        this.parent = parent;
        counterElem++;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setParent(Element parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public long getSize() {
        return size;
    }

    /**
     * Returns the Parent Folder of the current element, if it exists.
     * */
    public Element getParent() {
        return parent;
    }
}
