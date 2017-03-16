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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JPanel;

/** Canvas panel
 * nested class that cares about appropriate image painting
 * @author zikmuto2
 */
class Canvas extends JPanel{
    protected BufferedImage image = null;
    protected List<Shape> shapes = new LinkedList<>();
    protected HashMap<Shape, Color> shapesColor = new HashMap<>();
    protected HashMap<Shape, Stroke> shapesStroke = new HashMap<>();
    protected Color defaultColor = Color.BLUE;
    protected Stroke defaultStroke = new BasicStroke(1);
    private Rectangle imageRectangle;
    
    public void setImage(BufferedImage i){
        image=i;
        imageRectangle = new Rectangle(0, 0, i.getWidth(), i.getHeight());
    }
    public BufferedImage getImage() {return image;}
    public boolean isImageInside(){return image != null;}

    public Canvas(){
        super();
    }

    public Canvas(BufferedImage image){
        super();
        setImage(image);
    }
    
    public void setDefaultColor(Color c){
        defaultColor = c;
    }
    
    public void displayRegions(Polygon polygon, Color color, Stroke stroke){
        shapes.clear();
        shapes.add(polygon);
        if(color == null) shapesColor.remove(polygon); else shapesColor.put(polygon, color);
        if(stroke == null) shapesStroke.remove(polygon); else shapesStroke.put(polygon, stroke);
    }
    
    public void displayRegions(List<Polygon> polygons){
        shapes.clear();
        polygons.forEach(polygon->shapes.add(polygon));
    }
    
    public void hideShapes(){
        shapes.clear();
        shapesColor.clear();
        shapesStroke.clear();
    }
    
    public boolean showingRegions(){
        return shapes != null && !shapes.isEmpty();
    }
    
    public Rectangle getImageRectangle(){
        return imageRectangle;
    }

    @Override
    public void paintComponent(Graphics gr){
        super.paintComponent(gr);
        if(image != null){
            Graphics2D g = (Graphics2D) gr;
            Dimension dimensions = this.getSize();
            imageRectangle.setSize(getScalledDimensions());
            imageRectangle.setLocation((dimensions.width - imageRectangle.width)/2, 
                    (dimensions.height - imageRectangle.height)/2);
            AffineTransform xform = new AffineTransform();
            double scale = (double)imageRectangle.width/image.getWidth();
            xform.setToTranslation(imageRectangle.x, imageRectangle.y);
            xform.scale(scale, scale);
            
            //g.drawImage(image, imageRectangle.x, imageRectangle.y, 
            //        imageRectangle.width, imageRectangle.height, this);
            g.drawImage(image, xform, this);
            if(showingRegions()) {
                shapes.stream().forEach(shape->{
                    if(shapesColor != null && shapesColor.containsKey(shape)){
                        g.setColor(shapesColor.get(shape));
                    } else g.setColor(defaultColor);
                    if(shapesStroke != null && shapesStroke.containsKey(shape)){
                        g.setStroke(shapesStroke.get(shape));
                    } else g.setStroke(defaultStroke);
                    /*if(shape instanceof Polygon)
                        g.drawPolygon(rescaleAndMovePolygon((Polygon)shape));
                    else */
                        g.draw(xform.createTransformedShape(shape));
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
    
    private Polygon rescaleAndMovePolygon(Polygon polygon){
        double scale = (double)imageRectangle.width/image.getWidth();
        
        Polygon scalledPolygon = new Polygon();
        for(int i = 0; i < polygon.npoints; i++){
            double x = imageRectangle.x+scale*polygon.xpoints[i];
            double y = imageRectangle.y+scale*polygon.ypoints[i];
            scalledPolygon.addPoint((int)x, (int)y);
        }
        return scalledPolygon;
    }
}
