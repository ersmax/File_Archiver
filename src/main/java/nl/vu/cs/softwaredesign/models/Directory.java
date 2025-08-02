package nl.vu.cs.softwaredesign.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a directory in the file system.
 */
public class Directory extends Element {

    private List<Element> children;

    /**
     * Constructs a directory with the specified attributes.
     *
     * @param name         the name of the directory
     * @param dateCreation the creation date of the directory
     * @param size         the size of the directory
     * @param parent       the parent element (if exists)
     */
    public Directory(String name, Date dateCreation, long size, Element parent) {
        super(name, dateCreation, size, parent);
        children = new ArrayList<>();
    }

    /**
     * Constructs a directory with the specified attributes.
     *
     * @param name         the name of the directory
     * @param dateCreation the creation date of the directory
     * @param size         the size of the directory
     * @param parent       the parent element (if exists)
     * @param children     the children of the element (if exists)
     */
    public Directory(String name, Date dateCreation, long size, Element parent, List<Element> children) {
        super(name, dateCreation, size, parent);
        this.children = children;
    }
    /**
     * Adds a child element to the directory.
     *
     * @param element the element to add as a child
     */
    public void addChild(Element element) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(element);
    }

    public List<Element> getChildren() {
        return children;
    }

    public void setChildren(List<Element> children) {
        this.children = children;
    }

    @Override
    public long getSize() {
        long size = 0;
        for (Element child : children) {
            size += child.getSize();
        }
        return size;
    }
}
