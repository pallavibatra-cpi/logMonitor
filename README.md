# Log Monitoring Service

## Introduction

The Log Monitoring Service provides a REST API to retrieve log entries from UNIX-based servers without directly logging into the machine. The logs are fetched from the /var/log directory, and the service supports filtering by filename, number of lines, and keywords.

#Prerequisites
- mvn 
- java 8

# Design
## Structure

```
log-monitoring
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── cribl
│   │   │           └── logMonitor
│   │   │               ├── LogMonitorApplication.java
│   │   │               ├── controller
│   │   │               │   └── LogController.java
│   │   │               ├── service
│   │   │               │   └── LogService.java
│   │   │               └── model
│   │   │                   └── LogResponse.java
│   │   └── resources
│   │       └── application.properties
│   └── test
│       └── java
│           └── com
│               └── cribl
│                   └── logMonitor
│                       └── LogMonitorApplicationTests
│                           
└── README.md
```
# Explanation
1. Controller:
   - Uses ResponseBodyEmitter to stream log entries as soon as the service received it. The idea is to use HTTP Chunked Transfer encoding.
   - URL-decodes the filename to ensure it's correctly formatted.
   - Handles decoding errors and completes the emitter properly.

2. Service:
   - Validates the filename to ensure it contains only safe characters.
   - The streamLogEntries method reads the log file and sends each line through the ResponseBodyEmitter. The idea is to read file in chunks instead of reading it whole.
   - Filters the lines using the keyword provided and sends matching lines to the client.
3. Model:
   - Model class to define the response object.
   
## Setup

1. Clone the repository.
```bash
git clone git@github.com:pallavibatra-cpi/logMonitor.git
```
2. Go to logMonitor directory.
```
cd logMonitor
```
3. Build the project using Maven:
 ```bash
 mvn clean install
 ```
4. Run the Spring Boot application:
```bash
java -jar target/logMonitor-0.0.1-SNAPSHOT.jar
```

## Usage

### API Endpoint

`GET /v1/logs`

### Query Parameters

- `filename` (required): The name of the log file in `/var/log`. Should be a valid filename 
- `lines` (optional): The number of lines to retrieve from the end of the log file.
- `keyword` (optional): A keyword to filter the log entries.

### Example Request

```bash
With ALL Parameters
curl "http://localhost:8080/v1/logs?filename=system.log&lines=20&keyword=error"

Only FileName
curl "http://localhost:8080/v1/logs?filename=system.log"
