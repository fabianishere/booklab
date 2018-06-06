package nl.tudelft.booklab.vision.detection.opencv;

import nl.tudelft.booklab.vision.detection.BookDetector;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.opencv.core.Core.*;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.line;

public abstract class AbstractBookDetector implements BookDetector {

    static {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * Uses a list of x coordinates to split the image on
     *
     * @param image         openCV matrix containing an image
     * @param cropLocations list of x coordinates of each book-segement
     * @param strictCrop    boolean
     * @return list of separated book segments
     */
    @NotNull
    static List<Mat> cropBooks(Mat image, List<Integer> cropLocations, boolean strictCrop) {
        List<Mat> books = new ArrayList<>();
        for (int i = 0; i < cropLocations.size() - 1; i++) {
            Mat book = cropBook(image, cropLocations.get(i), cropLocations.get(i + 1) - cropLocations.get(i), strictCrop);
            books.add(book);
        }
        return books;
    }

    /**
     * Crops a segment from the image
     *
     * @param image      openCV matrix containing an image
     * @param x          x-coordinate
     * @param width      width of segment
     * @param strictCrop boolean
     * @return cropped image
     */
    @NotNull
    static Mat cropBook(Mat image, int x, int width, boolean strictCrop) {
        if (!strictCrop) {
            width = (int) (1.1 * (width + 0.05 * x));
            x = (int) (0.95 * x);
        }

        if (x + width > image.cols()) {
            width = width - (x + width - image.cols());
        }

        Rect roi = new Rect(x, 0, width, image.rows());
        return new Mat(image, roi);
    }


    /**
     * Finds the shelves in an image
     *
     * @param image openCV matrix containing an image
     * @param mask  openCV matrix containing a binary image of text regions
     * @return map of shelves with each key-value pair being an image of
     * the shelf and an image of its text regions
     */
    static Map<Mat, Mat> findShelves(Mat image, Mat mask) {
        Mat rotatedMask = new Mat();
        rotate(mask, rotatedMask, ROTATE_90_COUNTERCLOCKWISE);

        List<Integer> cropLocations = findCropLocations(rotatedMask, REDUCE_MAX);
        Map<Integer, Integer> locations = findShelfLocations(image, cropLocations);
        List<Mat> shelves = cropShelves(image, locations);
        List<Mat> shelfMasks = cropShelves(mask, locations);

        Map<Mat, Mat> shelfMaskMap = new HashMap<>();
        for (int i = 0; i < shelves.size(); i++) {
            shelfMaskMap.put(shelves.get(i), shelfMasks.get(i));
        }

        return shelfMaskMap;
    }

    /**
     * Finds the locations of the shelves in an image
     *
     * @param image         openCV matrix containing an image
     * @param cropLocations list of the locations of local minima on the y-axis of the image
     * @return map of locations of the shelves with each key-value pair
     * being the min and max y-coordinates of a shelf
     */
    @NotNull
    static Map<Integer, Integer> findShelfLocations(Mat image, List<Integer> cropLocations) {
        Map<Integer, Integer> locations = new HashMap<>();

        for (int i = 1; i < cropLocations.size(); i++) {
            int distance = cropLocations.get(i) - cropLocations.get(i - 1);

            if (distance > 0.2 * image.width()) {
                locations.put(cropLocations.get(i - 1), cropLocations.get(i));
            }
        }
        return locations;
    }


    /**
     * Crops segments from an image corresponding to the found shelf locations
     *
     * @param image     openCV matrix containing an image
     * @param locations map of locations of the shelves with each key-value pair
     *                  being the min and max y-coordinates of a shelf
     * @return list of images of shelves
     */
    static List<Mat> cropShelves(Mat image, Map<Integer, Integer> locations) {
        List<Mat> shelves = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : locations.entrySet()) {
            int y = entry.getKey();
            int height = entry.getValue() - y;
            Mat shelf = cropShelf(image, y, height);
            shelves.add(shelf);
        }

        return shelves;
    }

    /**
     * Crops a segment from the image
     *
     * @param image  openCV matrix containing an image
     * @param y      y-coordinate
     * @param height height of segment
     * @return cropped image
     */
    @NotNull
    static Mat cropShelf(Mat image, int y, int height) {
        if (y + height > image.rows()) {
            height = height - (y + height - image.rows());
        }

        Rect roi = new Rect(0, y, image.cols(), height);
        return new Mat(image, roi);
    }

    /**
     * Finds the locations where to split the image on the x-axis based on local minima
     *
     * @param image openCV matrix containing an image
     * @param reduceType type of openCV reduction to apply, either REDUCE_MAX or REDUCE_AVG
     * @return list of x-coordinates
     */
    @NotNull
    static List<Integer> findCropLocations(Mat image, int reduceType) {
        Mat reduced = new Mat();
        List<Point> coordinates = new ArrayList<>();

        reduce(image, reduced, 0, reduceType);
        GaussianBlur(reduced, reduced, new Size(), 3);

        for (int i = 0; i < image.cols(); i++) {
            coordinates.add(new Point(i, reduced.get(0, i)[0]));
        }

        List<Integer> localMinima = findLocalMinima(coordinates, 5);
        localMinima.add(0, 0);
        localMinima.add(image.cols());

        return localMinima;
    }

    /**
     * Find the local minima in a list of coordinates
     *
     * @param coordinates list of coordinate points
     * @param windowSize  size of window
     * @return list of indices of local minima
     */
    @NotNull
    static List<Integer> findLocalMinima(List<Point> coordinates, int windowSize) {
        List<Double> yCoordinates = coordinates.stream().map(a -> a.y).collect(Collectors.toList());
        List<Integer> localMinima = new ArrayList<>();
        for (int i = windowSize; i < coordinates.size() - windowSize; i++) {
            List<Double> sublist = yCoordinates.subList(i - windowSize, i + windowSize + 1);
            if (sublist.indexOf(Collections.min(sublist)) == windowSize) {
                localMinima.add((int) coordinates.get(i).x);
            }
        }
        return localMinima;
    }

    /**
     * Debug drawing: draws segments and the graph on which the segmentation is based, useful for debugging
     *
     * @param reducedImage  openCV matrix with reduced image
     * @param originalImage openCV matrix containing an image
     * @param lineLocations locations of lines
     */
    static void drawGraphs(Mat reducedImage, Mat originalImage, List<Integer> lineLocations) {
        for (int i = 0; i < originalImage.cols(); i++) {
            line(originalImage, new Point(i, 0), new Point(i, reducedImage.get(0, i)[0]), new Scalar(255, 255, 0), 2);
        }

        for (Integer lineLocation : lineLocations) {
            line(originalImage, new Point(lineLocation, 0), new Point(lineLocation, originalImage
                .rows()), new Scalar(0, 255, 0), 6);
        }
    }
}
