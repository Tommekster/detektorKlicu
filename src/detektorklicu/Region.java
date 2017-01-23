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

/**
 *
 * @author Acer
 */
public class Region {
    private int label;
    private int area; // count of pixels belonging to the region .. surface
    private BoundingBox  boundingBox;
    private Point center;
    private HusMoments husMoments = null;
    
    public Region(int label, int area, int left, int top, int right, int bottom, Point center){
        this.label = label;
        this.area = area;
        this.boundingBox = new BoundingBox(left, top, right, bottom);
        this.center = center;
    }

    private static class BoundingBox {
        Point leftTop;
        Point rightBottom;

        public BoundingBox(int left, int top, int right, int bottom) {
            leftTop = new Point(left,top);
            rightBottom = new Point(right,bottom);
        }
    }
    
}
