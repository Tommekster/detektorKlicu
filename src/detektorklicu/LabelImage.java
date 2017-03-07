/*
 * The MIT License
 *
 * Copyright 2017 zikmuto2.
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
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;

/**
 *
 * @author zikmuto2
 */
public class LabelImage extends BufferedImage{

    protected LabelImage(ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied, Hashtable<?,?> properties) {
        super(cm,raster,isRasterPremultiplied,properties);
        labels = new int[getWidth()][getHeight()];
        IntStream.iterate(0, n->n+1).limit(getWidth()).parallel()
                .forEach(x->{for(int y=0; y<getHeight();y++)setLabel(x, y, 1);});
    }
    
    private LabelImage(int width, int height, int type, IndexColorModel cm){
        super(width, height, type, cm);
        labels = new int[getWidth()][getHeight()];
    }
    
    private LabelImage(int width, int height, int type, IndexColorModel cm, int [][] labels){
        super(width, height, type, cm);
        this.labels = labels.clone();
    }
    
    public static LabelImage createLabelImage(BufferedImage image){
        ColorModel model = image.getColorModel();
        WritableRaster raster = image.copyData(null);
        LabelImage clone = new LabelImage(model, raster, model.isAlphaPremultiplied(), null);

        return clone;
    }
    
    private BufferedImage getCopyBufferedImage() {
        ColorModel model = getColorModel();
        WritableRaster raster = copyData(null);
        return new BufferedImage(model, raster, model.isAlphaPremultiplied(), null);
    }
    
    public static LabelImage fromFile(File file) throws ExceptionMessage{
        try{
            BufferedImage image = ImageIO.read(file);
            return LabelImage.createLabelImage(ImageProcessing.gray2RGB(image));
        }catch(IOException ex) {
            ExceptionMessage e = new ExceptionMessage("errorLabelImageFileReading");
            e.setStackTrace(ex.getStackTrace());
            throw e;
        }
    }
    
    public static List<Color> getPallete(){
        List<Color> colors = new LinkedList<>();
        colors.add(Color.white);
        colors.add(Color.cyan);
        //colors.add(Color.darkGray);
        colors.add(Color.gray);
        colors.add(Color.green);
        colors.add(Color.yellow);
        colors.add(Color.lightGray);
        colors.add(Color.magenta);
        colors.add(Color.orange);
        colors.add(Color.pink);
        colors.add(Color.red);
        colors.add(Color.YELLOW);
        
        return colors;
    }
    
    public int getLabel(int x, int y){return labels[x][y];}
    public void setLabel(int x, int y, int v){labels[x][y]=v;}
    
    public void separateBackground(){
        ImageProcessing.floodFillSeparateBackground(this);
        separatedBackground = true;
        regions = null;
    }
    
    public BufferedImage getBackgroundImage(Color color) {
        if(!separatedBackground) separateBackground();
        if(backgroundImage != null) return backgroundImage;
        BufferedImage backgroundImage = getCopyBufferedImage();
        int colorRGB = color.getRGB();
        IntStream.iterate(0, n->n+1).limit(getWidth()).parallel().forEach(x->{
            for(int y=0; y<getHeight(); y++){
                if(getLabel(x, y) == 0)
                    backgroundImage.setRGB(x, y, colorRGB);
            }
        });
        
        return backgroundImage;
    }
    
    public BufferedImage getLabelsImage(){
        if(labelsImage == null) 
            labelsImage = getLabelsImage(LabelImage.getPallete());
        return labelsImage;
    }
    
