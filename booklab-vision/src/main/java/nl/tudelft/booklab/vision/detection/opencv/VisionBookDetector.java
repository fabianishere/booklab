package nl.tudelft.booklab.vision.detection.opencv;

import com.google.cloud.vision.v1.*;
import nl.tudelft.booklab.vision.ocr.gvision.GoogleVisionTextExtractor;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.opencv.imgproc.Imgproc.fillPoly;

public class VisionBookDetector extends AbstractBookDetector {

    private final ImageAnnotatorClient client;

    public VisionBookDetector(ImageAnnotatorClient client) {
        this.client = client;
    }

    @NotNull
    @Override
    public List<Mat> detect(@NotNull Mat mat) {
        return detectBooks(mat);
    }

    /**
     * Method to detect books in an image
     *
     * @param image openCV matrix containing an image
     * @return list of images (openCV matrices)
     */
    private List<Mat> detectBooks(Mat image) {
        image = ImageProcessingHelper.colorhistEqualize(image);
        List<Integer> cropLocations = detectBookLocations(image);
        return cropBooks(image, cropLocations, false);
    }

    /**
     * Find the locations of the books in the image
     *
     * @param image openCV matrix containing an image
     * @return list of x coordinates of each book-segment
     */
    private List<Integer> detectBookLocations(Mat image) {
        List<AnnotateImageRequest> requests = new ArrayList<>();
        requests.add(GoogleVisionTextExtractor.createImageRequest(image));

        List<MatOfPoint> points = getImageResponseTextBoxes(requests);

        Mat mask = new Mat(image.size(), image.type());
        mask.setTo(new Scalar(0, 0, 0));

        fillPoly(mask, points, new Scalar(255, 255, 255));

        return findCropLocations(mask);
    }

    /**
     * Retrieves results from Google Vision for a list of requests
     *
     * @param requests list of requests
     * @return list of strings
     */
    private List<MatOfPoint> getImageResponseTextBoxes(List<AnnotateImageRequest> requests) {
        List<MatOfPoint> result = new ArrayList<>();

        BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
        List<AnnotateImageResponse> responses = response.getResponsesList();
        client.close();

        for (AnnotateImageResponse res : responses) {
            TextAnnotation annotation = res.getFullTextAnnotation();

            List<MatOfPoint> textBoxes = annotation.getPagesList().stream()
                .map(Page::getBlocksList)
                .flatMap(List::stream)
                .map(Block::getParagraphsList)
                .flatMap(List::stream)
                .map(Paragraph::getWordsList)
                .flatMap(List::stream)
                .map(VisionBookDetector::getBoundingBoxPoints)
                .map(VisionBookDetector::toMatOfPoint)
                .collect(Collectors.toList());

            result.addAll(textBoxes);
        }

        return result;
    }

    private static List<Point> getBoundingBoxPoints(Block block) {
        return block.getBoundingBox()
            .getVerticesList()
            .stream()
            .map(vertex -> new Point(vertex.getX(), vertex.getY()))
            .collect(Collectors.toList());
    }

    private static List<Point> getBoundingBoxPoints(Paragraph paragraph) {
        return paragraph.getBoundingBox()
            .getVerticesList()
            .stream()
            .map(vertex -> new Point(vertex.getX(), vertex.getY()))
            .collect(Collectors.toList());
    }

    private static List<Point> getBoundingBoxPoints(Word word) {
        return word.getBoundingBox()
            .getVerticesList()
            .stream()
            .map(vertex -> new Point(vertex.getX(), vertex.getY()))
            .collect(Collectors.toList());
    }

    private static List<Point> getBoundingBoxPoints(Symbol symbol) {
        return symbol.getBoundingBox()
            .getVerticesList()
            .stream()
            .map(vertex -> new Point(vertex.getX(), vertex.getY()))
            .collect(Collectors.toList());
    }

    private static MatOfPoint toMatOfPoint(List<Point> points) {
        MatOfPoint matOfPoint = new MatOfPoint();
        matOfPoint.fromList(points);
        return matOfPoint;
    }

}
