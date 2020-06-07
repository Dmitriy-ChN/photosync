package edu.mgkit.exam;

import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class ActionLogItems extends ListCell<Log> {

    private int index;

        @Override protected void updateItem(Log item, boolean empty) {
            super.updateItem(item, empty);
            if ( empty || item == null ) {
                setText(null);
                setGraphic(null);
            } else {
                Label lIndex = new Label(String.valueOf(index++));
                lIndex.setMinWidth(50);
                Label lImage = new Label(item.getImage());
                lImage.setMinWidth(150);
                lImage.setOnMouseClicked(eventHandler ->
                {
                    final WebView view = new WebView();
                    final WebEngine engine = view.getEngine();
                    engine.load(item.getImage());
                });

                Label lResult = new Label(item.getResult());
                lResult.setMinWidth(150);

                Label lMessage = new Label(item.getMessage());
                lMessage.setMinWidth(150);



                setGraphic(new HBox(lImage,
                        new Separator(Orientation.VERTICAL),
                        lResult,
                        new Separator(Orientation.VERTICAL),
                        lMessage));
            }
        }

        public void setIndex(int q)
        {
            this.index = q;
        }
}
