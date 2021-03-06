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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
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
    private Settings settings = Settings.getInstance();
    protected HashMap<Shape, Color> shapesColor = new HashMap<>();
    protected HashMap<Shape, Stroke> shapesStroke = new HashMap<>();
    protected Color defaultColor = settings.defaultShapeColor;
    protected Stroke defaultStroke = settings.defaultShapeStroke;
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
        addShape(polygon, color, stroke);
    }
    
    public void addShape(Shape shape, Color color, Stroke stroke){
        shapes.add(shape);
        if(color == null) shapesColor.remove(shape); else shapesColor.put(shape, color);
        if(stroke == null) shapesStroke.remove(shape); else shapesStroke.put(shape, stroke);
    }
    
    public void setShapeProperties(Shape shape, Color color, Stroke stroke){
        if(color == null) shapesColor.remove(shape); else shapesColor.put(shape, color);
        if(stroke == null) shapesStroke.remove(shape); else shapesStroke.put(shape, stroke);
    }
    
    public void removeShape(Shape shape){
        shapes.remove(shape);
        shapesColor.remove(shape);
        shapesStroke.remove(shape);
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
    
    public Point getOriginalImageCoords(Point point){
        if(imageRectangle.x < point.x && point.x < (imageRectangle.x + imageRectangle.width) 
                && imageRectangle.y < point.y && point.y < (imageRectangle.y + imageRectangle.height)){
            double scale = (double)imageRectangle.width/image.getWidth();
            double x = (point.x - imageRectangle.x) / scale;
            double y = (point.y - imageRectangle.y) / scale;
            return new Point((int)x,(int)y);
        }
        return null;
    }

    @Override
    public void paintComponent(Graphics gr){
        super.paintComponent(gr);
        if(image != null){
            Graphics2D g = (Graphics2D) gr;
            scaleImageRectangle();
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

    private void scaleImageRectangle() {
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
        imageRectangle.setBounds((d.width - w)/2, (d.height - h)/2, w, h);
    }
    
    public void export(File file) throws ExceptionMessage {
        ExportSVG export = new ExportSVG();
        Graphics2D g2svg = export.getGraphics(getWidth(), getHeight());
        paintComponent(g2svg);
        export.save(file);
    }
}
