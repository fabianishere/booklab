package nl.tudelft.booklab.backend.ocr.tesseract;

import kotlin.io.ByteStreamsKt;
import kotlin.io.ConstantsKt;
import org.bytedeco.javacpp.*;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

import static org.bytedeco.javacpp.lept.*;
import static org.bytedeco.javacpp.tesseract.*;
import static org.junit.Assert.assertTrue;

public class BasicTesseractExampleTest {

    @Test
    public void givenTessBaseApi_whenImageOcrd_thenTextDisplayed() throws Exception {
        BytePointer outText;

        TessBaseAPI api = new TessBaseAPI();
        // Initialize tesseract-ocr with English, without specifying tessdata path
        if (api.Init(".", "ENG") != 0) {
            System.err.println("Could not initialize tesseract.");
            System.exit(1);
        }

        // Open input image with leptonica library
        URL path = BasicTesseractExampleTest.class.getResource("/test.png");
        String pathstring = path.getPath();
        pathstring = pathstring.substring(1);
        PIX image = pixRead(pathstring);

        api.SetImage(image);
        // Get OCR result
        outText = api.GetUTF8Text();
        String string = outText.getString();
        assertTrue(!string.isEmpty());
        System.out.println("OCR output:\n" + string);

        // Destroy used object and release memory
        api.End();
        outText.deallocate();
        pixDestroy(image);
    }
}
