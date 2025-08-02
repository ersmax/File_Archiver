module nl.vu.cs.softwaredesign.softwaredesignvumaster {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.compress;
    requires java.prefs;


    opens nl.vu.cs.softwaredesign to javafx.fxml;
    exports nl.vu.cs.softwaredesign;
}