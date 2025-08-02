package nl.vu.cs.softwaredesign.models;

import nl.vu.cs.softwaredesign.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class Archive {
    private int nElem;
    private long size;
    private String name;
    private String location;
    private Boolean complete;
    private List<Element> elements;

    public Archive() {
        this.complete = false;
    }

    public Archive(int nElem, long size, String name, String location, boolean complete, List<Element> elements) {
        this.nElem = nElem;
        this.size = size;
        this.name = name;
        this.location = location;
        this.complete = complete;
        this.elements = new ArrayList<>(elements);
    }

    /**
     * Completes the archive creation process. Will set the field complete to true.
     */
    public void completeInsertion() {
        setComplete(true);
    }


    /**
     * Calculates and sets the size and nElem parameters of the Archive.
     * @return size of the archive
     */
    public long getSizeAndElement() throws FileNotFoundException {
        File compressedFile = new File(this.location);
        if (!compressedFile.exists()) {
            throw new FileNotFoundException("Trying to calculate the size of an archive for non existing file with path '" + this.location + "'");
        }
        setSize(compressedFile.length());
        setnElem(FileUtils.countFileElements(compressedFile));
        return getSize();
    }

    public int getnElem() {
        return nElem;
    }

    public void setnElem(int nElem) {
        this.nElem = nElem;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getComplete() {
        return complete;
    }

    public void setComplete(Boolean complete) {
        this.complete = complete;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<Element> getElements() {
        return elements;
    }

    public void setElements(List<Element> elements) {
        this.elements = elements;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== Archive Info =====\n");
        sb.append("Number of Elements: ").append(nElem).append("\n");
        sb.append("Size: ").append(size).append("\n");
        sb.append("Name: ").append(name).append("\n");
        sb.append("Elements:\n");
        for (Element element : elements) {
            sb.append(elementToString(element, 1));
        }
        return sb.toString();
    }

    private String elementToString(Element element, int depth) {
        StringBuilder sb = new StringBuilder();
        String indent = "    ".repeat(depth); // Adjust the indentation as needed
        sb.append(indent).append("- ").append(element.getName()).append("\n");
        if (element instanceof Directory) {
            for (Element child : ((Directory) element).getChildren()) {
                sb.append(elementToString(child, depth + 1));
            }
        }
        return sb.toString();
    }
}
