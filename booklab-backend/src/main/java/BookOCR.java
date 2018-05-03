import org.bytedeco.javacpp.*;
import org.opencv.core.Mat;

import static org.bytedeco.javacpp.lept.*;
import static org.bytedeco.javacpp.tesseract.*;


public class BookOCR {
    // resize
    // grayscale
    // reduce noise
    private static lept.PIX preprocessImg(Mat mat){
        lept.PIX piximage = new lept.PIX();
        return piximage;
    }

    public static String getText(){
        String result = "";
        BytePointer outText;
        String path = System.getProperty("user.dir");

        tesseract.TessBaseAPI api = new tesseract.TessBaseAPI();
        // Initialize tesseract-ocr with English, without specifying tessdata path
        if (api.Init(".", "ENG") != 0) {
            System.err.println("Could not initialize tesseract.");
            System.exit(1);
        }

        // Open input image with leptonica library
        lept.PIX image = pixRead(path+"/src/test/java/testbook2.PNG");
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
        getText();
    }

}
