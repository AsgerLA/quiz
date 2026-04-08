# Multiple Player Quiz

## Vision

This project is a backend API for a **Multiple Player Quiz**.
The system allows users to play quizzes in real time and create their own quizzes.

---

## Links

Portfolio website:
https://asgerla.github.io/portfolio

Project overview video:
https://asgerla.github.io/portfolio/projects/quiz/video

Source code repository:
https://github.com/AsgerLA/quiz

---

## Architecture

The backend architecture is built with 2 main layers:

- Controller layer (REST endpoints)
- Repository layer (database access)

The repository layer serves as a service layer.

Technologies used:

- Java
- Javalin
- JPA / Hibernate
- PostgreSQL
- Maven
- JWT authentication

### Architecture Diagram
```text
         ┌ ─ ─ ─ ─ ─ Quiz app ─ ─ ─ ─ ─ ┐
Client → │ REST Controller → Repository │ → Database (PostgreSQL)
         └ ─ ─ ─ ─ ─ ─ ─  ─ ─ ─ ─ ─ ─ ─ ┘
```
Each layer is in a separate java package:
- REST Controller => **app.web**
- Repository      => **app.db**

### Design Decisions

**Project structure**

The service layer was left out,
because the methods where a 1 to 1 with the repository layer.
Most of the business logic is database logic.

**Error handing**

Errors are preferably returned not thrown.
Originaly I did simply throw exceptions
as it was the easiest way of dealing with errors.
The json module I made does throw an exception on error.

**JSON**

I created my own json module,
because I wanted to use something other then DTO's.

**Authentication**

There a 2 roles: logged in and admin.
A user logs in and receives a JWT token,
which they will include in the HTTP header.
The admin uses a token (not JWT), which tells the server they are admin.
Due to time contraints I simply used the secret key used for JWT.

---

## Data Model

### ERD

```text
  ┌─account───┐
┌─│id         │
│ └───────────┘
│ ┌─quiz──────┐   ┌─quiz_tag──┐   ┌─tag─┐
│ │id         │─┬─│quiz_id    │ ┌─│id   │
└─│owner_id   │ │ │tags_id    │─┤ └─────┘
  └───────────┘ │ └───────────┘ │ ┌─category──┐
                │               │ │id         │
┌───────────────┘               └─│tag_id     │
│ ┌─question──┐   ┌─answer─────┐  └───────────┘
│ │id         │─┐ │id          │
└─│quiz_id    │ └─│question_id │
  └───────────┘   └────────────┘
```

### Important Entities

#### Quiz

Represents a quiz.

Fields:

- id
- title
- description
- owner
- tags
- questions

Includes fields for various counters for statistics.

#### Question

Represents a question for a quiz.

Fields:

- id
- quiz_id
- question
- type
- slot - sort order
- answers

#### Answer

Represents an answer for a question.

Fields:

- id
- question_id
- answer
- slot - sort order
- points - <= 0 is incorrent answer

#### Account

Represents a registered user.

Fields:

- id
- username
- password

---

## API Documentation

On error endpoints might respond with a JSON body:

{ "message": "custom message", "status" : 400, "instance" : "/api/example" }

Status codes 200 and 201 imply that a JSON body is included.
'204 No Content' is used when there's no JSON response data.

### Example Endpoints

#### Get catalog

The catalogs are the font pages showing an overview of quizzes.

GET /api/category/{name _optional_}

Response:

200 Ok

{"categories":["geography",...],"sections":[{"name":"geography","quizzes":[...]}

#### Signup/signin

POST /api/auth/signup

Request body:

{ “username”: “uname”, “password”: “passwd” }

Response:

204 No Content


POST /api/auth/signin

Request body:

{ “username”: “uname”, “password”: “passwd” }


Response:

200 Ok

{ “token”: “_jwt_” }

#### Create Quiz

POST /api/quiz

Request body:
```json
{
    "title" : "New Quiz",
    "description" : "A new Quiz",
    "tags" : ["newquiz", "trivia"],
    "hidden" : false,
    "questions" : [
        {
            "slot" : 0,
            "question" : "Question?",
            "type" : "MULTI",
            "answers" : [
                {
                    "slot" : 0,
                    "answer" : "true",
                    "points" : 1
                }
            ]
        }
    ]
}
```

Response:

204 No Content

---

## User Stories

- As a user, I want to view quizzes
- As a user, I want to start a quiz session
- As a user, I want to join a quiz session
- As a user, I want to register an account
- As a user, I want to create a quiz
