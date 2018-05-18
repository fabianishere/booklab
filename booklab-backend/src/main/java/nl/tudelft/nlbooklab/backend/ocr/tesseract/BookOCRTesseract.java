/*
 * Copyright 2018 The BookLab Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.tudelft.nlbooklab.backend.ocr.tesseract;
import nl.tudelft.nlbooklab.backend.ocr.BookDetector;
import nl.tudelft.nlbooklab.backend.ocr.BookOCR;
import nl.tudelft.nlbooklab.backend.ocr.ImgProcessHelper;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.tesseract;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.bytedeco.javacpp.lept.pixDestroy;
import static org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_UNCHANGED;
import static org.opencv.imgcodecs.Imgcodecs.imdecode;

/**
 * Class to read titles from books in image. This class uses Tesseract to provide an open source alternative to Google Vision.
 */
public class BookOCRTesseract implements BookOCR {

    /**
     * Initialize OpenCV
     */
    static {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * Retrieve text from single image with Tesseract.
     * @param mat image
     * @return String
     */
    private static String getText(Mat mat) {
        String result = "";
        BytePointer outText;
        String path = System.getProperty("user.dir");

        tesseract.TessBaseAPI api = new tesseract.TessBaseAPI();

        // Initialize tesseract-ocr with English, without specifying tessdata path
        if (api.Init(path + "/tessdata/", "ENG") != 0) {
            System.err.println("Could not initialize tesseract.");
            System.exit(1);
        }
        api.SetVariable("load_system_dawg", "false");
        api.SetVariable("load_freq_dawg", "false");
        api.ReadConfigFile(path+"/tessdata/configs/api_config");

        // Open input image with leptonica library
        lept.PIX image = ImgProcessHelper.convertMatToPix(mat);

        api.SetImage(image);

        // Get OCR resultx
        outText = api.GetUTF8Text();

        // Destroy used object and release memory
        api.End();
        outText.deallocate();
        pixDestroy(image);
        return result;
    }

    /**
     * Retrieve list of book titles from image.
     * @param is inputstream
     * @return list of titles
     * @throws IOException
     */
    public List<String> getBookList(InputStream is) throws IOException {
        // read stream into mat via buffer
        int nRead;
        byte[] data = new byte[16 * 1024];
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        byte[] bytes = buffer.toByteArray();
        Mat image = imdecode(new MatOfByte(bytes), CV_LOAD_IMAGE_UNCHANGED);

        List<Mat> books = BookDetector.detectBooks(image);

        return books.parallelStream().map(BookOCRTesseract::getText).collect(Collectors.toList());
    }

}
