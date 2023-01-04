/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.controller;

import com.model.HeaderTableModel;
import com.model.InvoiceHeader;
import com.model.InvoiceLine;
import com.model.LineTableModel;
import com.view.InvoiceHeaderDialog;
import com.view.InvoiceLineDialog;
import com.view.SIGFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author DELL
 */
public class SIGenerator implements ActionListener, ListSelectionListener {

    private SIGFrame frame;
    private InvoiceHeaderDialog headerDialog;
    private InvoiceLineDialog lineDialog;

    public SIGenerator(SIGFrame frame) {
        this.frame = frame;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        int selectedRow = frame.getInvoicesTable().getSelectedRow();
        if (selectedRow > -1) {
            InvoiceHeader inv = frame.getInvoices().get(selectedRow);
            frame.getInvNumLbl().setText("" + inv.getNum());
            frame.getInvDateLbl().setText(SIGFrame.sdf.format(inv.getDate()));
            frame.getInvCustNameLbl().setText(inv.getName());
            frame.getInvTotalLbl().setText("" + inv.getTotal());
            ArrayList<InvoiceLine> lines = inv.getLines();
            frame.setLineTableModel(new LineTableModel(lines));
        } else {
            frame.getInvNumLbl().setText("");
            frame.getInvDateLbl().setText("");
            frame.getInvCustNameLbl().setText("");
            frame.getInvTotalLbl().setText("");
            frame.setLineTableModel(new LineTableModel());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        switch (actionCommand) {
            case "Load":
                load(null, null);
                break;
            case "Save":
                save();
                break;
            case "Create Invoice":
                createInvoice();
                break;
            case "Delete Invoice":
                deleteInvoice();
                break;
            case "Create Item":
                createItem();
                break;
            case "Delete Item":
                deleteItem();
                break;
            case "newInvoiceOK":
                newInvoiceOK();
                break;
            case "newInvoiceCancel":
                newInvoiceCancel();
                break;
            case "newLineOK":
                newLineOK();
                break;
            case "newLineCancel":
                newLineCancel();
                break;
        }
    }

    public void load(String headerPath, String linePath) {
        File headerFile = null;
        File lineFile = null;
        if (headerPath == null && linePath == null) {
            JOptionPane.showMessageDialog(frame, "Select header file first, then select line file.", "Invoice files", JOptionPane.WARNING_MESSAGE);
            JFileChooser fc = new JFileChooser();
            int result = fc.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                headerFile = fc.getSelectedFile();
                result = fc.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    lineFile = fc.getSelectedFile();
                }
            }
        } else {
            headerFile = new File(headerPath);
            lineFile = new File(linePath);
        }

        if (headerFile != null && lineFile != null) {
            try {
                /*
                1,22-11-2020,Ali
                2,13-10-2021,Saleh
                 */
                // collection streams
                List<String> headerLines = Files.lines(Paths.get(headerFile.getAbsolutePath())).collect(Collectors.toList());
                /*
                1,Mobile,3200,1
                1,Cover,20,2
                1,Headphone,130,1	
                2,Laptop,9000,1
                2,Mouse,135,1
                 */
                List<String> lineLines = Files.lines(Paths.get(lineFile.getAbsolutePath())).collect(Collectors.toList());
                //ArrayList<Invoice> invoices = new ArrayList<>();
                frame.getInvoices().clear();
                for (String headerLine : headerLines) {
                    String[] parts = headerLine.split(",");  // "1,22-11-2020,Ali"  ==>  ["1", "22-11-2020", "Ali"]
                    String numString = parts[0];
                    String dateString = parts[1];
                    String name = parts[2];
                    int num = Integer.parseInt(numString);
                    Date date = frame.sdf.parse(dateString);
                    InvoiceHeader inv = new InvoiceHeader(num, name, date);
                    frame.getInvoices().add(inv);
                }
                System.out.println("Check point");
                for (String lineLine : lineLines) {
                    // lineLine = "1,Mobile,3200,1"
                    String[] parts = lineLine.split(",");
                    /*
                    parts = ["1", "Mobile", "3200", "1"]
                     */
                    int num = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    double price = Double.parseDouble(parts[2]);
                    int count = Integer.parseInt(parts[3]);
                    InvoiceHeader inv = frame.getInvoiceByNum(num);
                    InvoiceLine line = new InvoiceLine(name, price, count, inv);
                    inv.getLines().add(line);
                }
                System.out.println("Check point");
                frame.setHeaderTableModel(new HeaderTableModel(frame.getInvoices()));
                //frame.getInvoicesTable().setModel(new HeaderTableModel());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void save() {
        JFileChooser fc = new JFileChooser();
        File headerFile = null;
        File lineFile = null;
        int result = fc.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            headerFile = fc.getSelectedFile();
            result = fc.showSaveDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                lineFile = fc.getSelectedFile();
            }
        }
        
