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

import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 * 
 * @author Acer
 */

    
class MainToolBar{
    MainWindow window;

    private final JToolBar toolBar = new JToolBar();
    private final JButton fileOpen = new JButton();
    private final JButton fileSave = new JButton();
    private final JButton fileExport = new JButton();
    private final JButton viewOriginal = new JButton();
    private final JButton viewScalled = new JButton();
    private final JButton showOriginal = new JButton();
    private final JButton showBackground = new JButton();
    private final JButton showLabels = new JButton();
    private final JButton showRegions = new JButton();
    private final JButton regionsTable = new JButton();

    public MainToolBar(MainWindow win){
        window = win;
        initialization();
    }
    
    public JToolBar getJToolBar(){
        return toolBar;
    }
    
    private void initialization(){
        //window.setT
        toolBar.setFloatable(false);

        addButton("fileOpen.png", fileOpen, window::fileOpen);
        addButton("fileSave.png", fileSave, window::fileSave);
        addButton("fileExport.png", fileExport, window::fileExportCurrent);
        addSeparator();
        addButton("viewOriginal.png", viewOriginal, (e) -> window.viewSetZoomSize(e, 100));
        addButton("viewScalled.png", viewScalled, window::viewScalled);
        addSeparator();
        addButton("toolShowOriginal.png", showOriginal, window::toolShowOriginal);
        addButton("toolShowBackground.png", showBackground, window::toolShowBackground);
        addButton("toolShowLabels.png", showLabels, window::toolShowLabels);
        addButton("toolShowRegionBounds.png", showRegions, window::toolShowRegionsBounds);
        addSeparator();
        addButton("toolRegionsList.png", regionsTable, window::toolRegionsList);
        
    }

    private void addButton(JButton btn, String iconName, String textBundle, ActionListener action){
        btn.setIcon(icons.Icons.getIcon(iconName));
        try{
            btn.setToolTipText(ResourceBundle.getBundle("texts/MainToolBar").getString(textBundle));
        }catch(MissingResourceException e){
            btn.setToolTipText(textBundle);
        }
        if(action != null) btn.addActionListener(action);
        btn.setBorder(null);
        toolBar.add(btn);
    }
    private void addButton(String iconName, JButton item, ActionListener action){
        addButton(item, iconName, getFieldName(item), action);
    }
    private void addSeparator(){
        toolBar.addSeparator();
    }
    
    String getFieldName(Object field){
        try {
            for(Field f : this.getClass().getDeclaredFields()) {
                    if(f.get(this) == field) {
                        return f.getName();
                    }
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(MainToolBar.class.getName()).log(Level.SEVERE, null, ex);
        }
        return field.getClass().getName();
    }
    
    public void enableImageActions(boolean b){
        fileSave.setEnabled(b);
        fileExport.setEnabled(b);
        
        viewOriginal.setEnabled(b);
        viewScalled.setEnabled(b);
        
        showOriginal.setEnabled(b);
        showBackground.setEnabled(b);
        showLabels.setEnabled(b);
        showRegions.setEnabled(b);
        
        regionsTable.setEnabled(b);
    }
}
