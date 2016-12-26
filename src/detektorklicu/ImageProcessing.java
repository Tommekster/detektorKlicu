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
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Stack;
import java.util.stream.IntStream;
import static java.lang.Math.abs;
import java.time.Clock;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javafx.geometry.Bounds;
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
    /** Flood-fill background 
     * fills background with the given color. It uses paralelization.
     * @param image image to fill
     * @param color the color of the flood
     * @return 
     */
    public static void floodFillBackground(BufferedImage image, Color color){
        int h = image.getHeight();
        int w = image.getWidth();
        List<Point> startPoints = new LinkedList<>();
        startPoints.add(new Point(0,0));
        startPoints.add(new Point(w-1,h-1));
        startPoints.add(new Point(w-1,0));
        startPoints.add(new Point(0,h-1));
        
        startPoints.add(new Point(0,h/2));
        startPoints.add(new Point(w/2,0));
        startPoints.add(new Point(w-1,h/2));
        startPoints.add(new Point(w/2,h-1));
        
        startPoints.stream().parallel().forEach(p->floodFill(image, p.x, p.y, color));
    }
    /** FloodFill 
     * @param image image to fill
     * @param xi x-coordinate where flood starts
     * @param yi y-coordinate where flood starts
     * @param color the color of the flood
     * @return 
     */
    public static void floodFill(BufferedImage image, int xi, int yi, Color color){
        //Deque<Point> stack = new ArrayDeque<>();
        Queue<Point> queue = new ArrayDeque<>();
        queue.clear();
        
        int x = xi;
        int y = yi;
        int nRGB = color.getRGB();
        int oRGB = image.getRGB(x,y);
        int w = image.getWidth();
        int h = image.getHeight();
        
        queue.add(new Point(x, y));
        
        while(!queue.isEmpty()){
            // starts \w point at top of the stack
            Point p = queue.poll();
            x = p.x;
            y = p.y;
            //System.out.println(x+","+y);
            if(image.getRGB(x, y) == nRGB) continue;
            oRGB = image.getRGB(x,y);
            
            image.setRGB(x, y, nRGB);
            if(y>0 && testBarvy(image.getRGB(x, y-1), oRGB)) queue.add(new Point(x, y-1));
            if(x<(w-1) && testBarvy(image.getRGB(x+1, y), oRGB)) queue.add(new Point(x+1, y));
            if(y<(h-1) && testBarvy(image.getRGB(x, y+1), oRGB)) queue.add(new Point(x, y+1));
            if(x>0 && testBarvy(image.getRGB(x-1, y), oRGB)) queue.add(new Point(x-1, y));
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
    
    /** erode
     * should find borders of components
     * @param image
     * @param outside
     * @param border 
     */
    public static void erode(BufferedImage image, Color outside, Color border){
        int w = image.getWidth();
        int h = image.getHeight();
        int out = outside.getRGB();
        int brd = border.getRGB();
     
        // at first I will erode in columns
        IntStream.iterate(0, n->n+1)
                .limit(w)
                .parallel()
                .forEach(x->{
                    boolean isout = image.getRGB(x, 0) == out;
                    for(int y = 1; y < h; y++){
                        if(isout && image.getRGB(x, y) != out){
                            image.setRGB(x, y, brd);
                            isout = false;
                        }else if(!isout && image.getRGB(x, y)== out){
                            image.setRGB(x, y-1, brd);
                            isout = true;
                        }
                    }
                });
        
        // then it erodes in row
        // if I take care about border points or dont, result will be same.
        IntStream.iterate(0, n->n+1)
                .limit(h)
                .parallel()
                .forEach(y->{
                    boolean isout = image.getRGB(0, y) == out;
                    for(int x = 1; x < w; x++){
                        if(isout && image.getRGB(x, y) != out){
                            image.setRGB(x, y, brd);
                            isout = false;
                        }else if(!isout && image.getRGB(x, y)== out){
                            image.setRGB(x-1, y, brd);
                            isout = true;
                        }
                    }
                });
    }
    
    /**
     * contains methods for separating components in image
     * The components must be denoted in advance.
     */
    public static class SeparatableImage{
        private BufferedImage image;
        private int border, mark;
        private int height, width;
        
        /**
         * crates an instance of SeparatableImage
         * and sets an image and basic parameters
         * @param src an image with components denoted by border
         * @param border color of border
         * @param mark color suitable for marking detected border
         */
        public SeparatableImage(BufferedImage src, Color border, Color mark){
            image = src;
            height = image.getHeight();
            width = image.getWidth();
            
            this.border = border.getRGB();
            this.mark = mark.getRGB();
        }
        
        /** separateComponents
         * separates components in image
         * @return list of images containing components
         */
        public static List<ImageComponent> separateComponents(BufferedImage src, Color border, Color mark){
            SeparatableImage separatable = new SeparatableImage(src, border, mark);
            return separatable.separateComponents();
        }
        
        /** separateComponents
         * separates components in image
         * @return list of images containing components
         */
        public List<ImageComponent> separateComponents(){
            List<ImageComponent> components = new LinkedList<>();

            IntStream.iterate(0, n->n+1)
                .limit(height)
                .forEach(y->{
                    for(int x = 1; x < width-1; x++){
                        if(image.getRGB(x, y) == border 
                                && image.getRGB(x-1, y) == Color.red.getRGB()){
                            ImageComponent comp = separateComp(x, y);
                            if(comp != null) components.add(comp);
                        }
                    }
                });
            
            return components;
        }
        
        /** separates a component
         * separates a component by finding a border
        * @param xi x coordinate of start point
        * @param yi y coordinate of start point
        * @return image of a component
        */
        private ImageComponent separateComp(int xi, int yi){
            //List<Point> points = new LinkedList<>();
            /*int [] bounds = 
            int xmin = bounds[0];
            int xmax = bounds[1];
            int ymin = bounds[2];
            int ymax = bounds[3];*/
            
            // TODO: opravit: oznaceni hranice
            List<Point> pts = new LinkedList<>();
            //findBorderPath(xi, yi, pts);
            
            PathFinder pf = new PathFinder(image, xi, yi, 
                    new Color(border), new Color(mark));
            int xmin = pf.getXmin();
            int xmax = pf.getXmax();
            int ymin = pf.getYmin();
            int ymax = pf.getYmax();
            int [] bounds = {xmin,xmax,ymin,ymax};
            List<Point> points = pf.getPath();
            
            int h = (ymax-ymin)+3;
            int w = (xmax-xmin)+3;
            
            if(w < 3+10 || h < 3+10) return null;
            
            ImageComponent component = ImageComponent.createImageComponent(points,bounds);
            
            return component;
        }

        /** Finds border as a path
         * de facto provides 8 points floodfill and saves the points
         * @param xi x-coordinate of start point
         * @param yi y-coordinate of start point
         * @param points output list of the path's points
         * @return list of extremes of the coordinates {xmin,xmax,ymin,ymax}
         */
        public int[] findBorderPath(int xi, int yi, List<Point> points) {
            int x,y, xmin,xmax, ymin,ymax;
            x = xmin = xmax = xi;
            y = ymin = ymax = yi;
            Deque<Point> queue = new ArrayDeque<>();
            queue.clear();

            queue.push(new Point(x, y));

            while(!queue.isEmpty()){
                Point p = queue.pop();
                x = p.x;
                y = p.y;
                
                if(image.getRGB(x, y) != border) continue; // takovy bod me (uz) nezajima
                
                // bod oznacima a ulozime
                image.setRGB(x, y, mark);
                points.add(p);
                
                // min a max
                if(x < xmin) xmin = x;
                else if(xmax < x) xmax = x;
                if(y < ymin) ymin = y;
                else if(ymax < y) ymax = y;
                
                if(y>0 && image.getRGB(x, y-1) == border) 
                    queue.push(new Point(x, y-1));
                if(x<(width-1) && image.getRGB(x+1, y) == border) 
                    queue.push(new Point(x+1, y));
                if(y<(height-1) && image.getRGB(x, y+1) == border) 
                    queue.push(new Point(x, y+1));
                if(x>0 && image.getRGB(x-1, y) == border) 
                    queue.push(new Point(x-1, y));
                if(y>0 && x<(width-1) && image.getRGB(x+1, y-1) == border) 
                    queue.push(new Point(x+1, y-1));
                if(y<(height-1) && x<(width-1) && image.getRGB(x+1, y+1) == border) 
                    queue.push(new Point(x+1, y+1));
                if(y<(height-1) && x>0 && image.getRGB(x-1, y+1) == border) 
                    queue.push(new Point(x-1, y+1));
                if(y>0 && x>0 && image.getRGB(x-1, y-1) == border) 
                    queue.push(new Point(x-1, y-1));
            }
            
            int [] bounds = {xmin,xmax,ymin,ymax};
            return bounds;
        }
        
        /** 
         * PathFinder class is suitable for finding a path around 
         * the boundary of the assumed component.
         */
        private static class PathFinder{
            private int surface;
            private List<Point> points;
            private PointExtremes extremes;
            private BufferedImage image;
            private int border;
            private int mark;
            
            private enum Direction {D0, D90, D180, D270, NOWAY};
            
            /**
             * Constructor prepares PathFinder class
             * @param image An image containing the component denoted by color
             * @param border A color denoting the component boundary
             */
            public PathFinder(BufferedImage image, Color border, Color mark){
                points = new LinkedList<>();
                extremes = null;
                surface = 0;
                this.image = image;
                this.border = border.getRGB();
                this.mark = mark.getRGB();
            }
            /**
             * Constructor of PathFinder class: prepares and starts finding
             * @param image An image containing the component denoted by color
             * @param x x-coordinate of the start point
             * @param y y-coordinate of the start point
             * @param border A color denoting the component boundary
             */
            public PathFinder(BufferedImage image, int x, int y, 
                    Color border, Color mark) {
                this(image,border,mark);
                findPath(x,y);
            }
            /**
             * Constructor of PathFinder class: prepares and starts finding
             * @param image An image containing the component denoted by color
             * @param start A start point
             * @param border A color denoting the component boundary
             */
            public PathFinder(BufferedImage image, Point start, Color border, Color mark){
                this(image,start.x,start.y,border,mark);
            }
            
            /**
             * checks point's color if it is a border or it is marked
             * @param x x-coordinate of the point
             * @param y y-coordinate of the point
             * @return true if it is a border or it is marked
             */
            private boolean checkColor(int x, int y){
                int c = image.getRGB(x, y);
                //return c == border || c == mark;
                return c != Color.red.getRGB(); // TODO opravit na background
            }
            /**
             * choices direction in order to go around the component
             * @param p the point from where it goes
             * @return an appropriate direction
             */
            private Direction choiceDirection(Point p){
                int h = image.getHeight();
                int w = image.getWidth();
                // 0 deg
                if(p.x < w && p.y < h
                        && checkColor(p.x,p.y) 
                        && (p.y == 0 || !checkColor(p.x, p.y-1))) 
                    return Direction.D0;
                // 90 deg
                else if(p.y > 0 && p.x < w
                        && checkColor(p.x, p.y-1)
                        && (p.x == 0 || !checkColor(p.x-1, p.y-1)))
                    return Direction.D90;
                // 180 deg
                else if(p.x > 0 && p.y > 0
                        && checkColor(p.x-1, p.y-1)
                        && (p.y == h || !checkColor(p.x-1, p.y)))
                    return Direction.D180;
                // 270 deg
                else if(p.y < h && p.x > 0
                        && checkColor(p.x-1, p.y)
                        && (p.x == w || !checkColor(p.x, p.y)))
                    return Direction.D270;
                return Direction.NOWAY; // It couldn't find a way.
            }
            /** 
             * returns the next point in the given direction
             * @param p point from where it goes
             * @param d a direction
             * @return point to where it may go
             */
            private Point nextPointInDirection(Point p, Direction d){
                Point next = null;
                switch(d){
                    case D0:
                        next = new Point(p.x+1,p.y);
                        break;
                    case D90:
                        next = new Point(p.x, p.y-1);
                        break;
                    case D180:
                        next = new Point(p.x-1, p.y);
                        break;
                    case D270:
                        next = new Point(p.x, p.y+1);
                        break;
                    case NOWAY:
                        next = new Point(p.x, p.y);
                        break;
                    default:
                        throw new AssertionError(d.name());
                }
                return next;
            }
            /**
             * counts contributions of the surface during walking around
             * @param p current point
             * @param d current direction
             */
            private void integrateSurface(Point p, Direction d){
                switch(d){
                    case D90:
                        surface-=p.x;
                        break;
                    case D270:
                        surface+=p.x;
                        break;
                    default:
                        // do nothing
                        break;
                }
            }
            /**
             * marks surrounded point
             * @param p reached point
             * @param d used direction
             */
            private void markPoint(Point p, Direction d){
                int h = image.getHeight();
                int w = image.getWidth();
                
                switch(d){
                    case D0:
                        if(p.x >= 0 && p.x < w && p.y < h && p.y >=0)
                            image.setRGB(p.x, p.y, mark);
                        break;
                    case D90:
                        if(p.x >= 0 && p.x < w && p.y <= h && p.y >0)
                            image.setRGB(p.x, p.y-1, mark);
                        break;
                    case D180:
                        if(p.x > 0 && p.x <= w && p.y <= h && p.y >0)
                            image.setRGB(p.x-1, p.y-1, mark);
                        break;
                    case D270:
                        if(p.x > 0 && p.x <= w && p.y < h && p.y >=0)
                            image.setRGB(p.x-1, p.y, mark);
                        break;
                    default:
                        break;
                }
            }
            /**
            * generates an ordered list of points surrounding points
            * @param x x-coordinate of starting point
            * @param y y-coordinate of starting point
            */
            public void findPath(int x, int y){
                findPath(new Point(x, y));
            }
            /**
             * generates an ordered list of points surrounding points
             * @param start a starting point
             */
            public void findPath(Point start){
                // initialize extremes: min, max
                extremes = new PointExtremes(start);

                // starting point belongs to path
                points.clear();
                points.add(start);
                
                // initialize the surface
                surface = 0;

                // 1st movement
                Direction d = choiceDirection(start);
                markPoint(start, d);
                integrateSurface(start, d);
                Point p = nextPointInDirection(start, d);
                
                int n=2000;
                while (!start.equals(p)) { // while: not returned to the start
                    points.add(p);
                    extremes.checkExtremes(p);
                    d = choiceDirection(p);
                    markPoint(p, d);
                    if(d == Direction.NOWAY){
                        System.out.println(p.x+","+p.y+" break");
                        break;
                    }
                    integrateSurface(p, d);
                    p = nextPointInDirection(p, d);
                    
                    System.out.println(p.x+","+p.y);
                    if(n-- == 0) break;
                }
            }
            
            public boolean isPathFound(){
                return !points.isEmpty();
            }
            public List<Point> getPath(){
                return new LinkedList<>(points);
            }
            public int getXmin(){
                if(!isPathFound()) return 0;
                return extremes.getXmin();
            }
            public int getXmax(){
                if(!isPathFound()) return 0;
                return extremes.getXmax();
            }
            public int getYmin(){
                if(!isPathFound()) return 0;
                return extremes.getYmin();
            }
            public int getYmax(){
                if(!isPathFound()) return 0;
                return extremes.getYmax();
            }
            public int getSurface(){
                if(!isPathFound()) return 0;
                return surface;
            }
        }
    }
}
