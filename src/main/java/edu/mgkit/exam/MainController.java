package edu.mgkit.exam;

import com.google.gson.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;

public class MainController {
    @FXML
    private FlowPane modulePane;
    @FXML
    private Button addons;
    @FXML
    private Button sync1;
    @FXML
    private Button sync2;
    @FXML
    private Label MessageText;
    @FXML
    private ImageView CurrentMod;
    @FXML
    private ImageView nyan;

private ArrayList<Operator> operators = new ArrayList<>()
{
    @Override
    public boolean contains(Object o) {
        for (Object p:operators)
        {
            Operator a = (Operator)p;
            Operator b = (Operator)o;
            if (a.getName().equals(b.getName())) return true;
        }
        return false;
    }
};

private boolean Stop;


private Operator find(ArrayList<Operator> operatorsList, String name)
{
    for (Operator iterator:operatorsList)
        if (iterator.getName().equals(name)) return iterator;
        return null;
}

    public void openMenu() throws IOException {
        ArrayList<Operator> new_operators = new ArrayList<>();
        Stage mainStage = (Stage) modulePane.getScene().getWindow();
        Stage st2 = App.setScene("choose_scene");
        mainStage.getScene().getRoot().setDisable(true);
        st2.setOnCloseRequest(windowEvent -> mainStage.getScene().getRoot().setDisable(false));
        ListView elements = (ListView) st2.getScene().lookup("#elements");
        Button activate = (Button) st2.getScene().lookup("#activate");
        String pathToFolder = App.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        File folder = new File(pathToFolder);
        pathToFolder = folder.getParentFile().getPath()+"\\modules\\";

        folder = new File(pathToFolder);
        FileFilter filter = pathname -> pathname.toString().contains("json");
         File[] files = folder.listFiles(filter);
        Gson gson = new Gson();
        assert files != null;
        for (File entry:files)
        {
            FileReader reader = new FileReader(entry);
            int i;
            StringBuilder json = new StringBuilder();
            while ((i=reader.read())!=-1) json.append((char)i);
            Operator newOperator = gson.fromJson(json.toString(),Operator.class);
            new_operators.add(newOperator);
        }
        for (Operator currentOperator:new_operators)
        {
            CheckBox chooseForOperator = new CheckBox(currentOperator.getName());
            boolean opetarorIsActive = false;
            for (Object node:operators)
            {
                Operator iterator = (Operator)node;
                if (iterator.getName().equals(currentOperator.getName())) {opetarorIsActive = true;break;}
            }
            if (opetarorIsActive) chooseForOperator.setSelected(true);
            ObservableList items = elements.getItems();
            items.add(chooseForOperator);
            elements.setItems(items);
        }

        activate.setOnAction(actionEvent12 -> {
            modulePane.getChildren().clear();
            ObservableList items = elements.getItems();
            for (Object itemsNode:items)
            {
                CheckBox checkForOperator = (CheckBox)itemsNode;
                if (checkForOperator.isSelected()) {
                    String operatorName = checkForOperator.getText();
                        OperatorButton operatorIcon = new OperatorButton();
                        operatorIcon.setOperator(find(new_operators,operatorName));
                    operatorIcon.setOnAction( eventHandler ->
                    {
                        if (operatorIcon.getOperator().getRequired()) operatorIcon.getOperator().aut(operatorIcon);
                        mainStage.setOnShowing(windowEvent -> {
                            checkAuthorization();
                        });
                        checkAuthorization();
                    });
                        modulePane.getChildren().add(operatorIcon);
                }
            }
            operators = new_operators;
            items.clear();
            elements.setItems(items);
            st2.close();
            mainStage.getScene().getRoot().setDisable(false);
            operators.clear();
            for (Object moduleNode: modulePane.getChildren())
                operators.add(((OperatorButton)moduleNode).getOperator());
            if (operators.size()<=1)
            {
                CurrentMod.setImage(new Image(getClass().getResourceAsStream("pictures/mod6.jpg"),CurrentMod.getFitWidth(),CurrentMod.getFitHeight(),false,false));
                MessageText.setText("Нет модулей - нет работы");
            }
            else checkAuthorization();
        });
    }

    public void syncOne() throws IOException {
        Stage mainStage = (Stage) modulePane.getScene().getWindow();
        Stage chooseOperator = App.setScene("choose_service");
        mainStage.getScene().getRoot().setDisable(true);
        chooseOperator.setOnCloseRequest(windowEvent -> mainStage.getScene().getRoot().setDisable(false));
        ListView elements = (ListView) chooseOperator.getScene().lookup("#elements");
        Button activate = (Button) chooseOperator.getScene().lookup("#activate");
        ToggleGroup group = new ToggleGroup();
        for (Operator currentOperator:operators)
        {
            if (currentOperator.getAuthorized())
            {
                RadioButton b = new RadioButton(currentOperator.getName());
                b.setToggleGroup(group);
                ObservableList items = elements.getItems();
                items.add(b);
                elements.setItems(items);
            }
        }
        activate.setOnAction(actionEvent -> {
            Operator target;
            RadioButton selectedOperator = (RadioButton) group.getSelectedToggle();
            target = find(operators,selectedOperator.getText());
            elements.getItems().clear();
            chooseOperator.close();
            mainStage.getScene().getRoot().setDisable(false);
            try {
                Stage logsScene = App.setScene("Log");
                ListView<Log> logList = (ListView<Log>) logsScene.getScene().lookup("#ActionLog");
                logList.setCellFactory(param -> new ActionLogItems());
                logsScene.setOnCloseRequest(windowEvent -> {
                    logList.getItems().clear();
                });
                if (target != null)
                {
                    final Operator operator = target;
                    CurrentMod.setImage(new Image(getClass().getResourceAsStream("pictures/mod2.gif"),CurrentMod.getFitWidth(),CurrentMod.getFitHeight(),false,false));
                    MessageText.setText("Ждем...");
                    Thread threadForOne = new Thread(() -> {
                        Synchronization(operator,logList,1);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                checkResults(logList);
                            }
                        });
                    });
                    threadForOne.start();
                }
                logsScene.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }



