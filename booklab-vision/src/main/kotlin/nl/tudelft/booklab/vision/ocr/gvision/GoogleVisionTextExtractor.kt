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

package nl.tudelft.booklab.vision.ocr.gvision

import com.google.cloud.vision.v1.AnnotateImageRequest
import com.google.cloud.vision.v1.Feature
import com.google.cloud.vision.v1.Image
import com.google.cloud.vision.v1.ImageAnnotatorClient
import com.google.protobuf.ByteString
import nl.tudelft.booklab.vision.ocr.TextExtractor
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs.imencode

/**
 * A [TextExtractor] that used the Google Cloud Vision API to extract text from images.
 *
 * @param client The Google Vision annotator client to use for extracting the text.
 */
class GoogleVisionTextExtractor(private val client: ImageAnnotatorClient) : TextExtractor {
    override fun extract(mat: Mat): List<String> = batch(listOf(mat)).first()

    override fun batch(matrices: List<Mat>): List<List<String>> {
        return matrices.chunked(16) {
            val response = client.batchAnnotateImages(it.map { createRequest(it) })
            response.responsesList.map {
                val annotation = it.fullTextAnnotation
                listOf(annotation.text)
            }
        }.flatten()
    }

    /**
     * Create the Google Cloud Vision API request.
     */
    private fun createRequest(mat: Mat): AnnotateImageRequest {
        val byteString = let {
            val bytes = MatOfByte()
            imencode(".jpg", mat, bytes)
            ByteString.copyFrom(bytes.toArray())
        }

        val img = Image.newBuilder().setContent(byteString).build()
        val feat = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build()
        return AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build()
    }
}
