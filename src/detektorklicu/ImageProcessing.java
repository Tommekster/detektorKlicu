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
import java.util.stream.IntStream;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/** ImageProcessing
 * class containing image processing functions
 * @author zikmuto2
 */
public class ImageProcessing {
    static int d;
    
    public static void floodFillSeparateBackground(LabelImage image){
        floodFillSeparateBackground(image, null);
    }
    /** Flood-fill background 
     * fills background with the given color. It uses parallelization.
     * @param image image to fill
     * @param progress
     * @param color the color of the flood
     * @return 
     */
    static void floodFillSeparateBackground(LabelImage image, QueuedWorker.Progress progress){
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
        
        if(progress == null) startPoints.stream().parallel().forEach(p->floodFill(image, p.x, p.y));
        else {
            int pointSum = h * w;
            AtomicInteger points = new AtomicInteger(0);
            startPoints.stream().parallel().forEach(p->{
                progress.setValue((int) ((double)points.addAndGet(floodFill(image, p.x, p.y))/pointSum*1000));
            });
        }
    }
    
    /** test barvy
     * bude vhodne odstranit
     * @return 
     */
    private static boolean colorTest(int rgb1, int rgb2){
        Color c = new Color(rgb1);
        Color č = new Color(rgb2);
        
        /*int*/ d = abs(c.getRed()-č.getRed()) 
                + abs(c.getGreen()-č.getGreen()) 
                + abs(c.getBlue()-č.getBlue());
        
        return d < 10;
    }
    