    private int Synchronization(Operator target, ListView<Log> log, int cnt)
    {
        nyan.setImage(new Image(getClass().getResourceAsStream("pictures/loading.gif"),nyan.getFitWidth(),nyan.getFitHeight(),false,false));
        Stop = false;
        Thread nyanThread = new Thread(() -> {
            double shift = 5;
            int way = 1;
            while (!Stop)
            {
                nyan.setLayoutX(nyan.getLayoutX()+shift);
                if (nyan.getLayoutX()>=270||nyan.getLayoutX()<=20)
                {
                    shift *= -1;
                    way *= -1;
                    nyan.setScaleX(way);
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });
        nyanThread.start();
        addons.setDisable(true);
        sync1.setDisable(true);
        sync2.setDisable(true);
        Executer mainExecuter = new Executer();
        Comparer mainComparer = new Comparer();
        ArrayList<Operator> actionable = new ArrayList<>();
        for (Operator iterator:operators)
            if (iterator.getAuthorized()||!iterator.getRequired()) actionable.add(iterator);
            actionable.remove(target);
        mainExecuter.setAccess(target);
        int operationResult = mainExecuter.executeRequest(target,"GET",0,"");
        if (operationResult==1)
        {
            ArrayList<String> images = target.getLinks();
            for (Operator current:actionable)
            {
                mainExecuter.setAccess(current);
                operationResult = mainExecuter.executeRequest(current,"GET",0,"");
                if (operationResult==1)
                {
                    ArrayList<String> comperableImages = current.getLinks();
                    for (String currentImage:images)
                    {
                        boolean areEqual = mainComparer.compare(currentImage,comperableImages);
                        if (!areEqual) {

                            operationResult = mainExecuter.executeRequest(current,"POST",0,currentImage);

                        if (operationResult==1)
                            log.getItems().add(new Log(cnt, currentImage,"Успешно",target.getName()+" -> "+current.getName()));
                        else log.getItems().add(new Log(cnt, currentImage,"Ошибка","Не удалось опубликовать изображения с сайта "+target.getName()+" на сайт "+current.getName()+";"+mainExecuter.getError()));
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else log.getItems().add(new Log(cnt, "","Ошибка","Не удалось получить изображения от "+current.getName()+";"+mainExecuter.getError()));
                cnt++;
            }
        }
        else log.getItems().add(new Log(cnt, "","Ошибка","Не удалось получить изображения от "+target.getName()+";"+mainExecuter.getError()));
        cnt++;
        log.getItems().add(new Log(cnt, "","Завершено",target.getName()+" - Синхронизация завершена"));
        cnt++;
        addons.setDisable(false);
        sync1.setDisable(false);
        sync2.setDisable(false);
        Stop = true;
        nyan.setImage(null);
        return cnt;
    }



    public void syncAll() throws IOException {
        Stage logsScene = App.setScene("Log");
        logsScene.hide();
        ListView<Log> logList = (ListView<Log>) logsScene.getScene().lookup("#ActionLog");
        logList.setCellFactory(param -> new ActionLogItems());
        logsScene.setOnCloseRequest(windowEvent -> {
            logList.getItems().clear();
        });
            CurrentMod.setImage(new Image(getClass().getResourceAsStream("pictures/mod2.gif"),CurrentMod.getFitWidth(),CurrentMod.getFitHeight(),false,false));
            MessageText.setText("Ждем...");
            Thread newThread = new Thread(() -> {
                int logIndex = 1;
                for (Operator a:operators)
                    if (a.getAuthorized()) {
                        logIndex = Synchronization(a,logList,logIndex);
                    }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        checkResults(logList);
                    }
                });
            });
            newThread.start();
        logsScene.show();
    }

    private void checkAuthorization()
    {
        boolean allAut = true;
        for (Operator iterator:operators)
            if (!iterator.getAuthorized()&&iterator.getRequired()) allAut = false;
        if (!allAut)
        {
            CurrentMod.setImage(new Image(getClass().getResourceAsStream("pictures/mod7.jpg"), CurrentMod.getFitWidth(), CurrentMod.getFitHeight(), false, false));
            MessageText.setText("А теперь не забудь авторизоваться");
        }
        else
        {
            CurrentMod.setImage(new Image(getClass().getResourceAsStream("pictures/mod5.jpg"), CurrentMod.getFitWidth(), CurrentMod.getFitHeight(), false, false));
            MessageText.setText("Теперь можно начинать");
        }
    }

    private void checkResults(ListView<Log> log)
    {
        CurrentMod.setImage(new Image(getClass().getResourceAsStream("pictures/mod3.jpg"),CurrentMod.getFitWidth(),CurrentMod.getFitHeight(),false,false));
        MessageText.setText("Успешно");
        for (Log a:log.getItems())
        {
            if (a.getResult().equals("Ошибка"))
            {
                CurrentMod.setImage(new Image(getClass().getResourceAsStream("pictures/mod4.png"),CurrentMod.getFitWidth(),CurrentMod.getFitHeight(),false,false));
                MessageText.setText("Ничто не идеально");
            }
        }
    }

}

