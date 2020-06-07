package edu.mgkit.exam;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class OperatorButton extends Button {

    private Operator operator;
    public Operator getOperator()
    {
        return this.operator;
    }

    public void setOperator(Operator op)
    {
        this.operator = op;
        ImageView icon = new ImageView(new Image(operator.getImage()));
        icon.setFitWidth(40);
        icon.setFitHeight(40);
        this.setGraphic(icon);
    }
}
