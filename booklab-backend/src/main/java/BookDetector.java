import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.opencv.core.Core.REDUCE_AVG;
import static org.opencv.core.Core.reduce;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.*;

public class BookDetector {


    public static final int WINDOW_SIZE = 5;

    static {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    public static void detectBook() {
        Mat gray = new Mat();
        Mat dilation = new Mat();
        Mat reduced = new Mat();
        List<Point> coordinates = new ArrayList<>();

        String path = System.getProperty("user.dir");
        Mat im = imread(path + "/booklab-backend/resources/bookshelf.jpg");
        im = ImgProcessHelper.colorhist_equalize(im);

        cvtColor(im, gray, COLOR_BGR2GRAY);
        Mat edges = ImgProcessHelper.autoCanny(gray);
        dilate(edges, dilation, new Mat());
        reduce(dilation, reduced, 0, REDUCE_AVG);
        GaussianBlur(reduced, reduced, new Size(), 3);

        for (int i = 0; i < im.cols(); i++) {
            coordinates.add(new Point(i, reduced.get(0, i)[0]));
            line(im, new Point(i, 0), new Point(i, reduced.get(0, i)[0]), new Scalar(255, 255, 0), 1);
        }

        List<Double> yCoor = coordinates.stream().map(a -> a.y).collect(Collectors.toList());

        List<Point> localMinima = new ArrayList<>();
        for (int i = WINDOW_SIZE; i < coordinates.size() - WINDOW_SIZE; i++) {
            List<Double> sublist = yCoor.subList(i - WINDOW_SIZE, i + WINDOW_SIZE + 1);
            if (sublist.indexOf(Collections.min(sublist)) == WINDOW_SIZE) {
                localMinima.add(coordinates.get(i));
            }
        }

        for (int i = 0; i < localMinima.size(); i++) {
            line(im, new Point(localMinima.get(i).x, 0), new Point(localMinima.get(i).x, im.rows()), new Scalar(0, 255, 0), 2);
        }

        imwrite(path + "/booklab-backend/resources/lines_eq.jpg", im);
    }



    public static void main(String[] args) {
        detectBook();
    }

}
