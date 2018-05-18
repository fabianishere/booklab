package nl.tudelft.nlbooklab.backend.ocr;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface BookOCR {
    public List<String> getBookList(InputStream is) throws IOException;
}
