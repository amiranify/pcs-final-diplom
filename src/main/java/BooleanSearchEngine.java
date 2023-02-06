import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {
    private final Map<String, List<PageEntry>> map = new HashMap<>();
    List<String> stopList = new ArrayList<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        File stopFile = new File("stop-ru.txt");
        try (BufferedReader reader = new BufferedReader(new FileReader(stopFile))) {
            while (reader.ready()) {
                stopList.add(reader.readLine());
            }
        } catch (IOException e) {
            e.getMessage();
        }
        for (File pdf : pdfsDir.listFiles()) {
            var doc = new PdfDocument(new PdfReader(pdf));
            int pageCount = doc.getNumberOfPages();
            for (int i = 1; i <= pageCount; i++) {
                var page = doc.getPage(i);
                var text = PdfTextExtractor.getTextFromPage(page);
                var words = text.split("\\P{IsAlphabetic}+");
                Map<String, Integer> freqs = new HashMap<>(); // мапа, где ключом будет слово, а значением - частота
                for (var word : words) { // перебираем слова
                    if (word.isEmpty()) {
                        continue;
                    }
                    word = word.toLowerCase();
                    freqs.put(word, freqs.getOrDefault(word, 0) + 1);

                }
                for (String word : freqs.keySet()) {
                    PageEntry pageEntry = new PageEntry(pdf.getName(), i, freqs.get(word));
                    if (map.containsKey(word)) {
                        map.get(word).add(pageEntry);
                    } else {
                        map.put(word, new ArrayList<>());
                        map.get(word).add(pageEntry);
                    }
                    map.values().forEach(Collections::sort);
                }
            }
        }
    }

    @Override
    public List<PageEntry> search(String words) {
        String[] request = words.toLowerCase().split("\\P{IsAlphabetic}+");
        List<PageEntry> tempList = new ArrayList<>();
        List<PageEntry> result = new ArrayList<>();

        for (String newWord : request) {
            if (map.get(newWord) != null) {
                tempList.addAll(map.get(newWord));
                tempList.removeAll(stopList);
            }
        }
        Map<String, Map<Integer, Integer>> numberAndQuantity = new HashMap<>();
        for (PageEntry pageEntry : tempList) {
            numberAndQuantity.computeIfAbsent(pageEntry.getPdfName(), key -> new HashMap<>())
                    .merge(pageEntry.getPage(), pageEntry.getCount(), Integer::sum);
        }

        numberAndQuantity.forEach((key, value) -> {
            for (var tempPage : value.entrySet()) {
                result.add(new PageEntry(key, tempPage.getKey(), tempPage.getValue()));
            }
        });
        Collections.sort(result);
        return result;
    }
}