/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.model;

import com.view.SIGFrame;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author DELL
 */
public class HeaderTableModel extends AbstractTableModel {

    private String[] columns = {"Invoice Num", "Invoice Date", "Customer Name", "Invoice Total"};
    private ArrayList<InvoiceHeader> invoices;
    
    public HeaderTableModel(ArrayList<InvoiceHeader> invoices) {
        this.invoices = invoices;
    }
    
    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public int getRowCount() {
        return invoices.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        InvoiceHeader inv = invoices.get(rowIndex);
        switch(columnIndex) {
            case 0:
                return inv.getNum();
            case 1:
                return SIGFrame.sdf.format(inv.getDate());
            case 2:
                return inv.getName();
            case 3:
                return inv.getTotal();
            default:
                return "";
        }
    }
}
