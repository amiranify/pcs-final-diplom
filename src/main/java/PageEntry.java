import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PageEntry implements Comparable<PageEntry> {
    private final String pdfName;
    private final int page;
    private final int count;

    public PageEntry(String pdfName, int page, int count) {
        this.pdfName = pdfName;
        this.page = page;
        this.count = count;
    }

    public String getPdfName() {
        return pdfName;
    }

    public int getPage() {
        return page;
    }

    public int getCount() {
        return count;
    }

    @Override
    public int compareTo(PageEntry o) {
        int result = Integer.compare(o.getCount(), this.getCount());
        return result;
    }

    @Override
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
            return json;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
