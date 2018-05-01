import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;

public class BookDetector {


    static {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    public static void detectBook() {
        String path = System.getProperty("user.dir");
        Mat im = imread(path + "/booklab-backend/src/bookshelf.jpg");
        imwrite(path + "/booklab-backend/src/output.jpg", im);
    }

    public static void main(String[] args) {
        detectBook();
    }

}
