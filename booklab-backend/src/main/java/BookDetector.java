import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.*;

public class BookDetector {

    static {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    public static void detectBook() {
        Mat hierarchy = new Mat();
        Mat gray = new Mat();
        Mat edges = new Mat();
        java.util.List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Scalar color = new Scalar(0, 255, 0);

        String path = System.getProperty("user.dir");
        Mat im = imread(path + "/booklab-backend/src/bookshelf.jpg");

        cvtColor(im, gray, COLOR_BGR2GRAY);

        Canny(gray, edges, 0, 100);

        findContours(edges, contours, hierarchy,RETR_TREE, CHAIN_APPROX_SIMPLE);

        drawContours(im, contours, -1, color);

        System.out.println(hierarchy.toString());
        imwrite(path + "/booklab-backend/src/output.jpg", im);
    }

    public static void main(String[] args) {
        detectBook();
    }

}
