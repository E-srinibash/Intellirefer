# IntelliRefer - Application Workflow

This document explains the user flows and core business logic of the IntelliRefer application.

## Core Concept

IntelliRefer is a dual-persona application designed to bridge the gap between manager needs and employee availability. It automates the initial stages of internal recruitment and resource allocation.

*   **Employee Persona:** Focused on maintaining an up-to-date profile and resume to be discoverable for new projects.
*   **Manager Persona:** Focused on finding the best internal candidates for client requirements quickly and efficiently.

---

### The Employee Workflow

The employee's journey is about self-service and discoverability.

**1. Registration & Login:**
*   A new user registers through the public form, creating an account with the `EMPLOYEE` role.
*   The system creates a `User` record for authentication and an `EmployeeProfile` record for personal details.
*   The employee then logs in to receive a secure JWT for session management.

**2. The Dashboard Experience:**
*   The employee is directed to a personal dashboard (`/employee/dashboard`).
*   This page displays their current profile information: name, role, job level, availability status, and a list of their skills.

**3. Key Action: Uploading a Resume**
*   The employee uploads their resume (PDF or DOCX). This is the most critical action.
*   **Behind the Scenes:**
    1.  The backend saves the file to the configured local storage directory.
    2.  It then triggers an **asynchronous background task** (`EmployeeService.extractAndSaveSkills`).
    3.  This task parses the resume text and sends it to the Google Gemini API with a prompt asking for a list of skills.
    4.  When the LLM responds with a list like `["Java", "SQL", "Agile"]`, the backend processes it.
    5.  For each skill, it checks the master `skills` table. If a skill doesn't exist, it's added.
    6.  Finally, it populates the `employee_skills` join table, linking the employee to all the extracted skills.
*   The employee can refresh their dashboard after a few moments to see their skills list automatically populated.

**4. Key Action: Editing the Profile**
*   The employee can click "Edit" to open a modal form.
*   Here they can update their name, job level, current role, and, most importantly, their **Availability Status**.
*   If they set their status to `ON_PROJECT`, they are prompted to provide an **Expected Availability Date**. This is crucial for the matching logic.

---

### The Manager Workflow

The manager's journey is about efficiency and decision-making.

**1. Login & Manager Creation:**
*   The first manager account is created automatically on backend startup via the `AdminUserInitializer`.
*   The manager logs in and is directed to the manager's dashboard (`/manager/dashboard`).

**2. The Dashboard Experience:**
*   The manager sees a list of all Job Descriptions (JDs) they have previously uploaded.
*   Each JD card displays its title, client, and current status (`OPEN` or `CLOSED`).
*   A prominent "Upload New JD" button is available.

**3. Key Action: Uploading a Job Description**
*   The manager fills out a modal form with the JD's title, client, and the document file.
*   **Behind the Scenes:**
    1.  The backend saves the JD file to local storage.
    2.  It creates a `JobDescription` record in the database.
    3.  It then triggers the main asynchronous matching process (`MatchingService.processJdMatching`).

**4. The Automated Matching Process (The "Magic"):**
*   The background task starts for the new JD.
*   **Step A (Pre-filtering):** The service first sends the JD text to the LLM to extract the `required_experience` (e.g., 5 years) and saves it to the `job_descriptions` table.
*   **Step B (Candidate Search):** It queries the database for all employees who are `AVAILABLE` OR who are `ON_PROJECT` with an `expected_availability_date` within the next 90 days.
*   **Step C (Loop & Match):** It iterates through this list of potential candidates.
    *   It first checks if `employee.yearsOfExperience >= jd.requiredExperience`. If not, it skips the employee.
    *   If the experience matches, it then sends the full JD text and the employee's resume text to the LLM for a detailed analysis.
    *   The LLM returns a JSON object containing the `matchScore`, `justification`, and a list of the top 5-6 `matching_skills`.
    *   The system saves this analysis as a new record in the `referrals` table, linking the employee to the JD.

**5. Key Action: Reviewing Recommendations**
*   The manager clicks "View Recommendations" on a JD card.
*   This opens the `JdDetailsPage`, which fetches and displays all `Referral` records for that JD, sorted by the highest score first.
*   Each candidate card clearly shows:
    *   The employee's name, role, level, and experience.
    *   Their current availability status and expected available date (if applicable).
    *   The AI-generated match score and justification.
    *   The **top matching skills**, giving the manager immediate context.

**6. Key Action: Making a Decision**
*   The manager uses the "Select", "Reserve", or "Reject" buttons.
*   Clicking **"Select"**:
    *   Changes the employee's `availability` status to `ON_PROJECT`.
*   Clicking **"Reserve"**:
    *   Changes the employee's `availability` to `RESERVED`, taking them out of the pool for other JDs but not yet assigning them.

**7. Key Action: Tracking Selected Candidates**
*   The manager can click the "Selected Candidates" link in the navbar.
*   This page (`/manager/selected`) shows a summary table of all employees who are currently `ON_PROJECT` or `RESERVED`, and which project/client they are assigned to.