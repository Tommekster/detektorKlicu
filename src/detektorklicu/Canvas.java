/*
 * The MIT License
 *
 * Copyright 2016 zikmuto2.
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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/** Canvas panel
 * nested class that cares about appropriate image painting
 * @author zikmuto2
 */
class Canvas extends JPanel{
    protected BufferedImage image = null;
    protected List<Polygon> polygons;
    protected Color polygonsColor;
    public void setImage(BufferedImage i){image=i;}
    public BufferedImage getImage() {return image;}
    public boolean isImageInside(){return image != null;}

    public Canvas(){
        super();
    }

    public Canvas(BufferedImage image){
        super();
        setImage(image);
    }
    
    public void displayRegions(){
        displayRegions(Color.blue);
    }
    
    public void displayRegions(Color c){
        polygonsColor = c;
        if(!(image instanceof LabelImage)) return;
        displayRegions(((LabelImage)image).getRegionsPolygons());
    }
    
    public void displayRegions(List<Polygon> polygons, Color c){
        polygonsColor = c;
        displayRegions(polygons);
    }
    
    public void displayRegions(Polygon polygon, Color c){
        List<Polygon> polygons = new ArrayList<>();
        polygons.add(polygon);
        polygonsColor = c;
        displayRegions(polygons);
    }
    
    public void displayRegions(List<Polygon> polygons){
        this.polygons = polygons;
    }
    
    public void hideRegions(){
        polygons = null;
    }
    
    public boolean showingRegions(){
        return polygons != null && !polygons.isEmpty();
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        if(image != null){
            Dimension dimensions = this.getSize();
            Dimension scalled = getScalledDimensions();
            
            int top = (dimensions.height - scalled.height)/2;
            int left = (dimensions.width - scalled.width)/2;
            
            g.drawImage(image, left, top, scalled.width, scalled.height, this);
            if(showingRegions()) {
                g.setColor(polygonsColor);
                polygons.stream().forEach(pol->{
                    g.drawPolygon(rescaleAndMovePolygon(pol, top, left));
                });
            }
        }
        if(image instanceof ImageComponent){
            ImageComponent im = (ImageComponent)image;
            setToolTipText("S:"+im.getSurface());
        }
    }

    private Dimension getScalledDimensions() {
        Dimension d = this.getSize();
        float rs = (float)d.width/d.height;
        int w = image.getWidth();
        int h = image.getHeight();
        float ri = (float)w/h;
        if(rs > ri) { // width is smaller
            w = w*d.height/h;
            h = d.height;
        }else{ // height is smaller
            h = h*d.width/w;
            w = d.width;
        }
        Dimension scalled = new Dimension(w,h);
        return scalled;
    }
    
    private Polygon rescaleAndMovePolygon(Polygon pol, int top, int left){
        Dimension o = getScalledDimensions();
        double frac = (double)o.width/image.getWidth();
        
        Polygon spol = new Polygon();
        for(int i = 0; i < pol.npoints; i++){
            double x = left+frac*pol.xpoints[i];
            double y = top+frac*pol.ypoints[i];
            spol.addPoint((int)x, (int)y);
        }
        return spol;
    }
}
