# PDF Text Extractor - Professional Edition

A professional Java GUI application that extracts text from PDF files, including "uncopyable" image-based PDFs using OCR (Optical Character Recognition) technology. The application features a modern interface with drag-and-drop functionality, file upload, and comprehensive text extraction capabilities.

## Features

### Core Functionality
- **Dual Extraction Methods**: Automatically tries direct text extraction first, then falls back to OCR for image-based PDFs
- **Drag & Drop Interface**: Simply drag PDF files onto the application window
- **File Upload**: Click the "Upload PDF" button to select PDFs from your computer
- **Multi-page Support**: Processes entire PDFs with page-by-page progress indication
- **Text Display**: View extracted text in a scrollable text area with proper formatting
- **Copy to Clipboard**: One-click copying of extracted text
- **Save to File**: Save extracted text to a .txt file

### Advanced Features
- **OCR Integration**: Uses Tesseract OCR engine for image-based PDFs
- **Progress Indicators**: Visual feedback during PDF processing
- **Page Information**: Shows current page count and file information
- **Error Handling**: Comprehensive error handling with user-friendly messages
- **Modern GUI**: Professional-looking interface with system look and feel
- **Resource Management**: Proper cleanup of PDF documents and memory

## Supported PDF Types

### Text-based PDFs
- Standard PDFs with selectable text
- PDFs with embedded fonts
- Multi-column layouts
- Complex formatting

### Image-based PDFs (OCR)
- Scanned documents
- PDFs with embedded images
- "Uncopyable" PDFs
- Handwritten text (with varying accuracy)

## Requirements

### System Requirements
- Java 8 or higher
- Tesseract OCR engine (for image-based PDFs)

### Dependencies
- Apache PDFBox 2.0.29
- Tess4J 5.8.0
- JNA 5.13.0
- Commons Logging 1.2

## Installation

### Quick Start (Recommended)

1. **Clone or download** this repository
2. **Run the build script**:
   ```bash
   ./compile_and_run.sh
   ```
   This script will:
   - Check Java installation
   - Download all dependencies
   - Compile the application
   - Optionally run the application

### Manual Installation

#### Install Tesseract OCR

**On macOS:**
```bash
brew install tesseract
```

**On Ubuntu/Debian:**
```bash
sudo apt-get install tesseract-ocr
```

**On Windows:**
Download and install from: https://github.com/UB-Mannheim/tesseract/wiki

#### Download Dependencies

The build script automatically downloads all required JAR files to the `lib/` directory:
- `pdfbox-2.0.29.jar`
- `fontbox-2.0.29.jar`
- `tess4j-5.8.0.jar`
- `lept4j-1.0.1.jar`
- `jna-5.13.0.jar`
- `jna-platform-5.13.0.jar`
- `commons-logging-1.2.jar`

#### Compile and Run

```bash
# Compile
javac -cp "lib/*:." PDFTextExtractor.java

# Run
java -cp "lib/*:." PDFTextExtractor
```

## How to Use

### GUI Version (Recommended)

1. **Launch the Application**: Run using the build script or manually
2. **Add a PDF**: 
   - Drag and drop a PDF file onto the drop zone, OR
   - Click "Upload PDF" and select a file from your computer
3. **Wait for Processing**: The application will:
   - First attempt direct text extraction
   - If no text is found, use OCR on each page
   - Show progress for multi-page documents
4. **View Results**: Extracted text appears in the text area below
5. **Copy or Save**: Use "Copy Text" or "Save Text" buttons as needed
6. **Clear**: Click "Clear" to remove current text and start over

### CLI Version

For command-line usage or batch processing:

```bash
# Extract text to console
java -cp 'lib/*:.' PDFTextExtractorCLI document.pdf

# Extract text to file
java -cp 'lib/*:.' PDFTextExtractorCLI document.pdf output.txt

# Or use the convenience script
./run_cli.sh document.pdf output.txt
```

### Testing

Test the application with the provided test script:

```bash
# Test with any PDF file
./test_extraction.sh document.pdf

# Or let it find a test PDF automatically
./test_extraction.sh
```

### Interface Components

- **Drop Zone**: The blue area at the top for drag-and-drop
- **Upload Button**: Opens file chooser dialog
- **Copy Button**: Copies extracted text to clipboard
- **Save Button**: Saves extracted text to a .txt file
- **Clear Button**: Clears current text and closes PDF
- **Text Area**: Scrollable area displaying extracted text
- **Status Bar**: Shows current operation status and progress
- **Page Info**: Displays page count and current file name

## Technical Details

### Architecture
- **GUI Framework**: Java Swing with modern styling
- **PDF Processing**: Apache PDFBox for PDF manipulation
- **OCR Engine**: Tesseract 4.x via Tess4J Java wrapper
- **Drag & Drop**: Native Java DnD API
- **Threading**: SwingWorker for non-blocking processing
- **Memory Management**: Proper PDF document cleanup

