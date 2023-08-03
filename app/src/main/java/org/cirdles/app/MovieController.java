package org.cirdles.app;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.*;

import org.cirdles.*;
import org.cirdles.app.utilities.GenreOptions;
import org.cirdles.app.utilities.MovieEditor;
import org.cirdles.app.utilities.SaveButtonStateManager;
import org.cirdles.app.utilities.TableCellEditor;
import org.cirdles.utilities.file.MovieFileResources;

import java.io.*;
import java.util.*;

public class MovieController {

    private HostServices hostServices;

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @FXML
    private Label welcomeText;
    @FXML
    private TextField nameField, releaseField;
    @FXML
    private ComboBox<String> genreComboBox;
    @FXML
    private TableView<Movie> movieTableView;
    @FXML
    private TableColumn<Movie, String> nameColumn, genreColumn;
    @FXML
    private TableColumn<Movie, Integer> releaseYearColumn;
    @FXML
    private VBox sessionContainer;
    @FXML
    private ImageView logoImageView;
    @FXML
    private Button saveXMLButton;
    @FXML
    private Button saveBinaryButton;
    @FXML
    private Button saveCSVButton;
    private Set<Movie> movieSet;
    private MovieEditor movieEditor;
    private SaveButtonStateManager saveButtonStateManager;

    public MovieController() {
        movieSet = new TreeSet<>();
    }

    public void initialize() {
        movieEditor = new MovieEditor(movieTableView, welcomeText);
        TableCellEditor tableCellEditor = new TableCellEditor(movieTableView, movieEditor);

        genreComboBox.getItems().addAll(GenreOptions.getGenreOptions());

        tableCellEditor.addTextFieldCellFactory(nameColumn);
        tableCellEditor.addIntegerFieldCellFactory(releaseYearColumn);
        tableCellEditor.addComboBoxCellFactory(genreColumn);

        movieTableView.setEditable(true);
        movieTableView.getItems().addAll(movieSet);

        saveButtonStateManager = new SaveButtonStateManager(
                saveXMLButton, saveBinaryButton, saveCSVButton, nameField, releaseField, genreComboBox, movieTableView
        );

        saveButtonStateManager.updateSaveButtonsState();

        // Add an InvalidationListener to the table data to watch for changes in the items list
        movieTableView.getItems().addListener((InvalidationListener) observable -> {
            saveButtonStateManager.updateSaveButtonsState();
        });
    }

    @FXML
    protected void addMovieButtonClicked() {
        String name = nameField.getText();
        String releaseStr = releaseField.getText();
        String genre = genreComboBox.getValue();

        if (!name.isEmpty() && !releaseStr.isEmpty()) {
            try {
                int release = Integer.parseInt(releaseStr);
                if (release >= 1000 && release <= 3000) {
                    if (genre != null && !genre.equals("Select genre")) {
                        Movie movie = new Movie(name, release, genre);
                        movieSet.add(movie);
                        movieTableView.getItems().add(movie); // Add movie to the TableView
                        welcomeText.setText("Movie added: " + movie.getName());
                        nameField.clear();
                        releaseField.clear();

                        genreComboBox.getSelectionModel().select("Select genre");
                    } else {
                        welcomeText.setText("Please select a valid genre!");
                    }
                } else {
                    welcomeText.setText("Invalid release year!");
                }
            } catch (NumberFormatException e) {
                welcomeText.setText("Invalid release year! Please enter a valid number.");
            }
        } else {
            welcomeText.setText("Please enter movie details!");
        }
        saveButtonStateManager.updateSaveButtonsState();
    }

