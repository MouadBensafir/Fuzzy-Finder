module org.example.fuzzy_app {
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;   
    requires javafx.media;      
    requires javafx.swing;     

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires org.fxmisc.richtext;
    requires org.fxmisc.flowless;

    opens org.example.fuzzy_app to javafx.fxml;
    opens org.example.fuzzy_app.view to javafx.fxml;
    exports org.example.fuzzy_app;
}
