package nl.vu.cs.softwaredesign;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nl.vu.cs.softwaredesign.services.Encryptable;
import nl.vu.cs.softwaredesign.services.impl.*;
import nl.vu.cs.softwaredesign.services.Compressible;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 440);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {

        Compressible zipCompressible = new ZipCompression();
        Compressible tarCompressible = new TarCompression();
        Compressible SevenZCompression = new SevenZCompression();
        Compressible gzipCompressible = new GzipCompression();
        Encryptable encryptionService = new EncryptionMethod();
        Compressible compressibleLzo = new LzoCompression();
        Compressible compressibleBzip2 = new BZip2Compression();
        ArchiveService archiveService = new ArchiveService(zipCompressible, tarCompressible, SevenZCompression, gzipCompressible, encryptionService,compressibleLzo, compressibleBzip2);

        launch();
    }
}
