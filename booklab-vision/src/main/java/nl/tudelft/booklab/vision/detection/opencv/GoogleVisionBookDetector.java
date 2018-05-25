package nl.tudelft.booklab.vision.detection.opencv;

import com.google.cloud.vision.v1.*;
import nl.tudelft.booklab.vision.ocr.gvision.GoogleVisionTextExtractor;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.opencv.core.Core.REDUCE_MAX;
import static org.opencv.core.Core.ROTATE_90_CLOCKWISE;
import static org.opencv.core.Core.rotate;
import static org.opencv.imgproc.Imgproc.fillPoly;

public class GoogleVisionBookDetector extends AbstractBookDetector {

    private final ImageAnnotatorClient client;

    public GoogleVisionBookDetector(ImageAnnotatorClient client) {
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
        List<Mat> books = new ArrayList<>();

        Mat mask = findTextRegions(image);
        Map<Mat, Mat> shelfMaskMap = findShelves(image, mask);

        for (Map.Entry<Mat, Mat> entry : shelfMaskMap.entrySet()) {
            books.addAll(findBooks(entry.getKey(), entry.getValue()));
        }

        return books;
    }

    static List<Mat> findBooks(Mat shelf, Mat mask) {
        List<Integer> cropLocations = findCropLocations(mask, REDUCE_MAX);
        return cropBooks(shelf, cropLocations, false);
    }

    /**
     * Find the locations of text in the image
     *
     * @param image openCV matrix containing an image
     * @return
     */
    private Mat findTextRegions(Mat image) {
        List<AnnotateImageRequest> requests = new ArrayList<>();
        requests.add(GoogleVisionTextExtractor.createImageRequest(image));

        List<MatOfPoint> textBoxes = getImageResponseTextBoxes(requests);

        Mat mask = new Mat(image.size(), image.type());
        mask.setTo(new Scalar(0, 0, 0));

        fillPoly(mask, textBoxes, new Scalar(255, 255, 255));

        if (isHorizontal(textBoxes)) {
            rotate(mask, mask, ROTATE_90_CLOCKWISE);
            rotate(image, image, ROTATE_90_CLOCKWISE);
        }

        return mask;
    }

    private static boolean isHorizontal(List<MatOfPoint> boxes) {
        double averageRatio = boxes.stream()
            .map(Imgproc::boundingRect)
            .mapToDouble(a -> (double) a.width / (double) a.height)
            .average().getAsDouble();

        System.out.println(averageRatio);

        return averageRatio > 1;
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

        for (AnnotateImageResponse res : responses) {
            TextAnnotation annotation = res.getFullTextAnnotation();

            List<MatOfPoint> textBoxes = annotation.getPagesList().stream()
                .map(Page::getBlocksList)
                .flatMap(List::stream)
                .map(Block::getParagraphsList)
                .flatMap(List::stream)
                .map(Paragraph::getWordsList)
                .flatMap(List::stream)
                .map(GoogleVisionBookDetector::getBoundingBoxPoints)
                .map(GoogleVisionBookDetector::toMatOfPoint)
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
