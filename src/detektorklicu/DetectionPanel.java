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

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JTable;

/**
 *
 * @author Acer
 */
public class DetectionPanel extends javax.swing.JPanel {

    /**
     * Creates new form DetectionPanel
     * @param detection 
     */
    public DetectionPanel(Detection detection, MainWindow parent) {
        this.detection = detection;
        this.canvas = new Canvas(detection.getOriginal());
        this.parent = parent;
        initComponents();
        
        imageScrollPane.setViewportView(canvas);
        tableScrollPane.setVisible(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        splitPane = new javax.swing.JSplitPane();
        tableScrollPane = new javax.swing.JScrollPane();
        regionsTable = new javax.swing.JTable();
        imageScrollPane = new javax.swing.JScrollPane();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        splitPane.setDividerLocation(splitPane.getHeight());
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        tableScrollPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                tableScrollPaneComponentResized(evt);
            }
        });

        regionsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tableScrollPane.setViewportView(regionsTable);

        splitPane.setBottomComponent(tableScrollPane);
        splitPane.setLeftComponent(imageScrollPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        //if(regionsTableHidden) hideTable();
    }//GEN-LAST:event_formComponentResized

    private void tableScrollPaneComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_tableScrollPaneComponentResized
        //tableHidden = false;
        //fillTable();
    }//GEN-LAST:event_tableScrollPaneComponentResized
    
    public void showRegionsTable(boolean b) {
        tableScrollPane.setVisible(b);
        if(b) {
            splitPane.setDividerLocation(getHeight()/4*3);
            parent.runBackgroundProcess("fillTable", this::fillTable);// (0)->{fillTable();});
            //(<any> 0)->{fillTable();}
        }
    }
    
    public void toggleRegionsTable(){
        showRegionsTable(!tableScrollPane.isVisible());
    }
    
    public void fillTable(){
        regionsTable.setModel(detection.getImage().getRegionsTableModel());
        
        regionsTable.getColumnModel().getColumn(0).setWidth(30);
        regionsTable.getColumnModel().getColumn(3).setWidth(60);
    }
    
    public void viewOriginalSize() {
        Dimension d = new Dimension(detection.getOriginal().getWidth(), detection.getOriginal().getHeight());
        canvas.setPreferredSize(d);
        canvas.setSize(d);
    }
    
    public void viewScalledSize() {
        canvas.setPreferredSize(null);
        canvas.setSize(imageScrollPane.getSize());
    }
    
    public void detectRegions() {
        detection.startDetection();
    }
    
    public void showLabels() {
        parent.runBackgroundProcess("showLabels", ()->{
            canvas.setImage(detection.getLabelsImage());
            canvas.repaint();
        });
    }
    
    public void showBackground() {
        parent.runBackgroundProcess("showLabels", ()->{
            canvas.setImage(detection.getBackgroundImage());
            canvas.repaint();
        });
    }
    
    public void showOriginal() {
        canvas.setImage(detection.getOriginal());
        canvas.repaint();
    }
    
    public void showRegions() {
        canvas.displayRegions(detection.getImage().getRegionsPolygons(),Color.blue);
        canvas.repaint();
    }
    
    public void hideRegions() {
        canvas.displayRegions(null,Color.blue);
        canvas.repaint();
    }
    
    public void toggleRegions() {
        if(canvas.showingRegions()) hideRegions();
        else showRegions();
    }
    
    public JTable getRegionsTable() {
        return regionsTable;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane imageScrollPane;
    private javax.swing.JTable regionsTable;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JScrollPane tableScrollPane;
    // End of variables declaration//GEN-END:variables

    private Detection detection;
    private Canvas canvas;
    private MainWindow parent;
}
