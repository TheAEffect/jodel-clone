# Implementation of the web application Jodel with Svelte

> **Disclaimer:** This project is a learning/test project built to practice Svelte development. It is inspired by the concept of the Jodel app but is not affiliated with, endorsed by, or connected to Jodel GmbH in any way.

## Key Features

* Location-Based Feed: Uses browser geolocation (with automated IP fallback) to serve hyperlocal feeds and filter posts by distance (*Here*, *Very Close*, *City-wide*).
* Rich Post Creation:
  * **Text Posts:** Colored cards with custom or random background colors.
  * **Image Posts:** Picture uploads with optional caption/text overlays.
  * **Interactive Surveys:** Multi-choice polls (2–4 options) with real-time percentage votes.
  * **Link Posts:** Embed and preview external links.
  * **Hashtags:** Tag and categorize posts (e.g. `#campus`, `#food`).
* Channel System: Browse and post into thematic channels (e.g. 📢 Announcements, ❓ Questions, 💬 Off-Topic).
* Voting & Karma System:
  * Upvote and downvote posts and comments (`+1` / `-1`).
  * User karma score tracked in the user profile.
  * Feed sorting by **Recent** (🕒), **Most Discussed** (💬), and **Loudest / Top Voted** (🔥).
* Anonymous Comment Threads:
  * Privacy-first discussions where the author is highlighted as **OJ** (*Original Jodler*) and commentators are sequentially numbered (`1`, `2`, `3`...).
  * Picture replies within comment threads.
* User Profiles & Auth: Secure registration and login with personal karma statistics and post history.
## Screenshots
<details>
<summary><b>View screenshots of the web application (click here to expand)</b></summary>

| **Feed-Overview** | **Anonymous Discussion Thread** | **Interactive Survey** | **Image Post** |
| :---: | :---: | :---: | :---: |
| <img width="220" alt="01_feed_overview" src="https://github.com/user-attachments/assets/1f046efd-767c-497a-9029-cb033a822ea6" /> | <img width="220" alt="02_comments_thread" src="https://github.com/user-attachments/assets/c16cb16b-ab58-477a-8ca9-698bfb161814" /> | <img width="220" alt="03_survey_post" src="https://github.com/user-attachments/assets/05486862-fa2b-4438-b5fe-fc9ad595d81e" /> | <img width="220" alt="04_image_post" src="https://github.com/user-attachments/assets/9346c8b6-7fd3-48ed-8f45-5f054113dfcc" /> |
| *All post types (text, poll, image, link)* | *Discussions with OJ & 1, 2* | *Multiple-Choice Polls* | *Preview Before the Unveiling* |
| **Image-Post revealed** | **Full-Screen Image Editor** | **Channel Selection** | **Login & Registration** |
| <img width="220" alt="04_image_revealed" src="https://github.com/user-attachments/assets/67c0df17-2faf-44ff-9215-5dc8c6669f75" /> | <img width="220" alt="06_image_editor" src="https://github.com/user-attachments/assets/82c61c14-3e3d-4405-bcea-9da98a75119c" /> | <img width="220" alt="07_channel_selection" src="https://github.com/user-attachments/assets/3f311f08-58a6-4ecb-b104-9fd405402cb6" /> | <img width="220" alt="01_login" src="https://github.com/user-attachments/assets/2cd96282-1169-4310-8ee0-336905ee4fc8" /> |
| *Full-screen reveal when holding* | *Freehand Drawing & Text Captions* | *Topic-Specific Channels* | *User Authentication* |
</details>

## Tech Stack & Architecture

| Layer | Technologies |
| :--- | :--- |
| **Frontend** | **Svelte 3**, JavaScript (ES6+), Rollup, Responsive Mobile-First CSS |
| **Backend** | **Java 11**, **Quarkus Framework** (Panache ORM, RESTEasy, MicroProfile OpenAPI) |
| **Database** | **MySQL 8.0** with `utf8mb4` charset (full emoji support) |
| **DevOps** | **Docker** & **Docker Compose** (Multi-stage builds for lean containers) |

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
