package com.rental.ui;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/** Draws a picture scaled to fill the panel (keeping its proportions) with rounded corners. */
public class ImagePanel extends JPanel {

    private BufferedImage image;
    private final int arc;

    public ImagePanel(String fileName, int arc) {
        this.arc = arc;
        setOpaque(false);
        setImage(fileName);
    }

    public void setImage(String fileName) {
        this.image = ImageLoader.load(fileName);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));

        // "cover" scaling: fill the whole area, cropping a little if the shapes differ
        double scale = Math.max((double) getWidth() / image.getWidth(), (double) getHeight() / image.getHeight());
        int w = (int) Math.ceil(image.getWidth() * scale);
        int h = (int) Math.ceil(image.getHeight() * scale);
        g2.drawImage(image, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
        g2.dispose();
    }
}
