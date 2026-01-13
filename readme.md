# GitHub Proxy

A lightweight Spring Boot application that acts as a proxy to the GitHub API, allowing API consumers to list all non-fork repositories for a given GitHub user along with branch information.

## Features

- Retrieve all non-fork repositories for a GitHub user
- Display repository owner and name
- List all branches with their latest commit SHA
- Proper error handling for non-existent users (404 response)
- Integration tests with WireMock for API emulation
- Minimal, focused architecture following industry best practices

## Requirements

- Java 25 or later
- Gradle 9.2.1 or later

## Technology Stack

- **Java**: 25
- **Spring Boot**: 4.0.1
- **Build Tool**: Gradle with Kotlin DSL
- **Testing**: Spring Boot Test, WireMock

## Dependencies

- `org.springframework.boot:spring-boot-starter-webmvc` - Web MVC support
- `org.springframework.boot:spring-boot-starter-restclient` - REST client for GitHub API calls
- `org.springframework.boot:spring-boot-starter-test` - Testing framework
- `org.wiremock:wiremock-standalone:3.9.1` - API mocking for integration tests

## Getting Started

### Build the Application

```bash
./gradlew build
```

### Run the Application

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

### Run Tests

```bash
./gradlew test
```

## API Endpoints

### Get User Repositories

**Request:**
```
GET /users/{username}/repositories
```

**Parameters:**
- `username` (path parameter) - The GitHub username

**Success Response (200 OK):**
```json
[
  {
    "repositoryName": "repository-name",
    "ownerLogin": "username",
    "branches": [
      {
        "name": "main",
        "lastCommitSha": "abc123def456..."
      }
    ]
  }
]
```

**Error Response (404 Not Found):**
```json
{
  "status": 404,
  "message": "User {username} not found"
}
```

## Example Usage

### Request
```bash
curl http://localhost:8080/users/octocat/repositories
```

### Response
```json
[
  {
    "repositoryName": "Hello-World",
    "ownerLogin": "octocat",
    "branches": [
      {
        "name": "master",
        "lastCommitSha": "6dcb09b5b57875f334f61aebed695e2e4193db5e"
      }
    ]
  }
]
```

### Non-existent User
```bash
curl http://localhost:8080/users/nonexistentuser/repositories
```

Response:
```json
{
  "status": 404,
  "message": "User nonexistentuser not found"
}
```

## Configuration

The application requires the following configuration property:

```yaml
github:
  base-url: https://api.github.com
```

This is set in `application.yml` to point to the GitHub API v3.


## References

- [GitHub API v3 Documentation](https://docs.github.com/en/rest/reference/repos)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring RestClient Documentation](https://spring.io/blog/2023/11/01/spring-boot-restclient-and-resttemplate-comparison)

