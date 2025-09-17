import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import javax.swing.UIManager;

public class PDFTextExtractor extends JFrame implements DropTargetListener {
    
    private JPanel mainPanel;
    private JLabel dropZoneLabel;
    private JTextArea textArea;
    private JButton uploadButton;
    private JButton copyButton;
    private JButton clearButton;
    private JButton saveButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JLabel pageInfoLabel;
    private Tesseract tesseract;
    private DropTarget dropTarget;
    private PDDocument currentDocument;
    private int currentPageCount;
    private String currentFileName;
    
    public PDFTextExtractor() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupDragAndDrop();
        initializeTesseract();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("PDF Text Extractor - Professional Edition");
        setSize(900, 700);
        setLocationRelativeTo(null);
        setResizable(true);
    }
    
    private void initializeComponents() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Drop zone panel
        JPanel dropZonePanel = createDropZonePanel();
        
        // Control panel
        JPanel controlPanel = createControlPanel();
        
        // Text display panel
        JPanel textPanel = createTextPanel();
        
        // Status panel
        JPanel statusPanel = createStatusPanel();
        
        // Create a center panel that contains both control and text panels
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(controlPanel, BorderLayout.NORTH);
        centerPanel.add(textPanel, BorderLayout.CENTER);
        
        mainPanel.add(dropZonePanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createDropZonePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("PDF Drop Zone"));
        panel.setPreferredSize(new Dimension(0, 120));
        panel.setBackground(new Color(240, 248, 255));
        
        dropZoneLabel = new JLabel("<html><center><b>Drag & Drop PDF Here</b><br>" +
                                  "or click Upload to select file<br>" +
                                  "<small>Supports both text and image-based PDFs</small></center></html>");
        dropZoneLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dropZoneLabel.setVerticalAlignment(SwingConstants.CENTER);
        dropZoneLabel.setForeground(new Color(70, 130, 180));
        dropZoneLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        panel.add(dropZoneLabel, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBorder(new TitledBorder("Controls"));
        
        uploadButton = new JButton("Upload PDF");
        uploadButton.setPreferredSize(new Dimension(120, 35));
        uploadButton.setFont(new Font("Arial", Font.BOLD, 12));
        uploadButton.setBackground(new Color(70, 130, 180));
        uploadButton.setForeground(Color.BLACK);
        uploadButton.setFocusPainted(false);
        
        copyButton = new JButton("Copy Text");
        copyButton.setPreferredSize(new Dimension(120, 35));
        copyButton.setFont(new Font("Arial", Font.BOLD, 12));
        copyButton.setBackground(new Color(34, 139, 34));
        copyButton.setForeground(Color.BLACK);
        copyButton.setFocusPainted(false);
        copyButton.setEnabled(false);
        
        saveButton = new JButton("Save Text");
        saveButton.setPreferredSize(new Dimension(120, 35));
        saveButton.setFont(new Font("Arial", Font.BOLD, 12));
        saveButton.setBackground(new Color(255, 140, 0));
        saveButton.setForeground(Color.BLACK);
        saveButton.setFocusPainted(false);
        saveButton.setEnabled(false);
        
        clearButton = new JButton("Clear");
        clearButton.setPreferredSize(new Dimension(120, 35));
        clearButton.setFont(new Font("Arial", Font.BOLD, 12));
        clearButton.setBackground(new Color(220, 20, 60));
        clearButton.setForeground(Color.BLACK);
        clearButton.setFocusPainted(false);
        clearButton.setEnabled(false);
        
        panel.add(uploadButton);
        panel.add(copyButton);
        panel.add(saveButton);
        panel.add(clearButton);
        
        return panel;
    }
    
    private JPanel createTextPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Extracted Text"));
        panel.setPreferredSize(new Dimension(0, 400));
        
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setBackground(Color.WHITE);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setRows(15); // Set minimum number of rows
        textArea.setText("Extracted text will appear here...\n\nDrag and drop a PDF file or click 'Upload PDF' to get started.");
        textArea.setForeground(new Color(100, 100, 100)); // Gray text for placeholder
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        scrollPane.setPreferredSize(new Dimension(0, 400));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(5, 0, 0, 0));
        
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        progressBar.setVisible(false);
        
        statusLabel = new JLabel("Ready to extract text from PDFs");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(100, 100, 100));
        
        pageInfoLabel = new JLabel("");
        pageInfoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        pageInfoLabel.setForeground(new Color(100, 100, 100));
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.add(statusLabel);
        leftPanel.add(Box.createHorizontalStrut(20));
        leftPanel.add(pageInfoLabel);
        
        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(progressBar, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void setupLayout() {
        setContentPane(mainPanel);
    }
    
    private void setupEventHandlers() {
        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFileChooser();
            }
        });
        
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyToClipboard();
            }
        });
        
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveToFile();
            }
        });
        
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearText();
            }
        });
    }
    
    private void setupDragAndDrop() {
        dropTarget = new DropTarget(dropZoneLabel, this);
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
                throw new Exception("Tessdata directory not found. Tried paths: " + String.join(", ", possiblePaths));
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
    
    private void openFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "PDF Files", "pdf");
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            processPDF(selectedFile);
        }
    }
    
    private void processPDF(File pdfFile) {
        SwingWorker<String, String> worker = new SwingWorker<String, String>() {
            @Override
            protected String doInBackground() throws Exception {
                SwingUtilities.invokeLater(() -> {
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                    progressBar.setString("Processing PDF...");
                    statusLabel.setText("Extracting text from: " + pdfFile.getName());
                    uploadButton.setEnabled(false);
                });
                
                try {
                    // Close previous document if open
                    if (currentDocument != null) {
                        currentDocument.close();
                    }
                    
                    currentDocument = PDDocument.load(pdfFile);
                    currentPageCount = currentDocument.getNumberOfPages();
                    currentFileName = pdfFile.getName();
                    
                    publish("Processing " + currentPageCount + " pages...");
                    
                    // First, try to extract text directly
                    String extractedText = extractTextDirectly();
                    
                    // If no text found, try OCR on each page
                    if (extractedText == null || extractedText.trim().isEmpty()) {
                        publish("No text found, using OCR on images...");
                        extractedText = extractTextWithOCR();
                    }
                    
                    return extractedText;
                } catch (Exception e) {
                    throw new Exception("Error processing PDF: " + e.getMessage());
                }
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String chunk : chunks) {
                    statusLabel.setText(chunk);
                }
            }
            
            @Override
            protected void done() {
                try {
                    String result = get();
                    textArea.setText(result);
                    textArea.setForeground(Color.BLACK); // Set text color to black for actual content
                    textArea.setCaretPosition(0);
                    copyButton.setEnabled(true);
                    saveButton.setEnabled(true);
                    clearButton.setEnabled(true);
                    pageInfoLabel.setText("Pages: " + currentPageCount + " | File: " + currentFileName);
                    statusLabel.setText("Text extracted successfully from: " + currentFileName);
                } catch (Exception e) {
                    showError("Failed to extract text: " + e.getMessage());
                    statusLabel.setText("Error extracting text");
                    pageInfoLabel.setText("");
                } finally {
                    progressBar.setVisible(false);
                    uploadButton.setEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
    
    private String extractTextDirectly() {
        try {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(currentPageCount);
            return stripper.getText(currentDocument);
        } catch (Exception e) {
            System.out.println("Direct text extraction failed: " + e.getMessage());
            return null;
        }
    }
    
    private String extractTextWithOCR() {
        if (tesseract == null) {
            return "OCR not available - Tesseract not properly initialized";
        }
        
        StringBuilder fullText = new StringBuilder();
        PDFRenderer renderer = new PDFRenderer(currentDocument);
        
        try {
            for (int page = 0; page < currentPageCount; page++) {
                // Note: Progress updates will be handled in the main SwingWorker
                
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
    
    private void copyToClipboard() {
        String text = textArea.getText();
        if (text != null && !text.trim().isEmpty()) {
            StringSelection selection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
            statusLabel.setText("Text copied to clipboard");
            
            // Show temporary success message
            Timer timer = new Timer(2000, e -> statusLabel.setText("Ready"));
            timer.setRepeats(false);
            timer.start();
        }
    }
    
    private void saveToFile() {
        String text = textArea.getText();
        if (text == null || text.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No text to save", "Save Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Extracted Text");
        fileChooser.setSelectedFile(new File(currentFileName.replace(".pdf", "_extracted.txt")));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                java.nio.file.Files.write(selectedFile.toPath(), text.getBytes());
                statusLabel.setText("Text saved to: " + selectedFile.getName());
                
                // Show temporary success message
                Timer timer = new Timer(3000, e -> statusLabel.setText("Ready"));
                timer.setRepeats(false);
                timer.start();
            } catch (IOException e) {
                showError("Failed to save file: " + e.getMessage());
            }
        }
    }
    
    private void clearText() {
        textArea.setText("Extracted text will appear here...\n\nDrag and drop a PDF file or click 'Upload PDF' to get started.");
        textArea.setForeground(new Color(100, 100, 100)); // Restore placeholder color
        copyButton.setEnabled(false);
        saveButton.setEnabled(false);
        clearButton.setEnabled(false);
        pageInfoLabel.setText("");
        statusLabel.setText("Text cleared");
        
        // Close current document
        if (currentDocument != null) {
            try {
                currentDocument.close();
                currentDocument = null;
            } catch (IOException e) {
                System.err.println("Error closing document: " + e.getMessage());
            }
        }
        
        // Reset status after 2 seconds
        Timer timer = new Timer(2000, e -> statusLabel.setText("Ready"));
        timer.setRepeats(false);
        timer.start();
    }
    
    private void showError(String message) {
        // Create a custom error dialog with copy functionality
        JPanel panel = new JPanel(new BorderLayout());
        
        JTextArea errorText = new JTextArea(message);
        errorText.setEditable(false);
        errorText.setFont(new Font("Monospaced", Font.PLAIN, 11));
        errorText.setBackground(new Color(255, 240, 240));
        errorText.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        errorText.setLineWrap(true);
        errorText.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(errorText);
        scrollPane.setPreferredSize(new Dimension(500, 200));
        
        JButton copyButton = new JButton("Copy Error");
        copyButton.addActionListener(e -> {
            StringSelection selection = new StringSelection(message);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
            copyButton.setText("Copied!");
            Timer timer = new Timer(2000, ev -> copyButton.setText("Copy Error"));
            timer.setRepeats(false);
            timer.start();
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(copyButton);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        JOptionPane.showMessageDialog(this, panel, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    // DropTargetListener implementation
    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY);
            dropZoneLabel.setText("<html><center><b>Drop PDF Here</b><br>" +
                                 "<small>Release to process PDF</small></center></html>");
            dropZoneLabel.setForeground(new Color(0, 100, 0));
        } else {
            dtde.rejectDrag();
        }
    }
    
    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        // Visual feedback can be added here
    }
    
    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
        // Not needed for this implementation
    }
    
    @Override
    public void dragExit(DropTargetEvent dte) {
        dropZoneLabel.setText("<html><center><b>Drag & Drop PDF Here</b><br>" +
                             "or click Upload to select file<br>" +
                             "<small>Supports both text and image-based PDFs</small></center></html>");
        dropZoneLabel.setForeground(new Color(70, 130, 180));
    }
    
    @Override
    public void drop(DropTargetDropEvent dtde) {
        try {
            if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY);
                
                @SuppressWarnings("unchecked")
                java.util.List<File> files = (java.util.List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                
                if (files.size() > 0) {
                    File file = files.get(0);
                    if (isPDFFile(file)) {
                        processPDF(file);
                    } else {
                        showError("Please drop a PDF file (.pdf)");
                    }
                }
                
                dtde.dropComplete(true);
            } else {
                dtde.rejectDrop();
            }
        } catch (Exception e) {
            showError("Error processing dropped file: " + e.getMessage());
            dtde.dropComplete(false);
        } finally {
            // Reset drop zone appearance
            dropZoneLabel.setText("<html><center><b>Drag & Drop PDF Here</b><br>" +
                                 "or click Upload to select file<br>" +
                                 "<small>Supports both text and image-based PDFs</small></center></html>");
            dropZoneLabel.setForeground(new Color(70, 130, 180));
        }
    }
    
    private boolean isPDFFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".pdf");
    }
    
    @Override
    public void dispose() {
        // Clean up resources
        if (currentDocument != null) {
            try {
                currentDocument.close();
            } catch (IOException e) {
                System.err.println("Error closing document: " + e.getMessage());
            }
        }
        super.dispose();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
            } catch (Exception e) {
                // Use default look and feel
            }
            new PDFTextExtractor().setVisible(true);
        });
    }
}
