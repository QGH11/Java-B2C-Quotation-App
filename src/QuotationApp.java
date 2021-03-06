import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;

import java.awt.event.*;
import java.io.*;
import java.util.Iterator;


public class QuotationApp extends JFrame{
    private JPanel mainPanel;
    private JTextField quotationTitleTextField;
    private JButton businessSubmitButton;
    private JButton customerSubmitButton;
    private JList businessQuotationList;
    private JList detailedQuotationList;
    private JComboBox categoryComboBox;
    private JTextField quotationStatusTextField;
    private JCheckBox emergencyCheckBox;
    private JCheckBox financialCheckBox;
    private JTextField quotationDescriptionTextField;
    private JLabel customerSubmitStatusLabel;
    private JTextArea quotationStatusTextArea;
    private JLabel customerSideLabel;
    private JLabel businessSideLabel;
    private JLabel businessSubmitStatusLabel;
    private JPanel QuotationPanel;
    private static FileWriter file;
    private JSONArray tempQuotationList = new JSONArray();
    private JSONObject quotationSum = new JSONObject();

    public QuotationApp(String title) {
        super(title);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();

        // get the data form json file
        readJson();
        printQuotationList();

        customerSubmitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // get all the info
                // push them to .json
                // check for empty text field
                if (quotationTitleTextField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(null,"ERROR: QUOTATION TITLE IS EMPTY!");
                }
                else if (quotationDescriptionTextField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "ERROR: QUOTATION DESCRIPTION IS EMPTY!");
                }
                else if (categoryComboBox.getSelectedItem().toString().equals("Choose a category")) {
                    JOptionPane.showMessageDialog(null,"ERROR: CHOOSE A CATEGORY!");
                }
                else {
                    // read the json file
                    readJson();

                    String quotationTitle = quotationTitleTextField.getText();
                    String quotationDescription = quotationDescriptionTextField.getText();
                    String category = categoryComboBox.getSelectedItem().toString();
                    Boolean emergencyStatus = emergencyCheckBox.isSelected();
                    Boolean financialStatus = financialCheckBox.isSelected();

                    quotation newQuotation = new quotation(quotationTitle, quotationDescription, category, emergencyStatus, financialStatus);
                    newQuotation.storeQuotationJObject();

                    tempQuotationList.add(newQuotation.getQuotation());

                    quotationSum.put("QuotationList", tempQuotationList);

                    try {
                        // Constructs a FileWriter given a file name, using the platform's default charset
                        file = new FileWriter("./src/quotationList.json");
                        file.write(quotationSum.toJSONString());
                        file.flush();
                        file.close();

                        // clear all the inputs
                        quotationTitleTextField.setText("");
                        quotationDescriptionTextField.setText("");
                        categoryComboBox.setSelectedIndex(0);
                        emergencyCheckBox.setSelected(false);
                        financialCheckBox.setSelected(false);

                        //update the quotations
                        printQuotationList();

                        // submission successful message and clear it after 3 secs
                        customerSubmitStatusLabel.setText("Submitted!");
                        int delay = 3000; // 1sec
                        ActionListener taskPerformer = new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                customerSubmitStatusLabel.setText("");
                            }
                        };
                        new javax.swing.Timer(delay, taskPerformer).start();

                    } catch ( IOException c) {
                        c.printStackTrace();
                    }
                }
            }
        });

        businessQuotationList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                // show the full items of a quotation

                String quotationTitle;
                String quotationDescription;
                String category;
                Boolean emergencyStatus;
                Boolean financialStatus;

                JSONObject tempJObject = new JSONObject();
                DefaultListModel listModel = new DefaultListModel();

                int quotationIndex = businessQuotationList.getSelectedIndex();
                tempJObject = (JSONObject) tempQuotationList.get(quotationIndex);
                quotationTitle = (String) tempJObject.get("Title");
                quotationDescription = (String) tempJObject.get("Description");
                category = (String) tempJObject.get("Category");
                emergencyStatus = (Boolean) tempJObject.get("EmergencyStatus");
                financialStatus = (Boolean) tempJObject.get("FinancialStatus");

                listModel.addElement("Title: " + quotationTitle);
                listModel.addElement("Description: " + quotationDescription);
                listModel.addElement("Category: " + category);
                listModel.addElement("Emergency: " + emergencyStatus);
                listModel.addElement("Money is not a problem: " + financialStatus);

                detailedQuotationList.setModel(listModel);
            }
        });

        businessSubmitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int quotationIndex = businessQuotationList.getSelectedIndex();

                if (quotationIndex != -1) {
                    String quotationStatus = quotationStatusTextField.getText();

                    JSONObject tempJObject;
                    tempJObject = (JSONObject) tempQuotationList.get(quotationIndex);
                    String quotationTitle = (String) tempJObject.get("Title");

                    // remove the selected quotation
                    tempQuotationList.remove(quotationIndex);
                    quotationSum.put("QuotationList", tempQuotationList);

                    try {
                        // Constructs a FileWriter given a file name, using the platform's default charset
                        file = new FileWriter("./src/quotationList.json");
                        file.write(quotationSum.toJSONString());
                        file.flush();
                        file.close();
                    }
                    catch (IOException d) {
                        d.printStackTrace();
                    }

                    // set the quotation status
                    quotationStatusTextArea.append("Quotation Title: " + quotationTitle);
                    quotationStatusTextArea.append("\nQuotation Status: " + quotationStatus);

                    //update the Jlist
                    readJson();
                    printQuotationList();
                }
                else {
                    JOptionPane.showMessageDialog(null,"ERROR: CHOOSE A QUOTATION FROM THE LIST");
                }
            }
        });
    }

    // quotation class
    public class quotation {
        private String quotationTitle;
        private String quotationDescription;
        private String category;
        private Boolean emergencyStatus;
        private Boolean financialStatus;
        private JSONObject quotation;

        public quotation(String title, String description, String category, Boolean eStatus, Boolean fStatus ) {
            quotationTitle = title;
            quotationDescription = description;
            this.category = category;
            emergencyStatus = eStatus;
            financialStatus = fStatus;
        }

        // store quotation into json object
        public void storeQuotationJObject() {
            quotation = new JSONObject();
            quotation.put("Title", quotationTitle);
            quotation.put("Description", quotationDescription);
            quotation.put("Category", category);
            quotation.put("EmergencyStatus", emergencyStatus);
            quotation.put("FinancialStatus", financialStatus);
        }

        // get quotation object
        public JSONObject getQuotation() {
            return quotation;
        }
    }

    // read and return JSON file data
    public void readJson() {
        JSONParser parser = new JSONParser();

        try {
            Object quotation = parser.parse(new FileReader("./src/quotationList.json"));
            JSONObject jsonObject = (JSONObject) quotation;

            tempQuotationList = (JSONArray) jsonObject.get("QuotationList");
        }
        catch(FileNotFoundException e) {e.printStackTrace();}
        catch(IOException e) {e.printStackTrace();}
        catch(ParseException e) {e.printStackTrace();}
        catch(Exception e) {e.printStackTrace();}
    }

    // print the title of quotations
    public void printQuotationList() {
        String quotationTitle;

        JSONObject tempJObject;
        DefaultListModel listModel = new DefaultListModel();

        for (int i  = 0; i < tempQuotationList.size(); i++ ) {
            tempJObject = (JSONObject) tempQuotationList.get(i);
            quotationTitle = (String) tempJObject.get("Title");
            listModel.addElement(quotationTitle);
        }
        businessQuotationList.setModel(listModel);
    }

    public static void main(String[] args) {
        JFrame frame = new QuotationApp("B2C Quotation App");
        frame.setBounds(400, 100, 800, 700);
        frame.setVisible(true);
    }
}
