package com.example._pill_20102669_datastruc;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.*;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.Rectangle;

import javafx.scene.input.MouseEvent;


public class PillController {
    UnionFind uf;
    public Pane origPane;
    private Image origImage;
    private UnionFind unionFind;
    private int noiseThreshold = 100;
    @FXML
    private ImageView bAndWImageView;
    @FXML
    private ImageView normalImageView;
    @FXML
    private Slider thresholdSlider;
    @FXML
    Label thresholdLabel;

    @FXML
    private TextArea rectInfoDisplay;



    public void initialize() {
        thresholdSlider.setMin(0);
        thresholdSlider.setMax(300);
        thresholdSlider.setValue(noiseThreshold);
        thresholdLabel.setText("Threshold: " + noiseThreshold);

        thresholdSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            noiseThreshold = newValue.intValue();
            thresholdLabel.setText("Threshold: " + noiseThreshold);
            imageViewUpdateWithNoiseReduced(uf.parent);
        });
    }



    @FXML
    private void openFile() {                       //Opens a file chooser dialog to select an image file
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.bmp", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            origImage = new Image(selectedFile.toURI().toString(),bAndWImageView.getFitWidth(),bAndWImageView.getFitHeight(),false,true);
            bAndWImageView.setImage(origImage);
            normalImageView.setImage(origImage);
        }
    }

    @FXML
    private void onImageClick(MouseEvent event) {    // Handles a mouse click on the ImageView, turns black and white, and marks rectangles
        if (origImage == null) {                     // Check if the original image exists
            return;
        }
        int x = (int) event.getX();                  // Get the x and y coordinates of the click
        int y = (int) event.getY();
        PixelReader pixelReader = origImage.getPixelReader();
        Color clickedColor = pixelReader.getColor(x, y);            // Retrieve clicked position and color

        System.out.println("Clicked coordinates: (" + x + ", " + y + ")");
        System.out.println("Clicked color: " + clickedColor);
        BlackWhite(clickedColor);                  // Processes image for black and white conversion

        int width = (int) origImage.getWidth();          // Retrieve image dimensions
        int height = (int) origImage.getHeight();
        this.unionFind = new UnionFind(width * height);          // Initialize UnionFind for component detection

        processDSArrayusingUnionFind(origImage);          // Process the image using UnionFind to detect components

        int componentCount = componentCount(this.unionFind, width, height);        // Counts the number of found components

        System.out.println("Component count: " + componentCount);

        reduceNoise(uf.parent, noiseThreshold);                // Apply noise reduction to found components

        rectPosit(uf.parent);               // Retrieve and mark the positions of rectangles from the processed components

    }




    public void BlackWhite(Color targetColor) {

        if (origImage == null) {          // Check if the original image is loaded
            System.out.println("No image loaded.");
            return;
        }

        int height = (int) origImage.getHeight();
        int width = (int) origImage.getWidth();       // Get the dimensions of the original image.

        uf = new UnionFind(width * height);     // Initialize the Union-Find data structure for the image, treating each pixel as a separate set initially.

        WritableImage updatedImage = new WritableImage(width, height);
        PixelWriter pixelWriter = updatedImage.getPixelWriter();
        PixelReader pixelReader = origImage.getPixelReader();


        int countWhite = 0;
        int countBlack = 0;
        int totalPixels = width * height;

        for (int i = 0; i < totalPixels; i++) {
            int x = i % width;
            int y = i / width;

            Color color = pixelReader.getColor(x, y);
            if (similarCol(targetColor, color, 0.05)) {         // Check if the current pixel's color is similar to the target color within a tolerance.
                pixelWriter.setColor(x, y, Color.WHITE);
                countWhite++;
            } else {
                pixelWriter.setColor(x, y, Color.BLACK);
                uf.parent[y * width + x] = -1;
                countBlack++;
            }


            bAndWImageView.setImage(updatedImage);
        }

        bAndWImageView.setImage(updatedImage);
        System.out.println("Image view is now black and white!");
        System.out.println("White pixels: " + countWhite);
        System.out.println("Black pixels: " + countBlack);
    }

    @FXML
    private void ResetToOriginal() {
        if (origImage != null) {

            ImageView newImageView = new ImageView();
            newImageView.setImage(origImage);            // Create a new ImageView instance and set the original image
            bAndWImageView.setImage(newImageView.getImage());           // Update the existing ImageView with the new one

            System.out.println("Image reset");
        }
    }


    public static boolean similarCol(Color c1, Color c2, double tolerance) {         // Compares two colors and returns true if they are similar, false if not within tolerance
        double hueDifference = Math.abs(c1.getHue() - c2.getHue()) / 360.0;
        double satDifference = Math.abs(c1.getSaturation() - c2.getSaturation());
        double brightDifference = Math.abs(c1.getBrightness() - c2.getBrightness());

        double weightedDiff = (hueDifference * 0.4) + (satDifference * 0.3) + (brightDifference * 0.3);
        return weightedDiff < tolerance;
    }


    public void processDSArrayusingUnionFind(Image image) {      // Identifies connected components in an image with Union-Find.
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int numPixels = width * height;

        // Iterate through each pixel
        for (int pixelIndex = 0; pixelIndex < numPixels; pixelIndex++) {
            if (uf.parent[pixelIndex] != -1) {            // Checks if the current pixel is part of a pill
                int x = pixelIndex % width;
                int y = pixelIndex / width;                 // Calculate x and y from pixelIndex

                if (x < width - 1) {                            // Check the right neighbor
                    int rightIndex = pixelIndex + 1;
                    if (uf.parent[rightIndex] != -1) {
                        uf.union(pixelIndex, rightIndex);
                    }
                }
                if (y < height - 1) {                             // Check the bottom neighbor
                    int bottomIndex = pixelIndex + width;
                    if (uf.parent[bottomIndex] != -1) {
                        uf.union(pixelIndex, bottomIndex);
                    }
                }
            }
        }
        uf.displayDSAsText(width);         // Display the final state of the disjoint set

    }

    public int componentCount(UnionFind uf, int width, int height) {       // Counts pills components in the image.
        int numElements = width * height;
        boolean[] isRoot = new boolean[numElements];
        int uniqueCount = 0;

        for (int i = 0; i < numElements; i++) {
            int root = uf.find(i);
            if (!isRoot[root]) {
                isRoot[root] = true;
                uniqueCount++;
            }
        }

        return uniqueCount;
    }


    public void rectPosit(int[] imageArray) {
        Platform.runLater(() -> {
            origPane.getChildren().removeIf(node -> node instanceof Rectangle || node instanceof Text);            // Clear existing rectangles and text from the pane
            int width = (int) bAndWImageView.getImage().getWidth();            // Calculate the image width

            Map<Integer, List<int[]>> pixelGroups = new HashMap<>();        // Create a map to group pixels by their root value
            for (int i = 0; i < imageArray.length; i++) {
                if (imageArray[i] != -1) {
                    int root = rootFinder(imageArray, i);
                    pixelGroups.computeIfAbsent(root, k -> new ArrayList<>())
                            .add(new int[]{i % width, i / width});
                }
            }

            List<Rect> rectanglesWithPositions = pixelGroups.values().stream()     // Calculate rectangles from pixel groups and filters valid ones

                    .map(pixels -> {
                        int minX = pixels.stream().mapToInt(pixel -> pixel[0]).min().orElse(Integer.MAX_VALUE);
                        int minY = pixels.stream().mapToInt(pixel -> pixel[1]).min().orElse(Integer.MAX_VALUE);
                        int maxX = pixels.stream().mapToInt(pixel -> pixel[0]).max().orElse(0);
                        int maxY = pixels.stream().mapToInt(pixel -> pixel[1]).max().orElse(0);

                        int rectWidth = maxX - minX + 1;
                        int rectHeight = maxY - minY + 1;

                        return rectWidth > 2 && rectHeight > 2
                                ? new Rect(new Rectangle(minX, minY, rectWidth, rectHeight), minX, minY)
                                : null;
                    })
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparingInt(Rect::getMinYCoord).thenComparingInt(Rect::getMinXCoord))
                    .collect(Collectors.toList());

            StringBuilder infoText = new StringBuilder();
            infoText.append("Total Pill: ").append(rectanglesWithPositions.size()).append("\n");

            int label = 1;
            for (Rect rwp : rectanglesWithPositions) {
                Rectangle javafxRect = rwp.getRect();
                javafxRect.setStroke(Color.GREEN);
                javafxRect.setFill(Color.TRANSPARENT);
                origPane.getChildren().add(javafxRect);

                Text labelText = new Text(
                        javafxRect.getX() + javafxRect.getWidth() / 2,
                        javafxRect.getY() + javafxRect.getHeight() / 2,
                        "#" + label
                );
                labelText.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                origPane.getChildren().add(labelText);

                double size = javafxRect.getWidth() * javafxRect.getHeight();
                infoText.append("Pill ").append(label).append(" Size: ").append(String.format("%.2f", size)).append("\n");
                label++;
            }
            updateInfoDisplay(infoText.toString());              // Update the information display area with rectangle details

        });
    }
    public void updateInfoDisplay(String infoText) {
        rectInfoDisplay.setText(infoText);
    }

    private int rootFinder(int[] imageArray, int index) {        //Utility method used to find root of a component in the Union-Find structure.
        if (index < 0 || index >= imageArray.length) {
            return -1;
        }

        int current = index;
        while (current >= 0 && current < imageArray.length && imageArray[current] != current) {
            if (imageArray[current] == -1) {
                return -1;
            }
            current = imageArray[current];
        }
        return (current >= 0 && current < imageArray.length) ? current : -1;
    }

    public void reduceNoise(int[] imageArray, int threshold) {                   // Applies noise reduction to the image.
        List<Integer> sizes = new ArrayList<>();
        Map<Integer, Integer> indexToRoot = new HashMap<>();

        for (int index = 0; index < imageArray.length; index++) {                 //Determines root and track sizes in a List

            int root = rootFinder(imageArray, index);
            if (root != -1) {
                indexToRoot.put(index, root);
                while (sizes.size() <= root) {
                    sizes.add(0);
                }
                sizes.set(root, sizes.get(root) + 1);
            }
        }

        Set<Integer> smallRoots = new HashSet<>();                      // Identifies roots with sizes below the threshold
        for (int i = 0; i < sizes.size(); i++) {
            if (sizes.get(i) < threshold) {
                smallRoots.add(i);
            }
        }
        for (int index = 0; index < imageArray.length; index++) {                //Updates imageArray based on identified small roots
            int root = indexToRoot.getOrDefault(index, -1);
            if (smallRoots.contains(root)) {
                imageArray[index] = -1;
            }
        }
    }
    // Updates the image view with the reduced noise.
    private void imageViewUpdateWithNoiseReduced(int[] imageArray) {
        for (int i = 0; i < imageArray.length; i++) {                     // Apply noise reduction to the image array based on a predefined threshold.

            if (Math.abs(imageArray[i]) < noiseThreshold) {
                imageArray[i] = 0; // Reduces noise by zeroing out small values.
            }
        }
        Image updatedImage = convertArrayToImage(imageArray);        // Convert the modified image array into an Image object.
        Platform.runLater(() -> bAndWImageView.setImage(updatedImage));      // Update the ImageView on the JavaFX application thread.
    }
    private Image convertArrayToImage(int[] imageArray) {
        return null;
    }


    public void colorDisjointSets() {
        int imgW = (int) bAndWImageView.getImage().getWidth();
        int imgH = (int) bAndWImageView.getImage().getHeight();               // Retrieves image dimensions

        WritableImage newImage = new WritableImage(imgW, imgH);               // Create an image to colour
        PixelWriter pxWriter = newImage.getPixelWriter();
        Map<Integer, Color> colorMapping = new HashMap<>();                   //Stores colors associated with each set

        for (int posY = 0; posY < imgH; posY++) {                              // Process each pixel in the image
            for (int posX = 0; posX < imgW; posX++) {
                int pixelPos = posY * imgW + posX;                             // Calculate the pixel's linear position
                int setRepresentative = uf.find(pixelPos);                      // Find the root of the set for this pixel

                colorMapping.computeIfAbsent(setRepresentative, unused -> randColGen());    //Assigns colour to set if none exist
                pxWriter.setColor(posX, posY, colorMapping.get(setRepresentative));         // Color the pixel based on its set's color

            }
        }
        bAndWImageView.setImage(newImage);                    // Display the colored image
    }
    public Color randColGen() {
        Random random = new Random();
        double red = random.nextDouble();
        double green = random.nextDouble();
        double blue = random.nextDouble();
        return new Color(red, green, blue, 1.0);
    }


    @FXML
    private void handleClose(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).hide();    //closes app
    }
}