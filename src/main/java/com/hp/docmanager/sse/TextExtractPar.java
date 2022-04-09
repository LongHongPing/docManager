package com.hp.docmanager.sse;

import com.google.common.base.Charsets;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.xmlbeans.XmlException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Description:
 * 文档关键字处理
 * @Author hp long
 * @Date 2022/3/7 16:53
 */
public class TextExtractPar {
    public static int lengthStrings = 0;
    public static int totalNumberKeywords = 0;
    public static int maxTupleSize = 0;
    public static int threshold = 100;

    // lookup1，a plaintext inverted index
    Multimap<String, String> lookup1 = ArrayListMultimap.create();
    public static Multimap<String, String> lp1 = ArrayListMultimap.create();
    // lookup2，the document identifier (title) and the keywords contained
    Multimap<String, String> lookup2 = ArrayListMultimap.create();
    public static Multimap<String, String> lp2 = ArrayListMultimap.create();

    static int counter = 0;

    public TextExtractPar(Multimap<String, String> lookup, Multimap<String, String> lookup2) {
        this.lookup1 = lookup;
        this.lookup2 = lookup2;
    }

    public Multimap<String, String> getL1() {
        return this.lookup1;
    }

    public Multimap<String, String> getL2() {
        return this.lookup2;
    }

    public static void extractTextPar(ArrayList<File> listOfFile)
            throws InterruptedException, ExecutionException, IOException {
        int threads = 0;
        if (Runtime.getRuntime().availableProcessors() > listOfFile.size()) {
            threads = listOfFile.size();
        } else {
            threads = Runtime.getRuntime().availableProcessors();
        }

        ExecutorService service = Executors.newFixedThreadPool(threads);
        ArrayList<File[]> inputs = new ArrayList<File[]>(threads);

        Printer.extraln("Number of Threads " + threads);
        for (int i = 0; i < threads; i++) {
            File[] tmp;
            if (i == threads - 1) {
                tmp = new File[listOfFile.size() / threads + listOfFile.size() % threads];
                for (int j = 0; j < listOfFile.size() / threads + listOfFile.size() % threads; j++) {
                    tmp[j] = listOfFile.get((listOfFile.size() / threads) * i + j);
                }
            } else {
                tmp = new File[listOfFile.size() / threads];
                for (int j = 0; j < listOfFile.size() / threads; j++) {
                    tmp[j] = listOfFile.get((listOfFile.size() / threads) * i + j);
                }
            }
            inputs.add(i, tmp);
        }

        List<Future<TextExtractPar>> futures = new ArrayList<Future<TextExtractPar>>();
        for (final File[] input : inputs) {
            Callable<TextExtractPar> callable = new Callable<TextExtractPar>() {
                public TextExtractPar call() throws Exception {
                    TextExtractPar output = extractOneDoc(input);
                    return output;
                }
            };
            futures.add(service.submit(callable));
        }

        service.shutdown();

        for (Future<TextExtractPar> future : futures) {
            Set<String> keywordSet1 = future.get().getL1().keySet();
            Set<String> keywordSet2 = future.get().getL2().keySet();

            for (String key : keywordSet1) {
                lp1.putAll(key, future.get().getL1().get(key));
                if (lp1.get(key).size() > maxTupleSize){
                    maxTupleSize = lp1.get(key).size();
                }
            }
            for (String key : keywordSet2) {
                lp2.putAll(key, future.get().getL2().get(key));
            }
        }

    }


