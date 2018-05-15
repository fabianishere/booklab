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
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import org.bytedeco.javacpp.*;
import org.opencv.core.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.bytedeco.javacpp.lept.*;
import static org.opencv.imgcodecs.Imgcodecs.*;

/**
 * Class to read titles from books in image
 */
public class BookOCR {

    static {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * Wrapper method to preprocess all images
     * @param images list of images
     * @return preprocessed images
     */
    private static List<Mat> preprocessImages(List<Mat> images) {
        return images.stream().map(OCRPreprocessor::optimizeImg).collect(Collectors.toList());
    }

    /**
     * Retrieve text from image with Tesseract
     * @param mat image
     * @return String
     */
    public static String getTextWithTesseract(Mat mat) {
        String result = "";
        BytePointer outText;
        String path = System.getProperty("user.dir");

        tesseract.TessBaseAPI api = new tesseract.TessBaseAPI();

        // Initialize tesseract-ocr with English, without specifying tessdata path
        if (api.Init(path + "/booklab-backend/tessdata/", "ENG") != 0) {
            System.err.println("Could not initialize tesseract.");
            System.exit(1);
        }
        api.SetVariable("load_system_dawg", "false");
        api.SetVariable("load_freq_dawg", "false");
        api.ReadConfigFile(path+"/booklab-backend/tessdata/configs/api_config");

        // Open input image with leptonica library
        lept.PIX image = ImgProcessHelper.convertMatToPix(mat);

        api.SetImage(image);

        // Get OCR resultx
        outText = api.GetUTF8Text();
        String string = outText.getString();
        System.out.println("OCR output:\n" + string);

        // Destroy used object and release memory
        api.End();
        outText.deallocate();
        pixDestroy(image);
        return result;
    }

    /**
     * Retrieve list of books from image
     * @param is inputstream
     * @return list of titles
     * @throws IOException
     */
    public static List<String> getBookList(InputStream is) throws IOException {
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

        return books.stream().map(BookOCR::getTextFromVision).collect(Collectors.toList());

    }

    private static String getTextFromVision(Mat inputimg) {
        List<AnnotateImageRequest> requests = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        Mat mat = inputimg.clone();
        MatOfByte bytemat = new MatOfByte();
        imencode(".jpg", mat, bytemat);

        byte[] bytes = bytemat.toArray();

        ByteString imgBytes = null;
        try {
            imgBytes = ByteString.readFrom(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();
        AnnotateImageRequest request =
            AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();
            client.close();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.out.printf("Error: %s\n", res.getError().getMessage());
                    return sb.toString();
                }

                // For full list of available annotations, see http://g.co/cloud/vision/docs
                TextAnnotation annotation = res.getFullTextAnnotation();
                for (Page page: annotation.getPagesList()) {
                    String pageText = "";
                    for (Block block : page.getBlocksList()) {
                        String blockText = "";
                        for (Paragraph para : block.getParagraphsList()) {
                            String paraText = "";
                            for (Word word: para.getWordsList()) {
                                String wordText = "";
                                for (Symbol symbol: word.getSymbolsList()) {
                                    wordText = wordText + symbol.getText();
                                }
                                paraText = paraText + wordText;
                            }
                            // Output Example using Paragraph:
                            System.out.println("Paragraph: \n" + paraText);
                            System.out.println("Bounds: \n" + para.getBoundingBox() + "\n");
                            blockText = blockText + paraText;
                        }
                        pageText = pageText + blockText;
                    }
                }
                System.out.println(annotation.getText());
                sb.append(annotation.getText());
            }

        }

        catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
        String path = System.getProperty("user.dir") + "/booklab-backend/resources/bookshelf.jpg";

        InputStream is = new FileInputStream(path);
        getBookList(is);
    }

}
