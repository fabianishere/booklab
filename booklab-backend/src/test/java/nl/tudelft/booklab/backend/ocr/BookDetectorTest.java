package nl.tudelft.booklab.backend.ocr;

import nl.tudelft.nlbooklab.backend.ocr.BookDetector;
import org.junit.Assert;
import org.junit.Test;
import org.opencv.core.Mat;
import java.util.List;
import static org.opencv.imgcodecs.Imgcodecs.imread;
public class BookDetectorTest {

    static {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    @Test
    public void bookDetectorTest(){
        String path = System.getProperty("user.dir") + "/src/test/java/nl/tudelft/booklab/backend/ocr/testbookshelf.jpg";

        Mat img = imread(path);
        List<Mat> books = BookDetector.detectBooks(img);

        Assert.assertTrue(books.size() > 0);
    }
}
