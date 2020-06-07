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
        this.setGraphic(new ImageView(new Image(operator.getImage())));
        this.setWidth(100);
        this.setHeight(100);
    }
}
