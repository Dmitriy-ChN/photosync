module edu.mgkit.exam {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires com.google.gson;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpmime;
    requires org.apache.commons.io;
    requires org.apache.commons.codec;

    opens edu.mgkit.exam to javafx.fxml,com.google.gson;
    exports edu.mgkit.exam;
}