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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/** Canvas panel
 * nested class that cares about appropriate image painting
 * @author zikmuto2
 */
class Canvas extends JPanel{
    private BufferedImage image = null;
    public void setImage(BufferedImage i){image=i;}
    public BufferedImage getImage() {return image;}
    public boolean isImageInside(){return image != null;}

    public Canvas(){
        super();
    }

    public Canvas(BufferedImage image){
        super();
        setImage(image);
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        if(image != null){
            // fit image in the box
            Dimension d = this.getSize();
            float rs = d.width/d.height;
            int w = image.getWidth();
            int h = image.getHeight();
            float ri = w/h;

            if(rs > ri) { // width is smaller
                w = w*d.height/h;
                h = d.height;
            }else{ // height is smaller
                h = h*d.width/w;
                w = d.width;
            }
            int top = (d.height - h)/2;
            int left = (d.width - w)/2;
            // Draw image
            g.drawImage(image, left, top, w, h, this);
        }
        if(image instanceof ImageComponent){
            ImageComponent im = (ImageComponent)image;
            setToolTipText("S:"+im.getSurface());
        }
    }
}
