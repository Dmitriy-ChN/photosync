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
        ImageView icon = new ImageView(new Image(operator.getImage(),40,40,false,false));
        if (op.getRequired()) {

            PixelReader pxl = icon.getImage().getPixelReader();
            WritableImage img2 = new WritableImage((int)icon.getImage().getWidth(),(int)icon.getImage().getHeight());
            PixelWriter pxl2 = img2.getPixelWriter();
            for (int i = 0; i < img2.getWidth(); i++)
                for (int j = 0; j< img2.getHeight(); j++)
                {
                    Color color = pxl.getColor(i,j);
                    pxl2.setColor(i,j,color.grayscale());
                }
            icon.setImage(img2);
        }
        this.setGraphic(icon);
    }

    public void setActive()
    {
        ImageView icon = new ImageView(new Image(operator.getImage(),40,40,false,false));
        this.setGraphic(icon);
    }
}
