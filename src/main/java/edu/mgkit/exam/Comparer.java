package edu.mgkit.exam;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

import java.util.ArrayList;

public class Comparer {
    public Boolean compare(String image, ArrayList<String> compared)
    {
        Image source = new Image(image, 100, 100, false, false);
        Image target;
        PixelReader sourceReader;
        PixelReader targetReader;
        int equalPixels;
        for (String a:compared)
        {
            equalPixels = 0;
            target = new Image(a, 100, 100, false, false);
            sourceReader = source.getPixelReader();
            targetReader = target.getPixelReader();
            for (int i = 0; i< 100; i++)
                for (int j = 0; j< 100; j++)
                {
                    double redSource = sourceReader.getColor(i,j).getRed();
                    double greenSource = sourceReader.getColor(i,j).getGreen();
                    double blueSource = sourceReader.getColor(i,j).getBlue();
                    double redTarget = targetReader.getColor(i,j).getRed();
                    double greenTarget = targetReader.getColor(i,j).getGreen();
                    double blueTarget = targetReader.getColor(i,j).getBlue();
                    if (Math.abs(redSource-redTarget)<= 0.1 &&Math.abs(greenSource-greenTarget)<=0.1&&Math.abs(blueSource-blueTarget)<=0.1) equalPixels++;
                }
            if (equalPixels>=9000) return true;
        }
        return false;
    }
}
