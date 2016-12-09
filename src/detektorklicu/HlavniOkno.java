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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

/** Hlavni okno
 * Hlavni okno programu Deterktor klicu
 * @author zikmuto2
 */
public class HlavniOkno extends JFrame{
    private static final Lokalizator l = Lokalizator.getLokalizator();
    private final MainMenu mainMenu = new MainMenu();
    private final KresliciPanel panelSObrazkem = new KresliciPanel();
    
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
        add(panelSObrazkem);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e){
                System.exit(0);
            }
        });
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
        private final JMenuItem detekceFloodFill = new JMenuItem(l.tr("detekceFloodFill"));
        
        private final JMenu napovedaMenu = new JMenu(l.tr("napovedaMenu"));
        private final JMenuItem napovedaAbout = new JMenuItem(l.tr("napovedaAbout"));
        
        /** Konstruktor hlavni nabidky
         * vezme si okdkaz na okno a inicializuje obsah hlavni nabidky
         * @param o odkaz na hlavni okno
         */
        public MainMenu(){
            okno = HlavniOkno.this;
            this.inicializace();
        }

        private void inicializace() {
            okno.setJMenuBar(hlavniNabidka);
            
            hlavniNabidka.add(souborMenu);
            hlavniNabidka.add(detekceMenu);
            hlavniNabidka.add(napovedaMenu);
            
            souborMenu.add(souborOtevrit);
            souborMenu.add(souborUlozit);
            souborMenu.addSeparator();
            souborMenu.add(souborKonec);
            
            detekceMenu.add(detekceFloodFill);
            
            napovedaMenu.add(napovedaAbout);
        }
    }
        
    class KresliciPanel extends JPanel{
        @Override
        public void paintComponent(Graphics g){
            super.paintComponent(g);

        }
    }
}
