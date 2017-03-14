/*
 * The MIT License
 *
 * Copyright 2016 zikmuto2.
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
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Hlavni okno
 * Hlavni okno programu Deterktor klicu
 * @author zikmuto2
 */
public class MainWindow extends JFrame{
    private static final Lokalizator l = Lokalizator.getLokalizator();
    private final MainMenu mainMenu = new MainMenu(this);
    private final JTabbedPane tabsPane = new JTabbedPane();
    
    //private Canvas canvas = null; //new Canvas();
    private Process process = null;
    //private WorkerDialog workerDialog;
    
    public MainWindow(){
        super();
        super.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        initialization();
    }
    
    /** opens an image for detection */
    public void fileNew(ActionEvent e){
        // TODO
        openImage(e);
    }
    
    /** opens saved detection file */
    public void fileOpen(ActionEvent e){
        // TODO
        openImage(e);
    }
    
    /** saves detection file */
    public void fileSave(ActionEvent e){
        // TODO
        saveImage(e);
    }
    
    /** exports source image */
    public void fileExportOriginal(ActionEvent e){
        // TODO
    }
    
    /** exports labels */
    public void fileExportLabels(ActionEvent e){
        // TODO
    }
    
    /** exports regions list */
    public void fileExportRegionsList(ActionEvent e){
        // TODO
    }
    
    /** exports regions list */
    public void fileQuit(ActionEvent e){
        closeProgram();
    }
    
    public void viewOriginalSize(ActionEvent e) {
        DetectionPanel detPane = getActiveDetectionPane();
        if(detPane == null) return;
        detPane.viewOriginalSize();
    }
    
    public void viewScalled(ActionEvent e) {
        DetectionPanel detPane = getActiveDetectionPane();
        if(detPane == null) return;
        detPane.viewScalledSize();
    }
    
    public void toolShowOriginal(ActionEvent e) {
        DetectionPanel detPane = getActiveDetectionPane();
        if(detPane == null) return;
        detPane.showOriginal();
    }
    
    public void toolResetDetection(ActionEvent e) {
        DetectionPanel detPane = getActiveDetectionPane();
        if(detPane == null) return;
        detPane.resetDetection();
    }
    
    public void toolShowBackground(ActionEvent e) {
        DetectionPanel detPane = getActiveDetectionPane();
        if(detPane == null) return;
        detPane.showBackground();
    }
    
    public void toolShowLabels(ActionEvent e){
        DetectionPanel detPane = getActiveDetectionPane();
        if(detPane == null) return;
        detPane.showLabels();
    }
    
    public void toolRegionsList(ActionEvent e) {
        DetectionPanel detPane = getActiveDetectionPane();
        if(detPane == null) return;
        detPane.toggleRegionsTable();
    }
    
    public void toolShowRegionsBounds(ActionEvent e){
        DetectionPanel detPane = getActiveDetectionPane();
        if(detPane == null) return;
        detPane.toggleRegions();
    }
    
    public void toolRegionDetail(ActionEvent e) {
        DetectionPanel detPane = getActiveDetectionPane();
        if(detPane == null) return;
        detPane.showRegionDetail();
    }
    
    public void helpAbout(ActionEvent e){
        // TODO
    }
    
    private void newDetection(File file){
        try {
            DetectionPanel detectionPane = new DetectionPanel(Detection.newFromFile(file),this);
            tabsPane.add(file.getName(), detectionPane);
            //detectionPane.detectRegions();
        } catch (ExceptionMessage ex) {
            ex.displayMessage(this/*.workerDialog*/);
        }
    }

