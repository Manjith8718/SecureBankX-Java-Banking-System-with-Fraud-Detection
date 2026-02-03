package Services;

import DAO.ManagerDAO;
import DAO.FraudDAO;
import DAO.AccountDAO;
import Models.Manager;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Scanner;

public class ManagerService {

    public static void managerMenu(Scanner sc) {
        System.out.println("Welcome TO Manager Page");
        System.out.println("1.Login");
        System.out.println("2.Register");
        System.out.print("Your Option Please: ");

        int option = sc.nextInt();
        switch (option) {
            case 1 -> managerLogin(sc);
            case 2 -> {
                if (!ManagerDAO.managerExists()) {
                    managerRegister(sc);
                } else {
                    System.out.println("Manager Position Already Filled");
                }
            }
            default -> System.out.println("Enter Valid Option");
        }
    }

    public static void managerLogin(Scanner sc) {
        System.out.println("Manager Login Details Please");
        Manager m = loginDetails(sc);

        if (ManagerDAO.validateManager(m.getEmail(), m.getPassword())) {
            System.out.println("Manager Login Successful");
            managerOperations(sc);
            return;
        } else {
            System.out.println("Invalid Email or Password");
        }
    }

    // ✅ Manager operations after login
    private static void managerOperations(Scanner sc) {
        while (true) {
            System.out.println("\n--- Manager Dashboard ---");
            System.out.println("1.View Fraud Alerts");
            System.out.println("2.Resolve Fraud");
            System.out.print("3.Freeze Account");
            System.out.println("4.Logout");
            System.out.print("Choose option: ");

            int choice = sc.nextInt();
            switch (choice) {
                case 1 -> FraudDAO.viewAllFrauds();
                case 2 -> resolveFraud(sc);
                case 3 -> freezeAccountByManager(sc);
                case 4 -> {
                    System.out.println("Logged out");
                    return;
                }
                default -> System.out.println("Invalid choice");
            }
        }
    }

    // ✅ Resolve fraud
    private static void resolveFraud(Scanner sc) {
        System.out.print("Enter Fraud ID: ");
        int fraudId = sc.nextInt();
        if(!FraudDAO.checkExists(fraudId))
        {
            System.out.println("Wrong FraudId,Please Check");
            managerOperations(sc);
            return;
        }

        System.out.print("Enter Account ID to unfreeze: ");
        int accountId = sc.nextInt();

        if(!AccountDAO.checkAccountId(accountId))
        {
            System.out.println("Wrong AccountId,Please Check");
            managerOperations(sc);
            return;
        }

        FraudDAO.resolveFraud(fraudId);
        AccountDAO.unfreezeAccount(accountId);

        System.out.println("Fraud resolved & account unfrozen");
    }

    public static void managerRegister(Scanner sc) {
        System.out.println("Manager Registration Details Please");
        Manager m = managerDetails(sc);
        ManagerDAO.createManager(m);
        System.out.println("Manager Registered Successfully");
    }

    private static Manager managerDetails(Scanner sc) {
        System.out.print("Please Enter Email: ");
        String email = sc.next();

        System.out.print("Please Enter Password: ");
        String password = sc.next();

        String hashed = BCrypt.hashpw(password, BCrypt.gensalt(12));
        return new Manager(email, hashed);
    }

    private static Manager loginDetails(Scanner sc) {
        System.out.print("Please Enter Email: ");
        String email = sc.next();

        System.out.print("Please Enter Password: ");
        String password = sc.next();

        return new Manager(email, password);
    }

    private static void freezeAccountByManager(Scanner sc) {
        System.out.print("Please Enter Account ID to unfreeze: ");
        int accountId = sc.nextInt();
        if(!AccountDAO.checkAccountId(accountId))
        {
            System.out.println("Wrong AccountId,Please Check Manager");
            managerOperations(sc);
            return;
        }
        if(!FraudDAO.checkAccountIdExists(accountId))
        {
            System.out.println("Account Does Not Have Any Fraud, Please Check Manager");
            managerOperations(sc);
            return;
        }
        AccountDAO.freezeAccount(accountId);
        System.out.println("Account Frozen Successfully");
        managerOperations(sc);
        return;
    }
}
