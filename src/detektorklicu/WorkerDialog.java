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
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author zikmuto2
 */
public abstract class WorkerDialog extends JDialog{
    public WorkerDialog(JFrame owner,String title){
        super(owner, l.tr("workerTitle")+title, /*modal*/ false);
        this.owner = owner;
        initialization();
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
        }else setBounds(100,100,200,140);
        //setPreferredSize(new Dimension(300, 200));
        setResizable(false);
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        add(lblCalculation);
        add(btnCancel);
        //lblCalculation.setBounds(10, 10, WIDTH, WIDTH);
        btnCancel.addActionListener(e->cancelJob());
    }
    
    public abstract void cancelJob();
    
    JFrame owner = null;
    private static final Lokalizator l = Lokalizator.getLokalizator();
    private final JLabel lblCalculation = new JLabel(l.tr("workerLabel"));
    private final JButton btnCancel = new JButton(l.tr("workerButton"));
}
