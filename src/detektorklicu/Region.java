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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;

/**
 *
 * @author Acer
 */
public class Region {
    private int label;
    private int area; // count of pixels belonging to the region .. surface
    private BoundingBox  boundingBox;
    private Point2D center;
    private HusMoments husMoments = null;
    private double orientation = 0;
    private double [] halfAxes = {0,0};
    //private double sinTheta = 0, cosTheta = 0;
    private final LabelImage parent;
    
    public Region(LabelImage parent, int label, int area, int left, int top, int right, int bottom, Point2D center){
        this.parent = parent;
        this.label = label;
        this.area = area;
        this.boundingBox = new BoundingBox(left, top, right, bottom);
        this.center = center;
    }
    
    public int getLabel() {return label;}
    public int getArea() {return area;}
    public BoundingBox getBoundings(){return boundingBox;}
    public Point2D getCenter(){return center;}
    public boolean hasEllipse(){return halfAxes[0] != 0;}
    public double getOrientation() {return orientation;}
    public double getHalfAxisA() {return halfAxes[0];}
    public double getHalfAxisB() {return halfAxes[1];}
    
    public double centralMoment(int p, int q){
        //AtomicInteger sum = new AtomicInteger(0);
        DoubleAdder sum = new DoubleAdder();
        //int label = this.getLabel();
        //Point center = region.getCenter();
        double xc = center.getX();
        double yc = center.getY();
        this.getBoundings().getHorizontalIntStream().parallel().forEach(x->{
            double sumInCol = 0.0;
            double xPowered = Math.pow((double)x-xc, p);
            for(int y : this.getBoundings().getVerticalIntStream().toArray()) {
                if(parent.getLabel(x, y) == label){
                    sumInCol += xPowered * Math.pow((double)y-xc, q);
                    
                }
            }
            if(sumInCol != 0) sum.add(sumInCol);
        });
        
        return sum.doubleValue();
    }
    
    public void findBoundingEllipse(){
        double u11 = centralMoment(1, 1);
        double u20 = centralMoment(2, 0);
        double u02 = centralMoment(0, 2);
        
        //double Asquared = 2*u11;
        double A = 2*u11;
        double B = u20-u02;
        double C = u20+u02;
        double D_root = Math.sqrt(B*B+A*A);
        
        /*System.out.println(u20+" "+u11);
        System.out.println(u11+" "+u02);*/
        
        orientation = Math.atan(2*u11/(u20-u02))/2 + (((u20-u02)<0)?Math.PI/2:0);
        halfAxes[0] = Math.sqrt(2*(C+D_root)/area);
        halfAxes[1] = Math.sqrt(2*(C-D_root)/area);
        
        /*double tan = B/D_root;
        cosTheta = (A == 0)? 0 : Math.sqrt((1+tan)/2);
        sinTheta = (A == 0)? 0 : ((A>=0)?Math.sqrt((1+tan)/2):-Math.sqrt((1+tan)/2));*/
    }
    
    public void drawBoundingRectangle(Color color){
        if(!hasEllipse()) findBoundingEllipse();
        
        double a = halfAxes[0];
        double b = halfAxes[1];
        
        double cosTheta = Math.cos(orientation);
        double sinTheta = Math.sin(orientation);
        
        double aCosTh = a*cosTheta;
        double aSinTh = a*sinTheta;
        double bCosTh = b*cosTheta;
        double bSinTh = -b*sinTheta;
        
        double xc = center.getX();
        double yc = center.getY();
        
        Point2D [] p ={
            new Point2D.Double(xc + aCosTh + bSinTh, yc + aSinTh + bCosTh),
            new Point2D.Double(xc + aCosTh - bSinTh, yc + aSinTh - bCosTh),
            new Point2D.Double(xc - aCosTh - bSinTh, yc - aSinTh - bCosTh),
            new Point2D.Double(xc - aCosTh + bSinTh, yc - aSinTh + bCosTh)
        };
        
        Graphics g = parent.getGraphics();
        g.setColor(color);
        for(int i = 0; i<4; i++)
            g.drawLine((int)p[i].getX(),(int)p[i].getY(),(int)p[(i+1)%4].getX(),(int)p[(i+1)%4].getY());
        
        //AffineTransform.getRotateInstance(orientation)
          //.createTransformedShape(new Ellipse2D.Double(0, 0, 2*halfAxes[0], 2*halfAxes[1]));
    }
}
