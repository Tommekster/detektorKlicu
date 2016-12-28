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
import java.util.IntSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 *
 * @author zikmuto2
 */
public class ImageComponent extends BufferedImage{

    private List<Point> boundaryPath = null;
    private int xmin, xmax, ymin, ymax, surface;
    private final int mark = Color.BLACK.getRGB();
    
    public static ImageComponent createImageComponent(List<Point> boundary){
        int [] lims = getBoundaryLimits(boundary);
        int surf = getSurface(boundary);
        return createImageComponent(boundary, lims, 0);
    }
    
    public static ImageComponent createImageComponent(List<Point> boundary, 
            int [] lims, int surface){
        int h = (lims[3]-lims[2])+3;
        int w = (lims[1]-lims[0])+3;
        
        ImageComponent comp = new ImageComponent(w,h,BufferedImage.TYPE_INT_RGB);
        comp.boundaryPath = boundary;
        comp.xmin = lims[0];
        comp.xmax = lims[1];
        comp.ymin = lims[2];
        comp.ymax = lims[3];
        comp.surface = surface;
        comp.redraw(true);
        
        return comp;
    }
    
    private ImageComponent(int width, int height, int imageType) {
        super(width, height, imageType);
    }
    
    private static int [] getBoundaryLimits(List<Point> boundaryPath){
        Point first = boundaryPath.get(0);
        PointExtremes pe = new PointExtremes(first);
        
        Iterator<Point> it = boundaryPath.iterator();
        while(it.hasNext()){
            Point p = it.next();
            pe.checkExtremes(p);
        }
        
        int [] limits = {pe.getXmin(),pe.getXmax(),pe.getYmin(),pe.getYmax()};
        return limits;
    }
    
    private static int getSurface(List<Point> boundaryPath){
        AtomicInteger surface = new AtomicInteger(0);
        IntStream.iterate(1, n->n+1)
                .limit(boundaryPath.size()-1)
                .parallel()
                .forEach(i->{
                    Point prev = boundaryPath.get(i-1);
                    Point curr = boundaryPath.get(i);
                    
                    if(prev.y == curr.y-1) /* down */ {
                        surface.getAndAdd(curr.x);
                    }else if(prev.y == curr.y+1) /* up */ {
                        surface.getAndAdd(-curr.x);
                    }
        });
        
        return surface.get();
    }
    
    private void fillBackground(){
        Graphics g = this.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
    }
    
    /** redrawPoints
    * draws points with offset in image and fills background
    */
    public void redraw(){
        int xOffset = xmin-1;
        int yOffset = ymin-1;
        fillBackground();
        boundaryPath.forEach(p->{
            this.setRGB(p.x-xOffset, p.y-yOffset, mark);
        });
    }
    public void redraw(boolean boundary){
        if(!boundary){ 
            redraw();
            return;
        }
        int xOffset = xmin - 1;
        int yOffset = ymin - 1;
        fillBackground();
        
        //Point p = boundaryPath.get(0);
        //this.setRGB(p.x-xOffset, p.y-yOffset, mark);
        
        IntStream.iterate(1, n->n+1)
                .limit(boundaryPath.size()-1)
                .forEach(i->{
                    Point prev = boundaryPath.get(i-1);
                    Point curr = boundaryPath.get(i);
                    Point p = null;
                    
                    if(prev.x == curr.x) {
                        if(prev.y == curr.y+1) p = new Point(curr);
                        else if(prev.y == curr.y-1) p = new Point(curr.x-1,curr.y-1);
                    }
                    else if(prev.y == curr.y) {
                        if(prev.x == curr.x+1) p = new Point(curr.x,curr.y-1);
                        else if(prev.x == curr.x-1) p = new Point(curr.x-1,curr.y);
                    }
                    
                    if(p != null) this.setRGB(p.x-xOffset, p.y-yOffset, mark);
                });
    }
    
    public int getSurface(){return surface;}
}
