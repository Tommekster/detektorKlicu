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
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Stack;
import java.util.stream.IntStream;
import static java.lang.Math.abs;
import java.time.Clock;
import javax.swing.JFrame;

/** ImageProcessing
 * class containing image processing functions
 * @author zikmuto2
 */
public class ImageProcessing {
    static int d;
    /** test barvy
     * bude vhodne odstranit
     * @return 
     */
    private static boolean testBarvy(int rgb1, int rgb2){
        Color c = new Color(rgb1);
        Color č = new Color(rgb2);
        
        /*int*/ d = abs(c.getRed()-č.getRed()) 
                + abs(c.getGreen()-č.getGreen()) 
                + abs(c.getBlue()-č.getBlue());
        
        return d < 10;
    }
    /** FloodFill 
     * @param image image to fill
     * @param xi x-coordinate where flood starts
     * @param yi y-coordinate where flood starts
     * @param color the color of the flood
     * @return 
     */
    public static void floodFill(BufferedImage image, int xi, int yi, Color color){
        Deque<Point> stack = new ArrayDeque<>();
        stack.clear();
        
        int x = xi;
        int y = yi;
        int nRGB = color.getRGB();
        int oRGB = image.getRGB(x,y);
        int w = image.getWidth();
        int h = image.getHeight();
        
        stack.push(new Point(x, y));
        
        while(!stack.isEmpty()){
            // starts \w point at top of the stack
            Point p = stack.pop();
            x = p.x;
            y = p.y;
            //System.out.println(x+","+y);
            if(image.getRGB(x, y) == nRGB) continue;
            oRGB = image.getRGB(x,y);
            
            image.setRGB(x, y, nRGB);
            if(y>0 && testBarvy(image.getRGB(x, y-1), oRGB)) stack.push(new Point(x, y-1));
            if(x<(w-1) && testBarvy(image.getRGB(x+1, y), oRGB)) stack.push(new Point(x+1, y));
            if(y<(h-1) && testBarvy(image.getRGB(x, y+1), oRGB)) stack.push(new Point(x, y+1));
            if(x>0 && testBarvy(image.getRGB(x-1, y), oRGB)) stack.push(new Point(x-1, y));
        }
    }
    
    /** scanline flood fill 
     * @param image image to fill
     * @param xi x-coordinate where flood starts
     * @param yi y-coordinate where flood starts
     * @param color the color of the flood
     * @return 
     */
    public static void scanlineFill(BufferedImage image, int xi, int yi, Color color){
        Deque<Point> stack = new ArrayDeque<>();
        /* Napoveda: http://lodev.org/cgtutor/floodfill.html */
        stack.clear();
        
        int x = xi;
        int y = yi;
        int nRGB = color.getRGB();
        int oRGB = image.getRGB(xi,yi);
        int w = image.getWidth();
        int h = image.getHeight();
        boolean spanAbove, spanBelow;
        
        stack.push(new Point(x, y));
        
        while(!stack.isEmpty()){
            // starts \w point at top of the stack
            Point p = stack.pop();
            x = p.x;
            y = p.y;
            System.out.println(x+","+y);
            if(image.getRGB(x, y) == nRGB) continue;
            // the colors of the points in the stack are appropriate
            oRGB = image.getRGB(x,y);
            // at first reverse to posible begin
            while(x >= 0 && testBarvy(image.getRGB(x, y),oRGB)) {
                oRGB = image.getRGB(x,y);
                //System.out.println(--x+","+y);
                x--;
            }
            x++; // correct to do-while
            spanAbove = spanBelow = false; // off flags
            // for every appropriate point in the line ...
            while(x < w && testBarvy(image.getRGB(x, y),oRGB)){
                // take the old color
                oRGB = image.getRGB(x,y);
                // set up the new color
                image.setRGB(x, y, nRGB);
                //o.repaint();
                // smell point above
                if(!spanAbove && y>0 &&  testBarvy(image.getRGB(x,y-1),oRGB)){
                    stack.push(new Point(x,y-1));
                    spanAbove = true;
                }else if(spanAbove && !testBarvy(image.getRGB(x,y-1),oRGB))
                    spanAbove = false;
                // smell point below
                if(!spanBelow && y<(h-1) &&  testBarvy(image.getRGB(x,y+1),oRGB)){
                    stack.push(new Point(x,y+1));
                    spanBelow = true;
                }else if(spanBelow && !testBarvy(image.getRGB(x,y+1),oRGB))
                    spanBelow = false;
                // move forward
                x++;
            }
        }
  // pro prvni bod na zasobniku
    // zacneme nasim vychozim bodem na radku, tak ze ho vlozime do zasobniku
    // nacouvame na zacatek
    // shodime priznaky
    // konecne jsme zacali vyplnovat radek
    // pokud jsme nevypadli z obrazku a ma to smysl
      // nastavime barvu
      /* Pokud jsme jeste nesli nahoru a z tohoto bodu ma smysl jit nahoru, pak uloz na zasobnik bod nad nim. 
         Pokud jsme uz sli nahoru a do tohoto bodu se dostaneme, nebude delat nic, jinak sunda priznak, ze jsme nahoru sli. 
       */
      // stejnym zpusobem rovnou ocuchava radek pod sebou
      // posuneme se dale v radku
        
        /*IntStream stream = IntStream
                .iterate(image.getMinY(), n->n+1)
                .limit(image.getHeight());
        // stream.parallel();
        stream.forEach(n->ImageProcessing.scanlineFill(image,n));*/
    }
    
    public static BufferedImage gray2RGB(BufferedImage grayI){
        BufferedImage colorI = new BufferedImage(grayI.getWidth(), 
                grayI.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        BufferedImageOp op = new ColorConvertOp(
                grayI.getColorModel().getColorSpace(),
                colorI.getColorModel().getColorSpace(),null);
        op.filter(grayI, colorI);
        
        return colorI;
    }
}
