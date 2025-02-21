# Rental Service Application

This is a Spring Boot application for managing rental properties, bookings, block a property and user authentication. The application provides RESTful APIs for creating, updating, and managing properties, bookings, block a property and user accounts.

## Prerequisites

- Java 17 or higher
- Maven 3.6.0 or higher

## Running the Project

1. **Clone the repository:**

    ```sh
    git clone https://github.com/rodrigoncalves/rental-service.git
    cd rental-service
    ```

2. **Build the project:**

    ```sh
    mvn clean install
    ```

3. **Run the application:**

    ```sh
    mvn spring-boot:run
    ```

4. **API Documentation:**

    Swagger will be available at `http://localhost:8080/swagger-ui/index.html`.

## Approach and Challenges

### Approach

- **Modular Design:** The application is designed with a modular approach, separating concerns into different packages such as `service`, `controller`, `repository`, `domain`, `security`, and `exception`.
- **Spring Boot:** Utilized Spring Boot for rapid development and easy configuration.
- **Security:** Implemented JWT-based authentication and authorization to secure the endpoints.
- **Testing:** Wrote unit and integration tests to ensure the correctness of the application using JUnit and Mockito.
- **Exception Handling:** Centralized exception handling using `@RestControllerAdvice` to provide consistent error responses.

### Challenges

- **Spring Boot 3**: I had to learn about the new features of Spring Boot 3 and how to configure the application properly.
- **Database Transactions:** Ensuring database consistency and handling transactions properly, especially in the context of booking and blocking properties.
- **Security:** Implementing robust security measures to protect user data and ensure only authorized access to resources.
- **Edge Cases:** Handling various edge cases such as overlapping bookings, invalid date ranges, and ensuring data integrity.

## Suggested Improvements

- **Caching:** Implement caching mechanisms to improve performance, especially for frequently accessed data.
- **Scalability:** Optimize the application for scalability by introducing load balancing and horizontal scaling.
- **Readability:** Improve test organization by separating different scenarios into different test classes.
- **Logging:** Add detailed logging to track application behavior and troubleshoot issues.
- **Feature Enhancements:** Add more features such as management of properties (listing, create, update and delete).
