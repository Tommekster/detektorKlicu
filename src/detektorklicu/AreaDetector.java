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

import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.IntStream;

/**
 * AreaDetector labels separated regions
 * @author zikmuto2
 */
public class AreaDetector {
    private final LabelImage image;
    int label = 2;
    List<TreeSet<Integer>> repre;
    boolean fourEnviron = true;
    int progress = 0;
    PropertyChangeSupport changes = new PropertyChangeSupport(this);
    
    /**
     * Prepares AreaDetector for labelled image with separated background
     * @param image 
     */
    public AreaDetector(LabelImage image){
        repre = new LinkedList<>();
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
            setProgress(y*1000/h);
        }
        
        //dumpCollisionLeaves();
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
        
        if(v != 0) { // current label is not background
            
            // if there is one label in neighborhood it is eventually propagated
            // if there are more labels the lower is propagated and collision is marked
            for(int i = 0; i < 4; i++) {
                if(ns[i] != 0) {
                    if(label == 0) label = ns[i];
                    if(label != ns[i]){ // collision
                        label = union(label, ns[i]);
                    }
                }
            }
        
            if(label == 0) label = this.label++; // no other label is propagated
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
    
    private int union(int u, int v){
        TreeSet<Integer> cu = findComponent(u);
        TreeSet<Integer> cv = findComponent(v);
        if(cu == null){
            if(cv == null){
                TreeSet<Integer> nl = new TreeSet<>();
                nl.add(u);
                nl.add(v);
                repre.add(nl);
                return nl.first();
            }else{ /* cv != null */
                cv.add(u);
                return findMin(cv);
            }
        }else{ /* cu != null */
            if(cv == null){
                cu.add(v);
            }else if(cu != cv) { /* cu != null && cv != null */
                cu.addAll(cv);
                repre.remove(cv);
            }
            return findMin(cu);
        }
    }
    private void dumpComponent(TreeSet<Integer> list){
        System.out.print("{");
        for(int i: list){
            System.out.print(i+" ");
        }
        System.out.println("} represented by "+findMin(list));
    }
    private TreeSet<Integer> findComponent(int i){
        for(TreeSet<Integer> c : repre){
            if(c.contains(i)) return c;
        }
        return null;
    }
    private int findMin(TreeSet<Integer> list){
        return list.first();
    }
    
    
    private void dumpCollisionLeaves(){
        System.out.println("repre-lists:");
        for(TreeSet<Integer> l : repre){
            dumpComponent(l);
        }
    }
    
    private void agglomerateRegions(){
        IntStream.iterate(0, i->i+1).limit(image.getWidth()).parallel()
                .forEach(x->{
                    for(int y = 0; y < image.getHeight(); y++){
                        TreeSet<Integer> comp = findComponent(image.getLabel(x,y));
                        if(comp != null) 
                            image.setLabel(x, y, findMin(comp));
                    }
                });
    }
    
    private void setProgress(int i){
        changes.firePropertyChange("progress", progress, progress = i);
    }
    public void addPropertyChangeListener(PropertyChangeListener listener){
        changes.addPropertyChangeListener(listener);
    }
    public void addPropertyChangeListener(String property, PropertyChangeListener listener){
        changes.addPropertyChangeListener(property, listener);
    }
}
