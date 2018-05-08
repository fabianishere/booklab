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

        int width = original.width();
        int height = original.height();

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Mat mIntermediateMat = new Mat(width, height, CvType.CV_8UC3);

        Imgproc.Canny(original, mIntermediateMat, 200, 250);
        Imgproc.findContours(mIntermediateMat, contours, hierarchy,
            Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));


        List<MatOfPoint> keepers = new ArrayList<MatOfPoint>();
        int index = 0;
        for (MatOfPoint contour : contours) {

            if (validContour(contour, original)
                && validBox(index, contours, hierarchy, original)) {
                keepers.add(contour);
            }
            index++;
        }

        Mat new_mat = new Mat(mIntermediateMat.width(), mIntermediateMat.height(), CvType.CV_8U);

       for (MatOfPoint contour : contours) {
            double foregroundInt = 0.0;
            Point[] points_contour = contour.toArray();
            int nbPoints = points_contour.length;
            for (int i = 0; i < nbPoints; i++) {
                Point p = points_contour[i];
                foregroundInt += getIntensity(original, (int) p.x, (int) p.y);
            }

            foregroundInt = foregroundInt / nbPoints;

            Rect box = Imgproc.boundingRect(contour);
            double[] backgroundInt = {
                getIntensity(original, (int) box.x - 1, (int) box.y - 1),
                getIntensity(original, (int) box.x - 1, (int) box.y),
                getIntensity(original, (int) box.x, (int) box.y - 1),
                getIntensity(original, (int) box.x + box.width + 1,
                    (int) box.y - 1),
                getIntensity(original, (int) box.x + box.width,
                    (int) box.y - 1),
                getIntensity(original, (int) box.x + box.width + 1,
                    (int) box.y),
                getIntensity(original, (int) box.x - 1, (int) box.y
                    + box.height + 1),
                getIntensity(original, (int) box.x - 1, (int) box.y
                    + box.height),
                getIntensity(original, (int) box.x, (int) box.y
                    + box.height + 1),
                getIntensity(original, (int) box.x + box.width + 1,
                    (int) box.y + box.height + 1),
                getIntensity(original, (int) box.x + box.width, (int) box.y
                    + box.height + 1),
                getIntensity(original, (int) box.x + box.width + 1,
                    (int) box.y + box.height), };
            Arrays.sort(backgroundInt);
            double median = backgroundInt[6];

            int fg = 255;
            int bg = 0;
            if (foregroundInt <= median) {
                fg = 0;
                bg = 255;
            }
            for (int x = box.x; x < box.x + box.width; x++) {
                for (int y = box.y; y < box.y + box.height; y++) {
                    if (x < original.width() && y < original.height()) {
                        if (getIntensity(original, x, y) > foregroundInt)
                            new_mat.put(x, y, bg);
                        else
                            new_mat.put(x, y, fg);
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

    public static boolean validContour(MatOfPoint contour, Mat image) {
        double w = contour.width();
        double h = contour.height();

        // if the contour is too long or wide it is rejected
        if (w / h < 0.1 && w / h > 10) {
            return false;
        }

        // if the contour in too wide
        if (w > image.width() / 5)
            return false;

        // if the contour is too tall
        if (h > image.height() / 5)
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
            if (validContour(contours.get(iBuff[0]), image)) {
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
        while (!validContour(contours.get(parent), image)) {
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
        Mat imtmp =  optimizeImg(image);
        imwrite(outputpath, imtmp);
    }

}
