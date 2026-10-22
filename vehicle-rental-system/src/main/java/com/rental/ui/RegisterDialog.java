package com.rental.ui;

import com.rental.exception.RentalException;
import com.rental.model.Customer;
import com.rental.service.AuthService;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;

/** Customer registration form. Used on the login screen and by the administrator. */
public class RegisterDialog extends JDialog {

    private final AuthService auth;
    private final JTextField nameField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JTextField phoneField = new JTextField(20);
    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JPasswordField confirmField = new JPasswordField(20);
    private String registeredUsername = null;

    public RegisterDialog(Window owner, AuthService auth, boolean byAdmin) {
        super(owner, byAdmin ? "Register Customer" : "Create Account", ModalityType.APPLICATION_MODAL);
        this.auth = auth;

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));

        JLabel title = new JLabel(byAdmin ? "Register a new customer" : "Create your customer account");
        title.setFont(Theme.font(Font.BOLD, 20f));
        body.add(title, BorderLayout.NORTH);

        for (JTextField f : new JTextField[]{nameField, emailField, phoneField, usernameField, passwordField, confirmField}) {
            Theme.styleField(f);
        }
        FormPanel form = new FormPanel();
        form.addRow("Full name", nameField);
        form.addRow("Email", emailField);
        form.addRow("Phone (10 digits)", phoneField);
        form.addRow("Username", usernameField);
        form.addRow("Password (min 6)", passwordField);
        form.addRow("Confirm password", confirmField);
        body.add(form, BorderLayout.CENTER);

        RoundedButton cancel = new RoundedButton("Cancel", new Color(0x94A3B8));
        RoundedButton create = new RoundedButton("Create Account", Theme.PRIMARY);
        cancel.addActionListener(e -> dispose());
        create.addActionListener(e -> register());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(cancel);
        buttons.add(create);
        body.add(buttons, BorderLayout.SOUTH);

        setContentPane(body);
        getRootPane().setDefaultButton(null);
        setMinimumSize(new Dimension(460, 0));
        pack();
        setLocationRelativeTo(owner);
    }

    /** Username of the account that was just created, or null if the dialog was cancelled. */
    public String getRegisteredUsername() {
        return registeredUsername;
    }

    private void register() {
        String password = new String(passwordField.getPassword());
        if (!password.equals(new String(confirmField.getPassword()))) {
            JOptionPane.showMessageDialog(this, "The two passwords do not match.", "Check your input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Customer c = auth.register(nameField.getText(), emailField.getText(), phoneField.getText(),
                    usernameField.getText(), password);
            registeredUsername = c.getUsername();
            JOptionPane.showMessageDialog(this, "Account created for " + c.getName() + ".\nUsername: " + c.getUsername(),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (RentalException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot register", JOptionPane.WARNING_MESSAGE);
        }
    }
}
