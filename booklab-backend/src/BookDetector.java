import org.opencv.core.Mat;

import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;

public class BookDetector {


    static {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    public static void detectBook() {
        Mat im = imread("resources/bookshelf.jpg");
        imwrite("resources/output.jpg", im);
        System.out.println("Saved image");
    }

    public static void main(String[] args) {
        detectBook();
    }

}
