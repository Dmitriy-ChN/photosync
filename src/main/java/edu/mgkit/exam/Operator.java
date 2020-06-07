package edu.mgkit.exam;

import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;

public class Operator {
    private String name;
    private String image;
    private Boolean is_authorization_required;
    private Boolean has_upload_server;
    private String authorization;
    private String redirect;
    private String[] access_params;
    private Path[] path_to_url;
    private Request[] get_photos;
    private Request[] post_photos;
    private String result_success;


    public Operator(String name, String image, Boolean is_authorization_required, Boolean has_upload_server,
                    String authorization, String redirect, String[] access_params, Path[] path_to_url,
                    Request[] get_photos, Request[] post_photos, String result_success) {
        this.name = name;
        this.image = image;
        this.is_authorization_required = is_authorization_required;
        this.has_upload_server = has_upload_server;
        this.authorization = authorization;
        this.redirect = redirect;
        this.access_params = access_params;
        this.path_to_url = path_to_url;
        this.get_photos = get_photos;
        this.post_photos = post_photos;
        this.result_success = result_success;
    }

    private static HashMap<String,String> access_results = new HashMap<>();
    private static ArrayList<String> photo_links = new ArrayList<>();

    private static boolean isAuthorized = false;

    public void aut(OperatorButton butt) {
        Stage st = (Stage) butt.getScene().getWindow();
       st.getScene().getRoot().setDisable(true);
        final WebView view = new WebView();
        final WebEngine engine = view.getEngine();
        engine.load(authorization);
        Stage st2 = new Stage();
        st2.setScene(new Scene(view));
        st2.setOnCloseRequest(windowEvent -> st.getScene().getRoot().setDisable(false));
        st2.show();
        engine.locationProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.startsWith(redirect)){
                for (String a:access_params)
                {
                    final int beginIndex = newValue.indexOf(a) + a.length()+1;
                    String s = newValue.substring(beginIndex,newValue.indexOf('&', beginIndex));
                    System.out.println(s);
                    System.out.println(a);
                   access_results.put(a,s);
                }
                st.getScene().getRoot().setDisable(false);
                isAuthorized = true;
                butt.setActive();
                st2.close();
            }
        });
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public ArrayList<String> getLinks() {
        return photo_links;
    }

    public Request getMethod(String s, int i)
    {
        switch (s)
        {
            case "GET": if (i>=get_photos.length) return new Request("NEXT");
            else return get_photos[i];
            case "POST": if (i>=post_photos.length) return new Request("END");
            else return post_photos[i];
        }
        return new Request("ERROR");
    }

    public String[] getAccess_params() {
        return access_params;
    }

    public HashMap<String, String> getAccess_results() {
        return access_results;
    }

    public Path[] getPath_to_url() {
        return path_to_url;
    }

    public void setLinks(ArrayList<String> links)
    {
        photo_links = links;
    }

    public boolean getAuthorized() { return isAuthorized; }

    public boolean getRequired() { return is_authorization_required;}

    public String getSuccess() {return result_success;}

    public Boolean hasPost() {return has_upload_server;}
}
