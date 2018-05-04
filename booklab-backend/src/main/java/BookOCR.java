import org.bytedeco.javacpp.*;
import org.opencv.core.*;
import org.opencv.features2d.MSER;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.bytedeco.javacpp.lept.*;
import static org.bytedeco.javacpp.tesseract.*;
import static org.opencv.core.Core.*;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.*;


public class BookOCR {

    static {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    private static Mat preprocessImage(String path) {
        Mat image = imread(path);
        return preprocessImage(image);
    }

    private static Mat preprocessImage(Mat image) {
        Mat gray = new Mat();
        Mat dilation = new Mat();
        Mat result = new Mat();
        Mat element = getStructuringElement(MORPH_ELLIPSE, new Size(2, 2));
        List<MatOfPoint> msers = new ArrayList<>();
        MatOfRect bboxes = new MatOfRect();

        rotate(image, image, ROTATE_90_COUNTERCLOCKWISE);
        cvtColor(image, gray, COLOR_BGR2GRAY);

        int imageArea = image.height() * image.width();

        double mean = mean(gray).val[0];
        threshold(gray, gray, mean, 255, THRESH_BINARY_INV);
        double threshMean = mean(gray).val[0];

        // Black text on white background, so invert image
        if (threshMean > 128) {
            threshold(gray, gray, mean, 255, THRESH_BINARY_INV);
            erode(gray, gray, getStructuringElement(MORPH_ELLIPSE, new Size(2, 2)));
            dilate(gray, dilation, getStructuringElement(MORPH_ELLIPSE, new Size(3, 3)));
        } else {
            dilate(gray, dilation, element);
        }


        MSER mser = MSER.create();
        mser.detectRegions(dilation, msers, bboxes);
        List<Rect> filtered = bboxes.toList().stream().filter(a -> a.area() < 0.5 * imageArea).collect(Collectors.toList());

        Mat mask = new Mat(image.rows(), image.cols(), CvType.CV_8U, Scalar.all(0));
        for (Rect box : filtered) {
            rectangle(mask, new Point(box.x, box.y), new Point(box.x + box.width, box.y + box.height),
                new Scalar(255, 255, 255), FILLED);
        }

        bitwise_and(gray, gray, result, mask);

//        imwrite(System.getProperty("user.dir") + "/booklab-backend/resources/preprocess.jpg", gray);

        return gray;
    }

    private static List<Mat> preprocessImages(List<Mat> images) {
        return images.stream().map(BookOCR::preprocessImage).collect(Collectors.toList());
    }

    // resize
    // grayscale
    // reduce noise
    private static lept.PIX preprocessImg(Mat mat) {
        lept.PIX piximage = new lept.PIX();
        return piximage;
    }

    public static String getText(Mat mat) {
        String result = "";
        BytePointer outText;
        String path = System.getProperty("user.dir");

        tesseract.TessBaseAPI api = new tesseract.TessBaseAPI();
        // Initialize tesseract-ocr with English, without specifying tessdata path
        if (api.Init(path + "/booklab-backend/tessdata/", "ENG") != 0) {
            System.err.println("Could not initialize tesseract.");
            System.exit(1);
        }

        // Open input image with leptonica library
        lept.PIX image = ImgProcessHelper.convertMatToPix(mat);
        api.SetImage(image);

        // Get OCR result
        outText = api.GetUTF8Text();
        String string = outText.getString();
        System.out.println("OCR output:\n" + string);

        // Destroy used object and release memory
        api.End();
        outText.deallocate();
        pixDestroy(image);
        return result;
    }

    public static void main(String[] args) {
        String path = System.getProperty("user.dir") + "/booklab-backend/resources/bookshelf.jpg";
        List<Mat> books = BookDetector.detectBooks(path);

        preprocessImages(books).forEach(BookOCR::getText);
        getText(preprocessImage(System.getProperty("user.dir") + "/booklab-backend/resources/books/roi_2.jpg"));
    }

}
