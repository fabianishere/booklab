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
        String path = System.getProperty("user.dir");
        Mat im = imread(path + "/booklab-backend/src/bookshelf.jpg");
        Mat hierarchy = new Mat();
        Mat gray = new Mat();
        cvtColor(im, gray, COLOR_BGR2GRAY);
        java.util.List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        findContours(gray, contours, hierarchy,RETR_TREE, CHAIN_APPROX_SIMPLE);
        Scalar color = new Scalar(0, 255, 0);
        drawContours(im, contours, -1, color);

        System.out.println(hierarchy.toString());
        imwrite(path + "/booklab-backend/src/output.jpg", im);
    }

    public static void main(String[] args) {
        detectBook();
    }

}
