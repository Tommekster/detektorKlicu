/*
 * The MIT License
 *
 * Copyright 2017 zikmundt.
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.util.List;

/**
 *
 * @author zikmundt
 */
public class Settings {
    // General
    boolean parallel = true;
    // Canvas
    Color defaultShapeColor = Color.BLUE;
    Stroke defaultShapeStroke = new BasicStroke(1);
    // LabelImage
    Color backgroundColor = Color.RED;
    Color selectedRegionColor = Color.GREEN;
    List<Color> labelsColorPalette = LabelImage.getPallete();
    double minimalRegionSurfaceFraction = 0.001;
    
    private static Settings instance;
    private Settings(){
        
    }
    public static Settings getInstance(){
        if(instance == null) instance = new Settings();
        return instance;
    }
}