    @FXML
    protected void onSaveXMLButtonClick() {
        if (!movieSet.isEmpty()) {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save Movies as XML");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));

                Stage stage = (Stage) welcomeText.getScene().getWindow();
                File file = fileChooser.showSaveDialog(stage);

                if (file != null) {
                    String filename = file.getPath();
                    MovieSetWrapper movieSetWrapper = new MovieSetWrapper(movieSet);

                    try {
                        XMLSerializer.serializeToXML(movieSetWrapper, filename);
                        welcomeText.setText("Movie data saved as XML!");
                    } catch (IOException e) {
                        e.printStackTrace();
                        welcomeText.setText("Error occurred while saving movies as XML. See console for details.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                welcomeText.setText("Error occurred while saving movies as XML. See console for details.");
            }
        } else {
            welcomeText.setText("No movies to save!");
        }
    }

    @FXML
    protected void onSaveCSVButtonClick() {
        if (!movieSet.isEmpty()) {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save Movies as CSV");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

                // Show save file dialog
                Stage stage = (Stage) welcomeText.getScene().getWindow();
                File file = fileChooser.showSaveDialog(stage);

                if (file != null) {
                    String filename = file.getPath();
                    Movie.serializeSetToCSV(movieSet, filename);
                    welcomeText.setText("Movie data exported as CSV!");
                }
            } catch (IOException e) {
                welcomeText.setText("Error occurred while saving movies as CSV!");
            }
        } else {
            welcomeText.setText("No movies to save!");
        }
    }

    @FXML
    protected void onSaveBinaryButtonClick() {
        if (!movieSet.isEmpty()) {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save Movies as Binary");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Binary Files", "*.bin"));

                Stage stage = (Stage) welcomeText.getScene().getWindow();
                File file = fileChooser.showSaveDialog(stage);

                if (file != null) {
                    String filename = file.getPath();
                    BinarySerializer.serializeToBinary(movieSet, filename);
                    welcomeText.setText("Movie data saved as binary!");
                }
            } catch (IOException e) {
                welcomeText.setText("Error occurred while saving movies as binary!");
            }
        } else {
            welcomeText.setText("No movies to save!");
        }
    }

    @FXML
    protected void onHelpButtonClick() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Movie Application");

        VBox content = new VBox();
        content.setSpacing(10);

        Text helpText = new Text("Information\n\n" +
                "Enter the details of a movie in the respective fields and select a genre from the dropdown menu. " +
                "\nClick 'Add Movie' to add it to the collection. Click 'Edit' to edit a movie's data.\n" +
                "Once you have added multiple movies, use the 'Save as' buttons to save the movie data as CSV, XML, or Binary.\n");

        VBox vbox = new VBox();
        vbox.getChildren().add(helpText);
        content.getChildren().add(vbox);

        alert.getDialogPane().setContent(content);
        // Get the main application window (the root of the scene)
        Stage mainStage = (Stage) welcomeText.getScene().getWindow();
        alert.initOwner(mainStage);
        alert.showAndWait();
    }

    @FXML
    protected void onOpenXMLButtonClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Movie Set XML");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                Set<Movie> movieSet = XMLSerializer.deserializeFromXML(selectedFile.getPath());
                movieTableView.getItems().clear();
                movieTableView.getItems().addAll(movieSet);
                welcomeText.setText("Movie set loaded from XML");
                logoImageView.setVisible(false);

                // Show the session container
                sessionContainer.setVisible(true);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        genreComboBox.getSelectionModel().select("Select genre");
        saveButtonStateManager.updateSaveButtonsState();
    }

    @FXML
    protected void onOpenBinaryButtonClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Movie Set (Binary)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Binary Files", "*.bin"));

        // Show open file dialog
        Stage stage = (Stage) welcomeText.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            String filename = file.getPath();

            try {
                Set<Movie> loadedMovieSet = (Set<Movie>) BinarySerializer.deserializeFromBinary(filename);
                if (loadedMovieSet != null && !loadedMovieSet.isEmpty()) {
                    movieSet = loadedMovieSet;
                    movieTableView.getItems().clear();
                    movieTableView.getItems().addAll(movieSet);
                    welcomeText.setText("Movie set loaded from Binary");

                    logoImageView.setVisible(false);
                    sessionContainer.setVisible(true);
                } else {
                    welcomeText.setText("Invalid movie set in the Binary file!");
                }
            } catch (IOException | ClassNotFoundException e) {
                welcomeText.setText("Error occurred while loading movie set from Binary file!");
            }
        }
        genreComboBox.getSelectionModel().select("Select genre");
        saveButtonStateManager.updateSaveButtonsState();
    }

    @FXML
    protected void onOpenCSVButtonClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Movie Set (CSV)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        // Show open file dialog
        Stage stage = (Stage) welcomeText.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            String filename = file.getPath();
            try {
                Set<Movie> loadedMovieSet = Movie.deserializeSetFromCSV(filename);
                if (loadedMovieSet != null && !loadedMovieSet.isEmpty()) {
                    movieSet = loadedMovieSet;
                    movieTableView.getItems().clear();
                    movieTableView.getItems().addAll(movieSet);

                    welcomeText.setText("Movie set loaded from CSV");
                    logoImageView.setVisible(false);
                    sessionContainer.setVisible(true);
                } else {
                    welcomeText.setText("Invalid movie set in the CSV file!");
                }
            } catch (IOException e) {
                welcomeText.setText("Error occurred while loading movie set from CSV file!");
            }

        }
        genreComboBox.getSelectionModel().select("Select genre");
        saveButtonStateManager.updateSaveButtonsState();
    }

    @FXML
    protected void onNewSessionClicked() {

        sessionContainer.setVisible(true);
        sessionContainer.requestFocus();

        nameField.clear();
        releaseField.clear();
        genreComboBox.getSelectionModel().select("Select genre");

        movieTableView.getItems().clear();
        logoImageView.setVisible(false);
        welcomeText.setText("");

        saveButtonStateManager.updateSaveButtonsState();
    }

    @FXML
    protected void onCloseSessionClicked() {

        sessionContainer.setVisible(false);

        nameField.clear();
        releaseField.clear();
        genreComboBox.setValue(null);

        movieTableView.getItems().clear();
        logoImageView.setVisible(true);
        welcomeText.setText("");
    }

    @FXML
    protected void onQuitClicked() {
        Stage stage = (Stage) welcomeText.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void openDemonstrationSessionMenuItemAction() {
        try {
            sessionContainer.requestFocus();
            MovieFileResources.initLocalResources();

            String csvFilePath = "MovieResources/movieSetExample.csv";
            File csvFile = new File(csvFilePath);

            if (!csvFile.exists()) {
                throw new FileNotFoundException("movieSetExample.csv not found.");
            }

            movieSet = Movie.deserializeSetFromCSV(csvFile.getAbsolutePath());
            movieTableView.getItems().clear();
            movieTableView.getItems().addAll(movieSet);
            welcomeText.setText("Movie set loaded from CSV");

            logoImageView.setVisible(false);
            sessionContainer.setVisible(true);
            genreComboBox.getSelectionModel().select("Select genre");

            saveButtonStateManager.updateSaveButtonsState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteSelectedMovie() {
        Movie selectedMovie = movieTableView.getSelectionModel().getSelectedItem();
        if (selectedMovie != null) {
            movieSet.remove(selectedMovie);
            movieTableView.getItems().remove(selectedMovie);
            welcomeText.setText(selectedMovie.getName() + " has been deleted.");
        }
    }

    @FXML
    protected void openDocumentation() {
        hostServices.showDocument("https://github.com/mynguy/movieGradle/blob/main/README.md");
    }
}