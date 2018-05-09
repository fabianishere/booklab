import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.Core.*;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.cvtColor;

public class OCRPreprocessor {

    // init OpenCV
    static {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    public static Mat optimizeImg(Mat image) {
        Mat edges = new Mat();
        Mat hierarchy = new Mat();
        Mat gray = new Mat();
        Mat result = new Mat(image.width(), image.height(), CvType.CV_8U);

        List<MatOfPoint> contours = new ArrayList<>();

        cvtColor(image, gray, COLOR_BGR2GRAY);
        edges = ImgProcessHelper.autoCanny(gray);
//        Imgproc.Canny(gray, edges, 200, 250);

        Imgproc.findContours(edges, contours, hierarchy,
            Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        List<MatOfPoint> keepers = new ArrayList<>();
        int index = 0;
        for (MatOfPoint contour : contours) {
            if (isValidContour(contour, image)
                && isConnectedContour(contour)
                && isValidBox(index, contours, hierarchy, image)) {
                keepers.add(contour);
            }
            index++;
        }

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

            if (foregroundIntensity <= median) {
                foregroundColor = 0;
                backgroundColor = 255;
            }

            for (int x = boxX; x < boxX + boxWidth; x++) {
                for (int y = boxY; y < boxY + boxHeight; y++) {
                    if (x < image.width() && y < image.height()) {
                        if (getIntensity(image, x, y) > foregroundIntensity) {
                            result.put(x, y, backgroundColor);
                        } else {
                            result.put(x, y, foregroundColor);
                        }
                    }
                }
            }
        }

        flip(result, result, 0);

        return result;

    }

    public static double getIntensity(Mat image, int x, int y) {
        // check if the pixel index is out of the frame
        if (x >= image.width() || y >= image.height() || x < 0 || y < 0)
            return 0;

        double[] pixel = image.get(y, x);

        return 0.30 * pixel[2] + 0.59 * pixel[1] + 0.11 * pixel[0];
    }

    public static boolean isValidContour(MatOfPoint contour, Mat image) {

        double width = contour.width();
        double height = contour.height();

        // if the contour is too long or wide it is rejected
        if (width / height < 0.01 || width / height > 10
            || width > image.width() / 5 || height > image.height() / 5) {
            return false;
        }

        return true;
    }

    public static boolean isConnectedContour(MatOfPoint contour) {
        double[] first = contour.get(0,0);
        double[] last = contour.get(contour.rows() - 1, 0);
        return Math.abs(first[0] - last[0]) <= 1 && Math.abs(first[1] - last[1]) <= 1;
    }

    public static boolean isValidBox(int index, List<MatOfPoint> contours,
                                     Mat hierarchy, Mat image) {

        // if it is a child of a accepting contour and has no children it is
        // probably the interior of a letter

        int children = countChildren(index, contours, hierarchy, image);

        if (isChild(index, contours, hierarchy, image)
            && children <= 2) {
//            System.out.println("a");
            return false;
        }

        // if the contour has more than two children it is not a letter
        if (children > 2) {
//            System.out.println("b");
            return false;
        }

        return true;
    }

    public static int countChildren(int index, List<MatOfPoint> contours,
                                    Mat hierarchy, Mat image) {
        int count = 0;
        // get the child contour index
        int child = (int) hierarchy.get(0, index)[2];

        if (child < 0) {
            return 0;
        }

        if (isValidContour(contours.get(child), image)) {
            count = 1;
        }

        count += countSiblings(child, contours, hierarchy, image);

        return count;
    }

    public static int countSiblings(int index, List<MatOfPoint> contours,
                                    Mat hierarchy, Mat image) {
        int count = 0;

        int next = (int) hierarchy.get(0, index)[0];

        // counting the children of the next contour
        while (next > 0) {
            if (isValidContour(contours.get(next), image)) {
                count += 1;
            }
            next = (int) hierarchy.get(0, next)[0];
            if (next == index)
                break;
        }

        int prev = (int) hierarchy.get(0, index)[1];

        // counting the children of the previous contour
        while (prev > 0) {
            if (isValidContour(contours.get(prev), image)) {
                count += 1;
            }
            prev = (int) hierarchy.get(0, prev)[1];
            if (prev == index)
                break;
        }

        return count;
    }

    public static boolean isChild(int index, List<MatOfPoint> contours,
                                  Mat hierarchy, Mat image) {
        // get the parent in the contour hierarchy
        int parent = (int) hierarchy.get(0, index)[3];

        // searches until a valid parent is found
        while (parent > 0 && !isValidContour(contours.get(parent), image)) {
            parent = (int) hierarchy.get(0, parent)[3];
        }

        // return true of there is a valid parent
        return parent > 0;
    }

    public static void main(String[] args) {
        String path = System.getProperty("user.dir") + "/booklab-backend/resources/books/roi_64.jpg";
        String outputpath = System.getProperty("user.dir") + "/booklab-backend/resources/roi_0_corrected.jpg";
        Mat image = imread(path);
        Mat imtmp = optimizeImg(image);
        imwrite(outputpath, imtmp);
    }

}
