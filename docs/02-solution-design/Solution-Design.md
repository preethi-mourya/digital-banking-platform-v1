# Digital Banking & Account Management Platform V1 — Solution Design

## 1. Architecture Overview

V1 follows a **modular monolithic architecture**.

The system is divided into separate layers so that presentation, business logic, data access, security, and database responsibilities remain clearly separated.

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
Controller
     ↓
Service
     ↓
Repository
     ↓
MySQL Database
```

### Layer Responsibilities

- **Frontend:** Provides the user interface.
- **Controller:** Receives HTTP requests, triggers request validation, and returns API responses.
- **Service:** Implements business logic and banking rules.
- **Repository:** Handles database operations using JPA/Hibernate.
- **MySQL:** Stores customer, account, transaction, and application data.

### Cross-Cutting Components

- Spring Security
- JWT Authentication
- Validation
- Exception Handling
- Transaction Management
- Audit Logging

---

## 2. Application Layers

The application follows a layered architecture.

```text
Presentation Layer
       ↓
Controller Layer
       ↓
Service Layer
       ↓
Repository Layer
       ↓
Database Layer
```

### Presentation Layer

The frontend is developed using:

- HTML
- CSS
- JavaScript

It communicates with the backend through REST APIs.

### Controller Layer

The Controller layer:

- Accepts HTTP requests.
- Triggers request validation.
- Calls the appropriate service.
- Returns HTTP responses.
- Uses DTOs for API request and response data.

### Service Layer

The Service layer contains the main business logic.

Examples:

- Account operations
- Beneficiary validation
- Fund transfer rules
- Transaction processing
- Service request processing
- Authorization checks

### Repository Layer

The Repository layer communicates with MySQL through:

- Spring Data JPA
- Hibernate

It is responsible for database operations such as creating, reading, updating, and retrieving records.

### Database Layer

MySQL stores persistent application data including:

- Customers
- Users
- Accounts
- Beneficiaries
- Transactions
- Service Requests
- Notifications
- Audit Records

---

## 3. Requirements Traceability

The solution design maps each functional requirement to the technical components that will implement it.

| Requirement                      | Solution Design / Technical Approach                                                                                                                                                     |
|----------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **FR-01 Customer Profile**       | Customer Management module, Customer entity, REST Controller, Service layer, Repository, request validation, and resource-ownership authorization.                                       |
| **FR-02 Authentication**         | Spring Security, password hashing, JWT authentication, token validation, login/logout, password change/reset, and failed-login protection.                                               |
| **FR-03 Account Management**     | Account Management module, Account entity, REST APIs, JPA/Hibernate, account-status validation, and resource authorization.                                                              |
| **FR-04 Beneficiary Management** | Beneficiary Management module, Beneficiary entity, REST APIs, ownership validation, beneficiary-status validation, and Service layer business rules.                                     |
| **FR-05 Fund Transfer**          | Fund Transfer module, authentication/authorization, account and beneficiary validation, balance and limit checks, transaction management, duplicate prevention, audit, and notification. |
| **FR-06 Transaction Management** | Transaction Management module, Transaction entity, transaction-history APIs, filtering/sorting/pagination, and transaction-reference handling.                                           |
| **FR-07 Service Requests**       | Service Request module, customer APIs, request tracking/status management, cancellation rules, and authorized employee processing.                                                       |
| **FR-08 Employee Operations**    | Employee Operations module, Spring Security roles/permissions, resource authorization, and employee-service APIs.                                                                        |
| **FR-09 Administration**         | Administration module, role/permission management, authorized administrative APIs, configuration management, and audit logging.                                                          |
| **FR-10 Notifications**          | Notification module, Notification entity, in-app notification APIs, and event-based notification creation for supported activities.                                                      |
| **FR-11 Audit**                  | Audit component, AuditLog entity, and recording of important authentication, customer, financial, employee, and administrative activities.                                               |
| **FR-12 Authorization**          | Spring Security, role-based authorization, permission checks, and resource-ownership validation across protected APIs.                                                                   |

---

## 4. Security Design

Security is implemented using **Spring Security and JWT**.

### Authentication Flow

```text
User
 ↓
