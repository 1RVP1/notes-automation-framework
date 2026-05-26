# Notes Application — Automation Framework
### Automated Independent Validation System

---

## 🧰 Tech Stack

| Tool             | Purpose                        |
|------------------|-------------------------------|
| Java 17          | Programming Language           |
| Selenium 4       | UI Automation                  |
| TestNG 7         | Test Execution & Parallel Run  |
| RestAssured 5    | API Automation                 |
| Allure Reports   | Reporting & Evidence           |
| Log4j2           | Logging                        |
| Maven            | Build & Dependency Management  |
| WebDriverManager | Auto ChromeDriver setup        |

---

## 📁 Project Structure

```
notes-automation/
├── pom.xml
├── src/
│   ├── main/java/com/notes/
│   │   ├── config/
│   │   │   └── ConfigReader.java          # Reads config.properties
│   │   ├── pages/
│   │   │   ├── LoginPage.java             # POM - Login
│   │   │   ├── DashboardPage.java         # POM - Dashboard
│   │   │   └── CreateNotePage.java        # POM - Create Note
│   │   └── utils/
│   │       ├── DriverManager.java         # WebDriver lifecycle
│   │       ├── BaseTest.java              # Setup/Teardown + Screenshots
│   │       └── ApiHelper.java             # RestAssured API methods
│   └── test/
│       ├── java/com/notes/
│       │   ├── ui/
│       │   │   ├── LoginTest.java         # TC-01, TC-02, TC-03
│       │   │   ├── NoteTest.java          # TC-04 to TC-08, TC-14, TC-15
│       │   │   └── LogoutTest.java        # TC-13
│       │   ├── api/
│       │   │   └── NotesApiTest.java      # TC-09, TC-12
│       │   └── hybrid/
│       │       └── HybridValidationTest   # TC-10, TC-11
│       └── resources/
│           ├── config.properties          # URLs, credentials, timeouts
│           ├── testng.xml                 # Suite + parallel config
│           └── log4j2.xml                 # Logging config
└── logs/
    └── automation.log
```

---

## ⚙️ Prerequisites

1. **Java JDK 17** — [Download](https://adoptium.net/)
2. **Maven 3.8+** — [Download](https://maven.apache.org/download.cgi)
3. **Google Chrome** (latest)
4. **IntelliJ IDEA** (recommended)

Verify installation:
```bash
java -version
mvn -version
```

---

## 🚀 Setup & Run

### Step 1 — Clone / Open Project
Open the `notes-automation` folder in IntelliJ IDEA.

### Step 2 — Update config.properties
Edit `src/test/resources/config.properties`:
```properties
base.url=https://practice.expandtesting.com/notes/app
api.base.url=https://practice.expandtesting.com/notes/api
test.email=your_registered_email@example.com
test.password=YourPassword@123
```

### Step 3 — Run All Tests
```bash
mvn clean test
```

### Step 4 — Generate Allure Report
```bash
mvn allure:serve
```
This opens the Allure report automatically in your browser.

---

## ▶️ Run Specific Test Groups

```bash
# Run only UI tests
mvn test -Dgroups=ui

# Run only API tests
mvn test -Dgroups=api

# Run headless (no browser window)
mvn test -Dheadless=true
```

---

## 🧪 Test Coverage

| Test Case | Scenario | Type    | Class                    |
|-----------|----------|---------|--------------------------|
| TC-01     | TS-01    | UI      | LoginTest                |
| TC-02     | TS-02    | UI      | LoginTest                |
| TC-03     | TS-03    | UI      | LoginTest                |
| TC-04     | TS-04    | UI      | NoteTest                 |
| TC-05     | TS-05    | UI      | NoteTest                 |
| TC-06     | TS-08    | UI      | NoteTest                 |
| TC-07     | TS-07    | UI      | NoteTest                 |
| TC-08     | TS-06    | UI      | NoteTest                 |
| TC-09     | TS-09    | API     | NotesApiTest             |
| TC-10     | TS-10    | Hybrid  | HybridValidationTest     |
| TC-11     | TS-11    | Hybrid  | HybridValidationTest     |
| TC-12     | TS-12    | API     | NotesApiTest             |
| TC-13     | TS-13    | UI      | LogoutTest               |
| TC-14     | TS-14    | UI      | NoteTest                 |
| TC-15     | TS-15    | UI      | NoteTest                 |

---

## 📊 Allure Report Features

- ✅ Test results with Pass/Fail status
- 📸 Screenshots on failure (auto-attached)
- 📋 API request/response attached to each API test
- 🧵 Parallel execution results per thread
- 📁 Organized by Epic → Feature → Story

---

## 📝 Logs

Logs are written to:
- **Console** — during test run
- **`logs/automation.log`** — persistent log file

---

## ❗ Troubleshooting

| Issue | Fix |
|---|---|
| ChromeDriver version mismatch | WebDriverManager handles this automatically |
| Auth token null | Verify email/password in config.properties |
| Element not found | Increase `explicit.wait` in config.properties |
| Port conflict for Allure | Run `mvn allure:serve -Dallure.serve.port=9999` |
