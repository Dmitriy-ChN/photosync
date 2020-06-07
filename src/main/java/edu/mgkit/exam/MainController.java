package edu.mgkit.exam;

import com.google.gson.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
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
    @FXML private ImageView img1;

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
        File folder = new File("src/main/resources/edu/mgkit/exam/modules");
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
        for (Operator a:operators)
        {
            if (a.getAuthorized())
            {
                RadioButton b = new RadioButton(a.getName());
                ObservableList items = elements.getItems();
                items.add(b);
                elements.setItems(items);
            }

        }
        activate.setOnAction(actionEvent -> {
            Operator targ = null;
            for (Object q:elements.getItems())
            {
                RadioButton q2 = (RadioButton)q;
                if (q2.isPressed()) targ = find(operators,q2.getText());
            }
            elements.getItems().clear();
            st2.close();
            mainStage.getScene().getRoot().setDisable(false);
            try {
                Stage st3 = App.setScene("Log");
                ListView log = (ListView) st3.getScene().lookup("#ActionLog");
                log.setCellFactory(param -> new ActionLogItems());
                if (targ != null) Synchronization(targ,log);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }



    public void Synchronization(Operator target, ListView<Log> log)
    {
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
                else log.getItems().add(new Log("","Ошибка","Не удалось получить изображения от "+a.getName()));
            }
        }
        else log.getItems().add(new Log("","Ошибка","Не удалось получить изображения от "+target.getName()));

        addons.setDisable(false);
        sync1.setDisable(false);
        sync2.setDisable(false);
    }



    public void syncAll() throws IOException {
        Stage st2 = App.setScene("Log");
        ListView log = (ListView) st2.getScene().lookup("#ActionLog");
        log.setCellFactory(param -> new ActionLogItems());
    for (Operator a:operators)
        if (a.getAuthorized()) Synchronization(a,log);
    }
}


/*

public void test() {

        CloseableHttpClient httpclient = HttpClients.createDefault();
        String json = " ";
        StringBuilder url = new StringBuilder("https://api.vk.com/method/photos.getAll?extended=1&count=3&photo_sizes=1no_service_albums=0&need_hidden=0&pskip_hidden=1&v=5.107&access_token="+token);
        HttpUriRequest httpGet = new HttpGet(url.toString());
        try (
                CloseableHttpResponse response = httpclient.execute(httpGet)
        ) {
            HttpEntity entity = response.getEntity();
            json = EntityUtils.toString(entity);
            System.out.println(json);
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            String photo = obj.get("response").getAsJsonObject().get("items").getAsJsonArray().get(0).getAsJsonObject().get("sizes").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString();
            url = new StringBuilder("https://api.vk.com/method/photos.getWallUploadServer?v=5.107&access_token="+token);
            httpGet = new HttpGet(url.toString());
            CloseableHttpResponse response2 = httpclient.execute(httpGet);
            entity = response2.getEntity();
            json = EntityUtils.toString(entity);
            System.out.println(json);
            obj = JsonParser.parseString(json).getAsJsonObject();
            String path = obj.get("response").getAsJsonObject().get("upload_url").getAsString();

            String tDir = System.getProperty("java.io.tmpdir");
            String path2 = tDir + "tmp" + ".jpg";
            File file = new File(path2);
            FileUtils.copyURLToFile(new URL(photo), file);
            HttpPost httpPost = new HttpPost(path);
            MultipartEntityBuilder mpeBuilder = MultipartEntityBuilder.create();
            mpeBuilder.addBinaryBody("photo",file);
            httpPost.setEntity(mpeBuilder.build());
            CloseableHttpResponse response3 = httpclient.execute(httpPost);
            entity = response3.getEntity();
            json = EntityUtils.toString(entity);
            System.out.println(json);
            obj = JsonParser.parseString(json).getAsJsonObject();
            photo = obj.get("photo").getAsString();
            System.out.println(photo);
            //JsonArray arr = JsonParser.parseString(photo).getAsJsonArray();
            //photo = arr.get(0).getAsJsonObject().get("photo").getAsString();
            String server = obj.get("server").getAsString();
            String hash = obj.get("hash").getAsString();
            System.out.println(photo);
            System.out.println(server);
            System.out.println(hash);
            url = new StringBuilder("https://api.vk.com/method/photos.saveWallPhoto?v=5.107&access_token="+token+"&photo="+ URLEncoder.encode(photo, StandardCharsets.UTF_8)+"&server="+server+"&hash="+hash);
            httpGet = new HttpGet(url.toString());
            CloseableHttpResponse response4 = httpclient.execute(httpGet);
            entity = response4.getEntity();
            json = EntityUtils.toString(entity);
            System.out.println(json);
            obj = JsonParser.parseString(json).getAsJsonObject();
            photo = "photo"+obj.get("response").getAsJsonArray().get(0).getAsJsonObject().get("owner_id")+"_"+obj.get("response").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsString();
            url = new StringBuilder("https://api.vk.com/method/wall.post?v=5.107&access_token="+token+"&attachments="+photo);
            httpGet = new HttpGet(url.toString());
            CloseableHttpResponse response5 = httpclient.execute(httpGet);
            entity = response5.getEntity();
            json = EntityUtils.toString(entity);
            System.out.println(json);
            obj = JsonParser.parseString(json).getAsJsonObject();
            photo = obj.get("response").getAsJsonObject().get("post_id").getAsString();
            url = new StringBuilder("https://api.vk.com/method/wall.delete?v=5.107&access_token="+token+"&post_id="+photo);
            httpGet = new HttpGet(url.toString());
            CloseableHttpResponse response6 = httpclient.execute(httpGet);
            entity = response6.getEntity();
            json = EntityUtils.toString(entity);
            System.out.println(json);
            response.close();
            response2.close();
            response3.close();
            response4.close();
            response5.close();
            httpclient.close();
            String output = String.format("%s = %d", "joe", 35);
            System.out.println(output);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
 */
