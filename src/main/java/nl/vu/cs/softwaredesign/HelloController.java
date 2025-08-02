package nl.vu.cs.softwaredesign;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import nl.vu.cs.softwaredesign.models.Archive;
import nl.vu.cs.softwaredesign.models.CompressionData;
import nl.vu.cs.softwaredesign.services.Compressible;
import nl.vu.cs.softwaredesign.services.Encryptable;
import nl.vu.cs.softwaredesign.services.impl.*;




//import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
//import org.apache.commons.compress.archivers.sevenz.SevenZArchiveOutputStream;


//SevenZOutputFile


import javafx.scene.control.ComboBox;
import nl.vu.cs.softwaredesign.utils.FileUtils;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;


public class HelloController {

    @FXML
    private ComboBox
            compressionFormatComboBox;


    @FXML
    private ListView unarchiveListView;

    @FXML
    private TextField unarchivePasswordTextField;

    @FXML
    private Label unarchivePasswordLabel;

    @FXML
    private TextField archivePasswordTextField;

    @FXML
    private Label archivePasswordStrengthLabel;


    @FXML
    private Label selectedFilesLabel;

    @FXML
    private Label destinationFolderLabel;

    @FXML
    private Button selectFilesButton;

    @FXML
    private Button selectFoldersButton;

    @FXML
    private Button selectDestinationButton;

    @FXML
    Button archiveButton;

    @FXML
    Button unarchiveButton;

    @FXML
    private TextField archiveNameTextField;

    @FXML
    private ComboBox<String> compressionTypeComboBox;

    @FXML
    private Slider prioritySlider;

    @FXML
    private Label priorityLabel;

    @FXML
    private Label speedLabel;

    @FXML
    private Label compressionLabel;

    private final ArchiveService archiveService;

    private List<File> selectedFiles = new ArrayList<>();
    private StringProperty archiveName = new SimpleStringProperty();
    private File destinationFolder;

    private File unarchiveFile;

    private final String[] sortSpeed = {"LZO","GZIP", "ZIP", "BZIP2","RAR","TAR", "7z" };
    private final String[] sortCompression = {"7z", "BZIP2", "RAR", "GZIP", "ZIP", "TAR","LZO"};
    private String[] selectedPriority;

    public HelloController() {
        Compressible compressible = new ZipCompression();
        Compressible compressibleTar = new TarCompression();
        Compressible compressible7z = new SevenZCompression();
        Compressible compressibleGzip = new GzipCompression();
        Encryptable encryptionService = new EncryptionMethod();
        Compressible compressibleLzo = new LzoCompression();
        Compressible compressibleBzip2 = new BZip2Compression();
        this.archiveService = new ArchiveService(compressible, compressibleTar, compressible7z, compressibleGzip, encryptionService,compressibleLzo, compressibleBzip2);
    }

