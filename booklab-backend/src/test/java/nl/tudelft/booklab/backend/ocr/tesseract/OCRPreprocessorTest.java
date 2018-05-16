package nl.tudelft.booklab.backend.ocr.tesseract;

import nl.tudelft.nlbooklab.backend.ocr.tesseract.OCRPreprocessor;
import org.junit.Assert;
import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import static org.opencv.core.Core.randu;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_GRAY2BGR;
import static org.opencv.imgproc.Imgproc.cvtColor;

public class OCRPreprocessorTest {

    static {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    @Test
    public void preprocessorTest(){
        Mat random_image = new Mat(100,100, CvType.CV_8UC3);
        randu(random_image,0, 256);
        Mat opt_image = OCRPreprocessor.optimizeImg(random_image);

        cvtColor(opt_image, opt_image, COLOR_GRAY2BGR);

        Mat diff = new Mat();
         Core.absdiff(random_image, opt_image, diff);

        Scalar sum = Core.sumElems(diff);
        Assert.assertTrue(sum.val[0]+sum.val[1]+sum.val[2]+sum.val[3]>0);
    }
}
