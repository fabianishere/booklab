import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.Core.*;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.*;

public class OCRPreprocessor {

    // init OpenCV
    static {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    public static Mat optimizeImg(Mat image) {
//        copyMakeBorder(image, image, 50, 50, 50, 50, BORDER_CONSTANT);
        Mat edges = new Mat();
        Mat hierarchy = new Mat();

        List<MatOfPoint> contours = new ArrayList<>();

//        List<Mat> bgrList = new ArrayList<>(3);
//        split(image, bgrList);
//
//        Mat blue = bgrList.get(0);
//        Mat green = bgrList.get(1);
//        Mat red = bgrList.get(2);
//
//        Mat blue_edges = ImgProcessHelper.autoCanny(blue);
//        Mat green_edges = ImgProcessHelper.autoCanny(green);
//        Mat red_edges = ImgProcessHelper.autoCanny(red);
//
//        Core.bitwise_or(blue_edges, green_edges, edges);
//        Core.bitwise_or(red_edges, edges, edges);

        Mat gray = new Mat();
        cvtColor(image, gray, COLOR_BGR2GRAY);
//        adaptiveThreshold(gray, gray, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 15, 0);
//        blur(gray, gray, new Size(2, 2));
        edges = ImgProcessHelper.autoCanny(gray);
//        dilate(edges, edges, getStructuringElement(MORPH_ELLIPSE, new Size(3,3)));
//        erode(edges, edges, getStructuringElement(MORPH_ELLIPSE, new Size(2,2)));


        Imgproc.findContours(edges, contours, hierarchy,
            Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);

        List<MatOfPoint> keepers = new ArrayList<>();
        int index = 0;
        for (MatOfPoint contour : contours) {
            if ((keepContour(contour, image)
//                && (contourArea(contour) > 10)
                && includeBox(index, contours, hierarchy, image)
            )) {
                keepers.add(contour);
            }
            index++;
        }

        Mat new_image = new Mat();
        edges.copyTo(new_image);
        new_image.setTo(new Scalar(255,255,255));

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

        blur(new_image, new_image, new Size(2,2));
        rotate(new_image, new_image, ROTATE_90_COUNTERCLOCKWISE);

        drawContours(image, keepers, -1, new Scalar(0, 0, 255));


        return new_image;

    }

    public static double getIntensity(Mat image, int x, int y) {
        // check if the pixel index is out of the frame
        if (x >= image.width() || y >= image.height() || x < 0 || y < 0)
            return 0;

        double[] pixel = image.get(y, x);

        return 0.30 * pixel[2] + 0.59 * pixel[1] + 0.11 * pixel[0];
    }

    public static boolean isConnected(MatOfPoint contour) {
        double[] first = contour.get(0, 0);
        double[] last = contour.get(contour.rows() - 1, 0);
        return Math.abs(first[0] - last[0]) <= 1 && Math.abs(first[1] - last[1]) <= 1;
    }

    public static int countChildren(int index, List<MatOfPoint> contours,
                                    Mat hierarchy, Mat image) {
        int count = 0;
        // get the child contour index
        int child = (int) hierarchy.get(0, index)[2];

        if (child < 0) {
            return 0;
        }

        System.out.println("child: " + child);

        if (keepContour(contours.get(child), image)) {
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

    public static boolean isChild(int index, List<MatOfPoint> contours,
                                  Mat hierarchy, Mat image) {
        return getParent(index, contours, hierarchy, image) > 0;
    }


    public static boolean keepContour(MatOfPoint contour, Mat image) {
        return keepBox(contour, image) ; //&& isConnected(contour)
//        return true;
    }

    public static boolean keepBox(MatOfPoint contour, Mat image) {
        Rect rect = boundingRect(contour);
        double width = rect.width;
        double height = rect.height;
        int area = image.width() * image.height();
        // if the contour is too long or wide it is rejected
        if (width * height > 0.5 * area || height > 0.2 * image.height()) {
            return false;
        }

        if(width / height > 6 || height / width > 10 ) {
            return false;
        }

        return true;
    }


    public static boolean includeBox(int index, List<MatOfPoint> contours,
                                     Mat hierarchy, Mat image) {
        int parent = getParent(index, contours, hierarchy, image);


        if (isChild(index, contours, hierarchy, image)
            && countChildren(parent, contours, hierarchy, image) <= 5) {
//            System.out.println("a");
            return false;
        }

        // if the contour has more than two children it is not a letter
        if (countChildren(index, contours, hierarchy, image) > 5) {
//            System.out.println("b");
            return false;
        }

        return true;
    }

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
        String outputpath = System.getProperty("user.dir") + "/booklab-backend/resources/roi_0_corrected.jpg";
        Mat image = imread(path);
        Mat imtmp = optimizeImg(image);
        imwrite(outputpath, imtmp);
    }

}
