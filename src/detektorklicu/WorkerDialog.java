/*
 * The MIT License
 *
 * Copyright 2017 zikmuto2.
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
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.attribute.PosixFilePermission;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author zikmuto2
 */
public abstract class WorkerDialog extends JDialog{
    public WorkerDialog(JFrame owner,String textBundle){
        super(owner, "in process...", /*modal*/ false);
        this.owner = owner;
        initialization();
        setTexts(textBundle);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e){
                cancelJob();
            }
        });
    }

    private void initialization() {
        if(owner != null){
            Rectangle r = owner.getBounds();
            setBounds(r.x+(r.width-200)/2, r.y+(r.height-140)/2, 200, 140);
        }else{
            setBounds(100,100,200,140);
        }
        setPreferredSize(new Dimension(200, 140));
        setResizable(false);
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        add(lblMessage);
        add(btnCancel);
        btnCancel.addActionListener(e->cancelJob());
    }
    
    private void setTexts(String textBundle){
        String title;
        String message;
        try{
            title = ResourceBundle.getBundle("texts/WorkerDialog").getString(textBundle);
            message = ResourceBundle.getBundle("texts/WorkerDialog").getString(textBundle+"Msg");
        }catch(MissingResourceException e){
            title = textBundle;
            message = textBundle;
        }
        setTitle(title);
        lblMessage.setText(message);
        
        try{
            btnCancel.setText(ResourceBundle.getBundle("texts/WorkerDialog").getString("cancelButtonLabel"));
        }catch(MissingResourceException e){
            btnCancel.setText("Cancel");
        }
    }
    
    public abstract void cancelJob();
    
    JFrame owner = null;
    private final JLabel lblMessage = new JLabel(); //l.tr("workerLabel"));
    private final JButton btnCancel = new JButton();
}
