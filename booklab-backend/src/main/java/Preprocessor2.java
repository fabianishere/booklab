import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.Core.merge;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.*;

public class Preprocessor2 {

    static {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    public static Mat claheThreshold(Mat image) {
        List<Mat> channels = new ArrayList<>();
        Mat lab = new Mat();
        cvtColor(image, lab, COLOR_BGR2Lab);
        Core.split(lab, channels);
        Mat destImage = new Mat(image.height(),image.width(), CvType.CV_8UC4);

        CLAHE clahe = Imgproc.createCLAHE();
        clahe.apply(channels.get(0), destImage);
        channels.set(0, destImage);
        merge(channels, lab);

        return lab;
    }

    public static void main(String[] args) {
        Mat res = claheThreshold(imread(System.getProperty("user.dir") + "/booklab-backend/resources/books/roi_1.jpg"));

//        threshold(res, res, 100, 255, THRESH_OTSU+THRESH_BINARY);
//        adaptiveThreshold(res, res, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 15, 0);

        imwrite(System.getProperty("user.dir") + "/booklab-backend/resources/clahe.jpg", res);
    }

}
