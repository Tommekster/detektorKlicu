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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.beans.PropertyChangeEvent;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.TableModel;

/**
 *
 * @author Acer
 */
public class DetectionPanel extends javax.swing.JPanel implements MainWindow.ClosableTab{

    /**
     * Creates new form DetectionPanel
     * @param detection 
     * @param parent 
     */
    public DetectionPanel(Detection detection, MainWindow parent) {
        this.detection = detection;
        this.canvas = new Canvas(detection.getOriginal());
        initComponents();
        
        CanvasMouseListener canvasMouseListener = new CanvasMouseListener();
        this.canvas.addMouseWheelListener(canvasMouseListener);
        this.canvas.addMouseListener(canvasMouseListener);
        this.canvas.addMouseMotionListener(canvasMouseListener);
        this.canvas.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        setName(detection.getFilename());
        imageScrollPane.setViewportView(canvas);
        tableScrollPane.setVisible(false);
        progressPanel.setVisible(false);
        
        detection.getImage().setWorker(worker);
        worker.addPropertyChangeListener(this::workerChangeListener);
        
        regionsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                Region region = getSelectedRegion();
                if(region == null) return;
                canvas.displayRegions(region.getBoundingRectangle(),Color.green);
                canvas.repaint();
            }
        });
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
        progressPanel = new javax.swing.JPanel();
        progressBar = new javax.swing.JProgressBar();
        cancelBtn = new javax.swing.JToggleButton();

        splitPane.setDividerLocation(splitPane.getHeight());
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

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
        regionsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                regionsTableMouseClicked(evt);
            }
        });
        tableScrollPane.setViewportView(regionsTable);

        splitPane.setBottomComponent(tableScrollPane);
        splitPane.setLeftComponent(imageScrollPane);

        progressBar.setMaximum(1000);
        progressBar.setToolTipText("");
        progressBar.setMinimumSize(new java.awt.Dimension(100, 25));
        progressBar.setString("backgroundOperation");
        progressBar.setStringPainted(true);

        cancelBtn.setText("cancelBtn");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout progressPanelLayout = new javax.swing.GroupLayout(progressPanel);
        progressPanel.setLayout(progressPanelLayout);
        progressPanelLayout.setHorizontalGroup(
            progressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(progressPanelLayout.createSequentialGroup()
                .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelBtn))
        );
        progressPanelLayout.setVerticalGroup(
            progressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 25, Short.MAX_VALUE)
            .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 513, Short.MAX_VALUE)
            .addComponent(progressPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(progressPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void regionsTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_regionsTableMouseClicked
        if(evt.getClickCount() >= 2) showRegionDetail();
    }//GEN-LAST:event_regionsTableMouseClicked

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        worker.cancelJob();
    }//GEN-LAST:event_cancelBtnActionPerformed
    
    private void workerChangeListener(PropertyChangeEvent e){
        if("state".equals(e.getPropertyName())){
            progressPanel.setVisible(e.getNewValue() == SwingWorker.StateValue.STARTED);
        }
        else if("progressName".equals(e.getPropertyName())){
            progressBar.setString(worker.getProgressText((String)e.getNewValue()));
        }
        else if("progressValue".equals(e.getPropertyName())){
            progressBar.setValue((int)e.getNewValue());
        }
    }
    
    @Override
    public boolean onClosing(Component parent){
        int option;
        try{
            option = JOptionPane.showConfirmDialog(parent, 
                        ResourceBundle.getBundle("texts/DetecionPanel").getString("saveDlgMsg").replace("{{filename}}", getName()), // message
                        ResourceBundle.getBundle("texts/DetecionPanel").getString("saveDlg"), // title
                        JOptionPane.YES_NO_CANCEL_OPTION);
        }catch(MissingResourceException ex){
            option = JOptionPane.showConfirmDialog(parent, 
                        "Do you want to save the \"{{fileName}}\" detecion?".replace("{{filename}}", getName()), // message
                        "Save", // title
                        JOptionPane.YES_NO_CANCEL_OPTION);
        }
        return option == 1; /* 0 = Yes, 1 = No, 2 = Cancel, -1 = close */
    }
    
    public void showRegionsTable(boolean b) {
        tableScrollPane.setVisible(b);
        if(b) {
            splitPane.setDividerLocation(getHeight()/4*3);
            worker.runInBackground(this::fillTable);// (0)->{fillTable();});
            //(<any> 0)->{fillTable();}
        }
    }
    
    public void toggleRegionsTable(){
        showRegionsTable(!isRegionsTableShown());
    }
    
    private Region getSelectedRegion(){
        int row = regionsTable.getSelectedRow();
        if(row >= 0){
            TableModel model = regionsTable.getModel();
            if(model instanceof RegionsTableModel){
                return ((RegionsTableModel) model).getRegionAt(row);
            }
        }
        return null;
    }
    
    public boolean isSelectedRegion(){
        return false;
    }

    public boolean isRegionsTableShown() {
        return tableScrollPane.isVisible();
    }
    
    private void fillTable(){
        regionsTable.setModel(detection.getImage().getRegionsTableModel());

        regionsTable.getColumnModel().getColumn(0).setWidth(30);
        regionsTable.getColumnModel().getColumn(3).setWidth(60);
    }
    
    public void setZoom(int size) {
        if(size > 0) {
            Dimension d = new Dimension(detection.getOriginal().getWidth()*size/100, detection.getOriginal().getHeight()*size/100);
            canvas.setPreferredSize(d);
            canvas.setSize(d);
            zoomSize = size;
        } else {
            canvas.setPreferredSize(null);
            canvas.setSize(imageScrollPane.getSize());
            zoomSize = -1;
        }
    }
    
    public void viewScalledSize() {
        setZoom(-1);
    }
    
    /*public void detectRegions() {
        detection.startDetection();
    }*/
    
    public void showLabels() {
        worker.runInBackground(()->{
            canvas.setImage(detection.getLabelsImage());
            canvas.repaint();
        });
    }
    
    public void showBackground() {
        worker.runInBackground(()->{
            canvas.setImage(detection.getBackgroundImage());
            canvas.repaint();
        });
    }
    
    public void showOriginal() {
        canvas.setImage(detection.getOriginal());
        canvas.repaint();
    }
    
    public void showRegions() {
        worker.runInBackground(()->{
            canvas.displayRegions(detection.getImage().getRegionsPolygons(),Color.blue);
            canvas.repaint();
        });
    }
    
    public void hideRegions() {
        canvas.hideRegions();
        canvas.repaint();
    }
    
    public void toggleRegions() {
        if(canvas.showingRegions()) hideRegions();
        else showRegions();
    }
    
    public void showRegionDetail() {
        Region region = getSelectedRegion();
        if(region == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("thetha=").append(region.getOrientation()).append("\n")
                .append("a=").append(region.getHalfAxisA()).append("\n")
                .append("b=").append(region.getHalfAxisB()).append("\n");
        JOptionPane.showMessageDialog(this, sb.toString(), "Detail", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void resetDetection() {
        worker.runInBackground(()->{
            hideRegions();
            showOriginal();
            detection.resetDetection();
        });
    }
    
    private JTable getRegionsTable() {
        return regionsTable;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton cancelBtn;
    private javax.swing.JScrollPane imageScrollPane;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JPanel progressPanel;
    private javax.swing.JTable regionsTable;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JScrollPane tableScrollPane;
    // End of variables declaration//GEN-END:variables

    private Detection detection;
    private Canvas canvas;
    private QueuedWorker worker = new QueuedWorker();
    private int zoomSize = -1;
    
    class CanvasMouseListener extends MouseInputAdapter{
        Rectangle currentRect;
        Point origin;
        /*
        @Override
        public void mouseMoved(MouseEvent e){

        }
        @Override
        public void mouseDragged(MouseEvent e){

        }*/
        public void mousePressed(MouseEvent e) {
            origin = new Point(e.getPoint());
            
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if(origin != null){
                //JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, canvas);
                JViewport viewPort = imageScrollPane.getViewport();
                if (viewPort != null) {
                    int deltaX = origin.x - e.getX();
                    int deltaY = origin.y - e.getY();

                    Rectangle view = viewPort.getViewRect();
                    view.x += deltaX;
                    view.y += deltaY;

                    canvas.scrollRectToVisible(view);
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }
        
        @Override
        public void mouseWheelMoved(MouseWheelEvent e){
            if(e.isControlDown()){ // zoom
                int newSize = zoomSize + e.getWheelRotation()*5;
                if(newSize > 600) newSize = 600;
                if(newSize < 10) newSize = 10;
                setZoom(newSize);
            }
        }
    }
}
