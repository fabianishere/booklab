package nl.tudelft.booklab.vision.detection.opencv;

import com.google.cloud.vision.v1.*;
import nl.tudelft.booklab.vision.ocr.gvision.GoogleVisionTextExtractor;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.opencv.core.Core.*;
import static org.opencv.imgproc.Imgproc.*;

public class GoogleVisionBookDetector extends AbstractBookDetector {

    private final ImageAnnotatorClient client;

    public GoogleVisionBookDetector(ImageAnnotatorClient client) {
        this.client = client;
    }

    @NotNull
    @Override
    public List<Rect> detect(@NotNull Mat mat) {
        return detectBooks(mat);
    }

    /**
     * Method to detect books in an image
     *
     * @param image openCV matrix containing an image
     * @return list of images (openCV matrices)
     */
    private List<Rect> detectBooks(Mat image) {
        image = ImageProcessingHelper.colorhistEqualize(image);
        List<Rect> books = new ArrayList<>();

        Mat mask = findTextRegions(image);
        dilate(mask, mask, getStructuringElement(MORPH_ELLIPSE, new Size(10,10)));
        Map<Mat, Mat> shelfMaskMap = findShelves(image, mask);

        for (Map.Entry<Mat, Mat> entry : shelfMaskMap.entrySet()) {
            books.addAll(findBooks(entry.getKey(), entry.getValue()));
        }

        return books;
    }

    /**
     * Finds the books in an image of a shelf
     *
     * @param shelf openCV matrix containing an image of the shelf
     * @param mask openCV matrix containing a binary image containing the text regions of the shelf
     * @return
     */
    static List<Rect> findBooks(Mat shelf, Mat mask) {
        List<Integer> cropLocations = findCropLocations(mask, REDUCE_MAX);
        return cropBooks(shelf, cropLocations, false);
    }

    /**
     * Finds the locations of text in the image
     *
     * @param image openCV matrix containing an image
     * @return binary image with the text regions in white
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

    /**
     * Determines whether the majority of the books on an image are oriented horizontally
     *
     * @param boxes list of text boxes
     * @return true if the majority of books is horizontal
     */
    private static boolean isHorizontal(List<MatOfPoint> boxes) {
        long horizontalBoxes = boxes.stream()
            .map(Imgproc::boundingRect)
            .filter(rect -> rect.width > rect.height)
            .count();

        double ratio = (double) horizontalBoxes / (double) boxes.size();
        return ratio > 0.5;
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

    /**
     * Converts the bounding box of a block to a list of coordinate points
     *
     * @param block block to get the bounding box points for
     * @return list of points corresponding to the corners of the bounding box
     */
    private static List<Point> getBoundingBoxPoints(Block block) {
        return block.getBoundingBox()
            .getVerticesList()
            .stream()
            .map(vertex -> new Point(vertex.getX(), vertex.getY()))
            .collect(Collectors.toList());
    }

    /**
     * Converts the bounding box of a paragraph to a list of coordinate points
     *
     * @param paragraph block to get the bounding box points for
     * @return list of points corresponding to the corners of the bounding box
     */
    private static List<Point> getBoundingBoxPoints(Paragraph paragraph) {
        return paragraph.getBoundingBox()
            .getVerticesList()
            .stream()
            .map(vertex -> new Point(vertex.getX(), vertex.getY()))
            .collect(Collectors.toList());
    }

    /**
     * Converts the bounding word of a block to a list of coordinate points
     *
     * @param word block to get the bounding box points for
     * @return list of points corresponding to the corners of the bounding box
     */
    private static List<Point> getBoundingBoxPoints(Word word) {
        return word.getBoundingBox()
            .getVerticesList()
            .stream()
            .map(vertex -> new Point(vertex.getX(), vertex.getY()))
            .collect(Collectors.toList());
    }

    /**
     * Converts the bounding box of a symbol to a list of coordinate points
     *
     * @param symbol symbol to get the bounding box points for
     * @return list of points corresponding to the corners of the bounding box
     */
    private static List<Point> getBoundingBoxPoints(Symbol symbol) {
        return symbol.getBoundingBox()
            .getVerticesList()
            .stream()
            .map(vertex -> new Point(vertex.getX(), vertex.getY()))
            .collect(Collectors.toList());
    }

    /**
     * Converts a list of Points to an OpenCV MatOfPoint
     *
     * @param points the list of points to be converted
     * @return a MatOfPoint consisting of the input points
     */
    private static MatOfPoint toMatOfPoint(List<Point> points) {
        MatOfPoint matOfPoint = new MatOfPoint();
        matOfPoint.fromList(points);
        return matOfPoint;
    }

}
