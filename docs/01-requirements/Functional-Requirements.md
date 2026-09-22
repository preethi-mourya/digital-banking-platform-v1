# Digital Banking & Account Management Platform V1 — Functional Requirements

## 1. Overview

The Digital Banking & Account Management Platform V1 is a secure full-stack banking application that enables customers to perform routine banking activities online.

The platform also provides controlled operations for bank employees and administrators through role- and permission-based access.

V1 is implemented as a modular monolith using Java, Spring Boot, REST APIs, MySQL, and a web frontend.

---

## 2. Business Problem

Customers need secure self-service access to routine banking services such as profile management, account viewing, beneficiary management, fund transfers, transaction history, and service requests.

The platform must protect customer and financial information while ensuring authorized access, consistent financial processing, and traceability of important activities.

---

## 3. Objectives

- Provide secure digital banking services.
- Allow customers to manage permitted personal information.
- Provide secure account and transaction visibility.
- Support beneficiary management and fund transfers.
- Support service requests and tracking.
- Provide authorized employee operations.
- Provide administrative capabilities.
- Enforce role, permission, and resource-level authorization.
- Prevent duplicate and inconsistent financial transactions.
- Maintain audit records and notifications.
- Provide a maintainable and testable enterprise application.

---

## 4. User Roles

### Customer

Customers can:

- Manage permitted profile information.
- View their linked accounts, balances, and status.
- Manage their beneficiaries.
- Perform authorized fund transfers.
- View, search, filter, and paginate transactions.
- Create and track service requests.
- Manage passwords and authentication.
- View notifications.

Customers can access only their own authorized resources.

### Bank Employee

Employees can:

- Securely access the employee portal.
- Search and view authorized customer information.
- View permitted account information.
- Review and process authorized service requests.
- Perform permitted customer-service operations.

Employees cannot perform unauthorized administrative or financial operations.

### Administrator

Administrators can:

- Manage users.
- Manage roles and permissions.
- Manage authorized system configuration.
- Review audit information.
- Perform authorized administrative operations.

---

## 5. Functional Requirements

| ID    | Requirement            | Description                                                                                                                                                                |
|-------|------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| FR-01 | Customer Profile       | Customers can securely view and update permitted information such as name, email, phone, address, and communication preferences.                                           |
| FR-02 | Authentication         | The system provides secure login, logout, password change/reset, JWT-based API authentication, failed-login protection, and token expiration.                              |
| FR-03 | Account Management     | Customers can view authorized accounts, masked account numbers, account type, currency, balance, and account status.                                                       |
| FR-04 | Beneficiary Management | Customers can add, view, update, and deactivate/remove their beneficiaries after required validation.                                                                      |
| FR-05 | Fund Transfer          | Customers can transfer funds to active registered beneficiaries after authentication, ownership, account-status, balance, amount, limit, and duplicate-request validation. |
| FR-06 | Transaction Management | Customers can view, search, filter, sort, and paginate transaction history, including status and transaction reference.                                                    |
| FR-07 | Service Requests       | Customers can create, view, track, and cancel eligible service requests; authorized employees can process them.                                                            |
| FR-08 | Employee Operations    | Employees can perform customer-service operations according to assigned roles and permissions.                                                                             |
| FR-09 | Administration         | Administrators can manage users, roles, permissions, supported configuration, and authorized administrative operations.                                                    |
| FR-10 | Notifications          | The system provides in-app notifications for supported security, profile, beneficiary, transfer, and service-request events.                                               |
| FR-11 | Audit                  | Important authentication, customer, financial, employee, and administrative activities are recorded for traceability.                                                      |
| FR-12 | Authorization          | The system enforces role, permission, and resource-ownership rules and prevents unauthorized access to data or operations.                                                 |

---

## 6. Key Business Rules

- Customers can access only their own authorized resources.
- Transfers are allowed only from eligible accounts to active authorized beneficiaries.
- Transfer amount must be greater than zero and within the available balance and configured limits.
- Blocked or closed accounts cannot initiate transfers.
- Invalid or failed financial operations must not create partial updates.
- Duplicate transfer requests must not create duplicate financial transactions.
- Successful financial operations must create transaction records with unique references.
- Concurrent transfers must be processed safely.
- Customers cannot directly modify balances or transaction history.
- Only authorized employees can process service requests.
- Only authorized administrators can perform administrative operations.
- Important business and security operations must be auditable.

---

## 7. Security Requirements

The platform must:

- Store passwords using secure one-way hashing.
- Protect APIs using authentication and authorization.
- Use JWT for protected REST APIs.
- Enforce role-based, permission-based, and resource-level authorization.
- Prevent cross-customer data access.
- Validate user input and business rules.
- Protect sensitive account information through appropriate masking.
- Support secure password change and reset.
- Protect sensitive financial operations against duplicate processing.
- Prevent passwords, tokens, and sensitive credentials from appearing in logs or audit records.
- Record important security events.

---

## 8. Non-Functional Requirements

### Security

Customer, authentication, and financial data must be protected from unauthorized access.

### Performance

APIs and database operations should provide efficient response times under expected V1 usage.

### Reliability

The system should handle supported failures without corrupting financial data.

### Maintainability

The application should follow clean coding practices, separation of concerns, and layered architecture.

### Testability

Business logic, APIs, security, database operations, and critical financial workflows should be automated-testable.

### Auditability

Important business, security, and administrative operations must be traceable.

### Data Integrity

Financial and customer data must remain consistent through database constraints and transaction management.

---

## 9. Technology Scope

V1 will use:

- Java 21
- Spring Boot
- Spring MVC
- REST APIs
- Spring Security
- JWT
- MySQL
- JPA/Hibernate
- Jakarta Bean Validation
- JUnit 5
- Mockito
- Swagger/OpenAPI
- Postman
- HTML/CSS/JavaScript
- Maven
- Git/GitHub

---

## 10. Architecture & Scope

### High-Level Architecture

```text
Web Browser
    ↓
HTML / CSS / JavaScript
    ↓
REST APIs
    ↓
Spring Boot Application
    ↓
Controller → Service → Repository
    ↓
MySQL
```

Security, validation, exception handling, audit, and transaction management operate across the application.

### V1 In Scope

- Customer banking operations
- Employee operations
- Administrative operations
- Authentication and authorization
- Accounts and beneficiaries
- Fund transfers
- Transactions
- Service requests
- Notifications
- Audit
- Validation and exception handling
- Automated testing
- API documentation
- Git/GitHub

### V1 Out of Scope

- Microservices
- Kafka
- Redis
- API Gateway
- Kubernetes
- Distributed tracing
- Service-owned databases
- External payment/banking integrations
- External email/SMS integration
- AI/ML features
- Advanced fraud detection
- Multi-currency/FX processing

These capabilities may be introduced in the separate V2 project.

---

## 11. Success Criteria

The project will be considered complete when:

- Customers can securely perform permitted banking operations.
- Employees can perform authorized customer-service operations.
- Administrators can securely manage authorized system functions.
- Unauthorized resource access is prevented.
- Financial operations maintain consistency and prevent duplicate processing.
- Important activities are auditable.
- APIs, frontend, database, security, and testing work together successfully.
- The application can be built, tested, run, and maintained using standard Git-based development practices.
- Major requirements can be traced to design, implementation, and test coverage.
