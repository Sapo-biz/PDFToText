#!/bin/bash

# PDF Text Extractor CLI - Run Script
# This script runs the CLI version of the PDF Text Extractor

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if dependencies exist
check_dependencies() {
    if [ ! -d "lib" ] || [ ! -f "lib/pdfbox-2.0.29.jar" ]; then
        print_error "Dependencies not found. Please run ./compile_and_run.sh first to download dependencies."
        exit 1
    fi
}

# Check if Java is installed
check_java() {
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed or not in PATH"
        exit 1
    fi
}

# Compile CLI version if needed
compile_cli() {
    if [ ! -f "PDFTextExtractorCLI.class" ] || [ "PDFTextExtractorCLI.java" -nt "PDFTextExtractorCLI.class" ]; then
        print_status "Compiling CLI version..."
        javac -cp "lib/*:." PDFTextExtractorCLI.java
        if [ $? -eq 0 ]; then
            print_success "CLI compilation successful"
        else
            print_error "CLI compilation failed"
            exit 1
        fi
    else
        print_success "CLI version already compiled"
    fi
}

# Show usage
show_usage() {
    echo "PDF Text Extractor CLI"
    echo ""
    echo "Usage: $0 <pdf_file> [output_file]"
    echo ""
    echo "Arguments:"
    echo "  pdf_file    Path to the PDF file to process"
    echo "  output_file Optional path to save extracted text (default: prints to console)"
    echo ""
    echo "Examples:"
    echo "  $0 document.pdf"
    echo "  $0 document.pdf output.txt"
    echo ""
    echo "Note: Make sure Tesseract is installed for OCR functionality:"
    echo "  macOS: brew install tesseract"
    echo "  Ubuntu: sudo apt-get install tesseract-ocr"
}

# Main execution
main() {
    if [ $# -lt 1 ]; then
        show_usage
        exit 1
    fi
    
    check_java
    check_dependencies
    compile_cli
    
    print_status "Running PDF Text Extractor CLI..."
    print_warning "Note: If you encounter OCR errors, make sure Tesseract is installed"
    echo ""
    
    # Create classpath
    CLASSPATH="lib/*:."
    
    # Run CLI version
    java -cp "$CLASSPATH" PDFTextExtractorCLI "$@"
}

# Run main function
main "$@"
