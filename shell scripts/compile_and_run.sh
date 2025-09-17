#!/bin/bash

# PDF Text Extractor - Build and Run Script
# This script downloads dependencies, compiles the application, and runs it

set -e  # Exit on any error

echo "=== PDF Text Extractor Build Script ==="
echo ""

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

# Check if Java is installed
check_java() {
    print_status "Checking Java installation..."
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed or not in PATH"
        print_error "Please install Java 8 or higher"
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 8 ]; then
        print_error "Java 8 or higher is required. Found version: $JAVA_VERSION"
        exit 1
    fi
    
    print_success "Java $(java -version 2>&1 | head -n 1 | cut -d'"' -f2) found"
}

# Check if javac is available
check_javac() {
    print_status "Checking Java compiler..."
    if ! command -v javac &> /dev/null; then
        print_error "Java compiler (javac) is not installed or not in PATH"
        print_error "Please install JDK (Java Development Kit)"
        exit 1
    fi
    print_success "Java compiler found"
}

# Create directories
create_directories() {
    print_status "Creating directories..."
    mkdir -p lib
    mkdir -p tessdata
    print_success "Directories created"
}

# Download dependencies
download_dependencies() {
    print_status "Downloading dependencies..."
    
    # PDFBox dependencies
    PDFBOX_VERSION="2.0.29"
    TESS4J_VERSION="5.8.0"
    
    # Check if files already exist
    if [ ! -f "lib/pdfbox-${PDFBOX_VERSION}.jar" ]; then
        print_status "Downloading PDFBox ${PDFBOX_VERSION}..."
        curl -L -o "lib/pdfbox-${PDFBOX_VERSION}.jar" \
            "https://repo1.maven.org/maven2/org/apache/pdfbox/pdfbox/${PDFBOX_VERSION}/pdfbox-${PDFBOX_VERSION}.jar"
        print_success "PDFBox downloaded"
    else
        print_success "PDFBox already exists"
    fi
    
    if [ ! -f "lib/fontbox-${PDFBOX_VERSION}.jar" ]; then
        print_status "Downloading FontBox ${PDFBOX_VERSION}..."
        curl -L -o "lib/fontbox-${PDFBOX_VERSION}.jar" \
            "https://repo1.maven.org/maven2/org/apache/pdfbox/fontbox/${PDFBOX_VERSION}/fontbox-${PDFBOX_VERSION}.jar"
        print_success "FontBox downloaded"
    else
        print_success "FontBox already exists"
    fi
    
    if [ ! -f "lib/commons-logging-1.2.jar" ]; then
        print_status "Downloading Commons Logging..."
        curl -L -o "lib/commons-logging-1.2.jar" \
            "https://repo1.maven.org/maven2/commons-logging/commons-logging/1.2/commons-logging-1.2.jar"
        print_success "Commons Logging downloaded"
    else
        print_success "Commons Logging already exists"
    fi
    
    # Tess4J dependencies
    if [ ! -f "lib/tess4j-${TESS4J_VERSION}.jar" ]; then
        print_status "Downloading Tess4J ${TESS4J_VERSION}..."
        curl -L -o "lib/tess4j-${TESS4J_VERSION}.jar" \
            "https://repo1.maven.org/maven2/net/sourceforge/tess4j/tess4j/${TESS4J_VERSION}/tess4j-${TESS4J_VERSION}.jar"
        print_success "Tess4J downloaded"
    else
        print_success "Tess4J already exists"
    fi
    
    # Additional Tess4J dependencies
    if [ ! -f "lib/lept4j-1.0.1.jar" ]; then
        print_status "Downloading Lept4J..."
        curl -L -o "lib/lept4j-1.0.1.jar" \
            "https://repo1.maven.org/maven2/net/sourceforge/lept4j/lept4j/1.0.1/lept4j-1.0.1.jar"
        print_success "Lept4J downloaded"
    else
        print_success "Lept4J already exists"
    fi
    
    if [ ! -f "lib/jna-5.13.0.jar" ]; then
        print_status "Downloading JNA..."
        curl -L -o "lib/jna-5.13.0.jar" \
            "https://repo1.maven.org/maven2/net/java/dev/jna/jna/5.13.0/jna-5.13.0.jar"
        print_success "JNA downloaded"
    else
        print_success "JNA already exists"
    fi
    
    if [ ! -f "lib/jna-platform-5.13.0.jar" ]; then
        print_status "Downloading JNA Platform..."
        curl -L -o "lib/jna-platform-5.13.0.jar" \
            "https://repo1.maven.org/maven2/net/java/dev/jna/jna-platform/5.13.0/jna-platform-5.13.0.jar"
        print_success "JNA Platform downloaded"
    else
        print_success "JNA Platform already exists"
    fi
    
    # SLF4J dependencies for Tess4J
    if [ ! -f "lib/slf4j-api-1.7.36.jar" ]; then
        print_status "Downloading SLF4J API..."
        curl -L -o "lib/slf4j-api-1.7.36.jar" \
            "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar"
        print_success "SLF4J API downloaded"
    else
        print_success "SLF4J API already exists"
    fi
    
    if [ ! -f "lib/slf4j-simple-1.7.36.jar" ]; then
        print_status "Downloading SLF4J Simple..."
        curl -L -o "lib/slf4j-simple-1.7.36.jar" \
            "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.36/slf4j-simple-1.7.36.jar"
        print_success "SLF4J Simple downloaded"
    else
        print_success "SLF4J Simple already exists"
    fi
    
    # Note: JAI ImageIO dependencies are complex and may cause issues
    # The application will work without OCR for image-based PDFs
    # Users can install Tesseract system-wide for full OCR support
}

