package Services;

import DAO.*;
import Models.Account;
import Utils.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.util.Random;
import java.util.Scanner;

public class AccountService {

    public static void accountMenu(Scanner sc) {
        while (true) {
            System.out.println("\n--- Banking Menu ---");
            System.out.println("1.Create Account");
            System.out.println("2.Credit");
            System.out.println("3.Debit");
            System.out.println("4.Transfer");
            System.out.println("5.Change PIN");
            System.out.println("6.Check Balance");
            System.out.println("7.Back");

            int option = sc.nextInt();

            switch (option) {
                case 1 -> createAccount(sc);
                case 2 -> creditMoney(sc);
                case 3 -> debitMoney(sc);
                case 4 -> transferMoney(sc);
                case 5 -> changePin(sc);
                case 6 -> checkBalance(sc);
                case 7 -> { return; }
                default -> System.out.println("Invalid option");
            }
        }
    }

    public static void createAccount(Scanner sc) {
        Account acc = accountDetails(sc);
        if(acc == null)
        {
            AccountService.accountMenu(sc);
        }
        int accountCheck = AccountDAO.getAccountId(acc.getAccountNumber());
        if(accountCheck != 0)
        {
            System.out.println("Account Already Exists");
            AccountService.accountMenu(sc);
            return;
        }
        if (AccountDAO.createAccount(acc)) {
            int accountId = AccountDAO.getAccountId(acc.getAccountNumber());
            PinDAO.insertPin(accountId);
            System.out.println("Account Created Successfully");
            AccountService.accountMenu(sc);
        } else {
            System.out.println("Failed to Create Account");
        }
    }

