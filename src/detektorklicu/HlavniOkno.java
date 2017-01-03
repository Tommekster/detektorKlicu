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
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Hlavni okno
 * Hlavni okno programu Deterktor klicu
 * @author zikmuto2
 */
public class HlavniOkno extends JFrame{
    private static final Lokalizator l = Lokalizator.getLokalizator();
    private final MainMenu mainMenu = new MainMenu();
    private final JTabbedPane tabsPane = new JTabbedPane();
    
    private Canvas canvas = null; //new Canvas();
    private Process process = null;
    private WorkerDialog workerDialog;
    
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
        
        tabsPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e){
                tabsMouse(e);
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
    
    private void tabsChange(ChangeEvent e){
        if(e.getSource() != tabsPane) return;
        if(tabsPane.getTabCount() > 0){
            if(tabsPane.getSelectedComponent() instanceof Canvas)
            canvas = (Canvas)tabsPane.getSelectedComponent();
        }else{
            canvas = null;
            mainMenu.disableImageActions();
        }
    }
    /**
     * 
     * @param e MouseEvent further info about layer
     */
    private void tabsMouse(MouseEvent e){
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
                ImageIO.write(canvas.getImage(), format, vybranySoubor);
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
    
    private void detectionColorise(ActionEvent e){
        //canvas.setImage(ImageProcessing.gray2RGB(canvas.getImage()));
        //ImageProcessing.floodFillBackground(canvas.getImage(), Color.red);
        repaint();
    }
    private void detekcitonFloodFill4P(ActionEvent e){
        //ImageProcessing.floodFill(canvas.getImage(), 1, 1, Color.red);
        process = new Process(": "+l.tr("detekceFloodFill")) {

            @Override
            public void action() {
                ImageProcessing.floodFillBackground(canvas.getImage(), Color.red);
            }
        };
        process.execute();
    }
    
    private void detectionScanline(ActionEvent e){
        process = new Process(": "+l.tr("detekceScanLine")) {

            @Override
            public void action() {
                ImageProcessing.scanlineFill(canvas.getImage(), 10, 10, Color.red);
            }
        };
        process.execute();
    }
    
    private void detectionErode(ActionEvent e){
        process = new Process(": "+l.tr("detekceErode")) {

            @Override
            public void action() {
                ImageProcessing.erode(canvas.getImage(), Color.red, Color.GREEN);
            }
        };
        process.execute();
    }
    
    private void detectionComponents(ActionEvent e){
        List<ImageComponent> comps;
        process = new Process(": "+l.tr("detekceComponent")) {

            @Override
            public void action() {
                ImageComponent component;
                while(
                        (component = ImageProcessing.SeparatableImage
                                .findOneComponent(canvas.getImage(), 
                                        Color.GREEN, Color.BLUE, Color.RED)) != null) 
                    tabsPane.addTab(l.tr("componentImage"), new Canvas(component));
                /*List<ImageComponent> comps = ImageProcessing.SeparatableImage
                    .separateComponents(canvas.getImage(), Color.GREEN, 
                            Color.BLUE, Color.RED);
                comps.stream()
                    .forEach(c->tabsPane.addTab(l.tr("componentImage"), 
                            new Canvas(c)));*/
            }
        };
        process.execute();
    }
    
    /** setter pro obrazek do kresliciho panelu
     * @param obrazek obrazek k nakresleni
     */
    private void addImage(BufferedImage obrazek){
        //canvas.setImage(obrazek);
        tabsPane.addTab(l.tr("sourceImage"), new Canvas(obrazek));
        canvas = (Canvas)tabsPane.getSelectedComponent();
        mainMenu.enableDetection(canvas.isImageInside());
    }
    
    private void disableCounting(boolean disable){
        mainMenu.enableDetection(!disable);
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
            workerDialog.setVisible(false);
        }
    }
    
    /** Hlavni nabidka
     * vnorena trida generujici a obsluhujici hlavni nabidku okna
     */
    class MainMenu{
        /** Reference na rodice hlavni nabidky */
        HlavniOkno okno;
        
        private final JMenuBar hlavniNabidka = new JMenuBar();
        
        private final JMenu souborMenu = new JMenu(l.tr("souborMenu"));
        private final JMenuItem souborOtevrit = new JMenuItem(l.tr("souborOtevrit"));
        private final JMenuItem souborUlozit = new JMenuItem(l.tr("souborUlozit"));
        private final JMenuItem souborKonec = new JMenuItem(l.tr("souborKonec"));
        
        private final JMenu detekceMenu = new JMenu(l.tr("detekceMenu"));
        private final JMenuItem detekceDobarvi = new JMenuItem(l.tr("detekceDobarvi"));
        private final JMenuItem detekceFloodFill = new JMenuItem(l.tr("detekceFloodFill"));
        private final JMenuItem detekceScanLine = new JMenuItem(l.tr("detekceScanLine"));
        private final JMenuItem detekceErode = new JMenuItem(l.tr("detekceErode"));
        private final JMenuItem detekceComponent = new JMenuItem(l.tr("detekceComponent"));
        
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
            okno.setJMenuBar(hlavniNabidka);
            
            hlavniNabidka.add(souborMenu);
            hlavniNabidka.add(detekceMenu);
            hlavniNabidka.add(napovedaMenu);
            
            souborMenu.add(souborOtevrit);
            souborMenu.add(souborUlozit);
            souborMenu.addSeparator();
            souborMenu.add(souborKonec);
            
            detekceMenu.add(detekceDobarvi);
            detekceMenu.add(detekceFloodFill);
            detekceMenu.add(detekceScanLine);
            detekceMenu.add(detekceErode);
            detekceMenu.add(detekceComponent);
            
            napovedaMenu.add(napovedaAbout);
            
            disableImageActions();
        }
        
        private void inicializujListenery(){
            souborOtevrit.addActionListener(HlavniOkno.this::openImage);
            souborUlozit.addActionListener(HlavniOkno.this::saveImage);
            detekceDobarvi.addActionListener(HlavniOkno.this::detectionColorise);
            detekceFloodFill.addActionListener(HlavniOkno.this::detekcitonFloodFill4P);
            detekceScanLine.addActionListener(HlavniOkno.this::detectionScanline);
            detekceErode.addActionListener(HlavniOkno.this::detectionErode);
            detekceComponent.addActionListener(HlavniOkno.this::detectionComponents);
        }
        
        /** zablokuje nabidku detekce */
        public void disableImageActions(){
            detekceMenu.setEnabled(false);
            souborUlozit.setEnabled(false);
        }
        /** odblokuje nabidku detekce */
        public void enableImageActions(){
            detekceMenu.setEnabled(true);
            souborUlozit.setEnabled(true);
        }
        /** enables or disables the detection menu */
        public void enableDetection(boolean e){
            if(e) enableImageActions();
            else disableImageActions();
        }
    }
}
