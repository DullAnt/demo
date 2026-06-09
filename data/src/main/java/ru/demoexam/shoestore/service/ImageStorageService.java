package ru.demoexam.shoestore.service;

import ru.demoexam.shoestore.db.Database;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class ImageStorageService {
    private final Database database;

    public ImageStorageService(Database database) {
        this.database = database;
    }

    public String saveImage(File sourceFile, String oldImagePath) {
        try {
            BufferedImage originalImage = ImageIO.read(sourceFile);
            BufferedImage resizedImage = resizeToFit(originalImage, 300, 200);

            Path targetFile = database.getImageDirectory().resolve(UUID.randomUUID() + ".png");
            ImageIO.write(resizedImage, "png", targetFile.toFile());

            if (oldImagePath != null && !oldImagePath.isBlank()) {
                Files.deleteIfExists(Path.of(oldImagePath));
            }

            return targetFile.toAbsolutePath().toString();
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось сохранить изображение товара.", exception);
        }
    }

    private BufferedImage resizeToFit(BufferedImage source, int maxWidth, int maxHeight) {
        double scale = Math.min((double) maxWidth / source.getWidth(), (double) maxHeight / source.getHeight());
        scale = Math.min(scale, 1.0);

        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = result.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();
        return result;
    }
}
