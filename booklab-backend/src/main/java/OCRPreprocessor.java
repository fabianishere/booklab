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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.opencv.core.*;
import org.opencv.features2d.MSER;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.Core.*;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.*;

/**
 * Class containing functionality to preprocess images for OCR with Tesseract
 */
public class OCRPreprocessor {

    // init OpenCV
    static {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * Optimize the image
     *
     * @param image openCV matrix containing image
     * @return image
     */
    public static Mat optimizeImg(Mat image) {
        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
        // grayscale
        Mat gray = new Mat();
        cvtColor(image, gray, COLOR_BGR2GRAY);
        // get edges
        Mat edges = ImgProcessHelper.autoCanny(gray);

        dilate(edges, edges, getStructuringElement(MORPH_ELLIPSE, new Size(2, 2)));
        erode(edges, edges, getStructuringElement(MORPH_ELLIPSE, new Size(2, 2)));

        // get contours
        Imgproc.findContours(edges, contours, hierarchy,
            Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);
        // retrieve keepers
        List<MatOfPoint> keepers = findKeepers(contours, image, hierarchy);

        Mat new_image = new Mat();
        edges.copyTo(new_image);
        new_image.setTo(new Scalar(255,255,255));

        createNewImageFromContours(keepers, new_image, image);

        blur(new_image, new_image, new Size(2,2));
        rotate(new_image, new_image, ROTATE_90_COUNTERCLOCKWISE);

        drawContours(image, keepers, -1, new Scalar(0, 0, 255));

        return new_image;

    }

    /**
     * Takes a list of contours, an image and a destination image and adds the contours to that new image
     * @param keepers list of contours
     * @param new_image blank image
     * @param image original image
     */
    private static void createNewImageFromContours(List<MatOfPoint> keepers, Mat new_image, Mat image) {
        // loop over keepers
        for (MatOfPoint contour : keepers) {
            double foregroundIntensity = 0.0;
            Point[] contourPoints = contour.toArray();
            double totalIntensity = Arrays.stream(contourPoints).mapToDouble(p -> getIntensity(image, (int) p.x, (int) p.y)).sum();

            foregroundIntensity += totalIntensity;
            foregroundIntensity /= contourPoints.length;

            Rect box = Imgproc.boundingRect(contour);
            int boxX = box.x;
            int boxY = box.y;
            int boxWidth = box.width;
            int boxHeight = box.height;
            double[] backgroundIntensities = {
                getIntensity(image, boxX - 1, boxY - 1),
                getIntensity(image, boxX - 1, boxY),
                getIntensity(image, boxX, boxY - 1),
                getIntensity(image, boxX + boxWidth + 1, boxY - 1),
                getIntensity(image, boxX + boxWidth, boxY - 1),
                getIntensity(image, boxX + boxWidth + 1, boxY),
                getIntensity(image, boxX - 1, boxY + boxHeight + 1),
                getIntensity(image, boxX - 1, boxY + boxHeight),
                getIntensity(image, boxX, boxY + boxHeight + 1),
                getIntensity(image, boxX + boxWidth + 1, boxY + boxHeight + 1),
                getIntensity(image, boxX + boxWidth, boxY + boxHeight + 1),
                getIntensity(image, boxX + boxWidth + 1, boxY + boxHeight)};

            Arrays.sort(backgroundIntensities);
            double median = backgroundIntensities[6];

            int foregroundColor = 255;
            int backgroundColor = 0;

            if (foregroundIntensity < median) {
                foregroundColor = 0;
                backgroundColor = 255;
            }

            for (int x = boxX; x < boxX + boxWidth; x++) {
                for (int y = boxY; y < boxY + boxHeight; y++) {
                    if (x < image.width() && y < image.height()) {
                        if (getIntensity(image, x, y) > foregroundIntensity) {
                            new_image.put(y, x, backgroundColor);
                        } else {
                            new_image.put(y, x, foregroundColor);
                        }
                    }
                }
            }
        }
    }

    /**
     * Select suitable contours from list of contours
     *
     * @param contours  list of contours
     * @param image     source image
     * @param hierarchy contour hierarchy
     * @return list of selected contours
     */
    private static List<MatOfPoint> findKeepers(List<MatOfPoint> contours, Mat image, Mat hierarchy) {
        List<MatOfPoint> filtered = contours.stream()
            .filter(a -> keepContour(a, image))
            .collect(Collectors.toList());
        return filtered.stream()
            .filter(a -> includeBox(contours.indexOf(a), contours, filtered, hierarchy, image))
            .collect(Collectors.toList());
    }

    /**
     * Retrieve intensity of pixel in image
     *
     * @param image
     * @param x
     * @param y
     * @return
     */
    public static double getIntensity(Mat image, int x, int y) {
        // check if the pixel index is out of the frame
        if (x >= image.width() || y >= image.height() || x < 0 || y < 0)
            return 0;

        double[] pixel = image.get(y, x);
        return 0.30 * pixel[2] + 0.59 * pixel[1] + 0.11 * pixel[0];
    }

    /**
     * Count children of countour
     * @param index index of contour
     * @param contours list of contours
     * @param hierarchy hierarchy of contours
     * @param image source image
     * @return number of children
     */
    public static int countChildren(int index, List<MatOfPoint> contours,
                                    Mat hierarchy, Mat image) {
        int count = 0;
        // get the child contour index
        int child = (int) hierarchy.get(0, index)[2];

        if (child < 0) {
            return 0;
        }

        if (keepContour(contours.get(child), image)) {
            count = 1;
        }

        count += countSiblings(child, contours, hierarchy, image);

        return count;
    }


    /**
     * Count siblings of contour
     * @param index index of contour
     * @param contours all contours
     * @param hierarchy hierarchy structure
     * @param image source image
     * @return number of siblings
     */
    public static int countSiblings(int index, List<MatOfPoint> contours,
                                    Mat hierarchy, Mat image) {
        int count = 0;

        int next = (int) hierarchy.get(0, index)[0];

        // counting the children of the next contour
        while (next > 0) {
            if (keepContour(contours.get(next), image)) {
                count += 1;
            }
            next = (int) hierarchy.get(0, next)[0];
            if (next == index)
                break;
        }

        int prev = (int) hierarchy.get(0, index)[1];

        // counting the children of the previous contour
        while (prev > 0) {
            if (keepContour(contours.get(prev), image)) {
                count += 1;
            }
            prev = (int) hierarchy.get(0, prev)[1];
            if (prev == index)
                break;
        }

        return count;
    }

    /**
     * Check if contour is a child of any other contour
     * @param index index of contour
     * @param contours list of contours
     * @param hierarchy hierarchy structure
     * @param image source image
     * @return boolean
     */
    public static boolean isChild(int index, List<MatOfPoint> contours,
                                  Mat hierarchy, Mat image) {
        return getParent(index, contours, hierarchy, image) > 0;
    }

    /**
     * Check if contour should be kept
     * @param contour contour
     * @param image source image
     * @return boolean
     */
    public static boolean keepContour(MatOfPoint contour, Mat image) {
        return keepBox(contour, image) ;
    }

    /**
     * Check if box should be kept
     * @param contour contour
     * @param image source image
     * @return boolean
     */
    public static boolean keepBox(MatOfPoint contour, Mat image) {
        Rect rect = boundingRect(contour);
        double width = rect.width;
        double height = rect.height;
        int area = image.width() * image.height();

        if (width * height > 0.5 * area || height > 0.2 * image.height()) {
            return false;
        }

        if (width / height > 6 || height / width > 10) {
            return false;
        }

        if (contourArea(contour) < 0.01) {
            return false;
        }

        return true;
    }

    /**
     * Check if box should be included
     *
     * @param index     index
     * @param contours  list of contours
     * @param hierarchy hierarchy structure
     * @param image     source image
     * @return boolean
     */
    public static boolean includeBox(int index, List<MatOfPoint> contours, List<MatOfPoint> keepers,
                                     Mat hierarchy, Mat image) {
        int parent = getParent(index, contours, hierarchy, image);

        return !(isChild(index, contours, hierarchy, image) && keepers.contains(contours.get(parent)));
    }

    /**
     * Retrieve parent of contour
     *
     * @param index     index
     * @param contours  list of contours
     * @param hierarchy hierarchy structure for contours
     * @param image     source image
     * @return index of parent
     */
    private static int getParent(int index, List<MatOfPoint> contours, Mat hierarchy, Mat image) {
        // if it is a child of a accepting contour and has no children it is
        // probably the interior of a letter
        int parent = (int) hierarchy.get(0, index)[3];

        // searches until a valid parent is found
        while (parent > 0 && !keepContour(contours.get(parent), image)) {
            parent = (int) hierarchy.get(0, parent)[3];
        }
        return parent;
    }


    public static void main(String[] args) {
        String path = System.getProperty("user.dir") + "/booklab-backend/resources/books/roi_1.jpg";
        String outputpath = System.getProperty("user.dir") + "/booklab-backend/resources/roi_1_correctedbyOCRPreprocessor.jpg";
        Mat image = imread(path);
        Mat imtmp = optimizeImg(image);
        imwrite(outputpath, imtmp);
    }

}
