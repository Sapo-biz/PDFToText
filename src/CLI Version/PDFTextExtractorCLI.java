import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

public class PDFTextExtractorCLI {
    
    private Tesseract tesseract;
    
    public PDFTextExtractorCLI() {
        initializeTesseract();
    }
    
    private void initializeTesseract() {
        try {
            tesseract = new Tesseract();
            
            // Try different possible tessdata paths
            String[] possiblePaths = {
                "/opt/homebrew/share/tessdata",  // Apple Silicon Homebrew
                "/usr/local/share/tessdata",     // Intel Homebrew
                "/usr/share/tessdata",           // System installation
                "tessdata",                      // Local copy
                "lib/Tess4J/tessdata",          // Bundled copy
                "../GitHub/ImageToText/Tess4J/tessdata",
                "/Users/jasonhe/Desktop/future Github Projects/GitHub/ImageToText/Tess4J/tessdata"
            };
            
            boolean tessdataFound = false;
            for (String path : possiblePaths) {
                File tessdataDir = new File(path);
                if (tessdataDir.exists() && tessdataDir.isDirectory()) {
                    tesseract.setDatapath(path);
                    tessdataFound = true;
                    System.out.println("Using tessdata path: " + path);
                    break;
                }
            }
            
            if (!tessdataFound) {
                System.err.println("Warning: Tessdata directory not found. OCR functionality will be disabled.");
                System.err.println("Tried paths: " + String.join(", ", possiblePaths));
                tesseract = null;
                return;
            }
            
            tesseract.setLanguage("eng");
            tesseract.setPageSegMode(1);
            tesseract.setOcrEngineMode(1);
            
            // Don't test OCR initialization to avoid dependency issues
            // The OCR will be tested when actually needed
            System.out.println("OCR engine initialized successfully");
            
        } catch (Exception e) {
            System.err.println("Warning: Failed to initialize OCR engine: " + e.getMessage());
            System.err.println("OCR functionality will be disabled, but regular PDF text extraction will still work.");
            tesseract = null;
        }
    }
    
    public String extractTextFromPDF(String pdfPath) {
        File pdfFile = new File(pdfPath);
        if (!pdfFile.exists()) {
            throw new IllegalArgumentException("PDF file does not exist: " + pdfPath);
        }
        
        if (!pdfFile.getName().toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("File is not a PDF: " + pdfPath);
        }
        
        System.out.println("Processing PDF: " + pdfFile.getName());
        
        try (PDDocument document = PDDocument.load(pdfFile)) {
            int pageCount = document.getNumberOfPages();
            System.out.println("PDF has " + pageCount + " pages");
            
            // First, try to extract text directly
            String extractedText = extractTextDirectly(document);
            
            // If no text found, try OCR on each page
            if (extractedText == null || extractedText.trim().isEmpty()) {
                System.out.println("No text found with direct extraction, using OCR...");
                extractedText = extractTextWithOCR(document);
            } else {
                System.out.println("Text extracted successfully using direct method");
            }
            
            return extractedText;
            
        } catch (IOException e) {
            throw new RuntimeException("Error processing PDF: " + e.getMessage(), e);
        }
    }
    
    private String extractTextDirectly(PDDocument document) {
        try {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(document.getNumberOfPages());
            return stripper.getText(document);
        } catch (Exception e) {
            System.out.println("Direct text extraction failed: " + e.getMessage());
            return null;
        }
    }
    
    private String extractTextWithOCR(PDDocument document) {
        if (tesseract == null) {
            return "OCR not available - Tesseract not properly initialized";
        }
        
        StringBuilder fullText = new StringBuilder();
        PDFRenderer renderer = new PDFRenderer(document);
        
        try {
            int pageCount = document.getNumberOfPages();
            for (int page = 0; page < pageCount; page++) {
                System.out.println("Processing page " + (page + 1) + " of " + pageCount + "...");
                
                // Render page as image
                BufferedImage image = renderer.renderImageWithDPI(page, 300); // 300 DPI for better OCR
                
                // Extract text using OCR
                String pageText = tesseract.doOCR(image);
                fullText.append("--- Page ").append(page + 1).append(" ---\n");
                fullText.append(pageText).append("\n\n");
            }
        } catch (Exception e) {
            return "OCR extraction failed: " + e.getMessage();
        }
        
        return fullText.toString();
    }
    
    public void saveTextToFile(String text, String outputPath) {
        try {
            Files.write(Paths.get(outputPath), text.getBytes());
            System.out.println("Text saved to: " + outputPath);
        } catch (IOException e) {
            throw new RuntimeException("Error saving text to file: " + e.getMessage(), e);
        }
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("PDF Text Extractor CLI");
            System.out.println("Usage: java -cp 'lib/*:.' PDFTextExtractorCLI <pdf_file> [output_file]");
            System.out.println("");
            System.out.println("Arguments:");
            System.out.println("  pdf_file    Path to the PDF file to process");
            System.out.println("  output_file Optional path to save extracted text (default: prints to console)");
            System.out.println("");
            System.out.println("Examples:");
            System.out.println("  java -cp 'lib/*:.' PDFTextExtractorCLI document.pdf");
            System.out.println("  java -cp 'lib/*:.' PDFTextExtractorCLI document.pdf output.txt");
            System.exit(1);
        }
        
        String pdfPath = args[0];
        String outputPath = args.length > 1 ? args[1] : null;
        
        try {
            PDFTextExtractorCLI extractor = new PDFTextExtractorCLI();
            String extractedText = extractor.extractTextFromPDF(pdfPath);
            
            if (outputPath != null) {
                extractor.saveTextToFile(extractedText, outputPath);
            } else {
                System.out.println("\n=== EXTRACTED TEXT ===");
                System.out.println(extractedText);
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
