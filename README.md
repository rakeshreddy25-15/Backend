<<<<<<< HEAD
# backend
=======
# StudyLeague Spring Boot Backend

## Run locally

- Install Java 17 and Maven.
- From this `server/` directory, run:

```bash
mvn spring-boot:run
```

The API will be served at `http://localhost:5000/api`.

## Endpoints (matching frontend)
- POST `/api/auth/register` -> `{ token, user }`
- POST `/api/auth/login` -> `{ token, user }`
- GET `/api/courses`
- GET `/api/courses/{id}`
- POST `/api/courses` (body: `{ title, description, duration, teacherId }`)
- POST `/api/courses/{id}/enroll`
- GET `/api/courses/{id}/enrollments`
- POST `/api/courses/{courseId}/assignments`
- GET `/api/courses/{courseId}/assignments`
- POST `/api/assignments/{assignmentId}/submit` (multipart)
- GET `/api/assignments/{assignmentId}/submissions`
- POST `/api/submissions/{submissionId}/grade`
- GET `/api/users/{userId}/grades`

Note: This setup uses an in-memory store with demo data and a fake token.


>>>>>>> 0cbd4de (backend)
