# Implementation of the web application Jodel with Svelte

> **Disclaimer:** This project is a learning/test project built to practice Svelte development. It is inspired by the concept of the Jodel app but is not affiliated with, endorsed by, or connected to Jodel GmbH in any way.

## Prerequisites
Before installation, make sure the following software is installed on your system:
- **JDK** (version 11 or higher)
- **MySQL** (e.g. MySQL Community Server, version 8.0 recommended)
- **Node.js** (includes npm), LTS version recommended
- **Docker** and **Docker Compose**

## Installation
**Backend**
The backend requires at least JDK version 11. Dependencies should be obtained via Gradle.
To access the database in the Docker container, environment variables must be set at the beginning.
First, the .env.example, located in the backend directory, must be renamed to .env. Afterwards, you have to adjust the variables if necessary.
The variables set in the .env.example can already be used to run the backend server.
Also the connection to the MySQL database must be established.

The .env can look like this:
```sh
DB_USERNAME=root
DB_PASSWORD=admin
DB_URL=jdbc:mysql://localhost:3306/jodel?serverTimezone=UTC
```

**Frontend**
In the frontend Svelte is used. In the subdirectory "frontend" the .env.example must be renamed to .env. Afterwards, the variables in this file must also be adjusted. Alternatively the values of the .env.example can be left.

The .env can look like this:
```sh
SERVER_PATH = http://localhost
SERVER_PORT = 8080
```

**Start with Docker**
The installation of packages, database initialization, and starting both the backend and frontend servers are handled automatically via Docker.

The entire application can be started with the command:
```sh
docker compose up -d --build
```

After starting the containers:
- The frontend client is accessible at `http://localhost:3000`
- The backend server is accessible at `http://localhost:8080`
