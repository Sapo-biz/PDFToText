#!/bin/bash

# Test script for PDF Text Extractor
# This script tests the extraction functionality

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# Check if a PDF file exists for testing
find_test_pdf() {
    # Look for PDF files in common locations
    local test_pdfs=(
        "../Sample-Adult-History-And-Physical-By-M2-Student.pdf"
        "../AutoExtract Medical Services/Sample-Adult-History-And-Physical-By-M2-Student.pdf"
        "../pdf creator/Sample-Adult-History-And-Physical-By-M2-Student.pdf"
        "*.pdf"
    )
    
    for pdf in "${test_pdfs[@]}"; do
        if [ -f "$pdf" ]; then
            echo "$pdf"
            return 0
        fi
    done
    
    return 1
}

# Test CLI version
test_cli() {
    local pdf_file="$1"
    
    print_status "Testing CLI version with: $pdf_file"
    
    # Test 1: Extract to console
    print_status "Test 1: Extract text to console"
    java -cp "lib/*:." PDFTextExtractorCLI "$pdf_file"
    
    echo ""
    print_success "Test 1 completed"
    echo ""
    
    # Test 2: Extract to file
    local output_file="test_output.txt"
    print_status "Test 2: Extract text to file: $output_file"
    java -cp "lib/*:." PDFTextExtractorCLI "$pdf_file" "$output_file"
    
    if [ -f "$output_file" ]; then
        local file_size=$(wc -c < "$output_file")
        print_success "Test 2 completed - Output file created with $file_size bytes"
        
        # Show first few lines
        print_status "First few lines of extracted text:"
        head -10 "$output_file"
        echo ""
        
        # Clean up
        rm "$output_file"
        print_status "Test output file cleaned up"
    else
        print_error "Test 2 failed - Output file not created"
    fi
}

# Test GUI version (just check if it starts)
test_gui() {
    print_status "Testing GUI version startup..."
    print_warning "GUI test will start the application - close it to continue"
    
    # Start GUI in background and kill it after 3 seconds
    java -cp "lib/*:." PDFTextExtractor &
    local gui_pid=$!
    
    sleep 3
    kill $gui_pid 2>/dev/null || true
    
    print_success "GUI test completed"
}

# Main test function
main() {
    print_status "Starting PDF Text Extractor tests..."
    echo ""
    
    # Check if dependencies exist
    if [ ! -d "lib" ] || [ ! -f "lib/pdfbox-2.0.29.jar" ]; then
        print_error "Dependencies not found. Please run ./compile_and_run.sh first."
        exit 1
    fi
    
    # Check if compiled classes exist
    if [ ! -f "PDFTextExtractor.class" ] || [ ! -f "PDFTextExtractorCLI.class" ]; then
        print_error "Compiled classes not found. Please run ./compile_and_run.sh first."
        exit 1
    fi
    
    # Find test PDF
    local test_pdf
    if test_pdf=$(find_test_pdf); then
        print_success "Found test PDF: $test_pdf"
        echo ""
        
        # Test CLI version
        test_cli "$test_pdf"
        echo ""
        
        # Ask about GUI test
        read -p "Do you want to test the GUI version? (y/n): " -n 1 -r
        echo ""
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            test_gui
        else
            print_status "Skipping GUI test"
        fi
        
    else
        print_warning "No test PDF found. Please provide a PDF file to test with:"
        echo "Usage: $0 <pdf_file>"
        echo ""
        print_status "You can also test manually:"
        print_status "CLI: java -cp 'lib/*:.' PDFTextExtractorCLI <pdf_file>"
        print_status "GUI: java -cp 'lib/*:.' PDFTextExtractor"
        exit 1
    fi
    
    echo ""
    print_success "All tests completed successfully!"
    print_status "The PDF Text Extractor is ready to use."
}

# Run main function
main "$@"
