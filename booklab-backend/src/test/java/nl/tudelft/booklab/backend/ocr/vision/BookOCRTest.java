package nl.tudelft.booklab.backend.ocr.vision;

import nl.tudelft.nlbooklab.backend.ocr.vision.BookOCR;
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
        List<String> res = BookOCR.getBookList(is);
        Assert.assertTrue(res.size() > 0);
    }
}
