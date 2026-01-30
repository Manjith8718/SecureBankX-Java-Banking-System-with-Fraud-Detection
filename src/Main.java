import Services.AccountService;
import Services.ManagerService;
import Services.UserService;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Welcome TO Banking System Console App ===");
            System.out.println("1. User");
            System.out.println("2. Manager");
            System.out.println("3. Exit");
            System.out.print("Your Option Please: ");

            int option = sc.nextInt();

            switch (option) {
                case 1 -> UserService.userMenu(sc);
                case 2 -> ManagerService.managerMenu(sc);
                case 3 -> {
                    System.out.println("Thank you for using Banking System 🙏");
                    sc.close();
                    return;
                }
                default -> System.out.println("Enter Valid Option");
            }
        }
    }
}
