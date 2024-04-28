# User Management RESTful API

This project implements a RESTful API for managing user entities.

## Overview

The User Management RESTful API provides endpoints for performing CRUD (Create, Read, Update, Delete) operations on user entities. It includes pagination for fetching user lists and error handling for managing exceptions.

## Usage

### Endpoints

- `GET /users`: Retrieve a list of users with optional pagination.
- `GET /users/{id}`: Retrieve a user by ID.
- `POST /users`: Create a new user.
- `PUT /users/{id}`: Update an existing user.
- `PATCH /users/{id}`: Partially update an existing user.
- `DELETE /users/{id}`: Delete a user by ID.

### Pagination

The API supports pagination with the following query parameters:

- `page`: Page number (default: 0)
- `size`: Number of items per page (default: 20)

### Filtering
#### By birthdate
- `from`: Filter users by birthdate (start date **required**) 
- `to`: Filter users by birthdate (end date **required**) 

### Error Handling

The API includes global exception handling to return appropriate error responses for invalid requests or internal errors.

## Getting Started

### Prerequisites

- Java JDK 17 or higher
- Maven
- PostgreSQL database

### Setup

1. Clone the repository:
    ```bash
    git clone https://github.com/whereismidel/user-management-restful.git
    ```
2. Navigate to the project directory: 
    ```bash 
    cd user-management-restful
    ```
3. Update the database configuration in `application.properties`:
    ```bash
    spring.datasource.url=jdbc:postgresql://localhost:5432/yourdatabase
    spring.datasource.username=your_username
    spring.datasource.password=your_password
    ```
4. Build the project using Maven:
    ```bash 
    mvn clean install
    ```
5. Run the application:
    ```bash 
    java -jar target/user-management-restful-1.0.jar
    ```
6. Access the API using the base URL: `http://localhost:8080`