    public LabelImage getLabelsImage(List<Color> colors){
        if(!denotedRegions) denoteRegions();
        // prepare IndexColorModel
        byte [] reds = new byte [colors.size()+1];
        byte [] greens = new byte [colors.size()+1];
        byte [] blues = new byte[colors.size()+1];
        reds[0] = greens[0] = blues[0] = (byte)0; // black at first position
        for(int j = 0; j < colors.size(); j++){
            reds[j+1] = (byte)colors.get(j).getRed();
            greens[j+1] = (byte)colors.get(j).getGreen();
            blues[j+1] = (byte)colors.get(j).getBlue();
        }
        
        IndexColorModel cm = new IndexColorModel(4, reds.length, reds, greens, blues);
        
        // prepare output image
        LabelImage li = new LabelImage(getWidth(), getHeight(), 
                BufferedImage.TYPE_BYTE_INDEXED, cm);
        
        // draw labels into image
        IntStream.iterate(0, n->n+1).limit(getWidth()).parallel().forEach(x->{
            for(int y=0; y<getHeight(); y++){
                li.labels[x][y] = labels[x][y];
                if(labels[x][y] == 0)
                    li.setRGB(x, y, 0);
                else 
                    li.setRGB(x, y, colors.get((labels[x][y]-1)%colors.size()).getRGB());
            }
        });
        
        return li;
    }
        
    public List<Integer> getLabelsList(){
        if(!denotedRegions) denoteRegions();
        List<List<Integer>> inRow = new ArrayList<>(getWidth());
        
        IntStream.iterate(0, n->n+1).limit(getWidth())
                .forEachOrdered(i->inRow.add(new ArrayList<>()));
        
        IntStream.iterate(0, i->i+1).limit(getWidth()).parallel().forEach(x->{
            for(int y = 0; y < getHeight(); y++){
                int ii = getLabel(x, y);
                if(ii == 0) continue;
                Integer i = new Integer(ii);
                if(!inRow.get(x).contains(i)) inRow.get(x).add(i);
            }
        });
        
        List<Integer> uniqueLabels = new ArrayList<>();
        inRow.forEach((row) -> {
            row.stream().filter((i) -> (!uniqueLabels.contains(i))).forEachOrdered((i) -> {
                uniqueLabels.add(i);
            });
        });
        
        return uniqueLabels;
    }
    
    public void denoteRegions(){
        if(!separatedBackground) separateBackground();
        AreaDetector.detectRegions(this);
        denotedRegions = true;
    }
    
    public boolean hasRegions(){return regions!=null && !regions.isEmpty();}
    
    public void makeRegionsList(){
        if(!denotedRegions) denoteRegions();
        List<Integer> regionsLabels = getLabelsList();
        regions = Collections.synchronizedList(new ArrayList<>(regionsLabels.size()));
        //for(int i = 0; i < regionsLabels.size(); i++) regions.add(null);
        boolean disable1pixel = true;
        
        regionsLabels.stream().parallel().forEach(label->{
            PointExtremes extremes = null;
            int area = 0;
            int xc = 0;
            int yc = 0;
                    
            for(int x = 0; x<getWidth(); x++){
                for(int y = 0; y<getHeight(); y++){
                    if(getLabel(x, y) == label){
                        Point p = new Point(x,y);
                        xc += x; yc += y; // count centroid of the region
                        area++; // surface of the region
                        if(extremes == null) 
                            extremes = new PointExtremes(p);
                        extremes.checkExtremes(p);
                    }
                }
            }
            Point2D center = new Point2D.Double((double)xc/area, (double)yc/area);
            if(extremes != null && (area > 1 || !disable1pixel))
                regions.add(new Region(this,label, area, extremes.getXmin(), 
                        extremes.getYmin(), extremes.getXmax(), 
                        extremes.getYmax(), center));
        });
    }
    
    public List<Region> getRegions() {
        if(!hasRegions()) makeRegionsList();
        return regions;
    }
    
    public void drawRegions() {
        List<Region> regions = getRegions();
        regions.parallelStream().forEach(region->{
            region.drawBoundingRectangle(Color.blue);
        });
    }
    
    public List<Polygon> getRegionsPolygons(){
        List<Polygon> polygons = new ArrayList<>();
        getRegions().stream().forEach(r->{
            polygons.add(r.getBoundingRectangle());
        });
        return polygons;
    }
    
    public RegionsTableModel getRegionsTableModel() {
        return new RegionsTableModel(getRegions());
    }
    
    protected final int [][] labels;
    protected List<Region> regions = null;
    protected boolean separatedBackground = false;
    protected boolean denotedRegions = false;
    protected BufferedImage backgroundImage;
    protected BufferedImage labelsImage;
}
