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

package nl.tudelft.booklab.vision.detection.opencv;

import org.jetbrains.annotations.NotNull;
import org.opencv.core.Mat;

import java.util.List;

import static org.opencv.imgproc.Imgproc.*;

/**
 * This class provides functionality for the detection of books in an image using the Canny Edge
 * Detection algorithm.
 */
public class CannyBookDetector extends AbstractBookDetector {

    @NotNull
    @Override
    public List<Mat> detect(@NotNull Mat mat) {
        return detectBooks(mat);
    }

    /**
     * Method to detect books in an image
     *
     * @param image openCV matrix containing an image
     * @return list of images (openCV matrices)
     */
    private static List<Mat> detectBooks(Mat image) {
        image = ImageProcessingHelper.colorhistEqualize(image);
        List<Integer> cropLocations = detectBookLocations(image);
        return cropBooks(image, cropLocations, false);
    }

    /**
     * Find the locations of the books in the image
     *
     * @param image openCV matrix containing an image
     * @return list of x coordinates of each book-segement
     */
    private static List<Integer> detectBookLocations(Mat image) {
        Mat gray = new Mat();
        Mat dilation = new Mat();

        image = ImageProcessingHelper.colorhistEqualize(image);
        cvtColor(image, gray, COLOR_BGR2GRAY);
        Mat edges = ImageProcessingHelper.autoCanny(gray);
        dilate(edges, dilation, new Mat());

        return findCropLocations(dilation);
    }


}
