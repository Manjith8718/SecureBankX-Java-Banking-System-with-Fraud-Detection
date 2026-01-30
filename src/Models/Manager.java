package Models;

public class Manager {

    private int managerId;
    private String email;
    private String password;

    public Manager() {}

    public Manager(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public Manager(int managerId, String email, String password) {
        this.managerId = managerId;
        this.email = email;
        this.password = password;
    }

    public int getManagerId() {
        return managerId;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setManagerId(int managerId) {
        this.managerId = managerId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

