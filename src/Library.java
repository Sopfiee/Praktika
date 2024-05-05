import java.io.*;
import java.sql.*;
import java.lang.String;
import java.util.Scanner;


public class Library {
    private static Connection connection;

    private String[] getUserData() throws IOException {
        File file = new File("src/resources/resources.txt");
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        StringBuilder params = new StringBuilder();
        String line = bufferedReader.readLine();

        while (line != null) {
            params.append(line.trim().split("--")[1].trim());
            params.append("--");
            line = bufferedReader.readLine();
        }

        String[] split_params = params.toString().trim().split("--");
        fileReader.close();
        return split_params;
    }

    private Connection getNewConnection() throws SQLException, IOException {
        String[] split_params = getUserData();
        return DriverManager.getConnection(split_params[0], split_params[1], split_params[2]);
    }

    private String[] stringToArray(String s) {
        String[] st = s.trim().split(",");
        for (int i = 0; i < st.length; i++) {
            st[i] = st[i].trim();
        }
        return st;
    }

    private void closeConnection() throws SQLException {
        connection.close();
    }

    public void addBook(String s) throws SQLException, IOException {
        String[] data = stringToArray(s);
        if (data.length != 4 || data[3].equals("0")) {
            System.out.println("ОШИБКА: неправильный формат");
            return;
        }

        connection = getNewConnection();
        Statement stmt = connection.createStatement();

        stmt.executeUpdate("DO $$ BEGIN IF NOT EXISTS (SELECT * FROM books WHERE  title = '" + data[0] + "' AND author = '" + data[1] + "' AND published = " + data[2] +
                ") THEN INSERT INTO books VALUES ('" + data[0] + "','" + data[1] + "'," + data[2] + "," + data[3] + "," + data[3] + "); ELSE UPDATE books SET quantity = quantity + " + data[3] +
                ", in_stock = in_stock + " + data[3] + " WHERE title = '" + data[0] + "' AND author ='" + data[1] + "' AND published = " + data[2] + "; END IF; END $$");
        System.out.println("Книга успешно добавлена!");
        closeConnection();
    }

    public void removeBook(String s) throws SQLException, IOException {
        String[] data = stringToArray(s);
        if (data.length != 4 || data[3].equals("0")) {
            System.out.println("ОШИБКА: неправильный формат");
            return;
        }

        connection = getNewConnection();
        Statement stmt = connection.createStatement();
        ResultSet request = stmt.executeQuery("SELECT in_stock FROM books WHERE title = '" + data[0] +
                "' AND author = '" + data[1] + "' AND published = " + data[2] + ";");

        if (!request.isBeforeFirst()) {
            System.out.println("Такой книги нет в библиотеке!");
        } else {
            request.next();
            if (request.getInt("in_stock") > Integer.parseInt(data[3])) {
                stmt.executeUpdate("UPDATE books SET quantity = quantity - " + data[3] + ", in_stock = in_stock - " +
                        data[3] + " WHERE title = '" + data[0] + "' AND author ='" + data[1] + "' AND published = " + data[2] + ";");
                System.out.println("Книга успешно удалена!");
            } else if (request.getInt("in_stock") == Integer.parseInt(data[3])) {
                stmt.executeUpdate("DELETE FROM books WHERE title = '" + data[0] + "' AND author = '" + data[1] +
                        "' AND published = " + data[2] + ";");
                System.out.println("Книга успешно удалена!");
            } else { //in_stock < data[3]
                System.out.println("Вы не можете удалить столько книг!");
            }
        }
        closeConnection();
    }

    public void giveBook(String s) throws SQLException, IOException {
        String[] data = stringToArray(s);
        if (data.length != 5 || data[3].equals("0")) {
            System.out.println("ОШИБКА: неправильный формат");
            return;
        }

        connection = getNewConnection();
        Statement stmt = connection.createStatement();
        ResultSet request = stmt.executeQuery("SELECT * FROM books WHERE title = '" + data[0] +
                "' AND author = '" + data[1] + "' AND published = " + data[2] + ";");

        if (!request.isBeforeFirst()) {
            System.out.println("Такой книги нет в библиотеке!");
        } else {
            request.next();
            if (request.getInt("in_stock") < Integer.parseInt(data[3])) {
                System.out.println("Вы не можете выдать столько книг!");
            } else {
                stmt.executeUpdate("UPDATE books SET in_stock = in_stock - " + data[3] + " WHERE title = '" +
                        data[0] + "' AND author ='" + data[1] + "' AND published = " + data[2] + ";");
                stmt.executeUpdate("DO $$ BEGIN IF NOT EXISTS (SELECT * FROM borrowed_books WHERE  title = '" +
                        data[0] + "' AND author = '" + data[1] + "' AND published = " + data[2] + " AND reader = '" +
                        data[4] + "') THEN INSERT INTO borrowed_books VALUES ('" + data[4] + "','" + data[0] + "','" +
                        data[1] + "'," + data[2] + "," + data[3] + "); ELSE UPDATE borrowed_books SET quantity = quantity +" +
                        " " + data[3] + " WHERE title = '" + data[0] + "' AND author ='" + data[1] + "' AND published = " +
                        data[2] + " AND reader = '" + data[4] + "'; END IF; END $$");
                System.out.println("Книга успешно выдана!");
            }
        }
        closeConnection();
    }

