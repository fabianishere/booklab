import org.opencv.core.*

import java.util.ArrayList
import java.util.Collections
import java.util.stream.Collectors

import org.opencv.core.Core.REDUCE_AVG
import org.opencv.core.Core.reduce
import org.opencv.imgcodecs.Imgcodecs.imread
import org.opencv.imgcodecs.Imgcodecs.imwrite
import org.opencv.imgproc.Imgproc.*

object BookDetector {

    init {
        nu.pattern.OpenCV.loadShared()
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME)
    }

    fun detectBooks() {
        var path = System.getProperty("user.dir")
        path = path + "/booklab-backend/resources/bookshelf.jpg"

        val image = imread(path)
        val books = detectBooks(image)

        for (i in books.indices) {
            imwrite(System.getProperty("user.dir") + "/booklab-backend/resources/books/roi_" + i + ".jpg", books[i])
        }
    }

    fun detectBooks(image: Mat): List<Mat> {
        var image = image
        image = ImgProcessHelper.colorhist_equalize(image)
        val cropLocations = detectBookLocations(image)
        return cropBooks(image, cropLocations, false)
    }

    private fun detectBookLocations(image: Mat): List<Int> {
        var image = image
        val gray = Mat()
        val dilation = Mat()
        val reduced = Mat()
        val coordinates = ArrayList<Point>()

        image = ImgProcessHelper.colorhist_equalize(image)
        cvtColor(image, gray, COLOR_BGR2GRAY)
        val edges = ImgProcessHelper.autoCanny(gray)
        dilate(edges, dilation, Mat())
        reduce(dilation, reduced, 0, REDUCE_AVG)
        GaussianBlur(reduced, reduced, Size(), 3.0)

        for (i in 0..image.cols() - 1) {
            coordinates.add(Point(i.toDouble(), reduced.get(0, i)[0]))
        }

        val localMinima = findLocalMinima(coordinates, 5)

        val cropLocations = ArrayList(localMinima)
        cropLocations.add(0, 0)
        cropLocations.add(image.cols())

        return cropLocations
    }

    private fun cropBooks(im: Mat, cropLocations: List<Int>, strictCrop: Boolean): List<Mat> {
        val books = ArrayList<Mat>()
        for (i in 0..cropLocations.size - 1 - 1) {
            val book = cropBook(im, cropLocations[i], cropLocations[i + 1] - cropLocations[i], strictCrop)
            books.add(book)
        }
        return books
    }

    private fun cropBook(im: Mat, x: Int, width: Int, strictCrop: Boolean): Mat {
        var x = x
        var width = width
        if (!strictCrop) {
            width = (1.1 * (width + 0.05 * x)).toInt()
            x = (0.95 * x).toInt()
        }

        if (x + width > im.cols()) {
            width = width - (x + width - im.cols())
        }

        val roi = Rect(x, 0, width, im.rows())
        return Mat(im, roi)
    }

    private fun findLocalMinima(coordinates: List<Point>, windowSize: Int): List<Int> {
        val yCoordinates = coordinates.stream().map { a -> a.y }.collect<List<Double>, Any>(Collectors.toList())
        val localMinima = ArrayList<Int>()
        for (i in windowSize..coordinates.size - windowSize - 1) {
            val sublist = yCoordinates.subList(i - windowSize, i + windowSize + 1)
            if (sublist.indexOf(Collections.min(sublist)) == windowSize) {
                localMinima.add(coordinates[i].x.toInt())
            }
        }
        return localMinima
    }

    private fun drawGraphs(reducedImage: Mat, originalImage: Mat, lineLocations: List<Int>) {
        for (i in 0..originalImage.cols() - 1) {
            line(originalImage, Point(i.toDouble(), 0.0), Point(i.toDouble(), reducedImage.get(0, i)[0]), Scalar(255.0, 255.0, 0.0), 1)
        }

        for (i in lineLocations.indices) {
            line(originalImage, Point(lineLocations[i].toDouble(), 0.0), Point(lineLocations[i].toDouble(), originalImage.rows().toDouble()), Scalar(0.0, 255.0, 0.0), 2)
        }
    }


    @JvmStatic
    fun main(args: Array<String>) {
        detectBooks()
    }

}
