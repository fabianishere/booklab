import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.Core.flip;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;

public class OCRPreprocessor {

    // init OpenCV
    static {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    public static Mat optimizeImg(Mat original) {
        int imageWidth = original.width();
        int imageHeight = original.height();

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Mat mIntermediateMat = new Mat(imageWidth, imageHeight, CvType.CV_8UC3);

        Imgproc.Canny(original, mIntermediateMat, 200, 250);
        Imgproc.findContours(mIntermediateMat, contours, hierarchy,
            Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));


        List<MatOfPoint> keepers = new ArrayList<>();
        int index = 0;
        for (MatOfPoint contour : contours) {

            if (isValidContour(contour, original)
                && validBox(index, contours, hierarchy, original)) {
                keepers.add(contour);
            }
            index++;
        }

        Mat new_mat = new Mat(imageWidth, imageHeight, CvType.CV_8U);

        for (MatOfPoint contour : keepers) {
            double foregroundIntensity = 0.0;
            Point[] contourPoints = contour.toArray();
            for (int i = 0; i < contourPoints.length; i++) {
                Point p = contourPoints[i];
                foregroundIntensity += getIntensity(original, (int) p.x, (int) p.y);
            }

            foregroundIntensity = foregroundIntensity / contourPoints.length;

            Rect box = Imgproc.boundingRect(contour);
            int boxX = box.x;
            int boxY = box.y;
            int boxWidth = box.width;
            int boxHeight = box.height;
            double[] backgroundInt = {
                getIntensity(original, boxX - 1, boxY - 1),
                getIntensity(original, boxX - 1, boxY),
                getIntensity(original, boxX, boxY - 1),
                getIntensity(original, boxX + boxWidth + 1, boxY - 1),
                getIntensity(original, boxX + boxWidth, boxY - 1),
                getIntensity(original, boxX + boxWidth + 1, boxY),
                getIntensity(original, boxX - 1, boxY + boxHeight + 1),
                getIntensity(original, boxX - 1, boxY + boxHeight),
                getIntensity(original, boxX, boxY + boxHeight + 1),
                getIntensity(original, boxX + boxWidth + 1, boxY + boxHeight + 1),
                getIntensity(original, boxX + boxWidth, boxY + boxHeight + 1),
                getIntensity(original, boxX + boxWidth + 1, boxY + boxHeight)};

            Arrays.sort(backgroundInt);
            double median = backgroundInt[6];

            int fg = 255;
            int bg = 0;
            if (foregroundIntensity <= median) {
                fg = 0;
                bg = 255;
            }
            for (int x = boxX; x < boxX + boxWidth; x++) {
                for (int y = boxY; y < boxY + boxHeight; y++) {
                    if (x < imageWidth && y < imageHeight) {
                        if (getIntensity(original, x, y) > foregroundIntensity) {
                            new_mat.put(x, y, bg);
                        } else {
                            new_mat.put(x, y, fg);

                        }
                    }
                }
            }
        }

        flip(new_mat, new_mat, 1);
        return new_mat;

    }

    public static double getIntensity(Mat image, int x, int y) {

        // check if the pixel index is out of the frame
        if (image.width() <= x || image.height() <= y || x < 0 || y < 0)
            return 0;

        double[] pixel = image.get(y, x);

        return 0.30 * pixel[2] + 0.59 * pixel[1] + 0.11 * pixel[0];
    }

    public static boolean isValidContour(MatOfPoint contour, Mat image) {
        double width = contour.width();
        double height = contour.height();

        // if the contour is too long or wide it is rejected
        if (width / height < 0.1 && width / height > 10) {
            return false;
        }

        // if the contour in too wide or tall
        if (width > image.width() / 5 || height > image.height() / 5)
            return false;

        return true;
    }

    public static boolean validBox(int index, List<MatOfPoint> contours,
                                   Mat hierarchy, Mat image) {

        // if it is a child of a accepting contour and has no children it is
        // probably the interior of a letter
        if (isChild(index, contours, hierarchy, image)
            && countChildren(index, contours, hierarchy, image) <= 2)
            return false;

        // if the contour has more than two children it is not a letter
        if (countChildren(index, contours, hierarchy, image) > 2)
            return false;

        return true;
    }

    public static int countChildren(int index, List<MatOfPoint> contours,
                                    Mat hierarchy, Mat image) {

        // get the child contour index
        int iBuff[] = new int[(int) (hierarchy.total() * hierarchy.channels())];
        hierarchy.get(index, 2, iBuff);

        int count = 0;
        if (iBuff[0] < 0) {
            return 0;
        } else {
            if (isValidContour(contours.get(iBuff[0]), image)) {
                count = 1;
                // count += countSiblings(iBuff[0], contours, hierarchy, image);
            }
        }

        return count;
    }

    public static boolean isChild(int index, List<MatOfPoint> contours,
                                  Mat hierarchy, Mat image) {

        // get the parent in the contour hierarchy
        int iBuff[] = new int[(int) (hierarchy.total() * hierarchy.channels())];
        hierarchy.get(index, 3, iBuff);
        int parent = iBuff[0];

        // searches until a valid parent is found
        while (!isValidContour(contours.get(parent), image)) {
            hierarchy.get(parent, 3, iBuff);
            parent = iBuff[0];
        }

        // return true of there is a valid parent
        return parent > 0;

    }

    public static void main(String[] args) {
        String path = System.getProperty("user.dir") + "/booklab-backend/resources/bookshelf.jpg";
        String outputpath = System.getProperty("user.dir") + "/booklab-backend/resources/bookshelfcorrected.jpg";
        Mat image = imread(path);
        Mat imtmp = optimizeImg(image);
        imwrite(outputpath, imtmp);
    }

}
