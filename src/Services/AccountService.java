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
        System.out.println("Welcome TO Banking Page");
        System.out.println("1.Create Bank Account");
        System.out.println("2.Credit Money");
        System.out.println("3.Debit Money");
        System.out.println("4.Transfer Money");
        System.out.println("5.Change Pin");
        System.out.print("Your Option Please: ");

        int option = sc.nextInt();
        switch (option) {
            case 1 -> createAccount(sc);
            case 2 -> creditMoney(sc);
            case 3 -> debitMoney(sc);
            case 4 -> transferMoney(sc);
            case 5 -> changePin(sc);
            default -> System.out.println("Enter Valid Option");
        }
    }

    public static void createAccount(Scanner sc) {
        Account acc = accountDetails(sc);
        if (AccountDAO.createAccount(acc)) {
            int accountId = AccountDAO.getAccountId(acc.getAccountNumber());
            PinDAO.insertPin(accountId);
            System.out.println("Account Created Successfully");
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
                return;
            }

            System.out.print("Enter Amount: ");
            int amount = sc.nextInt();
            if (amount <= 0) {
                System.out.println("Invalid amount");
                return;
            }

            if (!validatePinFlow(sc, accNo, accountId)) return;

            if (AccountDAO.creditAmount(con, accNo, amount)) {
                con.commit();
                TransactionDAO.insertTransaction(accountId, amount, "CREDIT");
                System.out.println("Amount credited successfully");
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
                return;
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

            if (!validatePinFlow(sc, accNo, accountId)) return;

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

            // FRAUD RULE 1: RAPID TRANSACTION
            if (FraudDAO.isRapidTransaction(senderId)) {
                AccountDAO.freezeAccount(senderId);
                FraudDAO.insertFraud(senderId, "RAPID_TXN");
                System.out.println("⚠️ Fraud detected. Account frozen.");
                return;
            }

            if (AccountDAO.isFrozen(sender)) {
                System.out.println("Account is frozen.");
                return;
            }

            if (!validatePinFlow(sc, sender, senderId)) return;

            System.out.print("Enter Amount: ");
            int amount = sc.nextInt();

            if (AccountDAO.debitAmount(con, sender, amount)
                    && AccountDAO.creditAmount(con, receiver, amount)) {
                con.commit();
                TransactionDAO.insertTransaction(senderId, amount, "TRANSFER_SENT");
                TransactionDAO.insertTransaction(receiverId, amount, "TRANSFER_RECEIVED");
                System.out.println("Transfer successful");
            } else {
                con.rollback();
                System.out.println("Transfer failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- CHANGE PIN ----------------
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
    }

    // ---------------- PIN FLOW ----------------
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

    // ---------------- OTP (CONSOLE ONLY) ----------------
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
        System.out.print("User ID: ");
        int userId = sc.nextInt();

        System.out.print("Account Number: ");
        long accNo = sc.nextLong();

        System.out.print("PIN: ");
        String pin = sc.next();

        System.out.print("Account Type: ");
        String type = sc.next();

        String hash = BCrypt.hashpw(pin, BCrypt.gensalt());
        return new Account(userId, accNo, hash, type);
    }
}
