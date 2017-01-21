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

import com.sun.org.apache.xerces.internal.util.IntStack;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * AreaDetector labels separated regions
 * @author zikmuto2
 */
public class AreaDetector {
    private final LabelImage image;
    int label = 2;
    List<Point> collisions;
    boolean fourEnviron = true;
    
    /**
     * Prepares AreaDetector for labelled image with separated background
     * @param image 
     */
    public AreaDetector(LabelImage image){
        collisions = new ArrayList<>();
        this.image = image;
    }
    
    /**
     * detects and denotes separated regions in the given labelled image
     * @param image labelled image with separated background
     * @return instance of AreaDetector
     */
    public static AreaDetector detectRegions(LabelImage image){
        AreaDetector detector = new AreaDetector(image);
        detector.detectRegions();
        return detector;
    }
    
    /**
     * detects and denotes separated regions in the given labelled image
     */
    public void detectRegions(){
        int h = image.getHeight();
        int w = image.getWidth();
        
        for(int y=0; y < h; y++){
            for(int x = 0; x < w; x++) {
                detectAndLabel(x,y);
            }
        }
        
        findCollisionLeaves();
        agglomerateRegions();
    }
    
    /**
     * denotes pixel if it belongs in a region
     * @param x x-coordinate
     * @param y y-coordinate
     */
    private void detectAndLabel(int x, int y){
        int [] ns = neighbors(x, y);
        int v = image.getLabel(x, y);
        int label = 0;
        boolean sameNeighbours = true;
        
        // if there is a label in neighborhood it is eventually propagated
        // if there are more labels the lower is propagated and collision is marked
        for(int i = 0; i < 4; i++) {
            if(ns[i] != 0) {
                if(label == 0) label = ns[i];
                if(label != ns[i]){
                    sameNeighbours = false;
                    int bigger;
                    int lower;
                    if(label > ns[i]){
                        bigger = label;
                        lower = ns[i];
                        label = lower;
                    }else{
                        bigger = ns[i];
                        lower = label;
                    }
                    if(v != 0) addCollision(new Point(lower, bigger));
                }
            }
        }
        
        if(v != 0) {
            if(label == 0) label = this.label++;
            image.setLabel(x, y, label);
        }
    }
    
    /** returns neighbors pixels, if they are else 0
     * +-+-+-+
     * |1|2|3|
     * |0|x| |
     * | | | |
     * +-+-+-+
     * @return neighbor values
     */
    private int [] neighbors(int x, int y){
        int n0, n1, n2, n3;
        
        if(x>0) n0 = image.getLabel(x-1, y);
        else n0 = 0;
        
        if(y>0){
            if(x>0 && !fourEnviron) n1 = image.getLabel(x-1, y-1);
            else n1 = 0;
            n2 = image.getLabel(x, y-1);
            if(x<image.getWidth()-1 && !fourEnviron) n3 = image.getLabel(x+1, y-1);
            else n3 = 0;
        }
        else n1 = n2 = n3 = 0;
        
        int [] values = {n0, n1, n2, n3};
        
        return values;
    }
    
    private void addCollision(Point p){
        if(collisions.contains(p)) return;
        collisions.add(p);
    }
    
    /**
     * collisions contains edges of the graph the depending subareas
     */
    // TODO: zjistit lepsi algoritmus
    private void findCollisionLeaves(){
        for(Point c : collisions){
            for(int j = 0; j < collisions.size(); j++){
                if(collisions.get(j).y == c.x) { // has parent
                    c.x = collisions.get(j).x;
                    j = -1;
                }
            }
        }
    }
    
    private void agglomerateRegions(){
        for(Point c : collisions){
            IntStream.iterate(0, i->i+1).limit(image.getWidth()).parallel()
                    .forEach(x->{
                        for(int y = 0; y < image.getHeight(); y++){
                            if(c.y == image.getLabel(x, y))
                                image.setLabel(x, y, c.x);
                        }
            });
        }
    }
}