        if (headerFile != null && lineFile != null) {
            String headerData = "";
            String lineData = "";
            for (InvoiceHeader inv : frame.getInvoices()) {
                headerData += inv.getAsCSV();
                headerData += "\n";
                for (InvoiceLine line : inv.getLines()) {
                    lineData += line.getAsCSV();
                    lineData += "\n";
                }
            }
            try {
                FileWriter headerFW = new FileWriter(headerFile);
                FileWriter lineFW = new FileWriter(lineFile);
                headerFW.write(headerData);
                lineFW.write(lineData);
                headerFW.flush();
                lineFW.flush();
                headerFW.close();
                lineFW.close();
                
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Error while writing file(s)", "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        }
    }

    private void createInvoice() {
        headerDialog = new InvoiceHeaderDialog(frame);
        headerDialog.setLocation(300, 300);
        headerDialog.setVisible(true);
    }

    private void deleteInvoice() {

        int selectedRow = frame.getInvoicesTable().getSelectedRow();
        if (selectedRow > -1) {
            frame.getInvoices().remove(selectedRow);
            frame.getHeaderTableModel().fireTableDataChanged();
        }
    }

    private void createItem() {
        if (frame.getInvoicesTable().getSelectedRow() > -1) {
            lineDialog = new InvoiceLineDialog(frame);
            lineDialog.setLocation(300, 300);
            lineDialog.setVisible(true);
        }
    }

    private void deleteItem() {
        int selectedInvoice = frame.getInvoicesTable().getSelectedRow();
        int selectedItem = frame.getLinesTable().getSelectedRow();

        if (selectedInvoice > -1 && selectedItem > -1) {
            frame.getInvoices().get(selectedInvoice).getLines().remove(selectedItem);
            frame.getLineTableModel().fireTableDataChanged();
            frame.getHeaderTableModel().fireTableDataChanged();
            frame.getInvoicesTable().setRowSelectionInterval(selectedInvoice, selectedInvoice);
        }
    }

    private void newInvoiceOK() {
        String name = headerDialog.getCustNameField().getText();
        String dateStr = headerDialog.getInvDateField().getText();
        newInvoiceCancel();
        try {
            Date date = frame.sdf.parse(dateStr);
            InvoiceHeader inv = new InvoiceHeader(frame.getNextInvNum(), name, date);
            frame.getInvoices().add(inv);
            frame.getHeaderTableModel().fireTableDataChanged();
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(frame, "Invalid Date Format", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void newInvoiceCancel() {
        headerDialog.setVisible(false);
        headerDialog.dispose();
        headerDialog = null;
    }

    private void newLineOK() {
        String name = lineDialog.getItemNameField().getText();
        String countStr = lineDialog.getItemCountField().getText();
        String priceStr = lineDialog.getItemPriceField().getText();
        newLineCancel();
        try {
            int count = Integer.parseInt(countStr);
            double price = Double.parseDouble(priceStr);
            int currentInv = frame.getInvoicesTable().getSelectedRow();
            InvoiceHeader inv = frame.getInvoices().get(currentInv);
            InvoiceLine line = new InvoiceLine(name, price, count, inv);
            inv.getLines().add(line);
            frame.getHeaderTableModel().fireTableDataChanged();
            frame.getInvoicesTable().setRowSelectionInterval(currentInv, currentInv);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Invalid Number Format", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void newLineCancel() {
        lineDialog.setVisible(false);
        lineDialog.dispose();
        lineDialog = null;
    }

}
