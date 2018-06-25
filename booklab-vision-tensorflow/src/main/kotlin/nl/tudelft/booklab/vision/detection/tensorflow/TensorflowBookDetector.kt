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

package nl.tudelft.booklab.vision.detection.tensorflow

import nl.tudelft.booklab.vision.detection.BookDetector
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.imgproc.Imgproc
import org.tensorflow.Graph
import org.tensorflow.Session
import org.tensorflow.Tensor
import org.tensorflow.types.UInt8
import java.io.Closeable
import java.io.InputStream
import java.nio.ByteBuffer

/**
 * A [BookDetector] that uses a Tensorflow model to detect books in images.
 *
 * @property graph The Tensorflow model to use as a frozen inference graph.
 * @property score The minimum probability required for a book to be accepted (between 0 and 1).
 */
class TensorflowBookDetector(private val graph: Graph, private val score: Float = 0.5f) : BookDetector, Closeable {
    private val session = Session(graph)

    /**
     * A convenience constructor for constructing the detector from a model given as [InputStream].
     *
     * @param input The input stream to read the definition from.
     * @param estimatedSize The estimated size of the input stream.
     */
    constructor(input: InputStream, estimatedSize: Int = DEFAULT_BUFFER_SIZE) : this(Graph().apply {
        importGraphDef(input.readBytes(estimatedSize))
    })

    override fun detect(mat: Mat): List<Rect> {
        val outputs = mat.toImageTensor().use { input ->
            session.runner()
                .feed("image_tensor", input)
                .fetch("detection_scores")
                .fetch("detection_classes")
                .fetch("detection_boxes")
                .run()
        }

        val scoresT = outputs[0].expect(Float::class.javaObjectType)
        val boxesT = outputs[2].expect(Float::class.javaObjectType)

        val maxObjects = scoresT.shape()[1].toInt()
        val scores = scoresT.copyTo(Array(1) { FloatArray(maxObjects) })[0]
        val boxes = boxesT.copyTo(Array(1) { Array(maxObjects) { FloatArray(4) } })[0]

        scoresT.close()
        boxesT.close()

        return scores.asSequence()
            .mapIndexed { index, score ->
                val box = boxes[index]
                val min = Point(box[1].toDouble() * mat.width(), box[0].toDouble() * mat.height())
                val max = Point(box[3].toDouble() * mat.width(), box[2].toDouble() * mat.height())
                if (score >= this.score)
                    Rect(min, max)
                else
                    null
            }
            .filterNotNull()
            .toList()
    }

    override fun close() {
        session.close()
    }

    /**
     * Convert the given OpenCV matrix to an image tensor.
     *
     * @return The matrix represented as an image tensor.
     */
    private fun Mat.toImageTensor(): Tensor<UInt8> {
        // Convert the image to RGB format used by the Tensorflow model (OpenCV uses BGR)
        val rgb = Mat(width(), height(), CvType.CV_8UC3)
        Imgproc.cvtColor(this, rgb, Imgproc.COLOR_BGR2RGB)
        val size = rgb.total() * rgb.elemSize()
        val bytes = ByteArray(size.toInt())
        rgb.get(0, 0, bytes)
        val batchSize = 1L
        val channels = rgb.channels().toLong()
        val shape = longArrayOf(batchSize, height().toLong(), width().toLong(), channels)
        return Tensor.create(UInt8::class.java, shape, ByteBuffer.wrap(bytes))
    }

    private fun <T, R> Tensor<T>.use(block: (Tensor<T>) -> R): R = block(this).also { close() }

    companion object {
        init {
            nu.pattern.OpenCV.loadShared()
            System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME)
        }
    }
}
