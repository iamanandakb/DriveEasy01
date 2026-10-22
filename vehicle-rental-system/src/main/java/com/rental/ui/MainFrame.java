package com.rental.ui;

import com.rental.model.UserSession;
import com.rental.service.AuthService;
import com.rental.service.RentalService;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

/** The single application window. It switches between the login screen and the customer/admin portal. */
public class MainFrame extends JFrame {

    private final CardLayout layout = new CardLayout();
    private final JPanel root = new JPanel(layout);
    private final AuthService auth = new AuthService();
    private final RentalService service = new RentalService();
    private final LoginPanel loginPanel;
    private Portal currentPortal;

    public MainFrame() {
        super("DriveEasy - Vehicle Rental Service Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1280, screen.width - 60), Math.min(800, screen.height - 80));
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);

        loginPanel = new LoginPanel(auth, this::onLogin);
        root.add(loginPanel, "login");
        setContentPane(root);
    }

    private void onLogin(UserSession session) {
        if (currentPortal != null) root.remove(currentPortal);
        // Two different objects, one variable of the parent type Portal (POLYMORPHISM)
        currentPortal = session.isAdmin()
                ? new AdminPortal(session, service, auth, this::logout)
                : new CustomerPortal(session, service, this::logout);
        root.add(currentPortal, "portal");
        layout.show(root, "portal");
    }

    private void logout() {
        layout.show(root, "login");
        if (currentPortal != null) root.remove(currentPortal);   // forget the old session completely
        currentPortal = null;
        loginPanel.reset();
    }
}
