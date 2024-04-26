import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.lang.String;

public class Library {
    Connection con;

    public void add_book(String s) {
        String[] st = s.trim().split(","); // название, автор, год издания, количество
        try {
            con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/library", "postgres", "SonPost24");
            Statement stmt = con.createStatement();
            stmt.executeUpdate("INSERT INTO books (title, author, published, quantity, in_stock) VALUES ('" + st[0] + "','" + st[1] + "'," + st[2] + "," + st[3] + "," + st[3] + ");");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void remove_book(String s) {
        String[] st = s.trim().split(","); // название, автор, год издания, {количество}
        try {
            con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/library", "postgres", "SonPost24");
            Statement stmt = con.createStatement();
            stmt.executeUpdate("DELETE FROM books WHERE title = '"+st[0]+"' AND author = '"+st[1]+"' AND published = "+st[2]+";");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void give_book(String s) {
    }

    public void return_book(String s) {
    }

    public void search_book(int c) {
    }

    public void show_all_books() {
        File result_all_books = new File("result_all_books.txt");
        try {
            FileOutputStream fos = new FileOutputStream(result_all_books);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }

    }

    public void show_borrowed_books() {
    }
}
