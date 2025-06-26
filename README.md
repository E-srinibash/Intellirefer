# IntelliRefer - AI-Powered Talent Referral System (Project Setup)

This document provides a complete guide for setting up the IntelliRefer project on a local development machine.

## Table of Contents

1.  [Features](#features)
2.  [Technology Stack](#technology-stack)
3.  [Prerequisites](#prerequisites)
4.  [Backend Setup](#backend-setup)
5.  [Frontend Setup](#frontend-setup)
6.  [Initial User Creation](#initial-user-creation)

---

### Features

*   **Role-Based Access:** Separate dashboards and functionalities for Employees and Managers.
*   **Profile Management:** Employees can manage their profiles, availability, and skills.
*   **Document Handling:** Upload and parsing of resumes (for employees) and job descriptions (for managers).
*   **AI-Powered Matching:**
    *   Automated skill extraction from resumes.
    *   Experience pre-filtering to reduce unnecessary matching.
    *   LLM-based scoring of resumes against job descriptions.
    *   Identification of top matching skills for quick review.
*   **Resource Management:** Managers can select, reserve, or reject candidates, which automatically updates the employee's availability status.
*   **Lifecycle Management:** Job descriptions can be closed manually or automatically when a candidate is selected.

---

### Technology Stack

**Backend:**
*   **Language/Framework:** Java 17, Spring Boot 3.x
*   **Database:** PostgreSQL (or MySQL with minor configuration changes)
*   **ORM:** Spring Data JPA / Hibernate
*   **Security:** Spring Security with JWT (using `com.auth0:java-jwt`)
*   **AI Integration:** Google Gemini Pro (via REST API)
*   **File Storage:** Local Filesystem
*   **Build Tool:** Maven

**Frontend:**
*   **Framework:** React
*   **Build Tool:** Vite
*   **Styling:** Tailwind CSS
*   **API Client:** Axios
*   **Routing:** React Router DOM
*   **State Management:** Zustand

---

### Prerequisites

Ensure you have the following software installed on your machine:
*   **Java JDK 17** or later.
*   **Apache Maven** 3.6 or later.
*   **Node.js** v18 or later (which includes `npm`).
*   **PostgreSQL** (or MySQL) database server.
*   **Git** for cloning the repository.
*   An **IDE** like IntelliJ IDEA or VS Code.

---

### Backend Setup

1.  **Clone the Repository:**
    ```bash
    git clone <your-repository-url>
    cd <your-repository-url> # Navigate to the project root
    ```

2.  **Database Configuration:**
    *   Open your PostgreSQL/MySQL client.
    *   Create a new database named `intellirefer_db`.
        ```sql
        CREATE DATABASE intellirefer_db;
        ```
    *   Create a user and grant privileges (if necessary).

3.  **Configure `application.properties`:**
    *   Navigate to the backend project root (the directory with `pom.xml`).
    *   Open `src/main/resources/application.properties`.
    *   Update the following properties with your local configuration:

    ```properties
    # --- UPDATE THESE VALUES ---

    # Database Credentials
    spring.datasource.username=your_db_user
    spring.datasource.password=your_db_password

    # Local Storage Path (use a path outside the project folder)
    # For Windows: C:/Users/YourUsername/intellirefer-uploads
    # For Linux/macOS: /home/yourusername/intellirefer-uploads
    storage.location=./intellirefer-uploads

    # Google Gemini API Key
    llm.google.api.key=YOUR_GOOGLE_AI_API_KEY_HERE

    # Default Manager Credentials (for initial setup)
    admin.default.email=manager@intellirefer.com
    admin.default.password=SecureManagerPassword123!
    ```

4.  **Build and Run the Backend:**
    *   Open a terminal in the backend project root.
    *   Build the project using Maven:
        ```bash
        ./mvnw clean install
        ```
    *   Run the application:
        ```bash
        java -jar target/intellirefer-0.0.1-SNAPSHOT.jar
        ```
    *   The backend server should now be running on `http://localhost:8080`.

---

### Frontend Setup

1.  **Navigate to the Frontend Directory:**
    *   Open a new terminal.
    *   From the project root, navigate to the frontend folder:
        ```bash
        cd intellirefer-frontend
        ```

2.  **Install Dependencies:**
    ```bash
    npm install
    ```

3.  **Configure the API Base URL (if needed):**
    *   The frontend is configured to talk to the backend at `http://localhost:8080`.
    *   If your backend is running on a different port, update the `baseURL` in `src/api/apiService.js`.

4.  **Run the Frontend Development Server:**
    ```bash
    npm run dev
    ```
    *   The frontend application should now be accessible at `http://localhost:5173` (or another port specified by Vite).

---

### Initial User Creation

**Manager:**
*   The first `MANAGER` user is created **automatically** when the backend starts up for the first time.
*   The credentials are those you defined in `application.properties`:
    *   **Email:** `manager@intellirefer.com`
    *   **Password:** `SecureManagerPassword123!`

**Employee:**
*   Navigate to the application in your browser (`http://localhost:5173`).
*   Click the "Don't have an account? Register" link.
*   Fill out the registration form to create your first `EMPLOYEE` user.

You are now ready to use the application!