    /** Inicializace okna 
     * nastavi rozmery, titulku a eventy oknu
     */
    private void initialization() {
        int height = 450;
        int width = 600;
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screen.width - width)/2, (screen.height - height)/2, width, height);
        setTitle(l.tr("mainWindowTitle"));
        add(tabsPane);
        checkPossibleActions();
        
        tabsPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e){
                tabsMouseEvent(e);
            }
        });
        //tabsPane.addTab(l.tr("sourceImage"), canvas);
        tabsPane.addChangeListener(this::tabsChange);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e){
                closeProgram();
            }
        });
    }
    
    private Component getActiveComponent(){
        return tabsPane.getSelectedComponent();
    }
    
    private boolean activeIsDetectionPanel(){
        return getActiveComponent() instanceof DetectionPanel;
    }
    
    private DetectionPanel getActiveDetectionPane(){
        if(!activeIsDetectionPanel()) return null;
        return (DetectionPanel) getActiveComponent();
    }
    
    private void checkPossibleActions(){
        mainMenu.enableImageActions(activeIsDetectionPanel());
        DetectionPanel detPane = getActiveDetectionPane();
        mainMenu.enableRegionsActions((detPane == null)?false:detPane.isRegionsTableShown());
    }
    
    /** raise the event if active tabs is changed */
    private void tabsChange(ChangeEvent e){
        if(e.getSource() != tabsPane) return;
        checkPossibleActions();
    }
    
    private void closeProgram(){
        System.exit(0);
    }
    
    /** closes active tab after triple right-click
     * @param e MouseEvent further info about layer
     */
    private void tabsMouseEvent(MouseEvent e){
        if(e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() > 2 
                && e.getComponent() instanceof JTabbedPane){
            tabsPane.remove(tabsPane.getSelectedIndex());
        }
    }
    /** Otevri soubor 
     * Umozni vybrat obrazek k detekci klicu
     * @param e ActionEvent podrobnosti o události
     */
    private void openImage(ActionEvent e) {
        final FileFilter filter = new FileNameExtensionFilter(l.tr("otevritSouborJPEGType"), "jpg", "jpeg");
        JFileChooser fileChooser = new JFileChooser(".");
        fileChooser.setDialogTitle(l.tr("otevritSouborTitle"));
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.addChoosableFileFilter(filter);
        if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            File selectedFile = new File(fileChooser.getCurrentDirectory()
                    .getAbsolutePath(), fileChooser.getSelectedFile().getName());
            newDetection(selectedFile);
        }
    }
    private void saveImage(ActionEvent ae){
        final FileNameExtensionFilter filtrBMP = 
                new FileNameExtensionFilter(l.tr("saveFileBMPType"), "bmp");
        final FileNameExtensionFilter filtrPNG = 
                new FileNameExtensionFilter(l.tr("saveFilePNGType"), "png");
        final JFileChooser jfc = new JFileChooser();
        jfc.setCurrentDirectory(new File("."));
        jfc.setDialogTitle(l.tr("saveFileTitle"));
        jfc.setDialogType(JFileChooser.SAVE_DIALOG);
        jfc.setSelectedFile(new File(l.tr("saveFileName")+".png"));
        jfc.setFileFilter(filtrPNG);
        jfc.addChoosableFileFilter(filtrBMP);
        jfc.addPropertyChangeListener((pce)->{
            onExtensionChanged(pce,jfc,filtrPNG,filtrBMP);
        });
        if(jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
            String format = "PNG";
            if(jfc.getFileFilter().equals(filtrBMP)) format = "BMP";
             // TODO   
           /* try{
                File vybranySoubor = new File(jfc.getCurrentDirectory()
                        .getAbsolutePath(), jfc.getSelectedFile().getName());
                //ImageIO.write(getImage(), format, vybranySoubor);
            }catch(IOException ex){
                JOptionPane.showMessageDialog(jfc, 
                            l.tr("saveFileError")+"\n"+ex.toString(), 
                            l.tr("saveFileErrorTitle"), 
                            JOptionPane.ERROR_MESSAGE);
            }*/
        }
    }
    
    private void onExtensionChanged(PropertyChangeEvent pce, JFileChooser jfc, FileNameExtensionFilter fpng, FileNameExtensionFilter fbmp){
        if(pce.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)){
            String extension;
            if(jfc.getFileFilter().equals(fpng)) extension = ".png";
            else if(jfc.getFileFilter().equals(fbmp)) extension = ".bmp";
            else return;
            
            File selectedFile = jfc.getSelectedFile();
            if(selectedFile == null) 
                jfc.setSelectedFile(new File(l.tr("saveFileName") + extension));
            else{
                String name = jfc.getSelectedFile().getName();
                int i = name.lastIndexOf(".");
                name = name.substring(0,i) + extension;
                jfc.setSelectedFile(new File(name));
            }
                
        }
    }
    
    /** setter pro obrazek do kresliciho panelu
     * @param obrazek obrazek k nakresleni
     */
    private void addImage(BufferedImage obrazek){
        //canvas.setImage(obrazek);
        tabsPane.addTab(l.tr("sourceImage"), new Canvas(obrazek));
        checkPossibleActions();
    }
    
    void runBackgroundProcess(String textBundle, ProcessIface action){
        process = new Process(textBundle) {

            @Override
            public void action() {
                try {
                    action.action();
                } catch (ExceptionMessage ex) {
                    ex.displayMessage(this.workerDialog);
                }
            }
        };
        process.execute();
    }
    
    abstract class Process extends SwingWorker<Void, Void>{
        abstract public void action();
        WorkerDialog workerDialog;
        
        public Process(String textBundle){
            workerDialog = new WorkerDialog(MainWindow.this,textBundle) {
                @Override
                public void cancelJob() {
                    cancel(true);
                    workerDialog.setVisible(false);
                }
            };
            addPropertyChangeListener(this::propertyChanged);
        }
        
        void propertyChanged(PropertyChangeEvent evt){
            System.out.println(evt.getPropertyName());
            if("state".equals(evt.getPropertyName()))
                System.out.println(getState().toString());
        }
        
        @Override
        protected Void doInBackground() throws Exception {
            workerDialog.setVisible(true);
            action();
            return null;
        }
        
        @Override
        public void done(){
            MainWindow.this.repaint();
            MainWindow.this.checkPossibleActions();
            workerDialog.setVisible(false);
        }
    }
    
    static abstract interface ProcessIface{
        public abstract void action() throws ExceptionMessage;
    }
}