# Download tessdata files
download_tessdata() {
    print_status "Downloading Tesseract language data..."
    
    if [ ! -f "tessdata/eng.traineddata" ]; then
        print_status "Downloading English language data..."
        curl -L -o "tessdata/eng.traineddata" \
            "https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata"
        print_success "English language data downloaded"
    else
        print_success "English language data already exists"
    fi
    
    if [ ! -f "tessdata/osd.traineddata" ]; then
        print_status "Downloading orientation and script detection data..."
        curl -L -o "tessdata/osd.traineddata" \
            "https://github.com/tesseract-ocr/tessdata/raw/main/osd.traineddata"
        print_success "OSD data downloaded"
    else
        print_success "OSD data already exists"
    fi
}

# Compile the application
compile_application() {
    print_status "Compiling PDF Text Extractor..."
    
    # Create classpath
    CLASSPATH="lib/*:."
    
    # Compile GUI version
    javac -cp "$CLASSPATH" PDFTextExtractor.java
    
    if [ $? -eq 0 ]; then
        print_success "GUI version compilation successful"
    else
        print_error "GUI version compilation failed"
        exit 1
    fi
    
    # Compile CLI version
    print_status "Compiling CLI version..."
    javac -cp "$CLASSPATH" PDFTextExtractorCLI.java
    
    if [ $? -eq 0 ]; then
        print_success "CLI version compilation successful"
    else
        print_error "CLI version compilation failed"
        exit 1
    fi
}

# Run the application
run_application() {
    print_status "Choose which version to run:"
    echo "1) GUI Version (recommended for interactive use)"
    echo "2) CLI Version (for command-line usage)"
    echo "3) Skip running"
    echo ""
    read -p "Enter your choice (1-3): " -n 1 -r
    echo ""
    
    if [[ $REPLY =~ ^[1]$ ]]; then
        print_status "Starting PDF Text Extractor GUI..."
        print_warning "Note: If you encounter OCR errors, make sure Tesseract is installed:"
        print_warning "  macOS: brew install tesseract"
        print_warning "  Ubuntu: sudo apt-get install tesseract-ocr"
        print_warning "  Windows: Download from https://github.com/UB-Mannheim/tesseract/wiki"
        echo ""
        
        # Create classpath
        CLASSPATH="lib/*:."
        
        # Run GUI version
        java -cp "$CLASSPATH" PDFTextExtractor
    elif [[ $REPLY =~ ^[2]$ ]]; then
        print_status "Starting PDF Text Extractor CLI..."
        print_warning "Note: If you encounter OCR errors, make sure Tesseract is installed"
        echo ""
        echo "Usage: java -cp 'lib/*:.' PDFTextExtractorCLI <pdf_file> [output_file]"
        echo "Example: java -cp 'lib/*:.' PDFTextExtractorCLI document.pdf output.txt"
        echo ""
        
        # Create classpath
        CLASSPATH="lib/*:."
        
        # Run CLI version with help
        java -cp "$CLASSPATH" PDFTextExtractorCLI
    else
        print_status "Skipping application launch"
    fi
}

# Main execution
main() {
    echo "Starting build process..."
    echo ""
    
    check_java
    check_javac
    create_directories
    download_dependencies
    download_tessdata
    compile_application
    
    echo ""
    print_success "Build completed successfully!"
    echo ""
    
    # Ask user if they want to run the application
    read -p "Do you want to run the application now? (y/n): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        run_application
    else
        print_status "To run the applications later, use:"
        print_status "GUI Version: java -cp 'lib/*:.' PDFTextExtractor"
        print_status "CLI Version: java -cp 'lib/*:.' PDFTextExtractorCLI <pdf_file> [output_file]"
        print_status "Or use: ./run_cli.sh <pdf_file> [output_file]"
    fi
}

# Run main function
main "$@"
