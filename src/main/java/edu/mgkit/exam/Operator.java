package edu.mgkit.exam;

import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

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

    private Map<String,String> access_results = Map.of();
    private ArrayList<String> photo_links = new ArrayList<>();

    private Boolean isAuthorized = false;

    public void aut(Stage st) throws IOException {
        st.getScene().getRoot().setDisable(true);
        final WebView view = new WebView();
        final WebEngine engine = view.getEngine();
        engine.load(authorization);

        Stage st2 = App.setScene("choose_service");
        st2.setOnCloseRequest(windowEvent -> st.getScene().getRoot().setDisable(false));

        engine.locationProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.startsWith(redirect)){
                for (String a:access_params)
                {
                    final int beginIndex = newValue.indexOf(a) + a.length();
                    String s = newValue.substring(beginIndex,newValue.indexOf('&', beginIndex));
                   access_results.put(a,s);
                }
                st2.close();
                isAuthorized = true;
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
            case "GET": if (i>=get_photos.length) return new Request("NEXT",null,null,null);
            else return get_photos[i];
            case "POST": if (i>=post_photos.length) return new Request("END",null,null,null);
            else return post_photos[i];
        }
        return new Request("ERROR",null,null,null);
    }

    public String[] getAccess_params() {
        return access_params;
    }

    public Map<String, String> getAccess_results() {
        return access_results;
    }

    public Path[] getPath_to_url() {
        return path_to_url;
    }

    public void setLinks(ArrayList<String> links)
    {
        this.photo_links = links;
    }

    public Boolean getAuthorized() {
        if (is_authorization_required)
        return isAuthorized;
        return true;
    }

    public String getSuccess() {return result_success;}

    public Boolean hasPost() {return has_upload_server;}
}
