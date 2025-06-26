package com.yourcompany.intellirefer.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class DocumentParsingService {

    public String parse(InputStream inputStream, String fileExtension) throws IOException {
        if ("pdf".equalsIgnoreCase(fileExtension)) {
            return parsePdf(inputStream);
        } else if ("docx".equalsIgnoreCase(fileExtension)) {
            return parseDocx(inputStream);
        } else {
            throw new IllegalArgumentException("Unsupported file type for parsing: " + fileExtension);
        }
    }

    private String parsePdf(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }

    private String parseDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }
}