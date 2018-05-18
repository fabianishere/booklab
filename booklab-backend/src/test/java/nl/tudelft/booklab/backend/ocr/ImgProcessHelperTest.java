package nl.tudelft.booklab.backend.ocr;
import nl.tudelft.nlbooklab.backend.ocr.ImgProcessHelper;
import org.bytedeco.javacpp.lept;
import org.junit.Assert;
import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import static org.opencv.core.Core.randu;

public class ImgProcessHelperTest {

    static {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    @Test
    public void colorhist_equalizeTest(){
        Mat random_image = new Mat(100,100, CvType.CV_8UC3);
        randu(random_image,0, 256);
        Mat testmat_eq = ImgProcessHelper.colorhistEqualize(random_image);

        Assert.assertFalse(Core.sumElems(random_image).equals(Core.sumElems(testmat_eq)));
    }

    @Test
    public void autocannyTest(){
        Mat random_image = new Mat(100,100, CvType.CV_8UC3);
        randu(random_image,0, 256);
        Mat testmat_canny = ImgProcessHelper.autoCanny(random_image);

        Assert.assertFalse(Core.sumElems(testmat_canny).equals(0));
    }

    @Test
    public void getMedianTest(){
        Mat test_image = new Mat(10,10, CvType.CV_8UC3, new Scalar(1,2,0));
        Assert.assertTrue(ImgProcessHelper.getMedian(test_image) == 1);
    }

    @Test
    public void convertMatToPixTest(){
        Mat test_image = new Mat(10,10, CvType.CV_8UC3, new Scalar(1,1,1));
        lept.PIX testpix = ImgProcessHelper.convertMatToPix(test_image);
        Assert.assertTrue(testpix.w() == 10 && testpix.h() == 10);
    }

    @Test
    public void detectHoughlinesTest(){
        Mat random_image = new Mat(100,100, CvType.CV_8UC3);
        randu(random_image,0, 256);
        Mat testmat_lines = ImgProcessHelper.detectBookHoughLines(random_image);

        Assert.assertFalse(Core.sumElems(testmat_lines).equals(0));
    }
}
