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

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.StrictMath.max;
import static org.opencv.imgproc.Imgproc.Canny;
import static org.opencv.imgproc.Imgproc.equalizeHist;

/**
 * Container class for helper functions wrt image processing
 */
public class ImageProcessingHelper {
    /**
     * Color histogram equalization
     * @param img openCV matrix containing a BGR image
     * @return openCV matrix containing a BGR image with its color channels equalized
     */
    public static Mat colorhistEqualize(Mat img){
        Mat reassemble = new Mat();

        List<Mat> lim = new ArrayList<>(3);
        Core.split(img, lim);
        Mat mB = lim.get(0);
        Mat mG = lim.get(1);
        Mat mR = lim.get(2);

        equalizeHist(mB, mB);
        equalizeHist(mG, mG);
        equalizeHist(mR, mR);
        List<Mat> listMat = Arrays.asList(mB, mG, mR);
        Core.merge(listMat, reassemble);
        return reassemble;
    }

    /**
     * Select the edges from an image with the Canny method with adaptive thresholding
     * @param img openCV matrix containing an image
     * @return openCV matrix containing the edges in the image
     */
    public static Mat autoCanny(Mat img){
        float sigma = 0.33f;
        // median
        float v = (float)getMedian(img);

        Mat edges = new Mat();
        double lower = (double) max(0, (1.0-sigma)*v);
        double upper = (double) max(255, (1.0+sigma)*v);
        Canny(img, edges, lower, upper);
        return edges;
    }

    /**
     * Retrieve the median value of an openCV matrix
     * @param mat openCV matrix
     * @return median
     */
    public static int getMedian(Mat mat) {
        ArrayList<Mat> listOfMat = new ArrayList<>();
        listOfMat.add(mat);
        MatOfInt channels = new MatOfInt(0);
        Mat mask = new Mat();
        Mat hist = new Mat(256, 1, CvType.CV_8UC1);
        MatOfInt histSize = new MatOfInt(256);
        MatOfFloat ranges = new MatOfFloat(0, 256);

        Imgproc.calcHist(listOfMat, channels, mask, hist, histSize, ranges);

        double t = mat.rows() * mat.cols() / 2;
        double total = 0;
        int med = -1;
        for (int row = 0; row < hist.rows(); row++) {
            double val = hist.get(row, 0)[0];
            if ((total <= t) && (total + val >= t)) {
                med = row;
                break;
            }
            total += val;
        }

        return med;
    }
}
