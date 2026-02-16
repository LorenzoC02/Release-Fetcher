# Release Fetcher

A Spotify integration tool that automatically discovers new music releases from artists in your playlists. Built to solve the problem of staying current with your favorite artists without manually checking for new releases.

## What It Does

Release Fetcher connects to your Spotify account, scans your playlists to identify artists you listen to, and fetches their latest albums and singles. You can filter releases by date and save discoveries directly to your library.

## Technical Overview

This project demonstrates backend development skills with Spring Boot and the Spotify Web API. The focus is on building a clean REST API with proper OAuth 2.0 authentication and efficient data processing.

### Backend Architecture

**Spring Boot REST API** — The backend handles all Spotify API interactions, authentication flow, and business logic for release discovery.

**OAuth 2.0 Implementation** — Implements Spotify's OAuth flow with secure token management and refresh handling. The authentication layer properly manages user sessions and API credentials.

**RESTful Design** — Endpoints follow REST principles for playlist scanning, release fetching, and library management. Clean separation between controllers, services, and data models.

**Spotify Web API Integration** — Direct integration with Spotify's API for playlist access, artist discovery, and album/single retrieval. Handles rate limiting and pagination for large datasets.

### Tech Stack

- **Backend:** Spring Boot, Java
- **API Integration:** Spotify Web API
- **Authentication:** OAuth 2.0
- **Frontend:** HTML/CSS/JS (AI-assisted, minimal complexity)

The frontend is intentionally simple—this is a backend-focused project. I used AI to generate the UI quickly since frontend development isn't my primary focus.

## Key Features

- Scans multiple playlists simultaneously to extract artist lists
- Fetches recent releases (albums and singles) for discovered artists
- Date-based filtering to show only releases from specific time periods
- One-click saving of new releases to your Spotify library
- Session management with automatic token refresh

## Running Locally

You'll need a Spotify Developer account to get API credentials.

1. Register an app at [Spotify Developer Dashboard](https://developer.spotify.com/dashboard)
2. Add `http://127.0.0.1:8080/login/oauth2/code/spotify` to your app's redirect URIs
3. Clone this repository
4. Configure your credentials in `application.properties`:
   ```properties
   spotify.client.id=your_client_id
   spotify.client.secret=your_client_secret
   ```
5. Run with Gradle:
   ```bash
   ./gradlew bootRun
   ```
6. Navigate to `http://127.0.0.1:8888`

## Project Structure

```
src/main/java/
  ├── controllers/    # REST endpoints
  ├── services/       # Business logic and Spotify API calls
  ├── models/         # Data models for releases, artists, playlists
  └── config/         # OAuth and application configuration

src/main/resources/
  └── static/         # Frontend assets
```

## Why This Project

I built this to solve a real problem—keeping up with new music from artists I follow—while learning how to properly implement OAuth 2.0 and work with external APIs. The Spotify API presented interesting challenges around rate limiting, pagination, and handling large datasets efficiently.

The backend demonstrates practical skills with Spring Boot, RESTful API design, and secure authentication patterns that transfer to any API integration project.

## License

MIT
