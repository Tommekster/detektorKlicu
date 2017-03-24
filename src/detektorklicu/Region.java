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
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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
    private Polygon boundingRectangle;
    private final LabelImage parent;
    Shapes shapes = new Shapes(); 
    
    public Region(LabelImage parent, int label, int area, Rectangle boundings, Point2D center){
        this(parent, label, area, boundings.x, boundings.y, boundings.x+boundings.width, boundings.y+boundings.height, center);
    }
    
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
    public double getOrientation() {
        if(!hasEllipse()) findBoundingEllipse();
        return orientation;
    }
    public double getHalfAxisA() {
        if(!hasEllipse()) findBoundingEllipse();
        return halfAxes[0];
    }
    public double getHalfAxisB() {
        if(!hasEllipse()) findBoundingEllipse();
        return halfAxes[1];
    }
    
    public double centralMoment(int p, int q){
        DoubleAdder sum = new DoubleAdder();
        double xc = center.getX();
        double yc = center.getY();
        ((Settings.getInstance().parallel)
                ? this.getBoundings().getHorizontalIntStream().parallel()
                : this.getBoundings().getHorizontalIntStream())
        .forEach(x->{
            double sumInCol = 0.0;
            double xPowered = Math.pow((double)x-xc, p);
            for(int y : this.getBoundings().getVerticalIntStream().toArray()) {
                if(parent.getLabel(x, y) == label){
                    sumInCol += xPowered * Math.pow((double)y-yc, q);
                    
                }
            }
            if(sumInCol != 0) sum.add(sumInCol);
        });
        
        return sum.doubleValue();
    }
    
    private void findBoundingEllipse(){
        double u11 = centralMoment(1, 1);
        double u20 = centralMoment(2, 0);
        double u02 = centralMoment(0, 2);
        
        double A = 2*u11;
        double B = u20-u02;
        double C = u20+u02;
        double D_root = Math.sqrt(B*B+A*A);
        
        orientation = Math.atan(2*u11/(u20-u02))/2 + (((u20-u02)<0)?Math.PI/2:0);
        halfAxes[0] = Math.sqrt(2*(C+D_root)/area);
        halfAxes[1] = Math.sqrt(2*(C-D_root)/area);
        
        boundingRectangle = null;
    }
    
    public void drawBoundingRectangle(Color color){
        Polygon pol = getBoundingRectangle();
        Graphics g = parent.getGraphics();
        g.setColor(color);
        g.drawPolygon(pol);
    }

    public Polygon getBoundingRectangle() {
        if(!hasEllipse()) findBoundingEllipse();
        if(boundingRectangle != null) return boundingRectangle;
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
        Polygon pol = new Polygon();
        for(int i = 0; i < 4; i++) pol.addPoint((int)p[i].getX(),(int)p[i].getY());
        pol.addPoint((int)p[0].getX(),(int)p[0].getY());
        
        boundingRectangle = pol;
        return pol;
    }
    
    class Shapes {
        Ellipse2D ellipse;
        Ellipse2D getEllipse2D(){
            if(this.ellipse != null) return this.ellipse;
            Ellipse2D ellipse = new Ellipse2D.Double(0, 0, 2*getHalfAxisA(), 2*getHalfAxisB());
            
            this.ellipse = ellipse;
            return this.ellipse;
        }
    }
}
