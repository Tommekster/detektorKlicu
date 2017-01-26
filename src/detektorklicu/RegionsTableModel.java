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

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Acer
 */
public class RegionsTableModel extends AbstractTableModel {
    private static final Lokalizator l = Lokalizator.getLokalizator();
    private final String[] columnNames = {l.tr("columnRegionLabel"),
        l.tr("columnRegionArea"),l.tr("columnRegionBoundings"),
        l.tr("columnRegionCenter")};
    private List<Region> data;
    
    public RegionsTableModel(List<Region> regions){
        super();
        data = regions;
    }
    
    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
    
    public Region getRegionAt(int row){
        return data.get(row);
    }

    @Override
    public Object getValueAt(int row, int col) {
        Region region = data.get(row);
        switch(col){
            case 0:
                return region.getLabel();
            case 1:
                return region.getArea();
            case 2:
                return region.getBoundings();
            case 3:
                return new Point((int)region.getCenter().getX(),(int)region.getCenter().getY()){
                    @Override
                    public String toString(){
                        StringBuilder sb = new StringBuilder();
                        return sb.append("[")
                                .append(this.x).append(", ")
                                .append(this.y).append("]")
                                .toString();
                    }
                };
        }
        return null;
    }
    
    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }
}
