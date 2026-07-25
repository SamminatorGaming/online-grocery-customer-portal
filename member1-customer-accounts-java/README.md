# Online Grocery Customer Portal

This is the Member 1 section of the team project.

Member 1 owns customer accounts and delivery addresses.

## Features included

- Register a customer account
- Log in
- Log out
- View a customer profile
- Add a delivery address
- Edit a delivery address
- Save customers and addresses in a database
- Unit tests for registration, login, and addresses

## Tech used

- Java 17
- Spring Boot
- Maven
- H2 database
- Thymeleaf pages

## Run in Visual Studio Code

1. Install Java 17.
2. Install Maven.
3. Install the Extension Pack for Java in VS Code.
4. Open this folder in VS Code.
5. Open the terminal in VS Code.
6. Run:

```bash
mvn spring-boot:run
```

7. Open this in a browser:

```text
http://localhost:8080
```

## Run tests

```bash
mvn test
```

## Member 1 pages

```text
/register
/login
/profile
/addresses/new
```

## Database

The app uses H2. Data is saved in this folder:

```text
./data/customer_portal
```

The H2 console is here:

```text
http://localhost:8080/h2-console
```

Use this JDBC URL:

```text
jdbc:h2:file:./data/customer_portal
```

Username:

```text
sa
```

Leave the password blank.
