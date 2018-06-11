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

package nl.tudelft.booklab.vision.ocr.tesseract

import org.bytedeco.javacpp.lept
import org.bytedeco.javacpp.lept.pixDestroy
import org.bytedeco.javacpp.tesseract
import org.opencv.core.Mat
import java.io.Closeable
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * The default Tesseract configuration.
 */
val DEFAULT_CONFIGURATION = mapOf(
    "load_system_dawg" to "false",
    "load_freq_daw" to "false",
    "tessedit_zero_rejection" to "false",
    "tessedit_char_whitelist" to "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
)

/**
 * Wrapper class for the [tesseract.TessBaseAPI] class provided by the Tesseract JavaCPP preset.
 *
 * @param data An [InputStream] of the training data for Tesseract.
 * @param configuration The configuration properties for Tesseract.
 */
class Tesseract(
    data: InputStream,
    configuration: Map<String, String> = DEFAULT_CONFIGURATION
) : Closeable {
    /**
     * The internal [tesseract.TessBaseAPI] object.
     */
    private val api = tesseract.TessBaseAPI()

    /**
     * The prefix at which we store the Tesseract temporary files
     */
    private val prefix: Path

    /**
     * A lock to synchronize multithreaded accesses to the API.
     */
    private val lock = ReentrantLock()

    /**
     * Initialise the Tesseract API.
     */
    init {
        configuration.forEach { t, u -> api.SetVariable(t, u) }

        // Create Tesseract prefix in which we store the tessdata
        prefix = Files.createTempDirectory("tesseract").also {
            val tessData = Files.createDirectory(it.resolve("tessdata"))
            Files.copy(data, tessData.resolve("lang.traineddata"))
        }

        // Initialize the Tesseract API with the created prefix
        lock.withLock {
            val res = api.Init(prefix.toString(), "lang")
            if (res != 0) {
                throw IllegalStateException("The Tesseract API failed to initialise (error $res).")
            }
        }
    }

    /**
     * Extract the text from the given Leptonica pixel buffer.
     *
     * @param buffer The pixel buffer to extract the text from.
     * @return The text extracted from the pixel buffer.
     */
    fun extract(buffer: lept.PIX): String = lock.withLock {
        api.SetImage(buffer)
        val res = api.GetUTF8Text()
        val string = res.string
        res.deallocate()
        string
    }

    /**
     * Extract the text from the given OpenCV matrix.
     *
     * @param mat The matrix to extract the text from.
     * @return The string read from the image.
     */
    fun extract(mat: Mat): String = lock.withLock {
        val pix = mat.toPix()
        val string = extract(pix)
        pixDestroy(pix)
        string
    }

    /**
     * Release the resources that are hold by this class. It is import that this class is closed as it will otherwise
     * hold onto memory that will not be freed until the program ends.
     */
    override fun close() {
        lock.withLock {
            api.End()
            prefix.toFile().deleteRecursively()
        }
    }
}
