# YouREST

A Java-based REST API testing framework that drives test execution from JSON contract files, supporting GET/POST/PUT/DELETE operations, header/payload validation, XML schema validation, and Zephyr integration for test result reporting.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [How It Works](#how-it-works)
- [Contract File Format](#contract-file-format)
- [Configuration](#configuration)
- [Running Tests](#running-tests)

---

## Overview

YouREST is a data-driven REST API test runner. Test cases are defined in JSON contract files that specify the API endpoint, HTTP method, request headers, payload, and expected response (status code, headers, and body). The framework reads the contracts, executes each HTTP call, and validates the response.

It supports:
- JSON contract-based test definitions
- Excel-based test data (via `Book1.xlsx`)
- Variable substitution via JSON variable files
- XML response schema validation (XSD)
- TestNG and JUnit execution
- Zephyr (JIRA) test result reporting

---

## Tech Stack

| Tool | Purpose |
|------|---------|
| Java | Core language |
| Maven | Build and dependency management |
| TestNG | Test execution framework |
| JUnit | Alternate test runner |
| Apache HttpClient | HTTP request execution |
| Jackson / JSON | Contract parsing |
| XSD Validation | XML response validation |
| Zephyr API | Test result reporting to JIRA |

---

## Project Structure

```
YouREST/
├── src/main/java/com/dwiveddi/restapi/
│   ├── RestApiRunner.java                  # Main test runner entry point
│   ├── RestApiExecutor.java                # Executes HTTP calls and validates responses
│   ├── RestApiListener.java                # TestNG listener for result reporting
│   ├── Listener.java                       # Base listener interface
│   ├── ZephyrHelper.java                   # Posts results to Zephyr/JIRA
│   ├── config/
│   │   └── RunnerInput.java                # Parses runner input configuration
│   ├── dto/
│   │   ├── Api.java                        # Top-level API contract object
│   │   ├── Request.java                    # Request definition (method, headers, payload)
│   │   ├── Response.java                   # Expected response definition
│   │   ├── PayloadStructure.java           # Request/response payload schema
│   │   └── RequestResponseCombination.java # Individual test case (request + expected response)
│   └── utils/
│       ├── HttpClientUtils.java            # HTTP client helper methods
│       └── XmlValidator.java               # XSD-based XML response validation
├── conf/
│   ├── rest-api-contracts.json             # API test contracts definition
│   ├── variables.json                      # Global variable substitutions
│   ├── book1Variables.json                 # Test suite variable set
│   ├── book1SuiteVariables.json            # Suite-level variables
│   ├── Book1.xlsx                          # Excel-based test data
│   ├── response.xsd                        # XSD schema for response validation
│   ├── testng.xml                          # TestNG suite configuration
│   └── zephyrEnv.properties                # Zephyr environment settings
├── scripts/
│   ├── testng.sh                           # Shell script to run via TestNG
│   └── junit.sh                            # Shell script to run via JUnit
└── pom.xml
```

---

## How It Works

1. **Define contracts** — Write test cases in `conf/rest-api-contracts.json`
2. **Configure variables** — Set reusable values in `variables.json`
3. **Run tests** — Execute via TestNG/JUnit; `RestApiRunner` reads contracts and drives `RestApiExecutor`
4. **Validate responses** — Status codes, headers, payloads, and optionally XML schema
5. **Report results** — `ZephyrHelper` posts pass/fail results to Zephyr in JIRA

---

## Contract File Format

Each entry in `rest-api-contracts.json` defines a full API test contract:

```json
[
  {
    "defaultUrl": "http://localhost:8081/tests/100",
    "defaultMethod": "GET",
    "defaultRequest": {
      "queryParams": "",
      "headers": { "a": "a1", "b": "b1" },
      "payload": null
    },
    "defaultResponse": {
      "statusCode": 200,
      "payload": "hello world",
      "headers": { "a": "a1", "b": "b1" }
    },
    "requestResponseCombinations": [
      {
        "id": "T1",
        "request": {
          "queryParams": "",
          "headers": { "a": "a1", "b": "b1" },
          "payload": null
        },
        "response": {
          "statusCode": 200,
          "payload": "hello world",
          "headers": { "a": "a1", "b": "b1" }
        }
      }
    ]
  }
]
```

Each `requestResponseCombination` is an individual test case (`T1`, `T2`, etc.) with its own request overrides and expected response.

---

## Configuration

| File | Purpose |
|------|---------|
| `conf/rest-api-contracts.json` | API test contracts |
| `conf/variables.json` | Global variables for substitution |
| `conf/response.xsd` | XSD schema for XML response validation |
| `conf/testng.xml` | TestNG suite and test class configuration |
| `conf/zephyrEnv.properties` | Zephyr server URL, credentials, project key |

---

## Running Tests

**Via TestNG:**
```bash
bash scripts/testng.sh
```

**Via JUnit:**
```bash
bash scripts/junit.sh
```

**Via Maven:**
```bash
mvn test
```
