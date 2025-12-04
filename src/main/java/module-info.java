module org.example.fuzzy_app {
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;   
    requires javafx.media;      
    requires javafx.swing;     

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;

    opens org.example.fuzzy_app to javafx.fxml;
    exports org.example.fuzzy_app;
    exports org.example.fuzzy_app.algorithm;
    exports org.example.fuzzy_app.service;
    opens org.example.fuzzy_app.algorithm to javafx.fxml;
}
