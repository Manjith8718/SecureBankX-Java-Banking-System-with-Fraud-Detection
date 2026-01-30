package Services;

import DAO.UserDAO;
import Models.User;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Scanner;

public class UserService {

    public static void userMenu(Scanner sc) {


        System.out.println("Welcome TO User Page");
        System.out.println("1.Login");
        System.out.println("2.Register");
        System.out.print("Your Option Please: ");

        int option = sc.nextInt();
        switch (option) {
            case 1 -> userLogin(sc);
            case 2 -> userRegister(sc);
            default -> System.out.println("Enter Valid Option");
        }
    }

    public static void userLogin(Scanner sc) {
        System.out.println("User Login Details Please");
        User u = loginDetails(sc);

        if (UserDAO.validateUser(u.getEmail(), u.getPassword())) {
            System.out.println("User Login Successful");
            // ✅ Redirect to account operations
            AccountService.accountMenu(sc);
        } else {
            System.out.println("Invalid Email or Password");
        }
    }

    public static void userRegister(Scanner sc) {
        System.out.println("User Registration Details Please");
        sc.nextLine(); // clear buffer
        User u = registerDetails(sc);

        if (UserDAO.userExists(u)) {
            System.out.println("User Already Exists. Please Login.");
            userLogin(sc);
            return;
        }
        if(UserDAO.createUser(u))
        {
            System.out.println("User Registered Successfully");
            userLogin(sc);
        }

       else {
        System.out.println("Registration failed. Try again.");
        }
    }

    private static User loginDetails(Scanner sc) {
        System.out.print("Please Enter Email: ");
        String email = sc.next();
        System.out.print("Please Enter Password: ");
        String password = sc.next();
        return new User(null, email, password);
    }

    private static User registerDetails(Scanner sc) {
        System.out.print("Please Enter Name: ");
        String name = sc.nextLine();

        System.out.print("Please Enter Email: ");
        String email = sc.next();

        System.out.print("Please Enter Password: ");
        String password = sc.next();

        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        return new User(name, email, hashed);
    }
}
