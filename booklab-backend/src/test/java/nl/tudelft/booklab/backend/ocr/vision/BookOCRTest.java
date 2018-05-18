package nl.tudelft.booklab.backend.ocr.vision;

import nl.tudelft.nlbooklab.backend.ocr.vision.BookOCRVision;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.util.List;

public class BookOCRTest {
    static {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    @Test
    public void getBookListTest() throws IOException {
        URL path = BookOCRTest.class.getResource("/testbookshelf.jpg");
        String pathstring = path.getPath();
        pathstring = pathstring.substring(1);
        InputStream is = new FileInputStream(pathstring);
        BookOCRVision visionOCR = new BookOCRVision();
        List<String> res = visionOCR.getBookList(is);
        Assert.assertTrue(res.size() > 0);
    }
}
