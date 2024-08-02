package ru.MT.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;


import java.awt.*;
import java.io.*;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.poi.POIXMLException;
import ru.MT.logic.*;

public class Controller implements ProgressUpdateListener{
    private ApplicationMintrance logic = new ApplicationMintrance();

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    @FXML
    private Button homeButton;

    @FXML
    private Button settingButton;

    @FXML
    private Button infoButton;

    @FXML
    private Pane pane;

    @FXML
    private AnchorPane rootPane;

    @FXML
    private Text fileName;

    @FXML
    private Button open;

    @FXML
    private ImageView input;

    @FXML
    private ImageView inputActive;

    @FXML
    private ImageView downloadButtonTrue;

    @FXML
    private Text inputText;

    @FXML
    private Text keyActive;

    @FXML
    private TextField keyInput;

    @FXML
    private RadioButton driving;

    @FXML
    private RadioButton truck;

    @FXML
    private RadioButton transit;

    @FXML
    private RadioButton walking;

    @FXML
    private CheckBox avoidTolls;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Text text;

    private String mode;

    private ConfigManager configManager;

    private File selectedFile;

    @FXML
    private void importFile() throws Exception {
        if (selectedFile != null) {
            executor.submit(() -> {
                try {
                    Platform.runLater(() -> progressBar.setVisible(true));
                    logic.setProgressUpdateListener(progress -> Platform.runLater(() -> {
                        progressBar.setProgress(progress);
                    }));
                    logic.appMT(selectedFile);
                    Platform.runLater(() -> progressBar.setVisible(false));
                    open.setVisible(true);
                }catch (NullPointerException e) {
                    Platform.runLater(() -> {
                        e.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText("Файл Пуст. Проверьте на содержание");
                        alert.showAndWait();
                        open.setVisible(false);
                    });
                }catch (POIXMLException e){
                    Platform.runLater(() -> {
                        e.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText("Произошла ошибка: Неверный формат входного файла, выберите .xlsx");
                        alert.showAndWait();
                        open.setVisible(false);
                    });
                }catch (ColsMoreHundredException e){
                    Platform.runLater(() -> {
                        e.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText("К сожалению в вашем файле существует более 100 пунктов назначения, пожалуйста сократите количество");
                        alert.showAndWait();
                        open.setVisible(false);
                    });
                }catch (UnknownHostException e){
                    Platform.runLater(() -> {
                        e.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText("Не удалось подключиться к сервису Yandex. Проверьте Интернет - соединение");
                        alert.showAndWait();
                        open.setVisible(false);
                    });
                }catch (UnauthorisedException e){
                    Platform.runLater(() -> {
                        e.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText("Ошибка валидации ключа доступа. Проверьте текущий статус ключа, при необходимоти - смените");
                        alert.showAndWait();
                        open.setVisible(false);
                    });
                }catch (HTTPForbiddenException e){
                    Platform.runLater(() -> {
                        e.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText("!!! Внимание !!! Лимит запросов на сегодня превышен!");
                        alert.showAndWait();
                        open.setVisible(false);
                    });
                }catch (TooManyRequestsException e){
                    Platform.runLater(() -> {
                        e.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText("!!! Внимание !!! Лимит запросов на сегодня превышен!/ Слишком много запросов");
                        alert.showAndWait();
                        open.setVisible(false);
                    });
                }catch (IllegalArgumentException e){
                    Platform.runLater(() -> {
                        e.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText("В выбранном файле отсутвует 2й лист");
                        alert.showAndWait();
                        open.setVisible(false);
                    });
                }catch (FileNotFoundException e){
                    Platform.runLater(() -> {
                        e.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText("Выбранный файл открыт, закройте и повторите попытку");
                        alert.showAndWait();
                        open.setVisible(false);
                    });
                }catch (OutOfMemoryError e){
                    Platform.runLater(() -> {
                        e.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText("Файл слишком велик, выберите меньший по размеру файл");
                        alert.showAndWait();
                        open.setVisible(false);
                    });
                }catch (Exception e){
                    Platform.runLater(() -> {
                        e.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText("Произошла ошибка: Возникла непредвиденная ошибка");
                        alert.showAndWait();
                        open.setVisible(false);
                    });
                }
            });
        }
    }
    @FXML
    private void downloadProcessedFile() {
        if (selectedFile != null) {
            try {
                Desktop.getDesktop().open(selectedFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void initialize() {
        if (pane != null) {
            configManager = new ConfigManager();
            configManager.setAvoidTolls(true);
            mode = configManager.getMode();
            if(mode.equals("driving")){
                driving.setSelected(true);
            } else if (mode.equals("truck")) {
                truck.setSelected(true);
            } else if (mode.equals("transit")) {
                transit.setSelected(true);
            } else if (mode.equals("walking")) {
                walking.setSelected(true);
            }

            pane.setOnDragOver(event -> {
                if (event.getGestureSource() != pane && event.getDragboard().hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
                event.consume();
            });

            pane.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles() && db.getFiles().size() == 1) {
                    success = true;
                    selectedFile = db.getFiles().get(0);
                    fileName.setText(selectedFile.getName());
                    progressBar.setVisible(false);
                    open.setVisible(false);
                    fileName.setVisible(true);
                    input.setVisible(false);
                    inputActive.setVisible(true);
                    downloadButtonTrue.setVisible(true);
                    inputText.setVisible(false);
                }
                event.setDropCompleted(success);
                event.consume();
            });
            pane.setOnMouseClicked(event -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Выберите файл Excel");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel файлы", "*.xlsx"));

                selectedFile = fileChooser.showOpenDialog(null);

                if (selectedFile != null) {
                    fileName.setText(selectedFile.getName());
                    progressBar.setVisible(false);
                    open.setVisible(false);
                    fileName.setVisible(true);
                    input.setVisible(false);
                    inputActive.setVisible(true);
                    downloadButtonTrue.setVisible(true);
                    inputText.setVisible(false);
                }
            });
        }
        if (keyActive!=null){
            configManager = new ConfigManager();
            keyActive.setText(configManager.getKey());
            keyInput.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.isEmpty()) {
                    text.setVisible(false);
                } else {
                    text.setVisible(true);
                }
            });
        }
        homeButton.setOnAction(event -> loadScene("/home.fxml"));
        settingButton.setOnAction(event -> loadScene("/setting.fxml"));
        infoButton.setOnAction(event -> loadScene("/info.fxml"));
    }
    @FXML
    private void activateKey() {
        String newKey = keyInput.getText();
        if (!newKey.isEmpty()) {
            configManager.setKey(newKey);
            keyActive.setText(newKey);
        }
    }

    @FXML
    private void drivingActivate(){
        truck.setSelected(false);
        transit.setSelected(false);
        walking.setSelected(false);
        configManager.setMode("driving");
    }

    @FXML
    private void truckActivate(){
        driving.setSelected(false);
        transit.setSelected(false);
        walking.setSelected(false);
        configManager.setMode("truck");
    }

    @FXML
    private void transitActivate(){
        driving.setSelected(false);
        truck.setSelected(false);
        walking.setSelected(false);
        configManager.setMode("transit");
    }

    @FXML
    private void walkingActivate(){
        driving.setSelected(false);
        truck.setSelected(false);
        transit.setSelected(false);
        configManager.setMode("walking");
    }

    @FXML
    private void avoidTollsOnAction(){
        configManager.setAvoidTolls(avoidTolls.isSelected());
    }
    @FXML
    private void downloadExcelFile() {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("example.xlsx");

            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName("example.xlsx");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel files", "*.xlsx"));
            File file = fileChooser.showSaveDialog(null);

            if (file != null) {
                Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadScene(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);

            rootPane.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateProgressBar(double progress) {
        progressBar.setProgress(progress);
    }
}
