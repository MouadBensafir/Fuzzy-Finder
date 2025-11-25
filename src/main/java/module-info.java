module org.example.fuzzy_app {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;   // <-- OBLIGATOIRE
    requires javafx.media;      // <-- pour FXGL
    requires javafx.swing;      // <-- si FXGL ou TilesFX utilisent Swing

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;

    opens org.example.fuzzy_app to javafx.fxml;
    exports org.example.fuzzy_app;
}
