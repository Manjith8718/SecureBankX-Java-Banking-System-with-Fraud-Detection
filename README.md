🏦 SecureBankX is a Java-based Banking System Console Application designed to simulate real-world banking operations with a strong focus on security, transaction safety, and fraud detection.

📌 Project Objective

To build a secure and reliable banking backend system that handles user accounts, financial transactions, and fraud scenarios using Java, JDBC, and MySQL, following clean architecture and industry best practices.

🧩 Key Functional Modules
👤 User Management

   • User Registration & Login

   • Secure password storage using BCrypt

   • Email-based unique user identification

   • One user can own multiple bank accounts

💳 Account Management

   • Create bank accounts with auto-generated unique account numbers

   • Credit money

   • Debit money

   • Transfer money between accounts

   • Change account PIN

   • Check account balance

🔐 Security Implementation

   • 4-digit PIN authentication for sensitive operations

   • PIN stored using BCrypt hashing

   • Maximum 3 incorrect PIN attempts

   • Automatic account block for 24 hours after multiple failures

   • Automatic unblocking after lock duration

🚨 Fraud Detection & Prevention

The system actively monitors suspicious activities:

  • Rapid Transaction Detection

   • Multiple transactions in a short time

   • Account is automatically frozen

  • High Withdrawal Detection

   • Withdrawal ≥ 3× average of last 5 withdrawals

   • OTP verification required

  • OTP Verification

   • 6-digit OTP
   
   • Failure results in account freeze and fraud record

  • Fraud Logging
  
   • All fraud cases are stored with status tracking

👨‍💼 Manager (Admin) Module

   • Manager Registration (single manager constraint)

   • Manager Login

   • View all fraud alerts

   • Resolve fraud cases

   • Freeze / unfreeze bank accounts manually

🏗️ System Architecture

The project follows a layered architecture:

  Controller (Main)
          ↓
  Service Layer (Business Logic)
          ↓
  DAO Layer (Database Operations)
          ↓
  MySQL Database

🛠️ Technology Stack

   • Java

   • JDBC

   • MySQL

   • BCrypt (Password & PIN hashing)
