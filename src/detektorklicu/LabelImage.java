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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author zikmuto2
 */
public class LabelImage extends BufferedImage{

    protected LabelImage(ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied, Hashtable<?,?> properties) {
        super(cm,raster,isRasterPremultiplied,properties);
        labels = new int[getWidth()][getHeight()];
        IntStream str = IntStream.iterate(0, n->n+1).limit(getWidth());
        if(settings.parallel) str = str.parallel();
        str.forEach(x->{for(int y=0; y<getHeight();y++) setLabel(x, y, 1);});
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
    
    public static LabelImage fromBase64(String base64) throws ExceptionMessage {
        byte [] byteArray = DatatypeConverter.parseBase64Binary(base64);
        ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
        try {
            BufferedImage image = ImageIO.read(bais);
            return LabelImage.createLabelImage(ImageProcessing.gray2RGB(image));
        } catch (IOException ex) {
            Logger.getLogger(LabelImage.class.getName()).log(Level.SEVERE, null, ex);
            throw new ExceptionMessage("errorLabelImageFileReading",ex);
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
    
    public void resetDetection(){
        progress.setName("clearLabels");
        
        AtomicInteger col = new AtomicInteger(0);
        IntStream str = IntStream.iterate(0, n->n+1).limit(getWidth());
        if(settings.parallel) str = str.parallel();
        str.forEach(x->{
                for(int y=0; y<getHeight();y++)setLabel(x, y, 1);
                progress.setValue(col.addAndGet(1)*1000/getWidth());
            }
        );
        regions = null;
        separatedBackground = false;
        denotedRegions = false;
        backgroundImage = null;
        labelsImage = null;
    }
    
    public int getLabel(int x, int y){return labels[x][y];}
    public void setLabel(int x, int y, int v){labels[x][y]=v;}
    
    public void separateBackground(){
        progress.setName("separateBackground");
        ImageProcessing.floodFillSeparateBackground(this,progress);
        separatedBackground = true;
        regions = null;
    }
    
    public BufferedImage getBackgroundImage() {
        if(!separatedBackground) separateBackground();
        if(backgroundImage != null) return backgroundImage;
        progress.setName("backgroundImage");
        AtomicInteger col = new AtomicInteger(0);
        BufferedImage backgroundImage = getCopyBufferedImage();
        int colorRGB = settings.backgroundColor.getRGB();
        IntStream str = IntStream.iterate(0, n->n+1).limit(getWidth());
        if(settings.parallel) str = str.parallel();
        str.forEach(x->{
            for(int y=0; y<getHeight(); y++){
                if(getLabel(x, y) == 0)
                    backgroundImage.setRGB(x, y, colorRGB);
            }
            progress.setValue(col.addAndGet(1)*1000/getWidth());
        });
        this.backgroundImage = backgroundImage;
        
        return backgroundImage;
    }
    
    public BufferedImage getLabelsImage(){
        if(labelsImage == null) 
            labelsImage = getLabelsImage(settings.labelsColorPalette);
        return labelsImage;
    }
    
    public LabelImage getLabelsImage(List<Color> colors){
        if(!denotedRegions) denoteRegions();
        progress.setName("labelsImage");
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
        AtomicInteger col = new AtomicInteger(0);
        IntStream str = IntStream.iterate(0, n->n+1).limit(getWidth());
        if(settings.parallel) str = str.parallel();
        str.forEach(x->{
            for(int y=0; y<getHeight(); y++){
                li.labels[x][y] = labels[x][y];
                if(labels[x][y] == 0)
                    li.setRGB(x, y, 0);
                else 
                    li.setRGB(x, y, colors.get((labels[x][y]-1)%colors.size()).getRGB());
            }
            progress.setValue(col.addAndGet(1)*1000/getWidth());
        });
        
        return li;
    }
        
    public List<Integer> getLabelsList(){
        if(!denotedRegions) denoteRegions();
        progress.setName("getLabelsList");
        List<List<Integer>> inRow = new ArrayList<>(getWidth());
        
        IntStream.iterate(0, n->n+1).limit(getWidth())
                .forEachOrdered(i->inRow.add(new ArrayList<>()));
        
        AtomicInteger col = new AtomicInteger(0);
        IntStream str = IntStream.iterate(0, i->i+1).limit(getWidth());
        if(settings.parallel) str = str.parallel();
        str.forEach(x->{
            for(int y = 0; y < getHeight(); y++){
                int ii = getLabel(x, y);
                if(ii == 0) continue;
                Integer i = new Integer(ii);
                if(!inRow.get(x).contains(i)) inRow.get(x).add(i);
            }
            progress.setValue(col.addAndGet(1)*1000/getWidth());
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
        progress.setName("denoteRegions");
        AreaDetector detector = new AreaDetector(this);
        detector.addPropertyChangeListener("progress",(e)->{
            progress.setValue((int) e.getNewValue());
        });
        detector.detectRegions();
        denotedRegions = true;
    }
    
    public boolean hasRegions(){return regions!=null && !regions.isEmpty();}
    
    public void makeRegionsList(){
        if(!denotedRegions) denoteRegions();
        List<Integer> regionsLabels = getLabelsList();
        progress.setName("makeRegionsList");
        //regions = Collections.synchronizedList(new ArrayList<>(regionsLabels.size()));
        regions = new ArrayList<>(regionsLabels.size());
        for(int i = 0; i < regionsLabels.size(); i++) regions.add(null);
        
        AtomicInteger lbl = new AtomicInteger(0);
        ((settings.parallel)
                ? regionsLabels.parallelStream()
                : regionsLabels.stream())
        .forEach(label->{
            PointExtremes extremes = null;
            int area = 0;
            double xc = 0;
            double yc = 0;
            int minimalSurface = (settings.minimalRegionSurfaceFraction < 0)?0:
                    ((int) (((double)getWidth())*((double)getHeight())*settings.minimalRegionSurfaceFraction));
                    
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
            Point2D center = new Point2D.Double(xc/area, yc/area);
            if(extremes != null && (area > minimalSurface))
                regions.set(regionsLabels.indexOf(label),
                        new Region(this,label, area, extremes.getXmin(), 
                        extremes.getYmin(), extremes.getXmax(), 
                        extremes.getYmax(), center));
            progress.setValue(lbl.addAndGet(1)*1000/regionsLabels.size());
        });
        // TODO: remove small regions from the image
        while(regions.remove(null)); // remove empty positions in the list
        regions.sort(new Comparator<Region>() {
            @Override
            public int compare(Region o1, Region o2) {
                return o1.getLabel() - o2.getLabel();
            }
        });
    }
    
    public List<Region> getRegions() {
        if(!hasRegions()) makeRegionsList();
        return regions;
    }
    
    void insertRegion(Region region) {
        if(region == null) return;
        separatedBackground = true;
        denotedRegions = true;
        if(regions == null) regions = new ArrayList<>();
        regions.add(region);
    }
    
    public void drawRegions() {
        List<Region> regions = getRegions();
        ((settings.parallel)
                ? regions.parallelStream()
                : regions.stream())
        .forEach(region->{
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
    
    public void setWorker(QueuedWorker worker){
        progress.setWorker(worker);
    }
    
    void exportRegionsCSV(File file) throws ExceptionMessage {
        RegionsTableModel model = getRegionsTableModel();
        progress.setName("exportRegionsList");
        int rows = model.getRowCount();
        int cols = model.getColumnCount();
        try (Writer writer = new FileWriter(file)){
            for(int i = 0; i < rows; i++){
                for(int j = 0; j < cols; j++){
                    writer.write("\""+model.getValueAt(i, j).toString()+"\", ");
                }
                writer.write("\n");
                progress.setValue(1000*i/rows);
            }
        } catch (IOException ex) {
            Logger.getLogger(LabelImage.class.getName()).log(Level.SEVERE, null, ex);
            throw new ExceptionMessage("expotRegionsCSV", ex);
        } 
    }

    void exportImage(File file) throws ExceptionMessage {
        progress.setName("exportOriginalImage");
        try{
            ImageIO.write(this, "PNG", file);
        }catch(IOException e){
            throw new ExceptionMessage("exportOriginalImage", e);
        }
    }
    
    public String base64() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(this, "png", baos);
        return DatatypeConverter.printBase64Binary(baos.toByteArray());
    }
    
    protected final int [][] labels;
    protected List<Region> regions;
    protected boolean separatedBackground = false;
    protected boolean denotedRegions = false;
    protected BufferedImage backgroundImage;
    protected BufferedImage labelsImage;
    private Settings settings = Settings.getInstance();
    protected QueuedWorker.Progress progress = new QueuedWorker.Progress();

}
