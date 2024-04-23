import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.lang.String;

public class Library {
    Connection con;

    {
        try {
            con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/library", "postgres","SonPost24");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void add_book(String s) {
        String[] st = s.trim().split(","); // название, автор, год издания, количество
        Statement stmt;

    }
    public void remove_book(String s) {}

    public void give_book(String s){}
    public void return_book(String s){}
    public void search_book(int c){}
    public void show_all_books(){}
    public void show_borrowed_books(){}
}
