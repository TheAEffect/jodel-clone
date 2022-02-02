# Implementation of the web application Jodel with Svelte

> **Disclaimer:** This project is a learning/test project built to practice Svelte development. It is inspired by the concept of the Jodel app but is not affiliated with, endorsed by, or connected to Jodel GmbH in any way.

## Prerequisites
Before installation, make sure the following software is installed on your system:
- **JDK** (version 11 or higher)
- **MySQL** (e.g. MySQL Community Server, version 8.0 recommended)
- **Node.js** (includes npm), LTS version recommended

## Installation
**Backend**
The backend requires at least JDK version 11. Dependencies should be obtained via Gradle.
To access the database in the Docker container, environment variables must be set at the beginning.
First, the .env.example, located in the root directory, must be renamed to .env. Afterwards, you have to adjust the variables if necessary.
The variables set in the .env.example can already be used to run the backend server.
Also the connection to the MySQL database must be established.

The .env can look like this:
```sh
DB_USERNAME=root
DB_PASSWORD=admin
DB_URL=jdbc:mysql://localhost:3306/jodel?serverTimezone=UTC
```

After that the server and the database can be started.
In the subdirectory "database" is the SQL script with the name "jodel.sql". This must be executed and creates all tables, which are necessary for Jodel.

**Frontend**
In the frontend Svelte is used. In the subdirectory "svelteClient" the .env.example must be renamed to .env. Afterwards, the variables in this file must also be adjusted. Alternatively the values of the .env.example can be left.

The .env can look like this:
```sh
SERVER_PATH = http://localhost
SERVER_PORT = 8080
```

Likewise, the packages must be obtained by executing in the ``svelteClient'' directory the command
```sh
npm install
```
is called.

Finally, the client can be installed with the command
```sh
npm run dev
```
in developer mode or, if needed, by using
```sh
npm run start
```
can be started directly
