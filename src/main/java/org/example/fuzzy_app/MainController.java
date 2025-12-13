package org.example.fuzzy_app;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.event.ActionEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import org.example.fuzzy_app.service.FileService;
import org.example.fuzzy_app.service.FileContentService;
import org.example.fuzzy_app.service.FileSearchService;
import org.example.fuzzy_app.service.IndexService;

public class MainController implements Initializable {
    @FXML
    private Label welcomeText;

    @FXML
    private TextField searchField;

    @FXML
    private ListView<String> rootsList;

    @FXML
    private Label statusLabel;

    @FXML
    private Label statsLabel;

    @FXML
    private ListView<String> resultsList;

    @FXML
    private TextArea fileContentArea;

    @FXML
    private ImageView imageView;

    @FXML
    private ScrollPane imageScrollPane;

    @FXML
    private TextField editorCommandField;

    @FXML
    private Button openFileButton;

    @FXML
    private Button openWithEditorButton;

    @FXML
    private Button openFolderButton;

    @FXML
    private HBox fileActionButtons;

    @FXML
    private HBox folderActionButtons;

    private CompletableFuture<?> currentSearchTask = null;
    private PauseTransition searchDebounce;
    private IndexService indexService;
    private boolean imageSizeListenersSetup = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        indexService = new IndexService();
        setupSearchListener();
        setupResultsCellFactory();
        setupResultsSelectionListener();
        setupImageSizeListeners();
        updateStatsAsync();
    }

    private void setupResultsCellFactory() {
        resultsList.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Path path = Paths.get(item);
                    String name = path.getFileName() != null ? path.getFileName().toString() : item;
                    boolean isDirectory = Files.isDirectory(path);

                    Label icon = new Label(isDirectory ? "📁" : "📄");
                    Label nameLabel = new Label(name);

                    HBox box = new HBox(8, icon, nameLabel);
                    setText(null);
                    setGraphic(box);
                }
            }
        });
    }
    
    private void setupImageSizeListeners() {
        if (imageSizeListenersSetup) return;
        
        // Set up listeners once to adjust image size when pane dimensions change
        Runnable updateSize = () -> {
            if (imageView.getImage() == null) return;
            
            Image image = imageView.getImage();
            double imageWidth = image.getWidth();
            double imageHeight = image.getHeight();
            double paneWidth = imageScrollPane.getWidth();
            double paneHeight = imageScrollPane.getHeight();
            
            // Account for scrollbar space (approximately 15-20 pixels)
            double availableWidth = paneWidth > 20 ? paneWidth - 20 : paneWidth;
            double availableHeight = paneHeight > 20 ? paneHeight - 20 : paneHeight;
            
            if (availableWidth > 0 && availableHeight > 0 && imageWidth > 0 && imageHeight > 0) {
                // Calculate scaling to fit within available space while preserving aspect ratio
                double scaleX = availableWidth / imageWidth;
                double scaleY = availableHeight / imageHeight;
                double scale = Math.min(scaleX, scaleY);
                
                // Only scale down if image is larger than available space
                if (scale < 1.0 || imageWidth > availableWidth || imageHeight > availableHeight) {
                    imageView.setFitWidth(imageWidth * scale);
                    imageView.setFitHeight(imageHeight * scale);
                } else {
                    // If image is smaller, show at original size
                    imageView.setFitWidth(imageWidth);
                    imageView.setFitHeight(imageHeight);
                }
            }
        };
        
        // Listen to width and height changes
        imageScrollPane.widthProperty().addListener((obs, oldVal, newVal) -> updateSize.run());
        imageScrollPane.heightProperty().addListener((obs, oldVal, newVal) -> updateSize.run());
        
        imageSizeListenersSetup = true;
    }

    private void setupSearchListener() {
        // Debounce search to avoid searching on every keystroke
        searchDebounce = new PauseTransition(Duration.millis(300));
        searchDebounce.setOnFinished(event -> {
            String query = searchField.getText();
            performSearch(query);
        });
        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Cancel previous debounce
            searchDebounce.stop();

            // If query is empty, clear results (don't show indexed paths)
            if (newValue == null || newValue.trim().isEmpty()) {
                if (resultsList != null) resultsList.getItems().clear();
                if (statusLabel != null) statusLabel.setText("");
            } else {
                // Otherwise, debounce the search
                searchDebounce.playFromStart();
            }
        });
    }

    private void setupResultsSelectionListener() {
        resultsList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateActionButtons(newValue);
            loadFileContent(newValue);
        });
    }

    private void performSearch(String query) {
        // Cancel previous search if still running
        if (currentSearchTask != null && !currentSearchTask.isDone()) {
            currentSearchTask.cancel(true);
        }

        if (indexService == null) return;
        
        // Get all indexed paths
        List<String> indexedPaths = indexService.getAllPaths();
        if (indexedPaths.isEmpty()) {
            resultsList.getItems().clear();
            statusLabel.setText("No files indexed. Add root directories to index.");
            return;
        }

        statusLabel.setText("Searching...");
        currentSearchTask = FileSearchService.searchAsync(
            indexedPaths,
            query,
            results -> Platform.runLater(() -> {
                if (results != null) {
                    resultsList.getItems().setAll(results);
                    statusLabel.setText("Found " + results.size() + " results");
                } else {
                    resultsList.getItems().clear();
                    statusLabel.setText("Search completed with no results");
                }
            })
        );
        
        // Handle errors
        currentSearchTask.exceptionally(throwable -> {
            Platform.runLater(() -> {
                statusLabel.setText("Search error: " + throwable.getMessage());
                resultsList.getItems().clear();
            });
            return null;
        });
    }

    private void updateActionButtons(String selectedPath) {
        if (selectedPath == null) {
            hideAllActionButtons();
            return;
        }

        Path path = Paths.get(selectedPath);
        boolean isDirectory = Files.isDirectory(path);
        boolean isFile = Files.isRegularFile(path);

        if (isDirectory) {
            showFolderButtons();
        } else if (isFile) {
            showFileButtons();
        } else {
            hideAllActionButtons();
        }
    }

    private void showFileButtons() {
        folderActionButtons.setVisible(false);
        folderActionButtons.setManaged(false);
        fileActionButtons.setVisible(true);
        fileActionButtons.setManaged(true);
    }

    private void showFolderButtons() {
        fileActionButtons.setVisible(false);
        fileActionButtons.setManaged(false);
        folderActionButtons.setVisible(true);
        folderActionButtons.setManaged(true);
    }

    private void hideAllActionButtons() {
        fileActionButtons.setVisible(false);
        fileActionButtons.setManaged(false);
        folderActionButtons.setVisible(false);
        folderActionButtons.setManaged(false);
    }

    @FXML
    protected void handleAddRoot(ActionEvent event) {
        try {
            Window window = (searchField != null && searchField.getScene() != null)
                    ? searchField.getScene().getWindow() : null;
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select Root Directory");
            File selected = chooser.showDialog(window);
            if (selected != null) {
                String path = selected.getAbsolutePath();
                if (!rootsList.getItems().contains(path)) {
                    rootsList.getItems().add(path);
                    statusLabel.setText("Indexing " + path + "...");
                    indexDirectory(Paths.get(path));
                } else {
                    statusLabel.setText("Root already added");
                }
            }
        } catch (Exception e) {
            if (statusLabel != null) statusLabel.setText("Error adding root: " + e.getMessage());
        }
    }
    
    private void indexDirectory(Path root) {
        CompletableFuture.runAsync(() -> {
            try {
                indexService.indexDirectory(root);
                Platform.runLater(() -> {
                    statusLabel.setText("Indexed " + root);
                    updateStatsAsync();
                    // Perform search if there's a query
                    if (searchField != null && !searchField.getText().isEmpty()) {
                        performSearch(searchField.getText());
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> 
                    statusLabel.setText("Error indexing " + root + ": " + e.getMessage()));
            }
        });
    }

    @FXML
    protected void handleRemoveRoot(ActionEvent event) {
        String selected = rootsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Path rootPath = Paths.get(selected);
            rootsList.getItems().remove(selected);
            indexService.removeDirectory(rootPath);
            statusLabel.setText("Removed root: " + selected);
            updateStatsAsync();
            // Perform search if there's a query
            if (searchField != null && !searchField.getText().isEmpty()) {
                performSearch(searchField.getText());
            } else {
                resultsList.getItems().clear();
            }
        }
    }

    private void loadFileContent(String selectedPath) {
        if (selectedPath == null) {
            fileContentArea.setText("");
            hideImageView();
            return;
        }
        
        Path path = Paths.get(selectedPath);
        if (!Files.exists(path)) {
            fileContentArea.setText("");
            hideImageView();
            return;
        }

        if (Files.isDirectory(path)) {
            fileContentArea.setText("");
            hideImageView();
            return;
        }

        // Check if it's an image file
        if (FileContentService.isImageFile(path)) {
            loadImage(path);
        } else {
            loadTextContent(path);
        }
    }
    
    private void loadTextContent(Path path) {
        hideImageView();
        FileContentService.readFileContentAsync(
            path,
            content -> Platform.runLater(() -> {
                fileContentArea.setText(content);
                statusLabel.setText("Loaded content: " + path.getFileName());
            }),
            error -> Platform.runLater(() -> {
                fileContentArea.setText("");
                statusLabel.setText(error);
            }),
            binaryMessage -> Platform.runLater(() -> {
                fileContentArea.setText(binaryMessage);
                statusLabel.setText("Cannot view binary file or PDF: " + path.getFileName());
            })
        );
    }
    
    private void loadImage(Path path) {
        hideImageView();
        FileContentService.loadImageAsync(
            path,
            image -> Platform.runLater(() -> {
                imageView.setImage(image);
                adjustImageSize();
                showImageView();
                statusLabel.setText("Loaded image: " + path.getFileName() + 
                    " (" + (int)image.getWidth() + "x" + (int)image.getHeight() + ")");
            }),
            error -> Platform.runLater(() -> {
                fileContentArea.setText(error);
                hideImageView();
                statusLabel.setText("Error loading image: " + path.getFileName());
            })
        );
    }
    
    private void adjustImageSize() {
        if (imageView.getImage() == null) return;
        
        // Trigger size adjustment after image is set
        Platform.runLater(() -> {
            if (imageView.getImage() == null) return;
            
            Image image = imageView.getImage();
            double imageWidth = image.getWidth();
            double imageHeight = image.getHeight();
            double paneWidth = imageScrollPane.getWidth();
            double paneHeight = imageScrollPane.getHeight();
            
            // Account for scrollbar space (approximately 15-20 pixels)
            double availableWidth = paneWidth > 20 ? paneWidth - 20 : paneWidth;
            double availableHeight = paneHeight > 20 ? paneHeight - 20 : paneHeight;
            
            if (availableWidth > 0 && availableHeight > 0 && imageWidth > 0 && imageHeight > 0) {
                // Calculate scaling to fit within available space while preserving aspect ratio
                double scaleX = availableWidth / imageWidth;
                double scaleY = availableHeight / imageHeight;
                double scale = Math.min(scaleX, scaleY);
                
                // Only scale down if image is larger than available space
                if (scale < 1.0 || imageWidth > availableWidth || imageHeight > availableHeight) {
                    imageView.setFitWidth(imageWidth * scale);
                    imageView.setFitHeight(imageHeight * scale);
                } else {
                    // If image is smaller, show at original size
                    imageView.setFitWidth(imageWidth);
                    imageView.setFitHeight(imageHeight);
                }
            }
        });
    }
    
    private void showImageView() {
        fileContentArea.setVisible(false);
        fileContentArea.setManaged(false);
        imageScrollPane.setVisible(true);
        imageScrollPane.setManaged(true);
    }
    
    private void hideImageView() {
        imageScrollPane.setVisible(false);
        imageScrollPane.setManaged(false);
        fileContentArea.setVisible(true);
        fileContentArea.setManaged(true);
    }

    @FXML
    protected void handleOpenFile(ActionEvent event) {
        String selected = resultsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("No file selected to open");
            return;
        }
        
        Path path = Paths.get(selected);
        if (!Files.exists(path)) {
            statusLabel.setText("File not found: " + selected);
            return;
        }
        
        if (Files.isDirectory(path)) {
            statusLabel.setText("Selected item is a directory, use 'Open Folder' instead");
            return;
        }
        
        try {
            FileService.openFile(path);
            statusLabel.setText("Opened file: " + path.getFileName());
        } catch (IOException e) {
            statusLabel.setText("Error opening file: " + e.getMessage());
        }
    }

    @FXML
    protected void handleOpenWithEditor(ActionEvent event) {
        String editor = (editorCommandField != null) ? editorCommandField.getText().trim() : "";
        String selected = resultsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("No file selected to open with editor");
            return;
        }
        
        Path path = Paths.get(selected);
        if (!Files.exists(path)) {
            statusLabel.setText("File not found: " + selected);
            return;
        }
        
        if (Files.isDirectory(path)) {
            statusLabel.setText("Selected item is a directory, use 'Open Folder' instead");
            return;
        }

        try {
            FileService.openWithEditor(path, editor);
            statusLabel.setText("Launched editor: " + (editor.isEmpty() ? "default" : editor));
        } catch (IOException e) {
            statusLabel.setText("Error launching editor: " + e.getMessage());
        }
    }

    @FXML
    protected void handleOpenFolder(ActionEvent event) {
        String selected = resultsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("No item selected to open folder");
            return;
        }
        
        Path path = Paths.get(selected);
        Path folder = Files.isDirectory(path) ? path : path.getParent();
        if (folder == null || !Files.exists(folder)) {
            statusLabel.setText("Folder not found for: " + selected);
            return;
        }
        
        try {
            FileService.openFolder(folder);
            statusLabel.setText("Opened folder: " + folder.toString());
        } catch (IOException e) {
            statusLabel.setText("Error opening folder: " + e.getMessage());
        }
    }

    private void updateStatsAsync() {
        if (indexService == null || statsLabel == null) return;
        
        CompletableFuture.runAsync(() -> {
            IndexService.IndexStats stats = indexService.getStats();
            Platform.runLater(() -> statsLabel.setText(stats.toString()));
        });
    }
}

