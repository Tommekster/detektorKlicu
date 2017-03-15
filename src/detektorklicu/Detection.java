/*
 * The MIT License
 *
 * Copyright 2017 Acer.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package detektorklicu;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author Acer
 */
public class Detection {
    private String originalFilename;
    private LabelImage image;
    
    private Detection(){
    }
    
    public static Detection newFromFile(File file) throws ExceptionMessage{
        return newFromImage(file);
    }
    public static Detection newFromImage(File file) throws ExceptionMessage {
        if(! file.isFile()) throw new ExceptionMessage("errorDetectionNotFile");
        Detection d = new Detection();
        d.originalFilename = file.getName();
        d.image = LabelImage.fromFile(file);
        
        return d;
    }
    
    public String getFilename(){
        return originalFilename;
    }
    
    public BufferedImage getOriginal() {
        return (BufferedImage) image;
    }
    
    public LabelImage getImage() {
        return image;
    }
    
    public void startDetection(){
        image.separateBackground();
        image.denoteRegions();
    }
    
    public void resetDetection(){
        image.resetDetection();
    }
    
    public BufferedImage getLabelsImage() {
        return image.getLabelsImage();
    }
    
    public BufferedImage getBackgroundImage() {
        return image.getBackgroundImage(Color.red);
    }
    
    /*private class DatectionSave implements Serializable {
        
    }*/
}
