package com.rental.ui;

import com.rental.exception.RentalException;
import com.rental.model.UserSession;
import com.rental.service.AuthService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.function.Consumer;

/** The login screen: brand panel on the left, role selection + sign-in form on the right. */
public class LoginPanel extends JPanel {

    private final AuthService auth;
    private final Consumer<UserSession> onLogin;

    private final JTextField userField = new JTextField();
    private final JPasswordField passField = new JPasswordField();
    private final JLabel errorLabel = new JLabel(" ");
    private final JLabel hintLabel = new JLabel();
    private final RoundedButton customerButton = new RoundedButton("Customer", Theme.PRIMARY);
    private final RoundedButton adminButton = new RoundedButton("Administrator", Theme.BORDER);
    private final JButton registerLink = new JButton("New here? Create a customer account");
    private boolean adminMode = false;

    public LoginPanel(AuthService auth, Consumer<UserSession> onLogin) {
        this.auth = auth;
        this.onLogin = onLogin;
        setLayout(new GridLayout(1, 2));
        add(buildHeroPanel());
        add(buildFormPanel());
        setRole(false);
    }

    /** Clears the password after logging out. */
    public void reset() {
        passField.setText("");
        errorLabel.setText(" ");
        setRole(false);
    }

    private JPanel buildHeroPanel() {
        JPanel hero = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, Theme.NAVY, getWidth(), getHeight(), new Color(0x1D4ED8)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        hero.setBorder(BorderFactory.createEmptyBorder(40, 48, 40, 48));

        JLabel logo = new JLabel("DriveEasy");
        logo.setFont(Theme.font(Font.BOLD, 28f));
        logo.setForeground(Color.WHITE);
        hero.add(logo, BorderLayout.NORTH);

        ImagePanel picture = new ImagePanel("creta.png", 28);
        picture.setPreferredSize(new Dimension(380, 230));
        JPanel pictureWrap = new JPanel(new GridBagLayout());
        pictureWrap.setOpaque(false);
        pictureWrap.add(picture);
        hero.add(pictureWrap, BorderLayout.CENTER);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel headline = new JLabel("Your next ride is a click away.");
        headline.setFont(Theme.font(Font.BOLD, 28f));
        headline.setForeground(Color.WHITE);
        text.add(headline);
        text.add(Box.createVerticalStrut(10));
        for (String line : new String[]{"Cars, SUVs, motorcycles and scooters", "Transparent daily pricing", "Book and return in minutes"}) {
            JLabel l = new JLabel("\u2713  " + line);
            l.setFont(Theme.font(Font.PLAIN, 15f));
            l.setForeground(new Color(0xBFDBFE));
            text.add(l);
            text.add(Box.createVerticalStrut(5));
        }
        hero.add(text, BorderLayout.SOUTH);
        return hero;
    }

    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Color.WHITE);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(380, 520));

        JLabel title = new JLabel("Welcome back");
        title.setFont(Theme.font(Font.BOLD, 30f));
        title.setForeground(Theme.TEXT);
        JLabel subtitle = new JLabel("Choose how you want to sign in");
        subtitle.setFont(Theme.font(Font.PLAIN, 14f));
        subtitle.setForeground(Theme.MUTED);

        JPanel roles = new JPanel(new GridLayout(1, 2, 10, 0));
        roles.setOpaque(false);
        roles.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        roles.add(customerButton);
        roles.add(adminButton);
        customerButton.addActionListener(e -> setRole(false));
        adminButton.addActionListener(e -> setRole(true));

        Theme.styleField(userField);
        Theme.styleField(passField);
        passField.addActionListener(e -> doLogin());
        userField.addActionListener(e -> passField.requestFocus());

        errorLabel.setForeground(Theme.DANGER);
        errorLabel.setFont(Theme.font(Font.PLAIN, 13f));

        RoundedButton signIn = new RoundedButton("Sign In", Theme.ACCENT);
        signIn.setFont(Theme.font(Font.BOLD, 15f));
        signIn.addActionListener(e -> doLogin());

        registerLink.setForeground(Theme.PRIMARY);
        registerLink.setFont(Theme.font(Font.BOLD, 13f));
        registerLink.setBorder(BorderFactory.createEmptyBorder());
        registerLink.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        registerLink.setBorderPainted(false);
        registerLink.setContentAreaFilled(false);
        registerLink.setFocusPainted(false);
        registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLink.addActionListener(e -> {
            RegisterDialog dialog = new RegisterDialog(SwingUtilities.getWindowAncestor(this), auth, false);
            dialog.setVisible(true);
            if (dialog.getRegisteredUsername() != null) {
                userField.setText(dialog.getRegisteredUsername());
                passField.requestFocus();
            }
        });

        hintLabel.setFont(Theme.font(Font.PLAIN, 12f));
        hintLabel.setForeground(Theme.MUTED);

        for (Component c : new Component[]{title, subtitle, roles, signIn, errorLabel, registerLink, hintLabel}) {
            ((javax.swing.JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        form.add(title);
        form.add(Box.createVerticalStrut(4));
        form.add(subtitle);
        form.add(Box.createVerticalStrut(24));
        form.add(roles);
        form.add(Box.createVerticalStrut(24));
        form.add(fieldBlock("Username", userField));
        form.add(Box.createVerticalStrut(14));
        form.add(fieldBlock("Password", passField));
        form.add(Box.createVerticalStrut(8));
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(8));
        signIn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        form.add(signIn);
        form.add(Box.createVerticalStrut(10));
        form.add(registerLink);
        form.add(Box.createVerticalStrut(18));
        form.add(hintLabel);
        outer.add(form);
        return outer;
    }

    private JPanel fieldBlock(String label, javax.swing.JComponent field) {
        JPanel block = new JPanel(new BorderLayout(0, 6));
        block.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(Theme.font(Font.BOLD, 12f));
        l.setForeground(Theme.MUTED);
        block.add(l, BorderLayout.NORTH);
        block.add(field, BorderLayout.CENTER);
        block.setAlignmentX(Component.LEFT_ALIGNMENT);
        block.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        return block;
    }

    private void setRole(boolean admin) {
        adminMode = admin;
        customerButton.setBaseColor(admin ? new Color(0xE2E8F0) : Theme.PRIMARY);
        customerButton.setForeground(admin ? Theme.TEXT : Color.WHITE);
        adminButton.setBaseColor(admin ? Theme.PRIMARY : new Color(0xE2E8F0));
        adminButton.setForeground(admin ? Color.WHITE : Theme.TEXT);
        registerLink.setVisible(!admin);
        hintLabel.setText(admin ? "Demo administrator login: admin / admin123 (demo only)"
                                : "Demo customer login: demo / demo123 (demo only)");
        errorLabel.setText(" ");
    }

    private void doLogin() {
        try {
            String password = new String(passField.getPassword());
            UserSession session = adminMode ? auth.loginAdmin(userField.getText(), password)
                                            : auth.loginCustomer(userField.getText(), password);
            errorLabel.setText(" ");
            passField.setText("");
            onLogin.accept(session);
        } catch (RentalException ex) {
            errorLabel.setText(ex.getMessage());
        }
    }
}
