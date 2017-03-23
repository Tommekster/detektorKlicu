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

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Acer
 */
public class Detection {
    private String originalFilename;
    private File saveFile;
    private boolean saved;
    private LabelImage image;
    private PropertyChangeSupport changes = new PropertyChangeSupport(this);
    private int progress;
    private String progressName = "saveDetection";
    
    private Detection(){
    }
    
    public static Detection newFromFile(File file) throws ExceptionMessage{
        if(file.getName().endsWith(".xml")) return newFromXML(file);
        return newFromImage(file);
    }
    public static Detection newFromImage(File file) throws ExceptionMessage {
        if(! file.isFile()) throw new ExceptionMessage("errorDetectionNotFile");
        Detection d = new Detection();
        d.originalFilename = file.getName();
        d.image = LabelImage.fromFile(file);
        d.saved = false;
        return d;
    }
    public static Detection newFromXML(File file) throws ExceptionMessage {
        Detection d = new Detection();
        d.saveFile = file;
        try(XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(file)))){
            Save saved=(Save)decoder.readObject();
            d.originalFilename = saved.originalFilename;
            d.image = LabelImage.fromBase64(saved.originalImage);
            d.saved = true;
            if(saved.regions.isEmpty()) return d;
            
            for(int x = 0; x < d.image.getWidth(); x++)
                for(int y = 0; y < d.image.getHeight(); y++)
                    d.image.setLabel(x, y, 0);
            for(Region r : saved.regions){
                detektorklicu.Region region = new detektorklicu.Region(d.image, r.labelId, r.surfaceArea, r.boundings, r.center);
                for(String label : r.shape.split(";\\ ")){
                    String [] nums = label.split(",");
                    int x = Integer.parseInt(nums[0]);
                    int y = Integer.parseInt(nums[1]);
                    int len = Integer.parseInt(nums[2]);
                    for(int k = r.boundings.y+y; k < r.boundings.y+y+len-1; k++)
                        d.image.setLabel(r.boundings.x+x, k, r.labelId);
                    System.out.println("["+x+","+y+","+len+"]");
                }
                d.image.insertRegion(region);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Detection.class.getName()).log(Level.SEVERE, null, ex);
            throw new ExceptionMessage("openDetection", ex);
        }
        return d;
    }
        
    public String getFilename(){
        return originalFilename;
    }
    
    public boolean isSaved(){
        return saved;
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
        return image.getBackgroundImage();
    }
    
    
    private void setProgress(int i){
        changes.firePropertyChange("progress", progress, progress = i);
    }
    private void setProgressName(String s){
        changes.firePropertyChange("progress", progressName, progressName = s);
    }
    public void addPropertyChangeListener(PropertyChangeListener listener){
        changes.addPropertyChangeListener(listener);
    }
    public void addPropertyChangeListener(String property, PropertyChangeListener listener){
        changes.addPropertyChangeListener(property, listener);
    }
    
    public boolean hasFile(){return saveFile != null;}
    public File getFile(){return saveFile;}
    public void save() throws ExceptionMessage{
        save(saveFile);
    }
    public void save(File file) throws ExceptionMessage{
        try(XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(file)))){
            encoder.writeObject(getSaveObj());
            saveFile = file;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Detection.class.getName()).log(Level.SEVERE, null, ex);
            throw new ExceptionMessage("saveDetection", ex);
        }
    }
    private Save getSaveObj() throws ExceptionMessage{
        Save s = new Save();
        s.originalFilename = getFilename();
        try {
            s.originalImage = getImage().base64();
        } catch (IOException ex) {
            Logger.getLogger(Detection.class.getName()).log(Level.SEVERE, null, ex);
            throw new ExceptionMessage("saveDetection", ex);
        }
        
        if(getImage().hasRegions()){
            s.regions = new ArrayList<>();
            for(detektorklicu.Region region : getImage().getRegions()){
                Region r = new Region();
                r.labelId = region.getLabel();
                r.surfaceArea = region.getArea();
                r.center = region.getCenter();
                
                int left = region.getBoundings().leftTop.x;
                int top = region.getBoundings().leftTop.y;
                int right = region.getBoundings().rightBottom.x;
                int bottom = region.getBoundings().rightBottom.y;
                int width = right - left + 1;
                int height = bottom - top + 1;
                r.boundings = new Rectangle(left, top, width, height);
                
                StringBuilder sb = new StringBuilder("");
                for(int y = 0; y < height; y++) {
                    int len = 0;
                    for(int x = width-1; x >= 0; x--) {
                        int lbl = getImage().getLabel(left+x, top+y);
                        if(lbl == r.labelId){
                            len++;
                        }
                        if((lbl != r.labelId || x == 0) && len > 0){
                            sb
                                .append((lbl != r.labelId)?(x+1):x).append(",")
                                .append(y).append(",")
                                .append(len).append("; ");
                            len = 0;
                        }
                    }
                }
                r.shape = sb.toString();
                
                r.hasEllipseInfo = region.hasEllipse();
                if(r.hasEllipseInfo){
                    r.halfAxisA = region.getHalfAxisA();
                    r.halfAxisB = region.getHalfAxisB();
                    r.orientation = region.getOrientation();
                }
                s.regions.add(r);
            }
        }
        return s;
    }
    public static class Save{
        public String originalFilename;
        public String originalImage;
        //public List<Label> labels;
        public List<Region> regions;
    }
    public static class Label{
        public int labelId;
        public int x;
        public int y;
        public int length;
    }
    public static class Region{
        public int labelId;
        public int surfaceArea;
        public Rectangle boundings;
        public Point2D center;
        public String shape;
        public boolean hasEllipseInfo;
        public double halfAxisA;
        public double halfAxisB;
        public double orientation;
    }
}