Login Request
 ↓
Spring Security
 ↓
Credential Validation
 ↓
JWT Generated
 ↓
Client Stores Token
 ↓
JWT Sent With Protected Requests
 ↓
JWT Validation
 ↓
Authorized Resource Access
```

### Authorization

The application uses role-based and resource-level authorization.

Roles:

- CUSTOMER
- EMPLOYEE
- ADMIN

Customers can access only their permitted resources.

Employees and administrators can perform operations according to their assigned permissions.

### Security Controls

- Password hashing
- JWT-based authentication
- Role-based authorization
- Resource ownership validation
- Input validation
- Secure password change/reset
- Sensitive data protection
- Security event auditing

---

## 5. Core Business Modules

The V1 application is organized into functional modules.

### Customer Management

Handles:

- Customer profile
- Profile updates
- Customer information retrieval

### Account Management

Handles:

- Account creation
- Account retrieval
- Account status
- Account-related operations

### Beneficiary Management

Handles:

- Add beneficiary
- View beneficiaries
- Update beneficiary
- Disable/remove beneficiary where permitted

### Fund Transfer

Handles:

- Transfer initiation
- Beneficiary validation
- Balance validation
- Transfer limits
- Duplicate transfer prevention
- Transaction creation
- Balance updates

### Transaction Management

Handles:

- Transaction history
- Transaction details
- Transaction status
- Transaction reference

### Service Requests

Handles:

- Request creation
- Request tracking
- Request status
- Employee processing

### Employee Operations

Provides authorized employee operations for customer and service-request management.

### Administration

Provides authorized administrative operations such as:

- User/role management
- Account-related administrative operations
- System-level configuration where applicable

### Notifications

Provides in-application notifications for important events such as:

- Successful transfers
- Service request updates
- Security-related events

### Audit

Records important system and security activities for traceability.

---

## 6. Fund Transfer Design

Fund transfer is treated as a transactional operation.

### Transfer Flow

```text
Customer
   ↓
Transfer Request
   ↓
Authentication & Authorization
   ↓
Validate Source Account
   ↓
Validate Beneficiary
   ↓
Validate Amount & Limits
   ↓
Check Available Balance
   ↓
Process Transfer
   ↓
Update Account Balance
   ↓
Create Transaction Record
   ↓
Create Notification
   ↓
Return Transfer Result
```

### Important Rules

- Transfer amount must be greater than zero.
- Source account must be eligible for transfer.
- Beneficiary must be active and valid.
- Sufficient balance must be available.
- Transfer limits must be enforced.
- Duplicate financial requests must be prevented.
- Balance and transaction updates must remain consistent.
- Failed transfers must not result in partial financial updates.

---

## 7. Transaction Management

Financial operations use database transaction management to maintain data consistency.

For example, during a fund transfer:

```text
Validate Transfer
       ↓
Debit Source Account
       +
Credit Destination Account
       +
Create Transaction Record
       +
Create Audit Record
       ↓
Commit Transaction
```

These related operations are handled as one logical transaction.

If an operation fails, the financial changes are rolled back to prevent inconsistent account data.

---

## 8. Database Design

The application uses **MySQL** as the primary relational database.

JPA/Hibernate is used for object-relational mapping.

### Main Entities

```text
User
Customer
Account
Beneficiary
Transaction
ServiceRequest
Notification
AuditLog
```

### Basic Relationship Example

```text
Customer
   │
   ├── Accounts
   │
   ├── Beneficiaries
   │
   └── Service Requests

Account
   │
   └── Transactions
