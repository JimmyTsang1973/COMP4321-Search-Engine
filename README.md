# COMP4321 Group 43 Project - Search Engine

This repository contains the Java implementation of a search engine for the COMP4321 course project. It includes web crawling, indexing, and a user interface for query processing and result display.

## Installation Procedure

### Prerequisites
- IntelliJ IDEA
- Java SDK 17

### Setup Instructions
1. **Clone the Repository**  
   Clone this repository to your local machine using IntelliJ IDEA by navigating to `OUR_PROJECT/Idea/Code`.

2. **Configure Java SDK**  
   Set up your IDE to use Java 17 SDK.

3. **Install TomCat Plugin**  
   If not already installed, download and install the Smart TomCat plugin in IntelliJ IDEA.

4. **TomCat Configuration**  
   The TomCat server should be pre-configured. If not, follow the configuration instructions provided in the setup screen or refer to the demo video.

5. **Run the Project**  
   Launch the project by pressing the 'Run' button on the TomCat configuration in IntelliJ. The console will display the build process. Once it shows a localhost URL, your search engine is ready to use.

## Using the Search Engine
- **Access**: Navigate to `http://localhost:8080/SearchEngine`.
- **Basic Search**: Enter your query in the search bar and press 'enter' to retrieve results.
- **Browse by Keywords**: Click on "Browse and Search by Keywords" to see a web page filled with stemmed keywords. Select any keyword to perform a search.
- **I'm Feeling Lucky**: Enter your query and click "I am feeling lucky" to be redirected to the top-ranked web page.

## Bonus Features
- **Keyword Search**: Select desired keywords, which can then be submitted as a vector-space query to the search engine.
- **Get Similar Pages**: For each result, use the "Get similar pages" button to find web pages with similar content based on top keywords.

## Documentation
Detailed information about the system architecture, including the Spider (Crawler), Indexer, and Search Engine components, is available in the project report. The report discusses the BFS algorithm for web crawling, content extraction, indexing mechanisms, and query processing.
