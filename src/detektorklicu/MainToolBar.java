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
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 * 
 * @author Acer
 */

    
class MainToolBar{
    MainWindow window;

    private final JToolBar toolBar = new JToolBar();
    private final JButton btnOpen = new JButton();

    public MainToolBar(MainWindow win){
        window = win;
        initialization();
    }
    
    private void initialization(){
        toolBar.setFloatable(false);

    }

    private void addButton(JButton btn, String iconName, String textBundle, ActionListener action){
        btn.setIcon(icons.Icons.getIcon(iconName));
        try{
            btn.setToolTipText(ResourceBundle.getBundle("texts/MainToolBar").getString(textBundle));
        }catch(MissingResourceException e){
            btn.setToolTipText(textBundle);
        }
        btn.addActionListener(action);
        toolBar.add(btn);
    }
    private void addSeparator(){
        toolBar.addSeparator();
    }
}
