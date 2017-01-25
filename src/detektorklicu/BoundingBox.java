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
import java.util.stream.IntStream;

/**
 *
 * @author Acer
 */
public class BoundingBox {
    Point leftTop;
    Point rightBottom;

    public BoundingBox(int left, int top, int right, int bottom) {
        leftTop = new Point(left,top);
        rightBottom = new Point(right,bottom);
    }
    
    public IntStream getHorizontalIntStream(){
        return IntStream.iterate(leftTop.x, n->n+1).limit(rightBottom.x+1-leftTop.x);
    }
    
    public IntStream getVerticalIntStream(){
        return IntStream.iterate(leftTop.y, n->n+1).limit(rightBottom.y+1-leftTop.y);
    }
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        return sb.append("[")
                .append(leftTop.x).append(",")
                .append(leftTop.y).append(",")
                .append(rightBottom.x).append(",")
                .append(rightBottom.y).append("]")
                .toString();
    }
}
