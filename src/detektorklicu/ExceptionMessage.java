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

import java.awt.Component;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;

/**
 *
 * @author Acer
 */
public class ExceptionMessage extends Exception{
    Exception originalException;
    String textBundle;
    
    public ExceptionMessage(String textBundle, Exception ex){
        this.textBundle = textBundle;
        originalException = ex;
    }

    ExceptionMessage(String textBundle) {
        this(textBundle,null);
    }
    
    public void displayMessage(Component parent) {
        try{
            JOptionPane.showMessageDialog(parent, 
                        ResourceBundle.getBundle("texts/Errors").getString(textBundle+"Msg"), // message
                        ResourceBundle.getBundle("texts/Errors").getString(textBundle), // title
                        JOptionPane.ERROR_MESSAGE, icons.Icons.getIcon("error.png"));
        }catch(MissingResourceException ex){
            JOptionPane.showMessageDialog(parent, 
                        textBundle, // message
                        "Error", // title
                        JOptionPane.ERROR_MESSAGE, 
                        icons.Icons.getIcon("error.png"));
        }
    }
}
