# Bitespeed Identity Reconciliation Backend

## 🚀 Tech Stack
- Java 21
- Spring Boot 3
- PostgreSQL
- Spring Data JPA
- Maven

---

## 📌 Problem Statement
This service identifies and links customer contacts based on email and phone number.

If a new request shares either:
- Same email OR
- Same phone number

It links them under the oldest primary contact.

---

## 🔗 API Endpoint

### POST `/identify`

---

### 📥 Request Body

```json
{
  "email": "lorraine@hillvalley.edu",
  "phoneNumber": "123456"
}
```

---

### 📤 Sample Response

```json
{
  "contact": {
    "primaryContactId": 1,
    "emails": ["lorraine@hillvalley.edu"],
    "phoneNumbers": ["123456"],
    "secondaryContactIds": []
  }
}
```

---

## ▶️ Run Locally

Run the following command:

```bash
mvn spring-boot:run
```

Server runs at:

http://localhost:8080

---

## 👨‍💻 Author
**Gaurav Naike**