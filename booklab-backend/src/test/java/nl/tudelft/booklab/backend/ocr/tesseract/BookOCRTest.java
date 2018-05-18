package nl.tudelft.booklab.backend.ocr.tesseract;

import nl.tudelft.nlbooklab.backend.ocr.tesseract.BookOCRTesseract;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.List;

public class BookOCRTest {
    static {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    @Test
    public void getBookListTest() throws IOException {
        String path = System.getProperty("user.dir") + "/src/test/java/nl/tudelft/booklab/backend/ocr/testbookshelf.jpg";
        InputStream is = new FileInputStream(path);
        List<String> res = BookOCRTesseract.getBookList(is);
        Assert.assertTrue(res.size() > 0);
    }
}
