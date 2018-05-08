import org.jetbrains.annotations.NotNull;
import org.opencv.core.*;

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

    static {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    public static void detectBooks() {
        String path = System.getProperty("user.dir");
        path = path + "/booklab-backend/resources/bookshelf.jpg";

        Mat image = imread(path);
        List<Mat> books = detectBooks(image);

        for (int i = 0; i < books.size(); i++) {
            imwrite(System.getProperty("user.dir") + "/booklab-backend/resources/books/roi_" + i + ".jpg", books.get(i));
        }
    }

    public static List<Mat> detectBooks(Mat image) {
        image = ImgProcessHelper.colorhist_equalize(image);
        List<Integer> cropLocations = detectBookLocations(image);
        return cropBooks(image, cropLocations, false);
    }

    private static List<Integer> detectBookLocations(Mat image) {
        Mat gray = new Mat();
        Mat dilation = new Mat();
        Mat reduced = new Mat();
        List<Point> coordinates = new ArrayList<>();

        image = ImgProcessHelper.colorhist_equalize(image);
        cvtColor(image, gray, COLOR_BGR2GRAY);
        Mat edges = ImgProcessHelper.autoCanny(gray);
        dilate(edges, dilation, new Mat());
        reduce(dilation, reduced, 0, REDUCE_AVG);
        GaussianBlur(reduced, reduced, new Size(), 3);

        for (int i = 0; i < image.cols(); i++) {
            coordinates.add(new Point(i, reduced.get(0, i)[0]));
        }

        List<Integer> localMinima = findLocalMinima(coordinates, 5);

        List<Integer> cropLocations = new ArrayList<>(localMinima);
        cropLocations.add(0, 0);
        cropLocations.add(image.cols());

        return cropLocations;
    }

    @NotNull
    private static List<Mat> cropBooks(Mat im, List<Integer> cropLocations, boolean strictCrop) {
        List<Mat> books = new ArrayList<>();
        for (int i = 0; i < cropLocations.size() - 1; i++) {
            Mat book = cropBook(im, cropLocations.get(i), cropLocations.get(i + 1) - cropLocations.get(i), strictCrop);
            books.add(book);
        }
        return books;
    }

    @NotNull
    private static Mat cropBook(Mat im, int x, int width, boolean strictCrop) {
        if (!strictCrop) {
            width = (int) (1.1 * (width + 0.05 * x));
            x = (int) (0.95 * x);
        }

        if (x + width > im.cols()) {
            width = width - (x + width - im.cols());
        }

        Rect roi = new Rect(x, 0, width, im.rows());
        return new Mat(im, roi);
    }

    @NotNull
    private static List<Integer> findLocalMinima(List<Point> coordinates, int windowSize) {
        List<Double> yCoordinates = coordinates.stream().map(a -> a.y).collect(Collectors.toList());
        List<Integer> localMinima = new ArrayList<>();
        for (int i = windowSize; i < coordinates.size() - windowSize; i++) {
            List<Double> sublist = yCoordinates.subList(i - windowSize, i + windowSize + 1);
            if (sublist.indexOf(Collections.min(sublist)) == windowSize) {
                localMinima.add((int) coordinates.get(i).x);
            }
        }
        return localMinima;
    }

    private static void drawGraphs(Mat reducedImage, Mat originalImage, List<Integer> lineLocations) {
        for (int i = 0; i < originalImage.cols(); i++) {
            line(originalImage, new Point(i, 0), new Point(i, reducedImage.get(0, i)[0]), new Scalar(255, 255, 0), 1);
        }

        for (int i = 0; i < lineLocations.size(); i++) {
            line(originalImage, new Point(lineLocations.get(i), 0), new Point(lineLocations.get(i), originalImage.rows()), new Scalar(0, 255, 0), 2);
        }
    }


    public static void main(String[] args) {
        detectBooks();
    }

}
