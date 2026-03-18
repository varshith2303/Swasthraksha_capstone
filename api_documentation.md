# API Documentation

This document provides a comprehensive list of all the REST APIs implemented and used in the Swasthraksha health insurance project. The APIs are organized by their respective modules.

## Authentication & Users
These endpoints handle user registration, login, and administrative management of users (underwriters and claims officers).

| Method | Endpoint | Description | Role / Access |
|--------|----------|-------------|---------------|
| `POST` | `/register` | Register a new user | Public |
| `POST` | `/login` | Authenticate a user and receive a JWT token | Public |
| `POST` | `/admin/users` | Register a new Underwriter | Admin |
| `GET`  | `/admin/users` | Get a list of all Underwriters | Admin |
| `POST` | `/admin/claims-officers` | Register a new Claims Officer | Admin |
| `GET`  | `/admin/claims-officers` | Get a list of all Claims Officers | Admin |
| `DELETE` | `/admin/users/{id}` | Delete a user by their ID | Admin |

## Policies
These endpoints manage the different insurance policies offered by the system.

| Method | Endpoint | Description | Role / Access |
|--------|----------|-------------|---------------|
| `GET`  | `/policies` | Fetch all available policies | Authenticated |
| `POST` | `/policies` | Create a new policy | Admin (assumed) / Authenticated |
| `PUT`  | `/policies/{id}` | Update an existing policy | Admin (assumed) / Authenticated |
| `DELETE` | `/policies/{id}` | Delete an existing policy | Admin (assumed) / Authenticated |

## Policy Assignments
These endpoints handle the assignment/purchasing of policies to users.

| Method | Endpoint | Description | Role / Access |
|--------|----------|-------------|---------------|
| `GET`  | `/policyassignments/my` | Get all policy assignments for the currently logged-in user | User |
| `POST` | `/policyassignments` | Create a new policy assignment | Authenticated |
| `GET`  | `/policyassignments/all` | Get all policy assignments in the system | Admin/Staff |

## Applications
These endpoints manage the application process for policies.

| Method | Endpoint | Description | Role / Access |
|--------|----------|-------------|---------------|
| `GET`  | `/applications` | Fetch all applications | Admin/Staff |
| `GET`  | `/applications/myapplications` | Get applications submitted by the logged-in user | User |
| `POST` | `/applications` | Submit a new application | User |
| `GET`  | `/applications/pending` | Get all pending applications | Underwriter |
| `GET`  | `/applications/assigned` | Get applications assigned to a specific underwriter/staff | Underwriter |
| `GET`  | `/applications/{id}/members` | Get all policy members associated with a specific application | Authenticated |

## Claims
These endpoints manage the submission, assignment, and processing of insurance claims.

| Method | Endpoint | Description | Role / Access |
|--------|----------|-------------|---------------|
| `GET`  | `/claims` | Fetch all claims | Admin/Staff |
| `POST` | `/claims` | Submit a new claim | User |
| `GET`  | `/claims/my` | Get all claims submitted by the logged-in user | User |
| `GET`  | `/claims/assigned` | Get claims currently assigned to the logged-in Claims Officer | Claims Officer |
| `POST` | `/claims/{claimNumber}/assign` | Assign a claim to a specific Claims Officer | Admin |
| `POST` | `/claims/{claimNumber}/verify` | Verify, approve, or reject a claim | Claims Officer |

## Documents
These endpoints handle the uploading and retrieval of documents associated with applications and claims.

| Method | Endpoint | Description | Role / Access |
|--------|----------|-------------|---------------|
| `POST` | `/api/documents/upload/application` | Upload a document for an application | User |
| `POST` | `/api/documents/upload/claim` | Upload a document for a claim | User |
| `GET`  | `/api/documents/{id}` | Download/View a specific document by its ID | Authenticated |
| `GET`  | `/api/documents/application/{applicationId}` | Get all documents associated with a specific application | Authenticated |
| `GET`  | `/api/documents/claim/{claimId}` | Get all documents associated with a specific claim | Authenticated |
