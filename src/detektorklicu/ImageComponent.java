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
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author zikmuto2
 */
public class ImageComponent extends BufferedImage{

    private List<Point> boundaryPath = null;
    private int xmin, xmax, ymin, ymax;
    private final int mark = Color.BLACK.getRGB();
    
    public static ImageComponent createImageComponent(List<Point> boundary){
        int [] lims = getBoundaryLimits(boundary);        
        return createImageComponent(boundary, lims);
    }
    
    public static ImageComponent createImageComponent(List<Point> boundary, int [] lims){
        int h = (lims[3]-lims[2])+3;
        int w = (lims[1]-lims[0])+3;
        
        ImageComponent comp = new ImageComponent(w,h,BufferedImage.TYPE_INT_RGB);
        comp.boundaryPath = boundary;
        comp.xmin = lims[0];
        comp.xmax = lims[1];
        comp.ymin = lims[2];
        comp.ymax = lims[3];
        comp.redraw();
        
        return comp;
    }
    
    private ImageComponent(int width, int height, int imageType) {
        super(width, height, imageType);
    }
    
    private static int [] getBoundaryLimits(List<Point> boundaryPath){
        int xmin, xmax, ymin, ymax;
        Point first = boundaryPath.get(0);
        xmin = xmax = first.x;
        ymin = ymax = first.y;
        
        Iterator<Point> it = boundaryPath.iterator();
        while(it.hasNext()){
            Point p = it.next();
            if(p.x < xmin) xmin = p.x;
            else if(xmax < p.x) xmax = p.x;
            if(p.y < ymin) ymin = p.y;
            else if(ymax < p.y) ymax = p.y;
        }
        
        int [] limits = {xmin,xmax,ymin,ymax};
        return limits;
    }
    
    /** redrawPoints
    * draws points with offset in image and fills background
    */
    public void redraw(){
        int xOffset = xmin-1;
        int yOffset = ymin-1;
        Graphics g = this.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        boundaryPath.forEach(p->{
            this.setRGB(p.x-xOffset, p.y-yOffset, mark);
        });
    }
    
}
