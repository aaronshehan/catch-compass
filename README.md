# CatchCompass

> Log every catch. Learn every pattern.

CatchCompass is a mobile-first fishing journal. It lets anglers photograph a
fish, record where and when it was caught, enter its measurements and species,
and save the conditions and lure that produced the catch.

The project is also a way to learn full-stack development: forms, validation,
relational data, authentication, file uploads, browser APIs, third-party
integrations, testing, and deployment.

## Product vision

An angler should be able to record a catch in less than a minute while still on
the water. Later, the journal should help answer questions such as:

- Which lure works best for a particular species?
- What wind and tide conditions produce the most catches?
- Where and when have the largest fish been caught?
- How have catches changed throughout the season?

CatchCompass is a private journal first. Exact fishing locations must never be
made public unless the angler deliberately chooses to share them.

## Current state

The core loop works end to end: register, sign in, log a catch with a photo and
a GPS fix, record conditions and the lure, and browse your own journal.

### Built

- **Accounts** — registration, sign in, sign out. Session cookies, BCrypt
  password hashing, CSRF protection on every write.
- **Catch logging** — species, catch time, measurements, notes.
- **Photos** — rear-camera capture on mobile, upload, private serving. Content
  is verified by decoding the image, not by trusting the declared type.
- **Location** — browser Geolocation with accuracy and reading time, plus manual
  entry whenever GPS is denied, unavailable, or slow.
- **Conditions** — air and water temperature, wind speed and direction, tide,
  pressure and sky. Entered by hand, or fetched from Open-Meteo and corrected.
- **Lure** — type and free-text description recorded on the catch.
- **Journal** — chronological list with photo thumbnails, and a detail page.
- **Ownership** — every query is scoped to the signed-in user, enforced in the
  service layer rather than by hiding interface controls.

### Not built yet

- Private map, gallery, filters and sorting
- Editing or deleting a catch
- Unit preferences (the column exists; nothing reads it)
- Tide data (the provider interface exists; no provider implements it)
- Offline drafts, multiple photos per catch, species suggestions
- Stripping GPS metadata from exported images
- Object storage for photos

## Technical approach

| Area | Choice |
| --- | --- |
| Backend | Spring Boot 4.1, Spring MVC, Java 21 |
| Frontend | React 19, Vite 7, React Router 7 |
| Database | PostgreSQL 17 |
| Persistence | Spring Data JPA |
| Migrations | Flyway |
| Validation | Jakarta Bean Validation |
| Authentication | Spring Security 7, session cookies |
| Photo storage | Local filesystem, outside the static resource path |
| Testing | JUnit, MockMvc, Testcontainers |

The frontend and backend are separate: Spring serves a JSON API, and React is
built into the jar as static resources. In development they run as two servers,
with Vite proxying `/api` to Spring so every request is same-origin and no CORS
configuration is needed.

An earlier version used Thymeleaf. It was replaced by React and removed.

### Package structure

```text
com.example.catchcompass
├── catchlog     Catch, photos, the catch API
├── conditions   Weather and tide, providers, Open-Meteo
├── shared       Security config, error handling, SPA routing
├── species      Species lookup
├── storage      Photo storage abstraction
└── user         Accounts, registration, authentication
```

## Data model

| Entity | Purpose | Important fields |
| --- | --- | --- |
| `User` | Owns all private data | `id`, `username`, `passwordHash`, `unitPreference`, `enabled` |
| `Catch` | The central record | `id`, `userId`, `speciesId`, `caughtAt`, coordinates, measurements, `lureType`, `lureDescription`, `notes` |
| `CatchPhoto` | Photo metadata and storage key | `id`, `catchId`, `storageKey`, `contentType`, dimensions |
| `CatchConditions` | Conditions at catch time | temperatures, wind, tide, pressure, sky, `conditionsSource`, `observedAt` |
| `Species` | Extensible fish lookup | `id`, `commonName`, `scientificName`, `waterType`, `active` |

Measurements are stored in one canonical format: kilograms, centimetres, metres
per second, degrees Celsius. Wind direction is stored as 0–359 degrees using the
meteorological convention (the direction the wind comes *from*) and converted to
compass labels for display.

Measurement and condition fields may be absent, but any supplied value must be
valid. Tide fields are optional because tide does not apply to inland water.

`conditionsSource` records whether conditions were typed (`MANUAL`), retrieved
(`WEATHER_API`), or retrieved and then corrected (`WEATHER_API_EDITED`) — a
value someone read off a gauge is different evidence from one an API
interpolated.

## API

