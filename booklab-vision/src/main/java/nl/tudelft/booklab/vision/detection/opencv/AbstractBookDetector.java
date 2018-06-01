package nl.tudelft.booklab.vision.detection.opencv;

import nl.tudelft.booklab.vision.detection.BookDetector;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.opencv.core.Core.reduce;
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

        List<Integer> cropLocations = new ArrayList<>(localMinima);
        cropLocations.add(0, 0);
        cropLocations.add(image.cols());

        return cropLocations;
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