    public void returnBook(String s) throws SQLException, IOException {
        String[] data = this.stringToArray(s);
        if (data.length != 5 || data[3].equals("0")) {
            System.out.println("ОШИБКА: неправильный формат");
            return;
        }

        connection = getNewConnection();
        Statement stmt = connection.createStatement();
        ResultSet request = stmt.executeQuery("SELECT * FROM borrowed_books WHERE title = '" + data[0] +
                "' AND author = '" + data[1] + "' AND published = " + data[2] + " AND reader = '" + data[4] + "';");

        if (!request.isBeforeFirst()) {
            System.out.println("Неправильные данные!");
        } else {
            request.next();
            int q = request.getInt("quantity");
            if (q < Integer.parseInt(data[3])) {
                System.out.println("Вы не можете вернуть столько книг!");
            } else {
                stmt.executeUpdate("UPDATE books SET in_stock = in_stock + " + data[3] + " WHERE title = '" +
                        data[0] + "' AND author ='" + data[1] + "' AND published = " + data[2] + ";");
                if (q == Integer.parseInt(data[3])) {
                    stmt.executeUpdate("DELETE FROM borrowed_books WHERE reader='" + data[4] + "' AND title ='" +
                            data[0] + "' AND author ='" + data[1] + "'AND published = " + data[2] + ";");
                } else {
                    stmt.executeUpdate("UPDATE borrowed_books SET quantity = quantity - " + data[3] +
                            " WHERE reader ='" + data[4] + "' AND title ='" + data[0] + "' AND author ='" + data[1] +
                            "' AND published =" + data[2] + ";");
                }
                System.out.println("Книга успешно возвращена!");
            }
        }
        closeConnection();
    }

    public void searchBook(int search) throws SQLException, IOException {
        Scanner scanner = new Scanner(System.in);
        String data, need_data;
        File file = new File("result_last_search.txt");

        connection = getNewConnection();
        Statement stmt = connection.createStatement();
        ResultSet request;

        switch (search) {
            case 1: {
                System.out.println("Введите название книги:");
                need_data = "title";
                break;
            }
            case 2: {
                System.out.println("Введите автора книги:");
                need_data = "author";
                break;
            }
            case 3: {
                System.out.println("Введите год издания книги:");
                need_data = "published";
                break;
            }
            case 4: {
                System.out.println("Введите ФИО читателя:");
                need_data = "reader";
                break;
            }
            case 5: {
                System.out.println("Введите нужное количество:");
                need_data = "quantity";
                break;
            }
            case 6: {
                System.out.println("Введите нужное количество:");
                need_data = "in_stock";
                break;
            }
            default: {
                System.out.println("Неправильные данные!");
                return;
            }
        }

        data = scanner.nextLine();
        if (search == 4) {
            request = stmt.executeQuery("SELECT * FROM borrowed_books WHERE " + need_data + " = '" + data + "';");
        } else if (search == 1 || search == 2) {
            request = stmt.executeQuery("SELECT * FROM books WHERE " + need_data + " = '" + data + "';");
        } else {
            request = stmt.executeQuery("SELECT * FROM books WHERE " + need_data + " = " + data + ";");
        }

        if (!request.isBeforeFirst()) {
            System.out.println("Ничего не найдено!");
        } else {
            writeFile(file, 2, search, request);
        }

        closeConnection();
    }

    public void showAllBooks() throws SQLException, IOException {
        File file = new File("result_all_books.txt");
        connection = getNewConnection();
        Statement stmt = connection.createStatement();
        ResultSet request = stmt.executeQuery("SELECT * FROM books WHERE in_stock <> 0;");
        writeFile(file, 0, 0, request);
        closeConnection();
    }

    public void showBorrowedBooks() throws SQLException, IOException {
        File file = new File("result_borrowed_books.txt");
        connection = getNewConnection();
        Statement stmt = connection.createStatement();
        ResultSet request = stmt.executeQuery("SELECT * FROM borrowed_books;");
        writeFile(file, 1, 0, request);
        closeConnection();
    }

    private void writeFile(File file, int criteria, int search, ResultSet request) throws IOException, SQLException {
        FileWriter fileWriter = new FileWriter(file);

        while (request.next()) {
            String title = request.getString("title");
            String author = request.getString("author");
            int published = request.getInt("published");
            int quantity = request.getInt("quantity");
            if (criteria == 1 || (criteria == 2 && search == 4)) {
                String reader = request.getString("reader");
                fileWriter.write(title + ", " + author + ", " + published + ", " + quantity + ", " + reader + "\n");
            } else if (criteria == 0 || (criteria == 2 && search != 4)) {
                int in_stock = request.getInt("in_stock");
                fileWriter.write(title + ", " + author + ", " + published + ", " + quantity + ", " + in_stock + "\n");
            }
        }

        fileWriter.flush();
        fileWriter.close();
        System.out.println("Список найденных книг получен!");
    }
}
