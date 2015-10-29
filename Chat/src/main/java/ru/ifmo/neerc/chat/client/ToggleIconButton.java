package ru.ifmo.neerc.chat.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class ToggleIconButton extends JButton {
    public ToggleIconButton(final String image, final String toolTipText,
                            final String selectedImage, final String selectedToolTipText) {
        setIcon(new ImageIcon(ToggleIconButton.class.getResource(image)));
        setSelectedIcon(new ImageIcon(ToggleIconButton.class.getResource(selectedImage)));
        setFocusable(false);
        setToolTipText(toolTipText);

        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setSelected(!isSelected());
                setToolTipText(isSelected() ? selectedToolTipText : toolTipText);
            }
        });
    }
}