    // ---------------- CREDIT ----------------
    public static void creditMoney(Scanner sc) {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            System.out.print("Enter Account Number: ");
            long accNo = sc.nextLong();

            if (!AccountDAO.validAccount(accNo)) {
                System.out.println("Account does not exist");
                return;
            }

            int accountId = AccountDAO.getAccountId(accNo);
            PinDAO.unfreezeIfExpired(accountId);

            // FRAUD RULE 1: RAPID TRANSACTION
            if (FraudDAO.isRapidTransaction(accountId)) {
                AccountDAO.freezeAccount(accountId);
                FraudDAO.insertFraud(accountId, "RAPID_TXN");
                System.out.println("⚠️ Fraud detected. Account frozen.");
                return;
            }

            if (AccountDAO.isFrozen(accNo)) {
                System.out.println("Account is frozen.");
                AccountService.accountMenu(sc);
            }

            System.out.print("Enter Amount: ");
            int amount = sc.nextInt();
            if (amount <= 0) {
                System.out.println("Invalid amount");
                return;
            }

            if (!validatePinFlow(sc, accNo, accountId)) AccountService.accountMenu(sc);

            if (AccountDAO.creditAmount(con, accNo, amount)) {
                con.commit();
                TransactionDAO.insertTransaction(accountId, amount, "CREDIT");
                System.out.println("Amount credited successfully");
                AccountService.accountMenu(sc);
            } else {
                con.rollback();
                System.out.println("Credit failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- DEBIT ----------------
    public static void debitMoney(Scanner sc) {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            System.out.print("Enter Account Number: ");
            long accNo = sc.nextLong();

            if (!AccountDAO.validAccount(accNo)) {
                System.out.println("Account does not exist");
                return;
            }

            int accountId = AccountDAO.getAccountId(accNo);
            PinDAO.unfreezeIfExpired(accountId);

            // FRAUD RULE 1: RAPID TRANSACTION
            if (FraudDAO.isRapidTransaction(accountId)) {
                AccountDAO.freezeAccount(accountId);
                FraudDAO.insertFraud(accountId, "RAPID_TXN");
                System.out.println("⚠️ Fraud detected. Account frozen.");
                AccountService.accountMenu(sc);
            }

            if (AccountDAO.isFrozen(accNo)) {
                System.out.println("Account is frozen.");
                return;
            }

            System.out.print("Enter Amount: ");
            int amount = sc.nextInt();
            if (amount <= 0) {
                System.out.println("Invalid amount");
                return;
            }

            if (!validatePinFlow(sc, accNo, accountId)) AccountService.accountMenu(sc);;

            // FRAUD RULE 3: HIGH WITHDRAW (OTP)
            double avg = TransactionDAO.getAvgLast5Withdrawals(accountId);

            if (avg > 0 && amount >= avg * 3) {
                System.out.println("⚠️ High withdrawal detected.");

                if (!verifyOtp(sc, accountId)) {
                    // OTP failed → account frozen + fraud inserted
                    return;
                }
            }

            if (AccountDAO.debitAmount(con, accNo, amount)) {
                con.commit();
                TransactionDAO.insertTransaction(accountId, amount, "DEBIT");
                System.out.println("Amount debited successfully");
                AccountService.accountMenu(sc);
            } else {
                con.rollback();
                System.out.println("Insufficient balance");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- TRANSFER ----------------
    public static void transferMoney(Scanner sc) {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            System.out.print("Sender Account: ");
            long sender = sc.nextLong();

            System.out.print("Receiver Account: ");
            long receiver = sc.nextLong();

            if (!AccountDAO.validAccount(sender) || !AccountDAO.validAccount(receiver)) {
                System.out.println("Invalid account number");
                return;
            }

            int senderId = AccountDAO.getAccountId(sender);
            int receiverId = AccountDAO.getAccountId(receiver);

            PinDAO.unfreezeIfExpired(senderId);

            // FRAUD RULE: RAPID TRANSACTION
            if (FraudDAO.isRapidTransaction(senderId)) {
                AccountDAO.freezeAccount(senderId);
                FraudDAO.insertFraud(senderId, "RAPID_TXN");
                System.out.println("⚠️ Fraud detected. Account frozen.");
                AccountService.accountMenu(sc);
            }

            if (AccountDAO.isFrozen(sender)) {
                System.out.println("Account is frozen.");
                return;
            }

            if (!validatePinFlow(sc, sender, senderId)) {
                AccountService.accountMenu(sc);
                return;
            }

            System.out.print("Enter Amount: ");
            int amount = sc.nextInt();


            double balance = AccountDAO.checkBalance(sender);
            if (balance < amount) {
                System.out.println("❌ Insufficient balance.");
                con.rollback();
                AccountService.accountMenu(sc);
                return;
            }

            if (AccountDAO.debitAmount(con, sender, amount)
                    && AccountDAO.creditAmount(con, receiver, amount)) {

                con.commit();
                TransactionDAO.insertTransaction(senderId, amount, "TRANSFER_SENT");
                TransactionDAO.insertTransaction(receiverId, amount, "TRANSFER_RECEIVED");
                System.out.println("✅ Transfer successful");

            } else {
                con.rollback();
                System.out.println("❌ Transfer failed");
            }

            AccountService.accountMenu(sc);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void changePin(Scanner sc) {
        System.out.print("Enter Account Number: ");
        long accNo = sc.nextLong();

        if (!AccountDAO.validAccount(accNo)) {
            System.out.println("Account not found");
            return;
        }

        System.out.print("Enter Old PIN: ");
        String oldPin = sc.next();

        if (!AccountDAO.validatePin(accNo, oldPin)) {
            System.out.println("Incorrect old PIN");
            return;
        }

        System.out.print("Enter New PIN: ");
        String newPin = sc.next();

        if (!newPin.matches("\\d{4}")) {
            System.out.println("PIN must be 4 digits");
            return;
        }

        String hash = BCrypt.hashpw(newPin, BCrypt.gensalt());
        AccountDAO.createPin(accNo, hash);
        System.out.println("PIN updated successfully");
        AccountService.accountMenu(sc);
    }


    private static boolean validatePinFlow(Scanner sc, long accNo, int accountId) {
        System.out.print("Enter 4 digit PIN: ");
        String pin = sc.next();

        if (!pin.matches("\\d{4}")) {
            System.out.println("Invalid PIN format");
            return false;
        }

        if (!AccountDAO.validatePin(accNo, pin)) {
            PinDAO.incrementAttempts(accountId);
            int attempts = PinDAO.getAttempts(accountId);

            if (attempts >= 3) {
                AccountDAO.freezeAccount(accountId);
                PinDAO.blockFor24Hours(accountId);
                System.out.println("Account blocked for 24 hours");
            } else {
                System.out.println("Wrong PIN. Attempts left: " + (3 - attempts));
            }
            return false;
        }

        PinDAO.resetAttempts(accountId);
        return true;
    }


    private static boolean verifyOtp(Scanner sc, int accountId) {
        int otp = 100000 + new Random().nextInt(900000);

        // DEMO ONLY
        System.out.println("OTP generated (demo): " + otp);

        System.out.print("Enter OTP: ");
        int enteredOtp = sc.nextInt();

        if (enteredOtp != otp) {
            AccountDAO.freezeAccount(accountId);
            FraudDAO.insertFraud(accountId, "HIGH_WITHDRAW");
            System.out.println("OTP failed. Account frozen & fraud recorded.");
            return false;
        }

        System.out.println("OTP verified successfully.");
        return true;
    }

    private static Account accountDetails(Scanner sc) {

        System.out.print("Enter your registered Email: ");
        String email = sc.next();

        int userId = UserDAO.getUserIdByEmail(email);

        if (userId == -1) {
            System.out.println("Email not found. Please register first.");
            return null;
        }

        long accNo = AccountDAO.generateUniqueAccountNumber();
        System.out.println("Generated Account Number: " + accNo);


        System.out.print("Set 4-digit PIN: ");
        String pin = sc.next();

        if (!pin.matches("\\d{4}")) {
            System.out.println("Invalid PIN. Must be exactly 4 digits.");
            return null;
        }


        String pinHash = BCrypt.hashpw(pin, BCrypt.gensalt(12));

        return new Account(userId, accNo, pinHash);
    }


   public static void checkBalance(Scanner sc)
   {
       System.out.print("Enter Account Number: ");
       long accNo = sc.nextLong();

       if (!AccountDAO.validAccount(accNo)) {
           System.out.println("Account does not exist");
           return;
       }

       if (AccountDAO.isFrozen(accNo)) {
           System.out.println("Account is frozen.");
           return;
       }
       int accId = AccountDAO.getAccountId(accNo);
       if (!validatePinFlow(sc, accNo, accId)) AccountService.accountMenu(sc);

       Double balance = AccountDAO.checkBalance(accNo);

       if (balance != null) {
           System.out.println("Current Balance: ₹" + balance);
           AccountService.accountMenu(sc);
           return;
       } else {
           System.out.println("Account not found or inactive.");
       }
   }
}
