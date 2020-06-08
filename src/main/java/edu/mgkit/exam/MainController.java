package edu.mgkit.exam;

import com.google.gson.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
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
    @FXML private ImageView img1;
    @FXML
    private Label test;

private ArrayList<Operator> operators = new ArrayList<>();
private ArrayList<Operator> new_operators = new ArrayList<>();



private Operator find(ArrayList<Operator> op, String name)
{
    for (Operator a:op)
        if (a.getName().equals(name)) return a;
        return null;
}

    public void openMenu() throws IOException {
        Stage mainStage = (Stage) modulePane.getScene().getWindow();
        Stage st2 = App.setScene("choose_scene");
        mainStage.getScene().getRoot().setDisable(true);
        st2.setOnCloseRequest(windowEvent -> mainStage.getScene().getRoot().setDisable(false));
        ListView elements = (ListView) st2.getScene().lookup("#elements");
        Button activate = (Button) st2.getScene().lookup("#activate");
        String url = App.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        File folder = new File(url);
        url = folder.getParentFile().getPath()+"\\modules\\";

        folder = new File(url);
        String k = String.valueOf(folder.isDirectory());
        test.setText(url+" "+k);
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
            Operator op = gson.fromJson(json.toString(),Operator.class);
            System.out.println(op.getName());
            System.out.println(op.getImage());
            System.out.println(op.getAuthorized());
            System.out.println(op.getMethod("GET",0).link);
            System.out.println(op.getMethod("POST",0).link);
            new_operators.add(op);
        }
        for (Operator a:new_operators)
        {
            CheckBox b = new CheckBox(a.getName());
            if (operators.contains(a)) b.setSelected(true);
            ObservableList items = elements.getItems();
            items.add(b);
            elements.setItems(items);
        }

        activate.setOnAction(actionEvent12 -> {
            modulePane.getChildren().clear();
            ObservableList items = elements.getItems();
            for (Object q:items)
            {
                CheckBox q2 = (CheckBox)q;
                if (q2.isSelected()) {
                    String name = q2.getText();
                        OperatorButton butt = new OperatorButton();
                        butt.setOperator(find(new_operators,name));
                        img1.setImage(new Image(butt.getOperator().getImage()));
                        System.out.println(butt.getOperator().getImage());
                        modulePane.getChildren().add(butt);
                }
            }
            operators = new_operators;
            elements.getItems().clear();
            st2.close();
            mainStage.getScene().getRoot().setDisable(false);
            new_operators.clear();
            operators.clear();
            for (Object q: modulePane.getChildren())
                operators.add(((OperatorButton)q).getOperator());
        });
    }

    public void syncOne() throws IOException {
        Stage mainStage = (Stage) modulePane.getScene().getWindow();
        Stage st2 = App.setScene("choose_service");
        mainStage.getScene().getRoot().setDisable(true);
        st2.setOnCloseRequest(windowEvent -> mainStage.getScene().getRoot().setDisable(false));
        ListView elements = (ListView) st2.getScene().lookup("#elements");
        Button activate = (Button) st2.getScene().lookup("#activate");
        ToggleGroup group = new ToggleGroup();
        for (Operator a:operators)
        {
            if (a.getAuthorized())
            {
                RadioButton b = new RadioButton(a.getName());
                b.setToggleGroup(group);
                ObservableList items = elements.getItems();
                items.add(b);
                elements.setItems(items);
            }


        }
        activate.setOnAction(actionEvent -> {
            Operator targ = null;
            RadioButton q = (RadioButton) group.getSelectedToggle();
            targ = find(operators,q.getText());
            elements.getItems().clear();
            st2.close();
            mainStage.getScene().getRoot().setDisable(false);
            try {
                Stage st3 = App.setScene("Log");
                ListView log = (ListView) st3.getScene().lookup("#ActionLog");
                log.setCellFactory(param -> new ActionLogItems());
                st3.setOnCloseRequest(windowEvent -> {
                    log.getItems().clear();
                });
                if (targ != null) Synchronization(targ,log);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }



    public void Synchronization(Operator target, ListView<Log> log)
    {
        System.out.println("start");
        addons.setDisable(true);
        sync1.setDisable(true);
        sync2.setDisable(true);
        Executer exec = new Executer();
        Comparer comp = new Comparer();
        ArrayList<Operator> actionable = new ArrayList<>();
        for (Operator a:operators)
            if (a.getAuthorized()||!a.getRequired()) actionable.add(a);
            actionable.remove(target);
        exec.setAccess(target);
        int k = exec.executeRequest(target,"GET",0,"");
        if (k==1)
        {
            ArrayList<String> images = target.getLinks();
            for (Operator a:actionable)
            {
                exec.setAccess(a);
                k = exec.executeRequest(a,"GET",0,"");
                if (k==1)
                {
                    ArrayList<String> images2 = a.getLinks();
                    for (String i:images)
                    {
                        boolean b = comp.compare(i,images2);
                        if (!b) {
                            k = exec.executeRequest(a,"POST",0,i);
                        if (k==1) log.getItems().add(new Log(i,"Успешно",target.getName()+" -> "+a.getName()));
                        else log.getItems().add(new Log(i,"Ошибка","Не удалось опубликовать изображения с сайта "+target.getName()+" на сайт "+a.getName()));
                        }
                    }
                }
                else log.getItems().add(new Log("2","Ошибка","Не удалось получить изображения от "+a.getName()));
            }
        }
        else log.getItems().add(new Log("1","Ошибка","Не удалось получить изображения от "+target.getName()));

        log.getItems().add(new Log("","Завершено",target.getName()+" - Синхронизация завершена"));

        addons.setDisable(false);
        sync1.setDisable(false);
        sync2.setDisable(false);
    }



    public void syncAll() throws IOException {
        Stage st2 = App.setScene("Log");

        ListView log = (ListView) st2.getScene().lookup("#ActionLog");
        log.setCellFactory(param -> new ActionLogItems());
        st2.setOnCloseRequest(windowEvent -> {
            log.getItems().clear();
        });
    for (Operator a:operators)
        if (a.getAuthorized()) Synchronization(a,log);
    }




    public void test() {

    }

}

