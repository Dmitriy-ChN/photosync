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



private Operator find(ArrayList<Operator> op, String name)
{
    for (Operator a:op)
        if (a.getName().equals(name)) return a;
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
        String url = App.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        File folder = new File(url);
        url = folder.getParentFile().getPath()+"\\modules\\";

        folder = new File(url);
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
            boolean q = false;
            for (Object p:operators)
            {
                Operator c = (Operator)p;
                Operator d = a;
                if (c.getName().equals(d.getName())) {q = true;break;}
            }
            if (q) b.setSelected(true);
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
                    butt.setOnAction( eventHandler ->
                    {
                        if (butt.getOperator().getRequired()) butt.getOperator().aut(butt);
                        mainStage.setOnShowing(windowEvent -> {
                            checkAuthorization();
                        });
                        checkAuthorization();
                    });
                        System.out.println(butt.getOperator().getImage());
                        modulePane.getChildren().add(butt);
                }
            }
            operators = new_operators;
            items.clear();
            elements.setItems(items);
            st2.close();
            mainStage.getScene().getRoot().setDisable(false);
            operators.clear();
            for (Object q: modulePane.getChildren())
                operators.add(((OperatorButton)q).getOperator());
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
            Operator targ;
            RadioButton q = (RadioButton) group.getSelectedToggle();
            targ = find(operators,q.getText());
            final Operator targ2 = targ;
            elements.getItems().clear();
            st2.close();
            mainStage.getScene().getRoot().setDisable(false);
            try {
                Stage st3 = App.setScene("Log");
                st3.hide();
                ListView<Log> log = (ListView<Log>) st3.getScene().lookup("#ActionLog");
                log.setCellFactory(param -> new ActionLogItems());
                st3.setOnCloseRequest(windowEvent -> {
                    log.getItems().clear();
                });
                if (targ != null)
                {
                    CurrentMod.setImage(new Image(getClass().getResourceAsStream("pictures/mod2.gif"),CurrentMod.getFitWidth(),CurrentMod.getFitHeight(),false,false));
                    MessageText.setText("Ждем...");
                    Thread newThread = new Thread(() -> {
                        Synchronization(targ2,log,1);
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
                    });
                    newThread.start();
                }
                st3.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }



    private int Synchronization(Operator target, ListView<Log> log, int cnt)
    {
        nyan.setImage(new Image(getClass().getResourceAsStream("pictures/loading.gif"),nyan.getFitWidth(),nyan.getFitHeight(),false,false));
        Stop = false;
        Thread newThread = new Thread(() -> {
            double a = 5;
            int b = 1;
            while (!Stop)
            {
                nyan.setLayoutX(nyan.getLayoutX()+a);
                if (nyan.getLayoutX()>=270||nyan.getLayoutX()<=20)
                {
                    a *= -1;
                    b *= -1;
                    nyan.setScaleX(b);
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });
        newThread.start();
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
                        if (k==1) log.getItems().add(new Log(cnt, i,"Успешно",target.getName()+" -> "+a.getName()));
                        else log.getItems().add(new Log(cnt, i,"Ошибка","Не удалось опубликовать изображения с сайта "+target.getName()+" на сайт "+a.getName()+";"+exec.getError()));
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else log.getItems().add(new Log(cnt, "","Ошибка","Не удалось получить изображения от "+a.getName()+";"+exec.getError()));
                cnt++;
            }
        }
        else log.getItems().add(new Log(cnt, "","Ошибка","Не удалось получить изображения от "+target.getName()+";"+exec.getError()));
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
        Stage st2 = App.setScene("Log");
        st2.hide();
        ListView<Log> log = (ListView<Log>) st2.getScene().lookup("#ActionLog");
        log.setCellFactory(param -> new ActionLogItems());
        st2.setOnCloseRequest(windowEvent -> {
            log.getItems().clear();
        });
            CurrentMod.setImage(new Image(getClass().getResourceAsStream("pictures/mod2.gif"),CurrentMod.getFitWidth(),CurrentMod.getFitHeight(),false,false));
            MessageText.setText("Ждем...");
            Thread newThread = new Thread(() -> {
                int cnt = 1;
                for (Operator a:operators)
                    if (a.getAuthorized()) {
                        cnt = Synchronization(a,log,cnt);
                    }
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
            });
            newThread.start();
        st2.show();
    }

    private void checkAuthorization()
    {
        boolean allAut = true;
        for (Operator a:operators)
            if (!a.getAuthorized()&&a.getRequired()) allAut = false;
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

}