    /** FloodFillColor
     * @param image image to fill
     * @param xi x-coordinate where flood starts
     * @param yi y-coordinate where flood starts
     * @param color the color of the flood
     * @return 
     */
    public static void floodFillColor(LabelImage image, int xi, int yi, Color color){
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
            image.setLabel(x, y, 0);
            if(y>0 && colorTest(image.getRGB(x, y-1), oRGB)) queue.add(new Point(x, y-1));
            if(x<(w-1) && colorTest(image.getRGB(x+1, y), oRGB)) queue.add(new Point(x+1, y));
            if(y<(h-1) && colorTest(image.getRGB(x, y+1), oRGB)) queue.add(new Point(x, y+1));
            if(x>0 && colorTest(image.getRGB(x-1, y), oRGB)) queue.add(new Point(x-1, y));
        }
    }
    
    /** FloodFill 
     * @param image image to fill
     * @param xi x-coordinate where flood starts
     * @param yi y-coordinate where flood starts
     * @param color the color of the flood
     * @return 
     */
    public static int floodFill(LabelImage image, int xi, int yi){
        //Deque<Point> stack = new ArrayDeque<>();
        Queue<Point> queue = new ArrayDeque<>();
        queue.clear();
        
        int x = xi;
        int y = yi;
        int oRGB;
        int w = image.getWidth();
        int h = image.getHeight();
        int denoted = 0;
        
        queue.add(new Point(x, y));
        
        while(!queue.isEmpty()){
            // starts \w point at top of the stack
            Point p = queue.poll();
            x = p.x;
            y = p.y;
            //System.out.println(x+","+y);
            if(image.getLabel(x, y) == 0) continue;
            oRGB = image.getRGB(x,y);
            
            //image.setRGB(x, y, nRGB);
            image.setLabel(x, y, 0); 
            denoted++;
            if(  y>0   && image.getLabel(x, y-1) != 0 && colorTest(image.getRGB(x, y-1), oRGB)) queue.add(new Point(x, y-1));
            if(x<(w-1) && image.getLabel(x+1, y) != 0 && colorTest(image.getRGB(x+1, y), oRGB)) queue.add(new Point(x+1, y));
            if(y<(h-1) && image.getLabel(x, y+1) != 0 && colorTest(image.getRGB(x, y+1), oRGB)) queue.add(new Point(x, y+1));
            if(  x>0   && image.getLabel(x-1, y) != 0 && colorTest(image.getRGB(x-1, y), oRGB)) queue.add(new Point(x-1, y));
        }
        
        return denoted;
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
    
    
    
    /**
     * contains methods for separating components in image
     * The components must be denoted in advance.
     */
    public static class SeparatableImage{
        private LabelImage image;
        private int height, width;
        
        /**
         * crates an instance of SeparatableImage
         * and sets an image and basic parameters
         * @param src an image with components denoted by border
         */
        public SeparatableImage(LabelImage src){
            image = src;
            height = image.getHeight();
            width = image.getWidth();
        }
        
        /** separateComponents
         * separates components in image
         * @param src
         * @return list of images containing components
         */
        public static List<ImageComponent> separateComponents(LabelImage src){
            SeparatableImage separatable = new SeparatableImage(src);
            return separatable.separateComponents();
        }
        
        /** separateComponents
         * separates components in image
         * @param src
         * @param border
         * @param mark
         * @param background
         * @return list of images containing components
         */
        public static ImageComponent findOneComponent(LabelImage src, 
                int label){
            SeparatableImage separatable = new SeparatableImage(src);
            return separatable.findOneComponent(label);
        }
        
        /** separateComponents
         * separates components in image
         * @return list of images containing components
         */
        public List<ImageComponent> separateComponents(){
            List<Integer> labels = image.getLabelsList();
            List<ImageComponent> components = new ArrayList<>(labels.size());

            IntStream.iterate(0, n->n+1)
                .limit(labels.size())
                .forEach(lbl->{
                    ImageComponent comp = findOneComponent(lbl);
                    if(comp != null) components.add(comp);
                });
            
            return components;
        }
        
        /** separateComponents
         * separates components in image
         * @param label
         * @return list of images containing components
         */
        public ImageComponent findOneComponent(int label){
            ImageComponent component = null;

            out:for(int y = 0; y < height; y++){
                    for(int x = 1; x < width-1; x++){
                        if(image.getLabel(x, y) == label){
                            ImageComponent comp = separateComp(x, y, label);
                            if(comp != null){
                                component = comp;
                                break out;
                            }
                        }
                    }
            }
            
            return component;
        }
        
        /** separates a component
         * separates a component by finding a border
        * @param xi x coordinate of start point
        * @param yi y coordinate of start point
        * @return image of a component
        */
        private ImageComponent separateComp(int xi, int yi, int label){
            //List<Point> points = new LinkedList<>();
            /*int [] bounds = 
            int xmin = bounds[0];
            int xmax = bounds[1];
            int ymin = bounds[2];
            int ymax = bounds[3];*/
            
            // TODO: opravit: oznaceni hranice
            /*List<Point> pts = new LinkedList<>();
            findBorderPath(xi, yi, pts);*/
            
            PathFinder pf = new PathFinder(image, new Point(xi, yi), label);
            int xmin = pf.getXmin();
            int xmax = pf.getXmax();
            int ymin = pf.getYmin();
            int ymax = pf.getYmax();
            int [] bounds = {xmin,xmax,ymin,ymax};
            List<Point> points = pf.getPath();
            int surf = pf.getSurface();
            
            int h = (ymax-ymin)+3;
            int w = (xmax-xmin)+3;
            
            if(w < 3+10 || h < 3+10) return null;
            
            ImageComponent component = 
                    ImageComponent.createImageComponent(points,bounds,surf);
            
            return component;
        }

        /** Finds border as a path
         * de facto provides 8 points floodfill and saves the points
         * @param xi x-coordinate of start point
         * @param yi y-coordinate of start point
         * @param points output list of the path's points
         * @return list of extremes of the coordinates {xmin,xmax,ymin,ymax}
         * /
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
            private List<Point> collisions;
            private PointExtremes extremes;
            private LabelImage image;
            private int label;
            
            private enum Direction {D0, D90, D180, D270, NOWAY};
            
            /**
             * Constructor prepares PathFinder class
             * @param image An image containing the component denoted by color
             * @param border A color denoting the component boundary
             */
            public PathFinder(LabelImage image, int label){
                points = new LinkedList<>();
                collisions = new LinkedList<>();
                extremes = null;
                surface = 0;
                this.image = image;
                this.label = label;
            }
            /**
             * Constructor of PathFinder class: prepares and starts finding
             * @param image An image containing the component denoted by color
             * @param x x-coordinate of the start point
             * @param y y-coordinate of the start point
             * @param border A color denoting the component boundary
             */
            public PathFinder(LabelImage image, Point start, int label) {
                this(image,label);
                findPath(start.x,start.y);
            }
            
            /**
             * checks if point belongs to the region
             * @param x x-coordinate of the point
             * @param y y-coordinate of the point
             * @return true if 
             */
            private boolean isInRegion(int x, int y){
                return image.getLabel(x, y) == label;
            }
            /**
             * 
             * @param oldOne
             * @param newOne
             * @param p
             * @return 
             */
            private Direction assignDirection(Direction oldOne, Direction newOne, Point p){
                if(oldOne == Direction.NOWAY || collisions.contains(p)) 
                    oldOne = newOne;
                else if(oldOne != Direction.NOWAY)
                    collisions.add(p);
                
                return oldOne;
            }
            /**
             * choices direction in order to go around the component
             * @param p the point from where it goes
             * @return an appropriate direction
             */
            private Direction choiceDirection(Point p){
                int h = image.getHeight();
                int w = image.getWidth();
                
                Direction d = Direction.NOWAY;
                // 0 deg
                if(p.x < w && p.y < h
                        && isInRegion(p.x,p.y) 
                        && (p.y == 0 || !isInRegion(p.x, p.y-1))) 
                    d = assignDirection(d, Direction.D0, p);
                // 90 deg
                else if(p.y > 0 && p.x < w
                        && isInRegion(p.x, p.y-1)
                        && (p.x == 0 || !isInRegion(p.x-1, p.y-1)))
                    d = assignDirection(d, Direction.D90, p);
                // 180 deg
                else if(p.x > 0 && p.y > 0
                        && isInRegion(p.x-1, p.y-1)
                        && (p.y == h || !isInRegion(p.x-1, p.y)))
                    d = assignDirection(d, Direction.D180, p);
                // 270 deg
                else if(p.y < h && p.x > 0
                        && isInRegion(p.x-1, p.y)
                        && (p.x == w || !isInRegion(p.x, p.y)))
                    d = assignDirection(d, Direction.D270, p);
                return d; 
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
             * /
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
                //markPoint(start, d);
                integrateSurface(start, d);
                Point p = nextPointInDirection(start, d);
                
                int n = image.getWidth()*image.getHeight()/4;
                while (!start.equals(p)) { // while: not returned to the start
                    points.add(p);
                    extremes.checkExtremes(p);
                    d = choiceDirection(p);
                    //markPoint(p, d);
                    if(d == Direction.NOWAY){
                        System.out.println(p.x+","+p.y+" break");
                        break;
                    }
                    integrateSurface(p, d);
                    p = nextPointInDirection(p, d);
                    
                    //System.out.println(p.x+","+p.y);
                    if(n-- == 0) {
                        System.out.println("timeout");
                        break;
                    }
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
