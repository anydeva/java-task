package com.practic;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class ImageProcessor extends JFrame {
  private BufferedImage originalImage;
  private BufferedImage currentImage;
  private JLabel imageDisplayLabel;

  public ImageProcessor() {
    imageDisplayLabel = new JLabel();
    add(imageDisplayLabel);
    loadImage();

    createMenu();

    configureWindow();
  }

  private BufferedImage cloneImage(BufferedImage image) {
    ColorModel colorModel = image.getColorModel();
    boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
    WritableRaster raster = image.copyData(null);
    return new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
  }

  private void loadImage() {
    Object[] options = {"Выбрать изображение", "Использовать изображение по умолчанию"};
    int choice =
            JOptionPane.showOptionDialog(
                    this,
                    "Хотите выбрать изображение с компьютера или использовать изображение по умолчанию?",
                    "Выбор изображения",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

    if (choice == JOptionPane.YES_OPTION) {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setDialogTitle("Выберите изображение");
      fileChooser.setAcceptAllFileFilterUsed(false);
      fileChooser.addChoosableFileFilter(
              new FileNameExtensionFilter("Image files", "jpg", "png", "gif", "bmp"));

      int result = fileChooser.showOpenDialog(this);
      if (result == JFileChooser.APPROVE_OPTION) {
        File file = fileChooser.getSelectedFile();
        try {
          originalImage = ImageIO.read(file);
          currentImage = cloneImage(originalImage);
          imageDisplayLabel.setIcon(new ImageIcon(currentImage));
          pack();
        } catch (IOException e) {
          JOptionPane.showMessageDialog(
                  this,
                  "Не удалось загрузить изображение: " + e.getMessage(),
                  "Ошибка",
                  JOptionPane.ERROR_MESSAGE);
        }
      }
    } else if (choice == JOptionPane.NO_OPTION) {
      try {
        originalImage = ImageIO.read(getClass().getResource("/image.png"));
        currentImage = cloneImage(originalImage);
        imageDisplayLabel.setIcon(new ImageIcon(currentImage));
        pack();
      } catch (IOException e) {
        JOptionPane.showMessageDialog(
                this,
                "Не удалось загрузить изображение по умолчанию: " + e.getMessage(),
                "Ошибка",
                JOptionPane.ERROR_MESSAGE);
      }
    } else {
      JOptionPane.showMessageDialog(
              this,
              "Не выбрано изображение. Приложение будет закрыто.",
              "Закрытие приложения",
              JOptionPane.INFORMATION_MESSAGE);
      System.exit(0);
    }
    if (originalImage == null) {
      closeApplication();
    }
  }

  private void closeApplication() {
    JOptionPane.showMessageDialog(
            this,
            "Не выбрано изображение. Приложение будет закрыто.",
            "Закрытие приложения",
            JOptionPane.INFORMATION_MESSAGE);
    System.exit(0);
  }

  private void createMenu() {
    JMenuBar menuBar = new JMenuBar();
    JMenu menu = new JMenu("Опции");
    menuBar.add(menu);

    addMenuItem(menu, "Выбрать канал", e -> selectColorChannel());
    addMenuItem(menu, "Переключить оттенки серого", e -> convertToGrayscale());
    addMenuItem(menu, "Повысить яркость", e -> adjustBrightness());
    addMenuItem(menu, "Показать негативное изображение", e -> displayNegativeImage());
    addMenuItem(menu, "Усреднение изображения", e -> applyAverageFilter());
    addMenuItem(menu, "Нарисовать прямоугольник", e -> drawRectangleOnImage());
    addMenuItem(menu, "Сбросить изменения", e -> resetImageChanges());
    addMenuItem(menu, "Выбрать изображение", e -> loadImage());

    setJMenuBar(menuBar);
  }

  private void configureWindow() {
    try {
      Image icon = ImageIO.read(getClass().getResource("/icon.jpg"));
      setIconImage(icon);
    } catch (IOException e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(
              this,
              "Не удалось загрузить иконку: " + e.getMessage(),
              "Ошибка",
              JOptionPane.ERROR_MESSAGE);
    }

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    pack();
    setVisible(true);
  }

  private void addMenuItem(JMenu menu, String title, ActionListener listener) {
    JMenuItem menuItem = new JMenuItem(title);
    menuItem.addActionListener(listener);
    menu.add(menuItem);
  }

  private void displayColorChannel(Color channelColor) {
    int width = currentImage.getWidth();
    int height = currentImage.getHeight();
    BufferedImage channelImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        int rgb = currentImage.getRGB(x, y);
        int channel =
                channelColor.equals(Color.RED)
                        ? (rgb >> 16) & 0xff
                        : channelColor.equals(Color.GREEN)
                        ? (rgb >> 8) & 0xff
                        : channelColor.equals(Color.BLUE) ? rgb & 0xff : 0;
        channelImage.setRGB(x, y, (255 << 24) | (channel << 16) | (channel << 8) | channel);
      }
    }

    imageDisplayLabel.setIcon(new ImageIcon(channelImage));
    imageDisplayLabel.repaint();
  }

  private void selectColorChannel() {
    String[] options = {"Красный", "Зеленый", "Синий"};
    int choice =
            JOptionPane.showOptionDialog(
                    this,
                    "Выберите канал:",
                    "Выбор канала",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]);

    switch (choice) {
      case 0:
        displayColorChannel(Color.RED);
        break;
      case 1:
        displayColorChannel(Color.GREEN);
        break;
      case 2:
        displayColorChannel(Color.BLUE);
        break;
      default:
    }
  }

  private void convertToGrayscale() {
    for (int x = 0; x < currentImage.getWidth(); x++) {
      for (int y = 0; y < currentImage.getHeight(); y++) {
        Color originalColor = new Color(originalImage.getRGB(x, y));
        int grayLevel =
                (int)
                        (originalColor.getRed() * 0.299
                                + originalColor.getGreen() * 0.587
                                + originalColor.getBlue() * 0.114);
        Color grayColor = new Color(grayLevel, grayLevel, grayLevel);
        currentImage.setRGB(x, y, grayColor.getRGB());
      }
    }
    imageDisplayLabel.setIcon(new ImageIcon(currentImage));
  }

  private void adjustBrightness() {
    try {
      String input =
              JOptionPane.showInputDialog(
                      this, "Введите значение для увеличения яркости (например, 1.5):");
      if (input != null && !input.isEmpty()) {
        float scaleFactor = Float.parseFloat(input);

        if (scaleFactor >= 1.1) {
          float[] scales = new float[currentImage.getColorModel().getNumComponents()];
          float[] offsets = new float[currentImage.getColorModel().getNumComponents()];
          Arrays.fill(scales, scaleFactor);
          Arrays.fill(offsets, 0);

          RescaleOp op = new RescaleOp(scales, offsets, null);
          currentImage = op.filter(currentImage, null);
          imageDisplayLabel.setIcon(new ImageIcon(currentImage));
          imageDisplayLabel.repaint();
        } else {
          JOptionPane.showMessageDialog(
                  this,
                  "Фактор увеличения яркости должен быть больше или равен 1.1.",
                  "Ошибка",
                  JOptionPane.ERROR_MESSAGE);
        }
      }
    } catch (Exception e) {
      JOptionPane.showMessageDialog(
              this,
              "Ошибка при увеличении яркости: " + e.getMessage(),
              "Ошибка",
              JOptionPane.ERROR_MESSAGE);
    }
  }

  private void resetImageChanges() {
    currentImage = cloneImage(originalImage);
    imageDisplayLabel.setIcon(new ImageIcon(currentImage));
    imageDisplayLabel.repaint();
  }

  private void displayNegativeImage() {
    int width = currentImage.getWidth();
    int height = currentImage.getHeight();
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        int rgb = currentImage.getRGB(x, y);
        Color color = new Color(rgb, true);
        Color negative = new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue());
        currentImage.setRGB(x, y, negative.getRGB());
      }
    }
    imageDisplayLabel.setIcon(new ImageIcon(currentImage));
    imageDisplayLabel.repaint();
  }

  private void applyAverageFilter() {
    String input = JOptionPane.showInputDialog(this, "Введите размер ядра (например, 3):");
    if (input != null && !input.isEmpty()) {
      try {
        int kernelSize = Integer.parseInt(input.trim());
        if (kernelSize > 1) {
          BufferedImage newImage = new BufferedImage(currentImage.getWidth(), currentImage.getHeight(), currentImage.getType());
          for (int x = 0; x < currentImage.getWidth(); x++) {
            for (int y = 0; y < currentImage.getHeight(); y++) {
              int red = 0, green = 0, blue = 0;
              int count = 0;
              for (int i = -kernelSize / 2; i <= kernelSize / 2; i++) {
                for (int j = -kernelSize / 2; j <= kernelSize / 2; j++) {
                  int newX = x + i;
                  int newY = y + j;
                  if (newX >= 0 && newX < currentImage.getWidth() && newY >= 0 && newY < currentImage.getHeight()) {
                    Color color = new Color(currentImage.getRGB(newX, newY));
                    red += color.getRed();
                    green += color.getGreen();
                    blue += color.getBlue();
                    count++;
                  }
                }
              }
              newImage.setRGB(x, y, new Color(red / count, green / count, blue / count).getRGB());
            }
          }
          currentImage = newImage;
          imageDisplayLabel.setIcon(new ImageIcon(currentImage));
          imageDisplayLabel.repaint();
        } else {
          JOptionPane.showMessageDialog(this, "Размер ядра должен быть больше 1.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
      } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Некорректный формат ввода.", "Ошибка", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void drawRectangleOnImage() {
    try {
      String input =
              JOptionPane.showInputDialog(
                      this, "Введите координаты верхнего левого угла x, y и размеры width, height через запятую:");
      if (input != null && !input.isEmpty()) {
        String[] parts = input.split(",");
        int x = Integer.parseInt(parts[0].trim());
        int y = Integer.parseInt(parts[1].trim());
        int width = Integer.parseInt(parts[2].trim());
        int height = Integer.parseInt(parts[3].trim());

        if (x >= 0
                && y >= 0
                && width > 0
                && height > 0
                && x + width <= currentImage.getWidth()
                && y + height <= currentImage.getHeight()) {
          Graphics2D g2d = currentImage.createGraphics();
          g2d.setColor(Color.BLUE);
          g2d.drawRect(x, y, width, height);
          g2d.dispose();
          imageDisplayLabel.setIcon(new ImageIcon(currentImage));
          imageDisplayLabel.repaint();
        } else {
          JOptionPane.showMessageDialog(
                  this,
                  "Некорректные координаты или размеры прямоугольника.",
                  "Ошибка",
                  JOptionPane.ERROR_MESSAGE);
        }
      }
    } catch (NumberFormatException e) {
      JOptionPane.showMessageDialog(
              this, "Некорректный формат ввода.", "Ошибка", JOptionPane.ERROR_MESSAGE);
    }
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new ImageProcessor());
  }
}
