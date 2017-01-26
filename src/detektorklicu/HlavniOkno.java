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
import java.awt.event.FocusAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;

/** Hlavni okno
 * Hlavni okno programu Deterktor klicu
 * @author zikmuto2
 */
public class HlavniOkno extends JFrame{
    private static final Lokalizator l = Lokalizator.getLokalizator();
    private final MainMenu mainMenu = new MainMenu();
    private final JTabbedPane tabsPane = new JTabbedPane();
    
    //private Canvas canvas = null; //new Canvas();
    private Process process = null;
    //private WorkerDialog workerDialog;
    
    public HlavniOkno(){
        super();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        inicializace();
    }

    /** Inicializace okna 
     * nastavi rozmery, titulku a eventy oknu
     */
    private void inicializace() {
        int vyska = 300;
        int sirka = 400;
        Dimension obrazovka = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((obrazovka.width - sirka)/2, (obrazovka.height - vyska)/2, sirka, vyska);
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
    
    private void detectRegions(ActionEvent e){
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
    
    private void toolShowLabels(ActionEvent e){
        LabelImage image = (LabelImage)getImage();
        addImage(image.getLabelsImage(LabelImage.getPallete()));
    }
    
    private void toolRegionsList(ActionEvent e) {
        LabelImage image = (LabelImage)getImage();
        process = new Process(l.tr("evalToolRegionsList")) {

            @Override
            public void action() {
                addRegionsTable(image.getRegions());
            }
        };
        process.execute();
    }
    
    private void toolShowRegionsBounds(ActionEvent e){
        LabelImage image = (LabelImage) getImage();
        process = new Process(l.tr("evalToolShowRegionsBounds")) {

            @Override
            public void action() {
                List<Region> regions = image.getRegions();
                regions.parallelStream().forEach(region->{
                    region.drawBoundingRectangle(Color.blue);
                    HlavniOkno.this.repaint();
                });
            }
        };
        process.execute();
    }
    
    private void toolRegionDetail(ActionEvent e) {
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
            workerDialog = new WorkerDialog(HlavniOkno.this,title) {
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
            HlavniOkno.this.repaint();
            HlavniOkno.this.checkPossibleActions();
            workerDialog.setVisible(false);
        }
    }
    
    /** Hlavni nabidka
     * vnorena trida generujici a obsluhujici hlavni nabidku okna
     */
    class MainMenu{
        /** Reference na rodice hlavni nabidky */
        HlavniOkno okno;
        
        private final JMenuBar menuBar = new JMenuBar();
        
        private final JMenu souborMenu = new JMenu(l.tr("souborMenu"));
        private final JMenuItem souborOtevrit = new JMenuItem(l.tr("souborOtevrit"));
        private final JMenuItem souborUlozit = new JMenuItem(l.tr("souborUlozit"));
        private final JMenuItem souborKonec = new JMenuItem(l.tr("souborKonec"));
        
        private final JMenu detectionMenu = new JMenu(l.tr("detectionMenu"));
        private final JMenuItem detectionTest = new JMenuItem(l.tr("detectionTest"));
        private final JMenuItem detectionRegions = new JMenuItem(l.tr("detectionRegions"));
        //private final JMenuItem detectionRegionLabel = new JMenuItem(l.tr("detectionRegionLabel"));
        //private final JMenuItem detectionComponents = new JMenuItem(l.tr("detectionComponents"));
        
        private final JMenu toolsMenu = new JMenu(l.tr("toolsMenu"));
        private final JMenuItem toolShowLabels = new JMenuItem(l.tr("toolShowLabels"));
        private final JMenuItem toolRegionsList = new JMenuItem(l.tr("toolRegionsList"));
        private final JMenuItem toolShowRegionsBounds = new JMenuItem(l.tr("toolShowRegionsBounds"));
        private final JMenuItem toolRegionDetail = new JMenuItem(l.tr("toolRegionDetail"));
        
        private final JMenu napovedaMenu = new JMenu(l.tr("napovedaMenu"));
        private final JMenuItem napovedaAbout = new JMenuItem(l.tr("napovedaAbout"));
        
        /** Konstruktor hlavni nabidky
         * vezme si okdkaz na okno a inicializuje obsah hlavni nabidky
         * @param o odkaz na hlavni okno
         */
        public MainMenu(){
            okno = HlavniOkno.this;
            this.inicializace();
            this.inicializujListenery();
        }

        /** Propoji polozky menu s nabidkou a vlozi ji do okna */
        private void inicializace() {
            okno.setJMenuBar(menuBar);
            
            menuBar.add(souborMenu);
            menuBar.add(detectionMenu);
            menuBar.add(toolsMenu);
            menuBar.add(napovedaMenu);
            
            souborMenu.add(souborOtevrit);
            souborMenu.add(souborUlozit);
            souborMenu.addSeparator();
            souborMenu.add(souborKonec);
            
            detectionMenu.add(detectionTest);
            detectionMenu.add(detectionRegions);
            //detectionMenu.add(detectionRegionLabel);
            //detectionMenu.add(detectionComponents);
            
            toolsMenu.add(toolShowLabels);
            toolsMenu.add(toolRegionsList);
            toolsMenu.add(toolShowRegionsBounds);
            toolsMenu.addSeparator();
            toolsMenu.add(toolRegionDetail);
            
            napovedaMenu.add(napovedaAbout);
        }
        
        private void inicializujListenery(){
            souborOtevrit.addActionListener(HlavniOkno.this::openImage);
            souborUlozit.addActionListener(HlavniOkno.this::saveImage);
            detectionTest.addActionListener(HlavniOkno.this::detectionTest);
            detectionRegions.addActionListener(HlavniOkno.this::detectRegions);
            //detectionRegionLabel.addActionListener(HlavniOkno.this::detectionScanline);
            //detectionComponents.addActionListener(HlavniOkno.this::detectionComponents);
            toolShowLabels.addActionListener(HlavniOkno.this::toolShowLabels);
            toolRegionsList.addActionListener(HlavniOkno.this::toolRegionsList);
            toolShowRegionsBounds.addActionListener(HlavniOkno.this::toolShowRegionsBounds);
            toolRegionDetail.addActionListener(HlavniOkno.this::toolRegionDetail);
        }
        
        /** odblokuje nabidku detekce */
        public void enableImageActions(boolean b){
            detectionMenu.setEnabled(b);
            souborUlozit.setEnabled(b);
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
}
