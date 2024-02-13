Project Overview:

This project is a simple HTTP server implementation in Java. It includes classes to handle configuration, incoming HTTP requests, and generating HTTP responses.

Project Structure:
- Sources: Contains the source code for the HTTP server.
  - Config.java: Manages server configuration settings such as port number, root directory, and maximum threads.
  - HTTPRequest.java: Parses incoming HTTP requests, extracts relevant information, and handles request parameters.
  - HTTPResponse.java: Generates appropriate HTTP responses based on the request type and content.
  - WebServer.java: Main entry point for the HTTP server, listens for incoming connections and delegates request handling.

- Tests: Contains test cases for the source code classes.
  - ConfigTest.java: Tests for the Config class to ensure correct parsing of configuration settings.
  - HTTPRequestTest.java: Tests for the HTTPRequest class to handle various HTTP request scenarios.
  - HTTPResponseTest.java: Tests for the HTTPResponse class to generate proper HTTP responses.

- config.ini: Configuration file for the server, specifying settings such as port number, root directory, etc.
- compile.sh: Shell script to compile the Java source files.
- run.sh: Shell script to run the compiled server.
- server root directory: Root directory for serving web content.
- bonus.txt: Document outlining any additional features or bonuses implemented, if applicable.

Design Choices:

The server is designed to be a simple and lightweight implementation capable of handling basic HTTP requests and serving static content.
Each class has a specific responsibility to maintain separation of concerns and facilitate easier testing and maintenance.

- Config class:
    Manages server configuration settings, allowing easy adjustment of parameters without modifying the source code.
    The class reads settings from a configuration file (config.ini) and provides methods to retrieve these settings.
- HTTPRequest class:
    Parses incoming HTTP requests, extracts relevant information such as request type, requested page, parameters, headers, etc.
    It also handles edge cases such as malformed or invalid requests.
- HTTPResponse class:
    Generates appropriate HTTP responses based on the request type, requested page, and content.
    It handles various response scenarios including success, not found, internal server error, and bad request.
    The class also supports chunked encoding for large content.
- WebServer class:
    Serves as the main entry point for the HTTP server.
    It listens for incoming connections on a specified port, accepts client requests, and delegates request handling to appropriate classes.
    The server uses a fixed thread pool to handle multiple client connections concurrently.

Overall, the design prioritizes simplicity, modularity, and extensibility to facilitate future enhancements and modifications to the server functionality.
