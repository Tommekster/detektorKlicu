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

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;

/**
 *
 * @author zikmuto2
 */
public class Lokalizator {
    /** Singleton instance of localizator */
    private static Lokalizator lokalizator;
    /** Vyvolene jazyky, pro nez jsou dialogy lokalizovany */
    private final String[] predefinedLangs = {"de", "en", "es", "fr", "it"};
    /** Aktualni lokalni nastaveni */
    private Locale lokal;
    /** Zdrojovy soubor textu */
    private ResourceBundle zdroj;
    private NumberFormat numberFormat;
    private char desetinna;
    
    
    /** Lokalizator
     * konstruktor lokalizace
     */
    private Lokalizator(){
        inicializace();
    }
    
    /** Vytvori lokalni nastaveni a nacte podle nej pouzivane texty.
     * @param nastaveni Retezce upravujici nastaveni
     */
    public void inicializace(String ... nastaveni){
        try
        {
            switch(nastaveni.length)
            {
                case 0:
                    lokal = Locale.getDefault();
                    break;
                case 1:
                    lokal = new Locale(nastaveni[0]);
                    break;
                case 2:
                    lokal = new Locale(nastaveni[0], nastaveni[1]);
                    break;
                default:
                    throw new IllegalArgumentException("Illegal argument");
            }
        }
        catch(IllegalArgumentException | NullPointerException ex)
        {
            JOptionPane.showMessageDialog(null, "Chybné lokální nastavení\n"
                    + "(chybný počet parametrů programu).\n"
                    + "Bude užito implicitní nastavení\n"
                    + "Error in local settings\n"
                    + "(wrong program parameter number).\n"
                    + "Default locale used.", "Upozornění - Warning", 
                    JOptionPane.INFORMATION_MESSAGE);
            lokal = Locale.getDefault();
        }
        zdroj = ResourceBundle.getBundle("texty.texty", lokal);
        numberFormat = NumberFormat.getNumberInstance(lokal);
        numberFormat.setMaximumFractionDigits(15);
        desetinna = (new DecimalFormatSymbols(lokal)).getDecimalSeparator();
        //LokalizujPředdefinovanéDialogy();
    }
    
    /** vrati jedinou instanci
     * @return jedina instance lokalizatoru */
    public static Lokalizator getLokalizator(){
        if(lokalizator == null) lokalizator = new Lokalizator();
        
        return lokalizator;
    }
    
    /** Ziska znakovy retezec
     * @param jmeno klic k retezci
     * @return Retezec odpovidajici klici
     */
    public String tr(String jmeno){
        String s = zdroj.getString(jmeno);
        return s;
    }
}
