package edu.mgkit.exam;

import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class ActionLogItems extends ListCell<Log> {

        @Override protected void updateItem(Log item, boolean empty) {
            super.updateItem(item, empty);
            if ( empty || item == null ) {
                setText(null);
                setGraphic(null);
            } else {
                Label lIndex = new Label(item.getIndex());
                lIndex.setMaxWidth(50);
                lIndex.setMinWidth(50);
                Label lImage = new Label(item.getImage());
                lImage.setMaxWidth(150);
                lImage.setMinWidth(150);
                lImage.setOnMouseClicked(eventHandler ->
                {
                    final WebView view = new WebView();
                    final WebEngine engine = view.getEngine();
                    engine.load(item.getImage());
                });

                Label lResult = new Label(item.getResult());
                lResult.setMaxWidth(150);
                lResult.setMinWidth(150);

                Label lMessage = new Label(item.getMessage());
                lMessage.setMinWidth(150);



                setGraphic(new HBox(lIndex,
                        new Separator(Orientation.VERTICAL),
                        lImage,
                        new Separator(Orientation.VERTICAL),
                        lResult,
                        new Separator(Orientation.VERTICAL),
                        lMessage));
            }
        }

}
