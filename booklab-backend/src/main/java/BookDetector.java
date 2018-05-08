/*
 * Copyright 2018 The BookLab Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.jetbrains.annotations.NotNull;
import org.opencv.core.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.opencv.core.Core.REDUCE_AVG;
import static org.opencv.core.Core.reduce;
import static org.opencv.imgproc.Imgproc.*;

/**
 * Class to detect books in an image
 */
public class BookDetector {

    /**
     * Init
     */
    static {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * Method to detect books in an image
     * @param image openCV matrix containing an image
     * @return list of images (openCV matrices)
     */
    public static List<Mat> detectBooks(Mat image) {
        image = ImgProcessHelper.colorhist_equalize(image);
        List<Integer> cropLocations = detectBookLocations(image);
        return cropBooks(image, cropLocations, false);
    }

    /**
     * Find the locations of the books in the image
     * @param image openCV matrix containing an image
     * @return list of x coordinates of each book-segement
     */
    private static List<Integer> detectBookLocations(Mat image) {
        Mat gray = new Mat();
        Mat dilation = new Mat();
        Mat reduced = new Mat();
        List<Point> coordinates = new ArrayList<>();

        image = ImgProcessHelper.colorhist_equalize(image);
        cvtColor(image, gray, COLOR_BGR2GRAY);
        Mat edges = ImgProcessHelper.autoCanny(gray);
        dilate(edges, dilation, new Mat());
        reduce(dilation, reduced, 0, REDUCE_AVG);
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
     * Uses a list of x coordinates to split the image on
     * @param image openCV matrix containing an image
     * @param cropLocations list of x coordinates of each book-segement
     * @param strictCrop boolean
     * @return list of separated book segments
     */
    @NotNull
    private static List<Mat> cropBooks(Mat image, List<Integer> cropLocations, boolean strictCrop) {
        List<Mat> books = new ArrayList<>();
        for (int i = 0; i < cropLocations.size() - 1; i++) {
            Mat book = cropBook(image, cropLocations.get(i), cropLocations.get(i + 1) - cropLocations.get(i), strictCrop);
            books.add(book);
        }
        return books;
    }

    /**
     * Crops a segment from the image
     * @param image openCV matrix containing an image
     * @param x x-coordinate
     * @param width width of segment
     * @param strictCrop boolean
     * @return cropped image
     */
    @NotNull
    private static Mat cropBook(Mat image, int x, int width, boolean strictCrop) {
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
     * Find the local minima in a list of coordinates
     * @param coordinates list of coordinate points
     * @param windowSize size of window
     * @return list of indices of local minima
     */
    @NotNull
    private static List<Integer> findLocalMinima(List<Point> coordinates, int windowSize) {
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
     * Debug drawing
     * @param reducedImage openCV matrix with reduced image
     * @param originalImage openCV matrix containing an image
     * @param lineLocations locations of lines
     */
    @Deprecated
    private static void drawGraphs(Mat reducedImage, Mat originalImage, List<Integer> lineLocations) {
        for (int i = 0; i < originalImage.cols(); i++) {
            line(originalImage, new Point(i, 0), new Point(i, reducedImage.get(0, i)[0]), new Scalar(255, 255, 0), 1);
        }

        for (int i = 0; i < lineLocations.size(); i++) {
            line(originalImage, new Point(lineLocations.get(i), 0), new Point(lineLocations.get(i), originalImage.rows()), new Scalar(0, 255, 0), 2);
        }
    }

}
