# 🏦 SecureBankX – Secure Java Banking System

SecureBankX is a **Java-based Banking System Console Application** that simulates real-world banking operations with a strong focus on **security, transaction safety, and fraud detection**.

This project is designed to demonstrate **Java backend fundamentals**, including authentication, database transactions, fraud handling, and clean layered architecture.

---

## 📌 Project Objective

To build a **secure and reliable banking backend system** using **Java, JDBC, and MySQL**, implementing real-world banking rules such as PIN validation, account locking, fraud detection, and admin control.

---

## 🧩 Key Functional Modules

### 👤 User Management
- User registration and login
- Secure password storage using **BCrypt**
- Email-based unique user identification
- One user can create **multiple bank accounts**

---

### 💳 Account Management
- Create bank accounts with auto-generated unique account numbers
- Credit money
- Debit money
- Transfer money between accounts
- Change account PIN
- Check account balance

---

### 🔐 Security Implementation
- 4-digit PIN authentication for sensitive operations
- PIN stored using **BCrypt hashing**
- Maximum **3 incorrect PIN attempts**
- Automatic account block for **24 hours**
- Automatic unblocking after lock duration

---

### 🚨 Fraud Detection & Prevention
The system detects and handles suspicious activities such as:

- **Rapid Transaction Detection**
  - Multiple transactions in a short time
  - Account is automatically frozen

- **High Withdrawal Detection**
  - Withdrawal amount ≥ 3× average of last 5 withdrawals
  - OTP verification required

- **OTP Verification**
  - 6-digit OTP (demo mode)
  - Failure results in account freeze and fraud record

- **Fraud Logging**
  - All fraud cases stored with status tracking

---

### 👨‍💼 Manager (Admin) Module
- Manager registration (only one manager allowed)
- Manager login
- View all fraud alerts
- Resolve fraud cases
- Freeze and unfreeze bank accounts

---

## 🏗️ Project Architecture

The application follows a **layered architecture**:

Main (Controller)
↓
Service Layer (Business Logic)
↓
DAO Layer (Database Access)
↓
MySQL Database



---

## 🛠️ Technology Stack

- Java
- JDBC
- MySQL
- BCrypt (password and PIN hashing)

## ▶️ How to Run the Application

1. Clone the repository
   ```bash
   git clone https://github.com/Manjith8718/securebankx-java-banking-system.git
