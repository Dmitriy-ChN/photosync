package edu.mgkit.exam;

import javafx.scene.control.Button;
import javafx.scene.image.*;
import javafx.scene.paint.Color;

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
        if (op.getAuthorized())
        this.setGraphic(icon);
        else
        {
            PixelReader pxl = icon.getImage().getPixelReader();
            WritableImage img2 = (WritableImage) icon.getImage();
            PixelWriter pxl2 = img2.getPixelWriter();
            for (int i = 0; i < img2.getWidth(); i++)
                for (int j = 0; j< img2.getHeight(); j++)
                {
                    Color color = pxl.getColor(i,j);
                    pxl2.setColor(i,j,color.grayscale());
                }
            icon.setImage(img2);
                this.setGraphic(icon);
        }
    }
}
