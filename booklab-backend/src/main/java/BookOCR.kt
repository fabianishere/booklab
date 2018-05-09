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

import BookOCR.getText
import BookOCR.preprocessImages
import org.bytedeco.javacpp.*
import org.opencv.core.*
import org.opencv.features2d.MSER

import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.ArrayList
import java.util.stream.Collectors

import org.bytedeco.javacpp.lept.*
import org.opencv.core.Core.*
import org.opencv.imgcodecs.Imgcodecs.*
import org.opencv.imgproc.Imgproc.*

/**
 * This class is responsible for reading the backs of the books in an image
 *
 * @author Vera Hoveling (v.t.hoveling@student.tudelft.nl)
 * @author Sayra Ranjha (s.s.ranjha@student.tudelft.nl)
 */
object BookOCR {

    /**
     * Load OpenCV
     */
    init {
        nu.pattern.OpenCV.loadShared()
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME)
    }

    /**
     * Method to process an image such that it is optimized for OCR
     * @param image openCV matrix containing the image
     */
    private fun preprocessImage(image: Mat): Mat {
        val gray = Mat()
        val dilation = Mat()
        val result = Mat()
        val element = getStructuringElement(MORPH_ELLIPSE, Size(2.0, 2.0))
        val msers = ArrayList<MatOfPoint>()
        val bboxes = MatOfRect()

        rotate(image, image, ROTATE_90_COUNTERCLOCKWISE)
        cvtColor(image, gray, COLOR_BGR2GRAY)

        val imageArea = image.height() * image.width()

        val mean = mean(gray).`val`[0]
        threshold(gray, gray, mean, 255.0, THRESH_BINARY_INV)
        val threshMean = mean(gray).`val`[0]

        // Black text on white background, so invert image
        if (threshMean > 128) {
            threshold(gray, gray, mean, 255.0, THRESH_BINARY_INV)
            erode(gray, gray, getStructuringElement(MORPH_ELLIPSE, Size(2.0, 2.0)))
            dilate(gray, dilation, getStructuringElement(MORPH_ELLIPSE, Size(3.0, 3.0)))
        } else {
            dilate(gray, dilation, element)
        }


        val mser = MSER.create()
        mser.detectRegions(dilation, msers, bboxes)
        val filtered = bboxes.toList().filter{ a -> a.area() < 0.5 * imageArea}

        val mask = Mat(image.rows(), image.cols(), CvType.CV_8U, Scalar.all(0.0))
        for (box in filtered) {
            rectangle(mask, Point(box.x.toDouble(), box.y.toDouble()), Point((box.x + box.width).toDouble(), (box.y + box.height).toDouble()),
                    Scalar(255.0, 255.0, 255.0), FILLED)
        }

        bitwise_and(gray, gray, result, mask)

        return gray
    }

    /**
     * Process a list of images such that they are optimized for OCR
     * @param images list of openCV matrices containing images
     * @return list of images
     */
    private fun preprocessImages(images: List<Mat>): List<Mat> {
        val result = images.parallelStream().map{ preprocessImage(it)}
        return result.collect(Collectors.toList())
    }

    /**
     * Get text from image
     * @param mat openCV matrix containing image
     * @return String text read from image
     */
    fun getText(mat: Mat): String {
        val result = ""
        val outText: BytePointer
        val path = System.getProperty("user.dir")

        val api = tesseract.TessBaseAPI()
        // Initialize tesseract-ocr with English, without specifying tessdata path
        if (api.Init(path + "/booklab-backend/tessdata/", "ENG") != 0) {
            System.err.println("Could not initialize tesseract.")
            System.exit(1)
        }

        // Open input image with leptonica library
        val image = ImgProcessHelper.convertMatToPix(mat)
        api.SetImage(image)

        // Get OCR result
        outText = api.GetUTF8Text()
        val string = outText.string
        println("OCR output:\n" + string)

        // Destroy used object and release memory
        api.End()
        outText.deallocate()
        pixDestroy(image)
        return result
    }

    /**
     * Method that takes an image and returns a list of strings containing the books in the image
     * @param is inputstream containing the image
     * @return list of strings with books
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getBookList(`is`: InputStream): List<String> {
        // read stream into mat via buffer
        var nRead: Int
        val data = ByteArray(16 * 1024)
        val buffer = ByteArrayOutputStream()
        nRead = `is`.read(data, 0, data.size)
        while (nRead != -1) {
            buffer.write(data, 0, nRead)
            nRead = `is`.read(data, 0, data.size)
        }
        val bytes = buffer.toByteArray()
        val image = imdecode(MatOfByte(*bytes), CV_LOAD_IMAGE_UNCHANGED)

        // get the books
        val books = BookDetector.detectBooks(image)
        // get the names of the books
        val result = preprocessImages(books).parallelStream().map{getText(it)}
        return result.collect(Collectors.toList())
    }

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val path = System.getProperty("user.dir") + "/booklab-backend/resources/bookshelf.jpg"
        val `is` = FileInputStream(path)
        getBookList(`is`)
    }

}
