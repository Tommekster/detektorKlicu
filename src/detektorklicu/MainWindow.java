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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;

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
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        initialization();
    }
    
    /** opens an image for detection */
    public void fileNew(ActionEvent e){
        // TODO
    }
    
    /** opens saved detection file */
    public void fileOpen(ActionEvent e){
        // TODO
    }
    
    /** saves detection file */
    public void fileSave(ActionEvent e){
        // TODO
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
        // TODO
    }
    
    public void detectRegions(ActionEvent e){
        Canvas canvas = getCanvas();
        process = new Process(l.tr("evalDetectRegions")) {

            @Override
            public void action() {
                // Imabe should be label image with separated background
                // if it is not then we separate the background
                if(!(canvas.getImage() instanceof LabelImage)) {
                    canvas.setImage(ImageProcessing.floodFillBackground(canvas.getImage(), Color.red));
                }
                
                // background is separated, regions can be denoted
                ((LabelImage)canvas.getImage()).detectRegions();
                
            }
        };
        process.execute();
    }
    
    
    public void toolShowLabels(ActionEvent e){
        LabelImage image = (LabelImage)getImage();
        addImage(image.getLabelsImage(LabelImage.getPallete()));
    }
    
    public void toolRegionsList(ActionEvent e) {
        LabelImage image = (LabelImage)getImage();
        process = new Process(l.tr("evalToolRegionsList")) {

            @Override
            public void action() {
                addRegionsTable(image.getRegions());
            }
        };
        process.execute();
    }
    
    public void toolShowRegionsBounds(ActionEvent e){
        LabelImage image = (LabelImage) getImage();
        process = new Process(l.tr("evalToolShowRegionsBounds")) {

            @Override
            public void action() {
                List<Region> regions = image.getRegions();
                regions.parallelStream().forEach(region->{
                    region.drawBoundingRectangle(Color.blue);
                    MainWindow.this.repaint();
                });
            }
        };
        process.execute();
    }
    
    public void toolRegionDetail(ActionEvent e) {
        Component c = tabsPane.getSelectedComponent();
        if(c instanceof JScrollPane){
           Component table = ((JScrollPane)c).getViewport().getView();
           if(table instanceof JTable) {
               int row = ((JTable) table).getSelectedRow();
               if(row >= 0){
                   TableModel model = ((JTable) table).getModel();
                   if(model instanceof RegionsTableModel){
                       Region region = ((RegionsTableModel) model).getRegionAt(row);
                       if(!region.hasEllipse()) region.findBoundingEllipse();
                       StringBuilder sb = new StringBuilder();
                       sb.append("thetha=").append(region.getOrientation()).append("\n")
                               .append("a=").append(region.getHalfAxisA()).append("\n")
                               .append("b=").append(region.getHalfAxisB()).append("\n");
                       JOptionPane.showMessageDialog(this, sb.toString(), "Detail", JOptionPane.INFORMATION_MESSAGE);
                       region.drawBoundingRectangle(Color.blue);
                   }
               }
           }
        }
    }
    
    public void helpAbout(ActionEvent e){
        // TODO
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
                System.exit(0);
            }
        });
    }
    
    private Component getActiveComponent(){
        return tabsPane.getSelectedComponent();
    }
    private boolean activeIsRegionList(){
        Component c = getActiveComponent();
        return (c instanceof JTable 
                || (c instanceof JScrollPane 
                && ((JScrollPane)c).getViewport().getView() instanceof JTable));
    }
    private JTable getRegionList(){
        return (JTable)((JScrollPane)getActiveComponent()).getViewport().getView();
    }
    private boolean activeIsCanvas(){
        Component c = getActiveComponent();
        return c instanceof Canvas;
    }
    private boolean activeIsLabelledImage(){
        Component c = getActiveComponent();
        return activeIsCanvas() && ((Canvas)c).getImage() instanceof LabelImage;
    }
    private Canvas getCanvas(){
        return (Canvas)getActiveComponent();
    }
    private BufferedImage getImage(){
        return ((Canvas)getActiveComponent()).getImage();
    }
    
    private void checkPossibleActions(){
        mainMenu.enableImageActions(activeIsCanvas());
        mainMenu.enableLabelImageActions(activeIsLabelledImage());
        mainMenu.enableRegionListActions(activeIsRegionList());
    }
    
    /** raise the event if active tabs is changed */
    private void tabsChange(ChangeEvent e){
        if(e.getSource() != tabsPane) return;
        checkPossibleActions();
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
            try {
                BufferedImage image = ImageIO.read(selectedFile);
                image = ImageProcessing.gray2RGB(image);
                addImage(image);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(fileChooser, 
                        l.tr("otevritSouborChyba")+"\n"+ex.toString(), 
                        l.tr("otevritSouborChybaTitle"), 
                        JOptionPane.ERROR_MESSAGE);
            }
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
                
            try{
                File vybranySoubor = new File(jfc.getCurrentDirectory()
                        .getAbsolutePath(), jfc.getSelectedFile().getName());
                ImageIO.write(getImage(), format, vybranySoubor);
            }catch(IOException ex){
                JOptionPane.showMessageDialog(jfc, 
                            l.tr("saveFileError")+"\n"+ex.toString(), 
                            l.tr("saveFileErrorTitle"), 
                            JOptionPane.ERROR_MESSAGE);
            }
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
    
    private void detectionTest(ActionEvent e){
        List<Color> colors = new LinkedList<>();
        colors.add(Color.black);
        colors.add(Color.cyan);
        // colors.add(Color.darkGray);
        colors.add(Color.gray);
        colors.add(Color.green);
        colors.add(Color.yellow);
        colors.add(Color.lightGray);
        colors.add(Color.magenta);
        colors.add(Color.orange);
        colors.add(Color.pink);
        colors.add(Color.red);
        byte [] reds = new byte [colors.size()];
        byte [] greens = new byte [colors.size()];
        byte [] blues = new byte[colors.size()];
        for(int j = 0; j < colors.size(); j++){
            reds[j] = (byte)colors.get(j).getRed();
            greens[j] = (byte)colors.get(j).getGreen();
            blues[j] = (byte)colors.get(j).getBlue();
        }
        
        IndexColorModel cm = new IndexColorModel(4, reds.length, reds, greens, blues);
        BufferedImage pic = new BufferedImage(5, 5, BufferedImage.TYPE_BYTE_INDEXED, cm);
        colors.add(Color.white);
        colors.add(Color.blue);
        for(int j = 0; j < pic.getWidth(); j++){
            for(int i = 0; i < pic.getHeight(); i++){
                pic.setRGB(j, i, colors.get(j%colors.size()).getRGB());
                //System.out.println("["+i+","+j+"]");
            }
        }
        addImage(pic);
        //canvas.setImage(ImageProcessing.gray2RGB(canvas.getImage()));
        //ImageProcessing.floodFillBackground(canvas.getImage(), Color.red);
        repaint();
    }
    
    /** setter pro obrazek do kresliciho panelu
     * @param obrazek obrazek k nakresleni
     */
    private void addImage(BufferedImage obrazek){
        //canvas.setImage(obrazek);
        tabsPane.addTab(l.tr("sourceImage"), new Canvas(obrazek));
        checkPossibleActions();
    }
    
    private void addRegionsTable(List<Region> regions){
        JTable table = new JTable(new RegionsTableModel(regions));
        //table.getColorModel().getColorSpace()
        //table.getColumnModel().getColumn(WIDTH)
        tabsPane.add(l.tr("regionsTable"), new JScrollPane(table));
        table.getColumnModel().getColumn(0).setWidth(30);
        table.getColumnModel().getColumn(3).setWidth(60);
    }
    
    abstract class Process extends SwingWorker<Void, Void>{
        abstract public void action();
        WorkerDialog workerDialog;
        
        public Process(String title){
            workerDialog = new WorkerDialog(MainWindow.this,title) {
                @Override
                public void cancelJob() {
                    cancel(true);
                    workerDialog.setVisible(false);
                }
            };
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
}