### Processing Flow
1. **PDF Loading**: Load PDF using PDFBox
2. **Direct Extraction**: Attempt to extract text directly
3. **OCR Fallback**: If no text found, render pages as images
4. **OCR Processing**: Use Tesseract to extract text from images
5. **Text Assembly**: Combine all extracted text with page markers

### Performance Considerations
- **DPI Settings**: OCR uses 300 DPI for optimal accuracy
- **Memory Usage**: Large PDFs are processed page by page
- **Progress Updates**: Real-time progress indication for long operations

## Troubleshooting

### Common Issues

#### 1. "Tesseract not found!" or OCR Errors
**Symptoms**: OCR functionality not working, error messages about Tesseract
**Solutions**:
- Install Tesseract: `brew install tesseract` (macOS)
- Verify installation: `tesseract --version`
- Check tessdata location: `brew --prefix tesseract`
- Ensure language files exist in tessdata directory

#### 2. "UnsatisfiedLinkError" (OCR Issues)
**Symptoms**: Native library loading errors
**Solutions**:
- This is usually an architecture compatibility issue
- Ensure Tesseract is properly installed for your system architecture
- On Apple Silicon Macs, use Homebrew installation
- Check that native libraries match your Java architecture

#### 3. "Could not read PDF file"
**Symptoms**: PDF loading errors
**Solutions**:
- Verify the PDF file is not corrupted
- Ensure the file is a valid PDF
- Check file permissions
- Try with a different PDF file

#### 4. Poor OCR Results
**Symptoms**: Inaccurate text extraction from image-based PDFs
**Solutions**:
- Use high-quality PDFs with clear, readable text
- Ensure good contrast between text and background
- Try different PDF processing tools if results are consistently poor
- Consider preprocessing images for better OCR results

#### 5. Memory Issues with Large PDFs
**Symptoms**: OutOfMemoryError or slow performance
**Solutions**:
- Increase Java heap size: `java -Xmx2g -cp "lib/*:." PDFTextExtractor`
- Process smaller PDFs or split large documents
- Close other applications to free memory

### Performance Tips

#### For Best OCR Results:
- Use PDFs with:
  - High resolution (300+ DPI)
  - Good contrast
  - Clear, readable fonts
  - Minimal background noise
  - Proper orientation

#### For Best Performance:
- Close other applications when processing large PDFs
- Use SSD storage for faster file access
- Ensure adequate RAM (4GB+ recommended)
- Process PDFs during low system usage

## File Structure

```
pdfToText/
├── PDFTextExtractor.java      # Main GUI application
├── PDFTextExtractorCLI.java   # Command-line version
├── compile_and_run.sh         # Build and run script
├── run_cli.sh                 # CLI convenience script
├── test_extraction.sh         # Test script
├── README.md                  # This documentation
├── lib/                       # JAR dependencies (created by build script)
│   ├── pdfbox-2.0.29.jar
│   ├── fontbox-2.0.29.jar
│   ├── tess4j-5.8.0.jar
│   ├── lept4j-1.0.1.jar
│   ├── jna-5.13.0.jar
│   ├── jna-platform-5.13.0.jar
│   └── commons-logging-1.2.jar
└── tessdata/                  # Tesseract language data (created by build script)
    ├── eng.traineddata
    └── osd.traineddata
```

## Advanced Usage

### Command Line Options
While the application is primarily GUI-based, you can customize the JVM options:

```bash
# Increase memory allocation
java -Xmx2g -cp "lib/*:." PDFTextExtractor

# Enable debug logging
java -Djava.util.logging.config.file=logging.properties -cp "lib/*:." PDFTextExtractor
```

### Customization
The application can be customized by modifying:
- **DPI Settings**: Change OCR resolution in `extractTextWithOCR()` method
- **Language Support**: Add additional Tesseract language packs
- **UI Themes**: Modify colors and fonts in the GUI components
- **Processing Options**: Adjust PDFBox and Tesseract parameters

## License

This application uses the following open-source libraries:
- **Apache PDFBox**: Licensed under the Apache License 2.0
- **Tess4J**: Licensed under the Apache License 2.0
- **JNA**: Licensed under the Apache License 2.0

## Contributing

Contributions are welcome! Areas for improvement:
- Additional language support for OCR
- Batch processing capabilities
- Advanced PDF preprocessing options
- Export to different formats
- Performance optimizations

## Support

For issues and questions:
1. Check the troubleshooting section above
2. Verify all dependencies are properly installed
3. Test with different PDF files
4. Check system requirements and compatibility

## Version History

- **v1.0**: Initial release with basic PDF text extraction
- **v1.1**: Added OCR support for image-based PDFs
- **v1.2**: Enhanced GUI with drag-and-drop and progress indicators
- **v1.3**: Added save functionality and improved error handling
