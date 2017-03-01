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
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * MainMenu
 * @author tommekster@gmail.com
 */
class MainMenu{
    /** Reference na rodice hlavni nabidky */
    MainWindow window;

    private final JMenuBar menuBar = new JMenuBar();

    private final JMenu fileMenu = new JMenu();
    private final JMenuItem fileNew = new JMenuItem();
    private final JMenuItem fileOpen = new JMenuItem();
    private final JMenuItem fileSave = new JMenuItem();
    private final JMenuItem fileQuit = new JMenuItem();

    private final JMenu detectionMenu = new JMenu();
    private final JMenuItem detectionTest = new JMenuItem();
    private final JMenuItem detectionRegions = new JMenuItem();

    private final JMenu toolsMenu = new JMenu();
    private final JMenuItem toolShowLabels = new JMenuItem();
    private final JMenuItem toolRegionsList = new JMenuItem();
    private final JMenuItem toolShowRegionsBounds = new JMenuItem();
    private final JMenuItem toolRegionDetail = new JMenuItem();

    private final JMenu helpMenu = new JMenu();
    private final JMenuItem helpAbout = new JMenuItem();
    
    private JMenu lastAddedMenu;

    /** Konstruktor hlavni nabidky
     * vezme si okdkaz na okno a inicializuje obsah hlavni nabidky
     * @param o odkaz na hlavni okno
     */
    public MainMenu(MainWindow win){
        window = win;
        this.initialization();
        //this.initializeListeners();
    }

    /** Propoji polozky menu s nabidkou a vlozi ji do okna */
    private void initialization() {
        window.setJMenuBar(menuBar);

        addMenu(fileMenu);
        addMenuItem(fileNew, (e) -> window.fileNew(e));
        addMenuItem(fileOpen, e->window.fileOpen(e));
        addMenuItem(fileSave, e->window.fileSave(e));
        addSeparator();
        addMenuItem(fileQuit,e->window.fileQuit(e));
        
        addMenu(detectionMenu);
        addMenuItem(detectionRegions, e->window.detectRegions(e));
        
        addMenu(toolsMenu);
        addMenuItem(toolShowLabels, e->window.toolShowLabels(e));
        addMenuItem(toolRegionsList, e->window.toolRegionsList(e));
        addMenuItem(toolShowRegionsBounds, e->window.toolShowRegionsBounds(e));
        addSeparator();
        addMenuItem(toolRegionDetail, e->window.toolRegionDetail(e));
        
        /*menuBar.add(fileMenu);
        menuBar.add(detectionMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);

        fileMenu.add(fileOpen);
        fileMenu.add(fileSave);
        fileMenu.addSeparator();
        fileMenu.add(fileQuit);

        detectionMenu.add(detectionTest);
        detectionMenu.add(detectionRegions);
        //detectionMenu.add(detectionRegionLabel);
        //detectionMenu.add(detectionComponents);

        toolsMenu.add(toolShowLabels);
        toolsMenu.add(toolRegionsList);
        toolsMenu.add(toolShowRegionsBounds);
        toolsMenu.addSeparator();
        toolsMenu.add(toolRegionDetail);

        helpMenu.add(helpAbout);*/
    }

    private void initializeListeners(){
        /*fileOpen.addActionListener(MainWindow.this::openImage);
        fileSave.addActionListener(MainWindow.this::saveImage);
        detectionTest.addActionListener(MainWindow.this::detectionTest);
        detectionRegions.addActionListener(MainWindow.this::detectRegions);
        //detectionRegionLabel.addActionListener(HlavniOkno.this::detectionScanline);
        //detectionComponents.addActionListener(HlavniOkno.this::detectionComponents);
        toolShowLabels.addActionListener(MainWindow.this::toolShowLabels);
        toolRegionsList.addActionListener(MainWindow.this::toolRegionsList);
        toolShowRegionsBounds.addActionListener(MainWindow.this::toolShowRegionsBounds);
        toolRegionDetail.addActionListener(MainWindow.this::toolRegionDetail);*/
    }
    
    private void addMenu(JMenu menu, String textBundle){
        lastAddedMenu = menu;
        menuBar.add(menu);
        try{
            menu.setText(ResourceBundle.getBundle("texts/ToolBar").getString(textBundle));
        }catch(MissingResourceException e){
            menu.setText(textBundle);
        }
    }
    
    private void addMenu(JMenu menu){
        addMenu(menu,getFieldName(menu));
    }
    
    private void addMenu(JMenu menu, String textBundle, String iconName){
        addMenu(menu, textBundle);
        menu.setIcon(icons.Icons.getIcon(iconName));
    }
    
    private void addMenuItem(JMenuItem item, String textBundle, ActionListener action){
        lastAddedMenu.add(item);
        try{
            item.setText(ResourceBundle.getBundle("texts/ToolBar").getString(textBundle));
        }catch(MissingResourceException e){
            item.setText(textBundle);
        }
        item.addActionListener(action);
    }
    
    private void addMenuItem(JMenuItem item, ActionListener action){
        addMenuItem(item, getFieldName(item), action);
    }
    
    private void addMenuItem(JMenuItem item, String textBundle, String iconName, ActionListener action){
        addMenuItem(item, textBundle, action);
        item.setIcon(icons.Icons.getIcon(iconName));
    }
    
    private void addMenuItem(String iconName, JMenuItem item, ActionListener action){
        addMenuItem(item, getFieldName(item), iconName, action);
    }
    
    private void addSeparator(){
        lastAddedMenu.addSeparator();
    }
    
    String getFieldName(Object field){
        try {
            for(Field f : this.getClass().getFields()) {
                    if(f.get(this) == field) {
                        return f.getName();
                    }
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
        }
        return field.getClass().getName();
    }

    /** odblokuje nabidku detekce */
    public void enableImageActions(boolean b){
        detectionMenu.setEnabled(b);
        fileSave.setEnabled(b);
    }
    public void enableLabelImageActions(boolean b){
        toolShowLabels.setEnabled(b);
        toolRegionsList.setEnabled(b);
        toolShowRegionsBounds.setEnabled(b);
    }
    public void enableRegionListActions(boolean b){
        toolRegionDetail.setEnabled(b);
    }
}