| Method | Route | Purpose |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Create an account |
| `POST` | `/api/auth/login` | Sign in, establishing a session |
| `POST` | `/api/auth/logout` | Sign out |
| `GET` | `/api/auth/me` | Current account, or 401 |
| `GET` | `/api/catches` | Journal summaries, newest first |
| `POST` | `/api/catches` | Create a catch (multipart, photo included) |
| `GET` | `/api/catches/{id}` | Full catch detail |
| `GET` | `/api/catches/{id}/photo` | Photo bytes, owner only |
| `GET` | `/api/species` | Species list |
| `GET` | `/api/lure-types` | Lure type values |
| `GET` | `/api/conditions` | Weather lookup for a place and time |
| `GET` | `/api/conditions/options` | Tide and sky values |

Validation failures return RFC 9457 `ProblemDetail` with an added `errors`
object mapping field names to messages, so the client can render each message
beside its input.

Requests for another user's catch return `404`, never `403` — a 403 would
confirm the record exists.

## Running it

**Prerequisites:** JDK 21, Docker Desktop, Node 20+.

Development, two terminals:

```bash
./mvnw spring-boot:run          # API on 8080, starts PostgreSQL itself
cd frontend && npm run dev      # UI on 5173
```

Open <http://localhost:5173>. Always use 5173 in development; 8080 serves only
JSON.

Production-like, one jar:

```bash
./mvnw package
java -jar target/catchcompass-0.0.1-SNAPSHOT.jar
```

This build requires `DATABASE_URL`, `DATABASE_USERNAME` and `DATABASE_PASSWORD`,
and the `prod` profile. Spring Boot deliberately excludes its Docker Compose
support from packaged jars, so a deployed application never starts its own
database.

Tests:

```bash
./mvnw test -Dskip.frontend=true
```

Testcontainers starts a real PostgreSQL, so Docker must be running. The
migrations and every CHECK constraint are exercised for real rather than against
an in-memory substitute.

### Local quirks

- The database container maps to host port **5433**, because port 5432 is taken
  by a natively installed PostgreSQL.
- `./mvnw package` runs `npm ci`, which deletes `node_modules`. Stop the Vite dev
  server first, and run `npm install` afterwards to restore local tooling.
- Testing on a phone needs HTTPS, since geolocation and camera capture require a
  secure context. Tailscale works well: `tailscale serve --bg 8080` against the
  packaged jar.

## Security and privacy

- Passwords are BCrypt hashes. Plain passwords are never stored, logged, or
  returned.
- Login failures say "incorrect username or password" for both causes, so the
  response never reveals which usernames exist.
- Ownership is enforced in queries, not by hiding buttons.
- Uploaded images live outside the static resource path and are reachable only
  through a controller that checks ownership first.
- Storage keys are generated server-side. A filename from a browser is untrusted
  input, not a path.
- Upload size is capped and content type is verified by decoding the image.
- CSRF tokens are required on every state-changing request.
- Coordinates, credentials and image URLs are kept out of application logs.
- Database and image store must be backed up together, or references break.

## Known limitations

- **Timezones.** The browser submits a wall-clock time, which the server
  interprets in its own zone. Display uses the reader's zone, so a deployed
  server in a different zone will show a shifted time.
- **HEIC.** iOS Safari converts to JPEG when capturing through the camera, so
  this works in practice. Selecting an existing HEIC from the photo library is
  untested and may be rejected.
- **Orphaned photo files.** Deleting a catch removes the database row; nothing
  removes the file from disk.
- **`conditionsSource` is client-asserted.** A crafted request could claim
  `WEATHER_API` without a lookup.
- **The seeded `legacy-dev-user`** owns any catches created before accounts
  existed. Its password hash is deliberately invalid, so nobody can sign in as
  it and those records are unreachable.

## Roadmap

1. Catch editing and deletion
2. Gallery, filters, sorting, and the private map
3. Unit preferences
4. Object storage for photos, and EXIF stripping before any sharing
5. Offline drafts for poor connectivity
6. Trends by species, lure, season and conditions

## Helpful references

- [Spring Initializr](https://start.spring.io/)
- [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa)
- [Uploading Files](https://spring.io/guides/gs/uploading-files)
- [Securing a Web Application](https://spring.io/guides/gs/securing-web)
- [MDN Geolocation API](https://developer.mozilla.org/en-US/docs/Web/API/Geolocation_API)
- [MDN HTML `capture` attribute](https://developer.mozilla.org/en-US/docs/Web/HTML/Reference/Attributes/capture)
- [Open-Meteo](https://open-meteo.com/)

## License

Choose a license before publishing or accepting contributions. The MIT License
is a common option for an open-source learning project.