    @FXML
    public void initialize() {

        Preferences node = Preferences.userRoot().node("/softwaredesign");
        int lastPriority = node.getInt("lastPriority", 4);
        prioritySlider.setValue(lastPriority);
        priorityLabel.setText(String.valueOf(lastPriority));
        String type = node.get("selectedType", "speed");
        compressionTypeComboBox.setValue(type);
        if (type.equals("speed")){
            selectedPriority = sortSpeed;
        }else if (type.equals("compression")){
            selectedPriority = sortCompression;
        }

        archiveNameTextField.textProperty().bindBidirectional(archiveName);

        compressionTypeComboBox.setOnAction(event -> {
            String selectedType = compressionTypeComboBox.getValue();
            int p = prioritySlider.valueProperty().intValue();
            if (selectedType.equals("speed")){
                selectedPriority = sortSpeed;
                compressionFormatComboBox.setValue(this.selectedPriority[7- p]);
                node.put("selectedType", "speed");
            }else if (selectedType.equals("compression")){
                selectedPriority = sortCompression;
                compressionFormatComboBox.setValue(this.selectedPriority[7- p]);
                node.put("selectedType", "compression");
            }
            System.out.println("Selected Compression Type: " + selectedType);

            if (selectedFiles!=null && selectedFiles.size()>0){
                String selectedCompressionFormat = (String) compressionFormatComboBox.getValue();
                CompressionData compressionData = FileUtils.getCompressionData(selectedFiles, selectedCompressionFormat);
                compressionLabel.setText("compressed size: " + compressionData.getCompressedSize() + "bytes");
                speedLabel.setText("compression time: " + compressionData.getCompressionTime() + "ms");
            }
        });

        prioritySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (this.selectedPriority == null) {
                return;
            }
            int selectedPriority = newValue.intValue();
            node.putInt("lastPriority", selectedPriority);
            priorityLabel.setText(String.valueOf(selectedPriority));
            compressionFormatComboBox.setValue(this.selectedPriority[7- selectedPriority]);
            if (selectedFiles!=null && selectedFiles.size()>0){
                String selectedCompressionFormat = (String) compressionFormatComboBox.getValue();
                CompressionData compressionData = FileUtils.getCompressionData(selectedFiles, selectedCompressionFormat);
                compressionLabel.setText("compressed size: " + compressionData.getCompressedSize() + "bytes");
                speedLabel.setText("compression time: " + compressionData.getCompressionTime() + "ms");
            }

        });
    }

    /**
     * Method used only for testing while developing, can be removed later
     */
    private void test() {
        File file = new File("D:\\Desktop\\TestArchive\\ThisShouldBeZipped");
        this.selectedFiles.add(file);
        destinationFolder = new File("D:\\Desktop\\TestArchive");
        archiveName.setValue("Test");
        handleArchive();
    }

    @FXML
    public void refresh() {
        this.archiveButton.setDisable(shouldDisableArchiveButton());
//        this.unarchiveButton.setDisable(shouldDisableUnarchiveButton());
    }

    @FXML
    public void handleSelectFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Files you want to Archive");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("All Files", "*.*"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setInitialFileName("new-archive");

        // Allow selection of multiple files
        selectedFiles = fileChooser.showOpenMultipleDialog(selectFilesButton.getScene().getWindow());

        if (selectedFiles != null) {
            printSelectedFiles(selectedFiles);
        }
    }

    @FXML
    public void handleSelectFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select the Folder you want to Archive");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File selectedFolder = directoryChooser.showDialog(selectFoldersButton.getScene().getWindow());
        if (selectedFolder != null) {
            selectedFiles = new ArrayList<>();
            selectedFiles.add(selectedFolder);
            printSelectedFiles(selectedFiles);
        }
    }

    @FXML //For the Decompressing
    public void handleSelectArchive() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Archive");
        FileChooser.ExtensionFilter extensionFilter =
                new FileChooser.ExtensionFilter("ZIP Files", "*.zip", "*.tar", "*.7z", "*.gz", "*.encrypted");
        fileChooser.getExtensionFilters().addAll(extensionFilter);

        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        unarchiveFile = fileChooser.showOpenDialog(selectFilesButton.getScene().getWindow());


        if (unarchiveFile != null) {
            String fileExtension = getFileExtension(unarchiveFile);
            List<String> archiveSkeleton = archiveService.getListOfEntriesFromZip(unarchiveFile);
            ObservableList<String> items = FXCollections.observableArrayList();

            items.addAll(archiveSkeleton);

            unarchiveListView.setItems(items);

            switch (fileExtension.toLowerCase()) {
                case "zip":
                case "tar":
                case "7z":
                case "encrypted":
                case "gz":
                    break;
                default:
                    System.out.println("Invalid compression format selected");
                    break;
            }
        }

    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastDotIndex = name.lastIndexOf('.');
        if (lastDotIndex != -1 && lastDotIndex < name.length() - 1) {
            return name.substring(lastDotIndex + 1).toLowerCase(); // Convert to lowercase for case-insensitive check
        }
        return "";
    }

    @FXML
    public void handleArchive() {
        String selectedCompressionFormat = (String) compressionFormatComboBox.getValue();
        String encryptionPassword = archivePasswordTextField.getText();

        String passwordStrength = EncryptionMethod.checkPasswordStrength(encryptionPassword);

        if (!encryptionPassword.isEmpty() && passwordStrength.equals("weak")) {
            archivePasswordStrengthLabel.setText("Password Too Weak!");
            return;
        } else {
            archivePasswordStrengthLabel.setText("");
        }

        System.out.println(selectedCompressionFormat);

        switch (selectedCompressionFormat) {
            case "ZIP" -> handleZipArchive(encryptionPassword);
            case "TAR" -> handleTARArchive(encryptionPassword);
            case "7z" -> handle7zArchive(encryptionPassword);
            case "GZIP" -> handleGzipArchive(encryptionPassword);
            case "LZO" -> handleLzoArchive(encryptionPassword);
            case "BZIP2" -> handleBzip2Archive(encryptionPassword);
            case null, default -> System.out.println("Invalid compression format selected");
        }
    }

    private void handleBzip2Archive(String password) {
        Archive archive = archiveService.createBzip2Archive(selectedFiles, archiveName.getValue(), destinationFolder.getPath(), password);
        System.out.println("lzo Compression logic to be implemented" + archive);
    }

    private void handleLzoArchive(String password) {
        Archive archive = archiveService.createLzoArchive(selectedFiles, archiveName.getValue(), destinationFolder.getPath(), password);
        System.out.println("lzo Compression logic to be implemented" + archive);
    }


    private void handleTARArchive(String password) {
        Archive archive = archiveService.createTARArchive(selectedFiles, archiveName.getValue(), destinationFolder.getPath(), password);
        System.out.println("RAR Compression logic to be implemented" + archive);
    }

    private void handleZipArchive(String password) {
        Archive archive = archiveService.createArchive(selectedFiles, archiveName.getValue(), destinationFolder.getPath(), password);
        System.out.println("ZIP Archive:\n" + archive);
    }

    private void handle7zArchive(String password) {
        Archive archive = archiveService.create7ZArchive(selectedFiles, archiveName.getValue(), destinationFolder.getPath(), password);
        System.out.println("7z Archive:\n" + archive); // Print or handle the 7z archive
    }

    private void handleGzipArchive(String password) {
        Archive archive = archiveService.createGzipArchive(selectedFiles, archiveName.getValue(), destinationFolder.getPath(), password);
        System.out.println("Gzip Archive:\n" + archive);
    }

    @FXML
    public void handleUnarchive() {
        String password = unarchivePasswordTextField.getText();

        if (unarchiveFile.getName().contains(".encrypted") && password.isEmpty()) {
            unarchivePasswordLabel.setText("This file is encrypted, you need to provide a password");
            return;
        } else {
            unarchivePasswordLabel.setText("");
        }

        archiveService.extractArchive(unarchiveFile.getPath(), unarchiveFile.getParent(), password);
    }

    @FXML
    public void handleSelectDestination() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Destination Folder");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        destinationFolder = directoryChooser.showDialog(selectDestinationButton.getScene().getWindow());
        if (destinationFolder != null && destinationFolder.exists()) {
            destinationFolderLabel.setText("Destination Folder:\n    " + destinationFolder.getPath());
        }
    }

    public boolean shouldDisableArchiveButton() {
        return selectedFiles == null || selectedFiles.isEmpty() || archiveName.getValue() == null ||
                archiveName.getValue().isBlank() || destinationFolder == null || !destinationFolder.exists();
    }


    private void printSelectedFiles(List<File> selectedFiles) {
        StringBuilder selectionText = new StringBuilder("    ");
        for (File selectedFile : selectedFiles) {
            selectionText.append(selectedFile.getAbsolutePath()).append("\n    ");
        }
        selectedFilesLabel.setText(selectionText.toString());
    }
}
