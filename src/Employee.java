public class Employee {
    private final String email;
    private final String password;

    public Employee(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return this.email;
    }
}