    public static List<String> getWords(File file) throws FileNotFoundException{
        List<String> lines = extractDoc(file);
        List<String> words = new ArrayList<String>();

        for (int i = 0; i < lines.size(); i++) {
            CharArraySet noise = EnglishAnalyzer.getDefaultStopSet();
            Analyzer analyzer = new StandardAnalyzer(noise);
            List<String> token0 = com.hp.docmanager.sse.Tokenizer.tokenizeString(analyzer, lines.get(i));
            List<String> token = new ArrayList<String>();
            //removing numbers/1-letter keywords
            for (int j = 0; j < token0.size(); j++) {
                if ((!token0.get(j).matches(".*\\d+.*")
                        && (token0.get(j)).length() > 1)) {
                    token.add(token0.get(j));
                }
            }

            for (int j = 0; j < token.size(); j++) {
                if(!words.contains(token.get(j))){
                    words.add(token.get(j));
                }
            }
        }

        return words;
    }
    private static List<String> extractDoc(File file) throws FileNotFoundException{
        List<String> lines = new ArrayList<String>();

        FileInputStream fis = new FileInputStream(file);
        if (file.getName().endsWith(".docx")) {
            XWPFDocument doc;
            try {
                doc = new XWPFDocument(fis);
                XWPFWordExtractor ex = new XWPFWordExtractor(doc);
                lines.add(ex.getText());
                ex.close();
            } catch (IOException e) {
                Printer.debugln("File not read: " + file.getName());
            }
        } else if (file.getName().endsWith(".pptx")) {
            OPCPackage ppt;
            try {
                ppt = OPCPackage.open(fis);
                XSLFPowerPointExtractor xw = new XSLFPowerPointExtractor(ppt);
                lines.add(xw.getText());
                xw.close();
            } catch (XmlException e) {
                Printer.debugln("File not read: " + file.getName());
            } catch (IOException e) {
                Printer.debugln("File not read: " + file.getName());
            } catch (OpenXML4JException e) {
                Printer.debugln("File not read: " + file.getName());
            }
        } else if (file.getName().endsWith(".xlsx")) {
            OPCPackage xls;
            try {
                xls = OPCPackage.open(fis);
                XSSFExcelExtractor xe = new XSSFExcelExtractor(xls);
                lines.add(xe.getText());
                xe.close();
            } catch (InvalidFormatException e) {
                Printer.debugln("File not read: " + file.getName());
            } catch (IOException e) {
                Printer.debugln("File not read: " + file.getName());
            } catch (XmlException e) {
                Printer.debugln("File not read: " + file.getName());
            } catch (OpenXML4JException e) {
                Printer.debugln("File not read: " + file.getName());
            }
        } else if (file.getName().endsWith(".doc")) {
            NPOIFSFileSystem fs;
            try {
                fs = new NPOIFSFileSystem(file);
                WordExtractor extractor = new WordExtractor(fs.getRoot());
                for (String rawText : extractor.getParagraphText()) {
                    lines.add(extractor.stripFields(rawText));
                }
                extractor.close();
            } catch (IOException e) {
                Printer.debugln("File not read: " + file.getName());
            }
        }
        else if (file.getName().endsWith(".pdf")) {
            PDFParser parser;
            try {
                parser = new PDFParser((RandomAccessRead) fis);
                parser.parse();
                COSDocument cd = parser.getDocument();
                PDFTextStripper stripper = new PDFTextStripper();
                lines.add(stripper.getText(new PDDocument(cd)));
            } catch (IOException e) {
                Printer.debugln("File not read: " + file.getName());
            }
        } else if (file.getName().endsWith(".gif") && file.getName().endsWith(".jpeg")
                && file.getName().endsWith(".wmv") && file.getName().endsWith(".mpeg")
                && file.getName().endsWith(".mp4")) {
            lines.add(file.getName());
        } else {
            try {
                lines = Files.readLines(file, Charsets.UTF_8);
            } catch (IOException e) {
                Printer.debugln("File not read: " + file.getName());
            } finally {
                try {
                    fis.close();
                } catch (IOException ioex) {
                    ioex.printStackTrace();
                }
            }
        }

        return lines;
    }


    private static TextExtractPar extractOneDoc(File[] listOfFile) throws FileNotFoundException {
        Multimap<String, String> lookup1 = ArrayListMultimap.create();
        Multimap<String, String> lookup2 = ArrayListMultimap.create();

        for (File file : listOfFile) {
            for (int j = 0; j < 100; j++) {
                if (counter == (int) ((j + 1) * listOfFile.length / 100)) {
                    Printer.extraln("Number of files read equals " + j + " %");
                    break;
                }
            }

            counter++;
            List<String> lines = extractDoc(file);

            //Begin word extraction
            int temporaryCounter = 0;

            // Filter threshold
            int counterDoc = 0;
            for (int i = 0; i < lines.size(); i++) {
                CharArraySet noise = EnglishAnalyzer.getDefaultStopSet();
                Analyzer analyzer = new StandardAnalyzer(noise);
                List<String> token0 = com.hp.docmanager.sse.Tokenizer.tokenizeString(analyzer, lines.get(i));
                List<String> token = new ArrayList<String>();
                //removing numbers/1-letter keywords
                for (int j=0; j<token0.size();j++){
                    if ((!token0.get(j).matches(".*\\d+.*")
                            && (token0.get(j)).length() >1)){
                        token.add(token0.get(j));
                    }
                }
                temporaryCounter = temporaryCounter + token.size();

                for (int j = 0; j < token.size(); j++) {

                    // Avoid counting occurrences of words in the same file
                    if (!lookup2.get(file.getName()).contains(token.get(j))) {
                        lookup2.put(file.getName(), token.get(j));
                    }
                    // Avoid counting occurrences of words in the same file
                    if (!lookup1.get(token.get(j)).contains(file.getName())) {
                        lookup1.put(token.get(j), file.getName());
                    }
                }
            }
        }
        // Printer.debugln(lookup.toString());
        return new TextExtractPar(lookup1, lookup2);
    }
}
