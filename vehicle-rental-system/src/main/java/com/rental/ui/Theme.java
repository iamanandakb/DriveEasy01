package com.rental.ui;

import com.rental.model.Vehicle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** One place for the colour scheme, fonts and small formatting helpers so the whole app looks consistent. */
public final class Theme {

    public static final Color PRIMARY = new Color(0x2563EB);
    public static final Color NAVY = new Color(0x0F172A);
    public static final Color NAVY_LIGHT = new Color(0x1E293B);
    public static final Color ACCENT = new Color(0xF97316);
    public static final Color BG = new Color(0xF1F5F9);
    public static final Color TEXT = new Color(0x0F172A);
    public static final Color MUTED = new Color(0x64748B);
    public static final Color BORDER = new Color(0xE2E8F0);
    public static final Color SUCCESS = new Color(0x16A34A);
    public static final Color DANGER = new Color(0xDC2626);
    public static final Color WARNING = new Color(0xD97706);

    public static final String FONT_FAMILY = pickFont();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private Theme() { }

    /** Uses the first modern font that is installed, otherwise the Java default sans-serif. */
    private static String pickFont() {
        Set<String> installed = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String name : new String[]{"Segoe UI", "Inter", "SF Pro Text", "Helvetica Neue", "Ubuntu"}) {
            if (installed.contains(name)) return name;
        }
        return Font.SANS_SERIF;
    }

    public static Font font(int style, float size) {
        return new Font(FONT_FAMILY, style, 12).deriveFont(style, size);
    }

    public static String money(double amount) {
        return Vehicle.CURRENCY + String.format(Locale.ENGLISH, "%,.2f", amount);
    }

    public static String date(LocalDate date) {
        return date == null ? "-" : date.format(DATE_FORMAT);
    }

    /** Padding + thin border for text fields. */
    public static void styleField(JComponent field) {
        field.setFont(font(Font.PLAIN, 14f));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
    }

    /** Called once at start-up: Nimbus look & feel gives modern-looking combo boxes, spinners and scrollbars. */
    public static void install() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is missing the default look and feel is used. The app still works.
        }
        UIManager.put("OptionPane.messageFont", font(Font.PLAIN, 14f));
        UIManager.put("OptionPane.buttonFont", font(Font.BOLD, 13f));
        UIManager.put("ComboBox.font", font(Font.PLAIN, 14f));
        UIManager.put("Spinner.font", font(Font.PLAIN, 14f));
        UIManager.put("Label.font", font(Font.PLAIN, 13f));
        UIManager.put("CheckBox.font", font(Font.PLAIN, 13f));
        UIManager.put("TextArea.font", font(Font.PLAIN, 13f));
    }
}
