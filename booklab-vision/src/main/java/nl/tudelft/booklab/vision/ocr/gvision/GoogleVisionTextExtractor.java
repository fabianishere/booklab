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

package nl.tudelft.booklab.vision.ocr.gvision;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import kotlin.collections.CollectionsKt;
import nl.tudelft.booklab.vision.ocr.TextExtractor;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;

import java.util.*;
import java.util.stream.Collectors;

import static org.opencv.imgcodecs.Imgcodecs.imencode;

/**
 * A {@link TextExtractor} that used the Google Cloud Vision API to extract text from images.
 */
public class GoogleVisionTextExtractor implements TextExtractor {
    /**
     * The {@link ImageAnnotatorClient} to use the Google Cloud Vision API.
     */
    private final ImageAnnotatorClient client;

    /**
     * Construct a {@link GoogleVisionTextExtractor} instance.
     *
     * @param client The annotator client to use.
     */
    public GoogleVisionTextExtractor(ImageAnnotatorClient client) {
        this.client = client;
    }

    /**
     * This method takes a list of images and returns a list with the strings found in those images
     * @param images list of openCV matrices containing images
     * @return list of strings found in the images
     */
    private List<String> getTextFromVision(List<? extends Mat> images) {
        List<AnnotateImageRequest> requests = images.stream()
            .map(GoogleVisionTextExtractor::createImageRequest)
            .collect(Collectors.toList());

        List<List<AnnotateImageRequest>> requestPartitions = CollectionsKt.chunked(requests, 16);

        List<List<String>> responsePartitions = requestPartitions.stream()
            .map(this::getImageResponseText)
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
    private List<String> getImageResponseText(List<AnnotateImageRequest> requests) {
        List<String> responseTexts = new ArrayList<>();

        BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
        List<AnnotateImageResponse> responses = response.getResponsesList();

        for (AnnotateImageResponse res : responses) {
            if (res.hasError()) {
                // TODO Find a way to handle errors correctly
                System.out.printf("Error: %s\n", res.getError().getMessage());
                return responseTexts;
            }

            TextAnnotation annotation = res.getFullTextAnnotation();
            responseTexts.add(annotation.getText().replace("\n", " "));
        }

        return responseTexts;
    }

    /**
     * Creates an image request
     * @param image openCV matrix containing an image
     * @return AnnotateImageRequest
     */
    public static AnnotateImageRequest createImageRequest(Mat image) {
        MatOfByte byteMat = new MatOfByte();
        imencode(".jpg", image.clone(), byteMat);
        ByteString imgBytes = ByteString.copyFrom(byteMat.toArray());

        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();
        return AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
    }

    @NotNull
    @Override
    public List<String> extract(@NotNull Mat mat) {
        return getTextFromVision(Collections.singletonList(mat));
    }

    @NotNull
    @Override
    public List<List<String>> batch(@NotNull List<? extends Mat> matrices) {
        return getTextFromVision(matrices)
            .stream()
            .map(Collections::singletonList)
            .collect(Collectors.toList());
    }
}
