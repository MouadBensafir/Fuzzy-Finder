module org.example.fuzzy_app {
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;

    opens org.example.fuzzy_app to javafx.fxml;
    exports org.example.fuzzy_app;
}