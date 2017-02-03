package org.endeavourhealth.hl7test;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.endeavourhealth.hl7test.transforms.AdtFhirTransformer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class MainDialog {
    private JPanel panelMain;
    private JTextArea testPane1TextArea;
    private JButton transformButton;
    private JTextArea testPaneTextArea2;
    private JSplitPane splitPane;
    private JFrame frame;

    public MainDialog(JFrame frame) {
        this.frame = frame;
        transformButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    testPaneTextArea2.setText("");
                    testPaneTextArea2.repaint();
                    testPaneTextArea2.revalidate();
                    testPaneTextArea2.setText(AdtFhirTransformer.transform(testPane1TextArea.getText()));
                } catch (Exception e1) {
                    testPaneTextArea2.setText("[" + e1.getClass() + "] " + e1.getMessage() + "\r\n" + ExceptionUtils.getStackTrace(e1));
                }
            }
        });

        panelMain.addComponentListener(new ComponentListener() {
            public void componentResized(ComponentEvent e) {
                splitPane.setDividerLocation(frame.getWidth() / 2);
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                splitPane.setDividerLocation(frame.getWidth() / 2);
            }

            @Override
            public void componentShown(ComponentEvent e) {
                splitPane.setDividerLocation(frame.getWidth() / 2);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                splitPane.setDividerLocation(frame.getWidth() / 2);
            }
        });


    }

    public JPanel getPanelMain() {
        return panelMain;
    }
}
