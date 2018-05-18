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

package nl.tudelft.nlbooklab.backend.ocr.vision;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import kotlin.collections.CollectionsKt;
import nl.tudelft.nlbooklab.backend.ocr.BookDetector;
import nl.tudelft.nlbooklab.backend.ocr.BookOCR;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.opencv.imgcodecs.Imgcodecs.*;

/**
 * Class to read titles from books in image with use of Google Vision
 */
public class BookOCRVision implements BookOCR{

    /**
     * Initialize OpenCV
     */
    static {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * Retrieve list of books from image
     *
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

        return getTextFromVision(books);

    }

    /**
     * This method takes a list of images and returns a list with the strings found in those images
     * @param images list of openCV matrices containining images
     * @return list of strings found in the images
     */
    private static List<String> getTextFromVision(List<Mat> images) {
        List<AnnotateImageRequest> requests = images.stream()
            .map(BookOCRVision::createImageRequest)
            .collect(Collectors.toList());

        List<List<AnnotateImageRequest>> requestPartitions = CollectionsKt.chunked(requests, 16);

        List<List<String>> responsePartitions = requestPartitions.stream()
            .map(BookOCRVision::getImageResponseText)
            .collect(Collectors.toList());

        return responsePartitions.stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves results from Google Vision for a list of requests
     * @param requests list of requests
     * @return list of strings
     */
    private static List<String> getImageResponseText(List<AnnotateImageRequest> requests) {
        List<String> responseTexts = new ArrayList<>();

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();
            client.close();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.out.printf("Error: %s\n", res.getError().getMessage());
                    return responseTexts;
                }

                TextAnnotation annotation = res.getFullTextAnnotation();
                responseTexts.add(annotation.getText().replace("\n", " "));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseTexts;
    }

    /**
     * Creates an image request
     * @param image openCV matrix containing an image
     * @return AnnotateImageRequest
     */
    @NotNull
    private static AnnotateImageRequest createImageRequest(Mat image) {
        MatOfByte byteMat = new MatOfByte();
        imencode(".jpg", image.clone(), byteMat);

        byte[] bytes = byteMat.toArray();

        ByteString imgBytes = null;
        try {
            imgBytes = ByteString.readFrom(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();
        return AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
    }
}
