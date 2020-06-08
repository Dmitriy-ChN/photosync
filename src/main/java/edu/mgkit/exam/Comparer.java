package edu.mgkit.exam;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class Comparer {
    public Boolean compare(String image, ArrayList<String> compared)
    {
        Image img = new Image(image, 100, 100, false, false);
        Image img2;
        PixelReader pxl1;
        PixelReader pxl2;
        int cnt;
        for (String a:compared)
        {
            cnt = 0;
            img2 = new Image(a, 100, 100, false, false);
            pxl1 = img.getPixelReader();
            pxl2 = img2.getPixelReader();
            for (int i = 0; i< 100; i++)
                for (int j = 0; j< 100; j++)
                {
                    double red1 = pxl1.getColor(i,j).getRed();
                    double green1 = pxl1.getColor(i,j).getGreen();
                    double blue1 = pxl1.getColor(i,j).getBlue();
                    double red2 = pxl2.getColor(i,j).getRed();
                    double green2 = pxl2.getColor(i,j).getGreen();
                    double blue2 = pxl2.getColor(i,j).getBlue();
                    if (Math.abs(red1-red2)<= 0.1 &&Math.abs(green1-green2)<=0.1&&Math.abs(blue1-blue2)<=0.1) cnt++;
                }
            if (cnt>=9000) return true;
        }
        return false;
    }
}
