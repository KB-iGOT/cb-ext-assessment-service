# cb-ext-assessment-service

## Overview
The `cb-ext-assessment-service` is a backend microservice built with **Spring Boot** designed to handle assessment-related functionalities within the iGOT Karmayogi platform. It manages the lifecycle of user assessments, including reading assessment hierarchies, handling submissions, calculating results, and integrating with the broader ecosystem for progress tracking and certification.

## Technology Stack
-   **Framework**: Spring Boot 3.4.4
-   **Language**: Java 17
-   **Build Tool**: Maven
-   **Database**: Apache Cassandra (Primary Store)
-   **Caching**: Redis (Jedis client)
-   **Search**: ElasticSearch (Rest High Level Client)
-   **Messaging**: Apache Kafka
-   **Authentication**: Keycloak (SSO)

## Core Components

### 1. API Layer (`com.igot.cb.assessment.controller`)
-   **`AssessmentController`**: The main entry point for REST APIs. It supports multiple API versions (v2, v3, v4, v5, v6) to handle the evolution of assessment logic.
-   **Endpoints**:
    -   `GET /read/{assessmentId}`: Fetches assessment hierarchy and metadata.
    -   `POST /submit`: Handles assessment submission.
    -   `GET /retake/{assessmentId}`: Checks retake eligibility and resets attempts.
    -   `POST /question/list`: Fetches questions for an assessment.

### 2. Service Layer (`com.igot.cb.assessment.service`)
-   **`AssessmentService` (Interfaces: V2, V4, V5)**: Encapsulates the business logic.
    -   **`AssessmentServiceV5Impl`**: The latest and most comprehensive implementation. It handles:
        -   **Assessment Logic**: Validating submissions, calculating scores (section-level vs. assessment-level), and managing timers.
        -   **Async Processing**: Submitting assessments asynchronously to improve performance.
        -   **Retake Logic**: Managing max attempts and cool-off periods.
-   **`AssessmentUtilService`**: Shared utility logic for fetching content, parsing hierarchies, and validation.

### 3. Data & Repository Layer (`com.igot.cb.assessment.repo`)
-   **`AssessmentRepository`**: Interacts with **Cassandra** to store and retrieve:
    -   User assessment states (start time, end time, status).
    -   Submitted answers and results.
-   **Redis-based Store**: Used heavily for caching assessment hierarchies and question lists to reduce database load and latency.

### 4. External Integrations
The service acts as an orchestrator, communicating with several other microservices:

| Service | Purpose |
| :--- | :--- |
| **Content Service** | Fetches content hierarchy and metadata (Courses, Resources). |
| **Learner Service (LMS)** | Manages user data, organization details, and progress updates. |
| **Search Service** | Used for searching content and frameworks. |
| **Notification Service** | Sends email/SMS notifications (e.g., assessment completion). |
| **PDF Generator Service** | Generates certificates upon successful assessment completion. |
| **FRAC Service** | Integrates with the Competency framework. |
| **Keycloak** | Handles user authentication and token validation. |

### 5. Async Processing (Kafka)
The service uses **Kafka** for asynchronous task processing:
-   **Producers**: Publish events for:
    -   Assessment submission (`dev.cb.ext.assessment.service.submit`).
    -   Content progress updates.
    -   Telemetry generation.
-   **Consumers**: likely consume events for background processing tasks (though primary focus in code analysis was on production).

## Data Flow (High Level)

1.  **Read Assessment**:
    -   User requests an assessment.
    -   Service checks **Redis** for cached hierarchy. If missing, fetches from **Content/Assessment Service** and caches it.
    -   Service checks **Cassandra** for existing user attempts/state.
    -   Returns the question set to the user.

2.  **Submit Assessment**:
    -   User submits answers.
    -   Service validates the payload against the cached hierarchy (timers, question validity).
    -   Calculates the score (Pass/Fail).
    -   Persists the result in **Cassandra**.
    -   Updates User Progress in **LMS/Learner Service**.
    -   Triggers a **Kafka** event for async post-processing (e.g., certificate generation).

## Configuration
-   Managed via `application.properties`.
-   Supports profile-based configuration (e.g., dev, prod).
-   Contains massive configuration for external service URLs, Kafka topics, and ElasticSearch indices.



