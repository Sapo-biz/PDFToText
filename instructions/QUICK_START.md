# PDF Text Extractor - Quick Start Guide

## 🚀 Get Started in 3 Steps

### 1. Build the Application
```bash
./compile_and_run.sh
```
This will:
- Download all dependencies automatically
- Compile both GUI and CLI versions
- Set up OCR support

### 2. Choose Your Version

**GUI Version (Recommended for most users):**
- Drag & drop PDFs
- Visual progress indicators
- Copy to clipboard
- Save to file
- Professional interface

**CLI Version (For automation/batch processing):**
```bash
./run_cli.sh document.pdf output.txt
```

### 3. Test It Out
```bash
./test_extraction.sh
```

## ✨ Key Features

- **Handles "Uncopyable" PDFs**: Uses OCR for image-based PDFs
- **Dual Extraction**: Tries direct text extraction first, then OCR
- **Professional GUI**: Modern interface with drag-and-drop
- **Command Line**: Perfect for batch processing
- **Multi-page Support**: Processes entire PDFs with progress tracking
- **Save & Copy**: Export extracted text easily

## 📋 Requirements

- Java 8 or higher
- Tesseract OCR (installed automatically on macOS with Homebrew)

## 🛠️ Troubleshooting

**OCR not working?**
```bash
brew install tesseract  # macOS
```

**Need more memory for large PDFs?**
```bash
java -Xmx2g -cp 'lib/*:.' PDFTextExtractor
```

## 📁 What You Get

- `PDFTextExtractor.java` - Main GUI application
- `PDFTextExtractorCLI.java` - Command-line version
- `compile_and_run.sh` - One-click build script
- `run_cli.sh` - CLI convenience script
- `test_extraction.sh` - Test script
- Complete documentation and error handling

Ready to extract text from any PDF! 🎉
