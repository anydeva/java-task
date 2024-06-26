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
  private BufferedImage processedImage;
  private JLabel imageLabel;

  public ImageProcessor() {
    imageLabel = new JLabel(); // Инициализация imageLabel
    add(imageLabel); // Добавление imageLabel в JFrame
    // Загрузка и отображение изображения
    selectImage();

    // Создание меню
    createMenu();

    // Настройка окна приложения
    setupWindow();
  }

  private BufferedImage deepCopy(BufferedImage bi) {
    ColorModel cm = bi.getColorModel();
    boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
    WritableRaster raster = bi.copyData(null);
    return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
  }

  private void selectImage() {
    // Предложение пользователю выбрать изображение или использовать изображение по умолчанию
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
      // Пользователь выбрал загрузку изображения с компьютера
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
          processedImage = deepCopy(originalImage);
          imageLabel.setIcon(new ImageIcon(processedImage));
          pack(); // Обновить размер окна в соответствии с новым изображением
        } catch (IOException e) {
          JOptionPane.showMessageDialog(
              this,
              "Не удалось загрузить изображение: " + e.getMessage(),
              "Ошибка",
              JOptionPane.ERROR_MESSAGE);
        }
      }
    } else if (choice == JOptionPane.NO_OPTION) {
      // Пользователь выбрал использование изображения по умолчанию
      try {
        // Загрузка изображения по умолчанию из ресурсов приложения
        originalImage = ImageIO.read(getClass().getResource("/image.png"));
        processedImage = deepCopy(originalImage);
        imageLabel.setIcon(new ImageIcon(processedImage));
        pack(); // Обновите размер окна в соответствии с изображением по умолчанию
      } catch (IOException e) {
        JOptionPane.showMessageDialog(
            this,
            "Не удалось загрузить изображение по умолчанию: " + e.getMessage(),
            "Ошибка",
            JOptionPane.ERROR_MESSAGE);
      }
    } else {
      // Пользователь закрыл диалоговое окно
      JOptionPane.showMessageDialog(
          this,
          "Не выбрано изображение. Приложение будет закрыто.",
          "Закрытие приложения",
          JOptionPane.INFORMATION_MESSAGE);
      System.exit(0); // Закрыть приложение
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

    // Добавление пунктов меню
    addMenuItem(menu, "Выбрать канал", e -> chooseChannel());
    addMenuItem(menu, "Переключить оттенки серого", e -> toggleGrayscale());
    addMenuItem(menu, "Обрезать изображение", e -> cropImage());
    addMenuItem(menu, "Повысить яркость", e -> increaseBrightness());
    addMenuItem(menu, "Нарисовать круг", e -> drawCircle());
    addMenuItem(menu, "Сбросить изменения", e -> resetChanges());
    addMenuItem(menu, "Выбрать изображение", e -> selectImage());

    setJMenuBar(menuBar);
  }

  private void setupWindow() {
    // Загрузка иконки
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

  private void showChannel(Color channelColor) {
    int width = processedImage.getWidth();
    int height = processedImage.getHeight();
    BufferedImage channelImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        int rgb = processedImage.getRGB(x, y);
        int channel =
            channelColor.equals(Color.RED)
                ? (rgb >> 16) & 0xff
                : channelColor.equals(Color.GREEN)
                    ? (rgb >> 8) & 0xff
                    : channelColor.equals(Color.BLUE) ? rgb & 0xff : 0;
        channelImage.setRGB(x, y, (255 << 24) | (channel << 16) | (channel << 8) | channel);
      }
    }

    imageLabel.setIcon(new ImageIcon(channelImage));
    imageLabel.repaint();
  }

  // Метод для выбора цветового канала
  private void chooseChannel() {
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
      case 0: // Красный
        showChannel(Color.RED);
        break;
      case 1: // Зеленый
        showChannel(Color.GREEN);
        break;
      case 2: // Синий
        showChannel(Color.BLUE);
        break;
      default:
        // Ничего не делаем, если пользователь закрыл диалоговое окно
    }
  }

  // Метод для переключения в оттенки серого
  private void toggleGrayscale() {
    for (int x = 0; x < processedImage.getWidth(); x++) {
      for (int y = 0; y < processedImage.getHeight(); y++) {
        Color originalColor = new Color(originalImage.getRGB(x, y));
        int grayLevel =
            (int)
                (originalColor.getRed() * 0.299
                    + originalColor.getGreen() * 0.587
                    + originalColor.getBlue() * 0.114);
        Color grayColor = new Color(grayLevel, grayLevel, grayLevel);
        processedImage.setRGB(x, y, grayColor.getRGB());
      }
    }
    imageLabel.setIcon(new ImageIcon(processedImage));
  }

  // Метод для обрезки изображения
  private void cropImage() {
    try {
      String input =
          JOptionPane.showInputDialog(
              this, "Введите координаты x, y и размеры обрезки width, height через запятую:");
      if (input != null && !input.isEmpty()) {
        String[] parts = input.split(",");
        int x = Integer.parseInt(parts[0].trim());
        int y = Integer.parseInt(parts[1].trim());
        int width = Integer.parseInt(parts[2].trim());
        int height = Integer.parseInt(parts[3].trim());

        // Проверка, что координаты и размеры находятся в пределах изображения
        if (x >= 0
            && y >= 0
            && width > 0
            && height > 0
            && x + width <= processedImage.getWidth()
            && y + height <= processedImage.getHeight()) {
          processedImage = processedImage.getSubimage(x, y, width, height);
          imageLabel.setIcon(new ImageIcon(processedImage));
          imageLabel.repaint(); // Обновление отображения
        } else {
          JOptionPane.showMessageDialog(
              this,
              "Некорректные координаты или размеры обрезки.",
              "Ошибка",
              JOptionPane.ERROR_MESSAGE);
        }
      }
    } catch (NumberFormatException e) {
      JOptionPane.showMessageDialog(
          this, "Некорректный формат ввода.", "Ошибка", JOptionPane.ERROR_MESSAGE);
    }
  }

  // Метод для увеличения яркости
  private void increaseBrightness() {
    try {
      String input =
          JOptionPane.showInputDialog(
              this, "Введите значение для увеличения яркости (например, 1.5):");
      if (input != null && !input.isEmpty()) {
        float scaleFactor = Float.parseFloat(input);

        if (scaleFactor >= 1.1) {
          // Проверка типа изображения и настройка массивов для RescaleOp
          float[] scales = new float[processedImage.getColorModel().getNumComponents()];
          float[] offsets = new float[processedImage.getColorModel().getNumComponents()];
          Arrays.fill(scales, scaleFactor);
          Arrays.fill(offsets, 0);

          RescaleOp op = new RescaleOp(scales, offsets, null);
          processedImage = op.filter(processedImage, null);
          imageLabel.setIcon(new ImageIcon(processedImage));
          imageLabel.repaint();
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

  // Метод для рисования круга
  private void drawCircle() {
    try {
      String input =
          JOptionPane.showInputDialog(
              this, "Введите координаты центра круга x, y и радиус через запятую:");
      if (input != null && !input.isEmpty()) {
        String[] parts = input.split(",");
        int x = Integer.parseInt(parts[0].trim());
        int y = Integer.parseInt(parts[1].trim());
        int radius = Integer.parseInt(parts[2].trim());

        // Проверка, что координаты центра и радиус находятся в пределах изображения
        if (x - radius >= 0
            && y - radius >= 0
            && x + radius <= processedImage.getWidth()
            && y + radius <= processedImage.getHeight()) {
          Graphics2D g2d = processedImage.createGraphics();
          g2d.setColor(Color.RED);
          g2d.drawOval(x - radius, y - radius, radius * 2, radius * 2);
          g2d.dispose();
          imageLabel.setIcon(new ImageIcon(processedImage));
        } else {
          JOptionPane.showMessageDialog(
              this,
              "Некорректные координаты центра или радиус.",
              "Ошибка",
              JOptionPane.ERROR_MESSAGE);
        }
      }
    } catch (NumberFormatException e) {
      JOptionPane.showMessageDialog(
          this, "Некорректный формат ввода.", "Ошибка", JOptionPane.ERROR_MESSAGE);
    }
  }

  // Метод для сброса изменений
  private void resetChanges() {
    processedImage = deepCopy(originalImage);
    imageLabel.setIcon(new ImageIcon(processedImage));
    imageLabel.repaint();
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new ImageProcessor());
  }
}
