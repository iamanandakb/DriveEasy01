package com.rental.ui;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads the bundled vehicle pictures from the classpath (src/main/resources/images),
 * so they work from VS Code, from "mvn exec:java" and from the packaged jar - with no internet.
 */
public final class ImageLoader {

    public static final String[] ALL_IMAGES = {
        "swift.png", "i20.png", "city.png", "camry.png", "creta.png", "fortuner.png", "thar.png",
        "classic350.png", "pulsar.png", "duke.png", "activa.png", "jupiter.png"
    };

    private static final Map<String, BufferedImage> CACHE = new HashMap<>();

    private ImageLoader() { }

    public static BufferedImage load(String fileName) {
        BufferedImage image = CACHE.get(fileName);
        if (image != null) return image;

        try (InputStream in = ImageLoader.class.getResourceAsStream("/images/" + fileName)) {
            if (in != null) image = ImageIO.read(in);
        } catch (IOException e) {
            image = null;
        }
        if (image == null) image = placeholder();      // never crash because of a missing picture
        CACHE.put(fileName, image);
        return image;
    }

    private static BufferedImage placeholder() {
        BufferedImage img = new BufferedImage(600, 360, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0xE2E8F0));
        g.fillRect(0, 0, 600, 360);
        g.setColor(Theme.MUTED);
        g.drawString("Image not found", 250, 180);
        g.dispose();
        return img;
    }
}