```

The database maintains relationships and constraints required to protect data integrity.

---

## 9. Validation and Exception Handling

The application uses centralized validation and exception handling.

### Validation

Jakarta Bean Validation is used for request validation.

Examples:

- Required fields
- Valid email format
- Positive transaction amount
- Valid account information
- Valid request parameters

### Exception Handling

A centralized exception-handling mechanism provides consistent API responses.

Examples:

- Invalid request
- Authentication failure
- Authorization failure
- Resource not found
- Insufficient balance
- Duplicate transaction
- Business rule violation
- Internal server error

---

## 10. API Design

The backend exposes RESTful APIs.

Example API areas:

```text
/api/auth
/api/customers
/api/accounts
/api/beneficiaries
/api/transfers
/api/transactions
/api/service-requests
/api/notifications
/api/admin
```

APIs use standard HTTP methods:

- GET — Retrieve data
- POST — Create/process data
- PUT — Update data
- DELETE — Remove or disable data where applicable

API responses use appropriate HTTP status codes and structured JSON responses.

---

## 11. Testing Strategy

Testing is performed at multiple levels.

### Unit Testing

Used for:

- Service logic
- Business rules
- Validation logic

Technologies:

- JUnit 5
- Mockito

### Integration Testing

Used to verify interaction between application components such as:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

### API Testing

REST APIs are tested using:

- Postman
- Spring Boot testing support

### Security Testing

Security tests verify:

- Authentication
- Authorization
- Role restrictions
- Resource ownership
- Protected endpoints

---

## 12. API Documentation

The REST APIs are documented using **Swagger/OpenAPI**.

API documentation provides:

- Available endpoints
- HTTP methods
- Request parameters
- Request/response models
- Authentication requirements
- Response status codes

This makes the APIs easier to understand and test.

---

## 13. Configuration and Build

### Build Tool

The project uses **Maven**.

Maven manages:

- Dependencies
- Compilation
- Testing
- Packaging

### Configuration

Application configuration is maintained separately from application logic.

Sensitive values such as database credentials and JWT secrets should not be hardcoded in source code.

---

## 14. Version Control

Git is used for source-code version control.

GitHub is used as the remote repository.

Repository:

```text
digital-banking-platform-v1
```

Development changes follow the basic workflow:

```text
Modify Code
    ↓
git status
    ↓
git add
    ↓
git commit
    ↓
git push
```

Changes are committed with meaningful commit messages.

---

## 15. Deployment Scope

V1 is designed as a single Spring Boot application.

```text
Frontend
   ↓
Spring Boot Application
   ↓
MySQL
```

The application can be packaged and deployed as a Spring Boot application.

Advanced distributed deployment technologies such as Kubernetes, API Gateway, Kafka, Redis, and microservices are intentionally excluded from V1.

---

## 16. Technology Stack

| Area              | Technology              |
|-------------------|-------------------------|
| Language          | Java 21                 |
| Backend           | Spring Boot             |
| Web/API           | Spring MVC, REST        |
| Security          | Spring Security, JWT    |
| Database          | MySQL                   |
| ORM               | JPA / Hibernate         |
| Validation        | Jakarta Bean Validation |
| Testing           | JUnit 5, Mockito        |
| API Documentation | Swagger / OpenAPI       |
| API Testing       | Postman                 |
| Frontend          | HTML, CSS, JavaScript   |
| Build             | Maven                   |
| Version Control   | Git, GitHub             |
| IDE               | IntelliJ IDEA           |

---

## 17. V1 Scope Boundaries

### Included

- Customer management
- Authentication and authorization
- Account management
- Beneficiary management
- Fund transfers
- Transaction management
- Service requests
- Employee operations
- Administration
- Notifications
- Audit logging
- Validation
- Exception handling
- Automated testing
- API documentation

### Excluded from V1

- Microservices
- Apache Kafka
- Redis
- API Gateway
- Kubernetes
- Distributed tracing
- Service-owned databases
- External banking/payment integrations
- AI/ML features
- Advanced fraud detection
- Multi-currency/FX processing

These capabilities can be introduced in the future **V2 enterprise/distributed version**.

---

## 18. Design Goals

The V1 design focuses on:

- Clear separation of responsibilities
- Secure API access
- Strong authorization
- Financial data consistency
- Maintainable code
- Testable business logic
- Auditability
- Reliable transaction processing
- Clear API contracts
- Simple and understandable architecture

The architecture provides a strong foundation for evolving the banking platform into a more distributed and enterprise-oriented V2 system.
