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

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Stack;
import java.util.stream.IntStream;

/** ImageProcessing
 * class containing image processing functions
 * @author zikmuto2
 */
public class ImageProcessing {
    /** test barvy
     * bude vhodne odstranit
     * @return 
     */
    private static boolean testBarvy(int rgb){
        return true;
    }
    /** FloodFill 
     * @param image image to fill
     * @param x x-coordinate where flood starts
     * @param y y-coordinate where flood starts
     * @return 
     */
    public static BufferedImage floodFill(BufferedImage image, int x, int y){
        int treshold= 200; // TODO napravit a změřit
        Deque<Point> stack = new ArrayDeque<>();
        /* Napoveda: http://lodev.org/cgtutor/floodfill.html */
        stack.clear();
        
        int x1;
        boolean spanAbove, spanBelow;
        
        stack.push(new Point(x, y));
        
        while(!stack.isEmpty()){
            Point p = stack.pop();
            x1 = x;
            while(x1 >= 0 && testBarvy(image.getRGB(x1, y))) x1--; // couvám?
            x1++; // postupuji vpred: PROC?
            spanAbove = spanBelow = false;
            while(x1 < image.getWidth() && testBarvy(image.getRGB(x1, y))){
                
            }
        }
        
        /*IntStream stream = IntStream
                .iterate(image.getMinY(), n->n+1)
                .limit(image.getHeight());
        // stream.parallel();
        stream.forEach(n->ImageProcessing.scanlineFill(image,n));*/
        return image;
    }
    private static void scanlineFill(BufferedImage image, int y){}
}
