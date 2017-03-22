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
    
    private final JMenu exportMenu = new JMenu();
    private final JMenuItem exportOriginal = new JMenuItem();
    private final JMenuItem exportCurrent = new JMenuItem();
    private final JMenuItem exportRegions = new JMenuItem();

    /*private final JMenu detectionMenu = new JMenu();
    private final JMenuItem detectionTest = new JMenuItem();
    private final JMenuItem detectionRegions = new JMenuItem();*/

    private final JMenu viewMenu = new JMenu();
    private final JMenuItem view25Size = new JMenuItem();
    private final JMenuItem view33Size = new JMenuItem();
    private final JMenuItem view50Size = new JMenuItem();
    private final JMenuItem view100Size = new JMenuItem();
    private final JMenuItem view200Size = new JMenuItem();
    private final JMenuItem view300Size = new JMenuItem();
    private final JMenuItem view400Size = new JMenuItem();
    private final JMenuItem viewScalled = new JMenuItem();
    
    private final JMenu toolsMenu = new JMenu();
    private final JMenuItem toolResetDetection = new JMenuItem();
    private final JMenuItem toolShowOriginal = new JMenuItem();
    private final JMenuItem toolShowBackground = new JMenuItem();
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
        //addMenuItem(fileNew, (e) -> window.fileNew(e));
        addMenuItem("fileOpen16.png",fileOpen, e->window.fileOpen(e));
        addMenuItem("fileSave16.png",fileSave, e->window.fileSave(e));
        addMenuItem("fileExport16.png",exportMenu, null);
        addSeparator();
        addMenuItem("exit16.png",fileQuit,e->window.fileQuit(e));
        
        selectMenu(exportMenu);
        addMenuItem(exportOriginal, e->window.fileExportOriginal(e));
        addMenuItem(exportCurrent, e->window.fileExportCurrent(e));
        addMenuItem(exportRegions, e->window.fileExportRegionsList(e));
        
        //addMenu(detectionMenu);
        //addMenuItem(detectionRegions, e->window.detectRegions(e));
        
        addMenu(viewMenu);
        addMenuItem(view25Size, e->window.viewSetZoomSize(e,25));
        addMenuItem(view33Size, e->window.viewSetZoomSize(e,33));
        addMenuItem(view50Size, e->window.viewSetZoomSize(e,50));
        addSeparator();
        addMenuItem("viewOriginal16.png", view100Size, e->window.viewSetZoomSize(e,100));
        addSeparator();
        addMenuItem(view200Size, e->window.viewSetZoomSize(e,200));
        addMenuItem(view300Size, e->window.viewSetZoomSize(e,300));
        addMenuItem(view400Size, e->window.viewSetZoomSize(e,400));
        addSeparator();
        addMenuItem("viewScalled16.png", viewScalled, e->window.viewScalled(e));
        
        addMenu(toolsMenu);
        addMenuItem("toolResetDetection16.png", toolResetDetection, e->window.toolResetDetection(e));
        addSeparator();
        addMenuItem("toolShowOriginal16.png", toolShowOriginal, e->window.toolShowOriginal(e));
        addMenuItem("toolShowBackground16.png", toolShowBackground, e->window.toolShowBackground(e));
        addMenuItem("toolShowLabels16.png", toolShowLabels, e->window.toolShowLabels(e));
        addMenuItem("toolShowRegionBounds16.png", toolShowRegionsBounds, e->window.toolShowRegionsBounds(e));
        addSeparator();
        addMenuItem("toolRegionsList16.png", toolRegionsList, e->window.toolRegionsList(e));
        //addMenuItem("toolRegionDetail16.png", toolRegionDetail, e->window.toolRegionDetail(e));
        
        
        addMenu(helpMenu);
        addMenuItem("help16.png", helpAbout, e->window.helpAbout(e));
        
    }
    
    private void selectMenu(JMenu menu){
        lastAddedMenu = menu;
    }
    
    private void addMenu(JMenu menu, String textBundle){
        selectMenu(menu);
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
        if(action != null) item.addActionListener(action);
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
            for(Field f : this.getClass().getDeclaredFields()) {
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
        fileSave.setEnabled(b);
        exportMenu.setEnabled(b);
        viewMenu.setEnabled(b);
        toolsMenu.setEnabled(b);
    }
    
    public void enableRegionsActions(boolean b){
        toolRegionDetail.setEnabled(b);
    }
}
