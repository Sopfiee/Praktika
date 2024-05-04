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

        String line = bufferedReader.readLine();
        String params = "";

        while (line != null) {
            params = params + line.trim().split("--")[1].trim() + "--";
            line = bufferedReader.readLine();
        }
        String[] split_params = params.trim().split("--");
        return split_params;
    }

    private Connection getNewConnection() throws SQLException, IOException {
        String[] split_params = this.getUserData();
        return DriverManager.getConnection(split_params[0], split_params[1], split_params[2]);
    }

    private String[] stringToArray(String s) {
        String[] st = s.trim().split(","); // название, автор, год издания, {количество, читатель}
        for (int i = 0; i < st.length; i++) { //убираем лишние пробелы
            st[i] = st[i].trim();
        }
        return st;
    }

    private void closeConnection() throws SQLException {
        connection.close();
    }

    public void addBook(String s) throws SQLException, IOException {
        connection = getNewConnection();
        Statement stmt = connection.createStatement();
        String[] data = this.stringToArray(s);

        if (data.length != 4 || data[3].equals("0")) {
            System.out.println("ОШИБКА: неправильный формат");
            return;
        }

        stmt.executeUpdate("DO $$ BEGIN IF NOT EXISTS (SELECT * FROM books WHERE  title = '" + data[0] + "' AND author = '" + data[1] + "' AND published = " + data[2] +
                ") THEN INSERT INTO books VALUES ('" + data[0] + "','" + data[1] + "'," + data[2] + "," + data[3] + "," + data[3] + "); ELSE UPDATE books SET quantity = quantity + " + data[3] +
                ", in_stock = in_stock + " + data[3] + " WHERE title = '" + data[0] + "' AND author ='" + data[1] + "' AND published = " + data[2] + "; END IF; END $$");
        System.out.println("Книга успешно добавлена!");
        this.closeConnection();
    }

    public void removeBook(String s) throws SQLException, IOException {
        connection = getNewConnection();
        Statement stmt = connection.createStatement();
        String[] data = this.stringToArray(s);

        if (data.length != 4 || data[3].equals("0")) {
            System.out.println("ОШИБКА: неправильный формат");
            return;
        }

        ResultSet request = stmt.executeQuery("SELECT in_stock FROM books WHERE title = '" + data[0] + "' AND author = '" + data[1] + "' AND published = " + data[2] + ";");

        if (!request.isBeforeFirst()) { //если нет строки
            System.out.println("Такой книги нет в библиотеке!");
        } else {
            request.next();
            //сравниваем введённое количество и сколько есть в наличии
            if (request.getInt("in_stock") > Integer.parseInt(data[3])) {
                stmt.executeUpdate("UPDATE books SET quantity = quantity - " + data[3] + ", in_stock = in_stock - " + data[3] + " WHERE title = '" + data[0] + "' AND author ='" + data[1] + "' AND published = " + data[2] + ";");
                System.out.println("Книга успешно удалена!");
            } else if (request.getInt("in_stock") == Integer.parseInt(data[3])) {
                stmt.executeUpdate("DELETE FROM books WHERE title = '" + data[0] + "' AND author = '" + data[1] + "' AND published = " + data[2] + ";");
                System.out.println("Книга успешно удалена!");
            } else { //in_stock < data[3]
                System.out.println("Вы не можете удалить столько книг!");
            }
        }

        this.closeConnection();
    }

    public void giveBook(String s) throws SQLException, IOException {
        connection = getNewConnection();
        Statement stmt = connection.createStatement();
        String[] data = this.stringToArray(s);

        if (data.length != 5 || data[3].equals("0")) {
            System.out.println("ОШИБКА: неправильный формат");
            return;
        }

        ResultSet request = stmt.executeQuery("SELECT * FROM books WHERE title = '" + data[0] + "' AND author = '" + data[1] + "' AND published = " + data[2] + ";");

        if (!request.isBeforeFirst()) { //если нет строки
            System.out.println("Такой книги нет в библиотеке!");
        } else {
            request.next();
            // смотрим сколько можно выдать, сравнивая введённое кол-во и в наличии
            if (request.getInt("in_stock") < Integer.parseInt(data[3])) {
                System.out.println("Вы не можете выдать столько книг!");
            } else {
                stmt.executeUpdate("UPDATE books SET in_stock = in_stock - " + data[3] + " WHERE title = '" + data[0] + "' AND author ='" + data[1] + "' AND published = " + data[2] + ";");
                stmt.executeUpdate("DO $$ BEGIN IF NOT EXISTS (SELECT * FROM borrowed_books WHERE  title = '" + data[0] + "' AND author = '" + data[1] + "' AND published = " + data[2] + " AND reader = '" + data[4] +
                        "') THEN INSERT INTO borrowed_books VALUES ('" + data[4] + "','" + data[0] + "','" + data[1] + "'," + data[2] + "," + data[3] + "); ELSE UPDATE borrowed_books SET quantity = quantity + " + data[3] +
                        " WHERE title = '" + data[0] + "' AND author ='" + data[1] + "' AND published = " + data[2] + " AND reader = '" + data[4] + "'; END IF; END $$");
                System.out.println("Книга успешно выдана!");
            }
        }

        this.closeConnection();
    }

    public void returnBook(String s) throws SQLException, IOException { //
        connection = getNewConnection();
        Statement stmt = connection.createStatement();
        String[] data = this.stringToArray(s);

        if (data.length != 5 || data[3].equals("0")) {
            System.out.println("ОШИБКА: неправильный формат");
            return;
        }

        ResultSet request = stmt.executeQuery("SELECT * FROM borrowed_books WHERE title = '" + data[0] + "' AND author = '" + data[1] + "' AND published = " + data[2] + " AND reader = '" + data[4] + "';");

        if (!request.isBeforeFirst()) { //если нет строки
            System.out.println("Неправильные данные!");
        } else {
            request.next();
            int q = request.getInt("quantity");
            if (q < Integer.parseInt(data[3])) {
                System.out.println("Вы не можете вернуть столько книг!");
            } else {
                stmt.executeUpdate("UPDATE books SET in_stock = in_stock + " + data[3] + " WHERE title = '" + data[0] + "' AND author ='" + data[1] + "' AND published = " + data[2] + ";");
                if (q == Integer.parseInt(data[3])) {
                    stmt.executeUpdate("DELETE FROM borrowed_books WHERE reader='" + data[4] + "' AND title ='" + data[0] + "' AND author ='" + data[1] + "'AND published = " + data[2] + ";");
                } else {
                    stmt.executeUpdate("UPDATE borrowed_books SET quantity = quantity - " + data[3] + " WHERE reader ='" + data[4] + "' AND title ='" + data[0] + "' AND author ='" + data[1] + "' AND published =" + data[2] + ";");
                }
                System.out.println("Книга успешно возвращена!");
            }
        }

        this.closeConnection();
    }

    public void searchBook(int search) throws SQLException, IOException {
        Scanner scanner = new Scanner(System.in); //для поиска
        String data, need_data; //для поиска: введённая строка и параметр по которому осуществляется поиск
        File file = new File("result_last_search.txt");

        connection = this.getNewConnection();
        Statement stmt = connection.createStatement();
        ResultSet request;

        switch (search) {
            case 1: { // по названию
                System.out.println("Введите название книги:");
                need_data = "title";
                break;
            }
            case 2: { // по автору
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

        if (!request.isBeforeFirst()) { //если нет строки
            System.out.println("Ничего не найдено!");
        } else {
            this.writeFile(file, 2, search, request);
        }

        this.closeConnection();
    }

    public void showAllBooks() throws SQLException, IOException {
        File file = new File("result_all_books.txt");
        connection = this.getNewConnection();
        Statement stmt = connection.createStatement();
        ResultSet request = stmt.executeQuery("SELECT * FROM books WHERE in_stock <> 0;");

        this.writeFile(file, 0, 0, request);

        this.closeConnection();
    }

    public void showBorrowedBooks() throws SQLException, IOException {
        File file = new File("result_borrowed_books.txt");
        connection = this.getNewConnection();
        Statement stmt = connection.createStatement();
        ResultSet request = stmt.executeQuery("SELECT * FROM borrowed_books;");

        this.writeFile(file, 1, 0, request);

        this.closeConnection();
    }

    private void writeToFile(int k, int search) throws SQLException, IOException { // k: 0 - все книги, 1 - у читателей, 2 - поиск, search: критерий поиска
        String title_file = "result_last_search.txt";
        Scanner scanner = new Scanner(System.in); //для поиска
        String st, n_st; //для поиска: введённая строка и параметр по которому осуществляется поиск

        //название файла
        if (k == 0) {
            title_file = "result_all_books.txt";
        }
        if (k == 1) {
            title_file = "result_borrowed_books.txt";
        }
        File result_file = new File(title_file);


        connection = this.getNewConnection();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM borrowed_books;"); // k==1 - книги у читателей

        if (k == 0) { //книги в наличии
            rs = stmt.executeQuery("SELECT * FROM books WHERE in_stock <> 0;");
        }
        if (k == 2) { //поиск
            switch (search) {
                case 1: { // по названию
                    System.out.println("Введите название книги:");
                    n_st = "title";
                    break;
                }
                case 2: { // по автору
                    System.out.println("Введите автора книги:");
                    n_st = "author";
                    break;
                }
                case 3: {
                    System.out.println("Введите год издания книги:");
                    n_st = "published";
                    break;
                }
                case 4: {
                    System.out.println("Введите ФИО читателя:");
                    n_st = "reader";
                    break;
                }
                case 5: {
                    System.out.println("Введите нужное количество:");
                    n_st = "quantity";
                    break;
                }
                case 6: {
                    System.out.println("Введите нужное количество:");
                    n_st = "in_stock";
                    break;
                }
                default: {
                    System.out.println("Неправильные данные!");
                    return;
                }
            }

            //запросы по которым осуществляется поиск
            st = scanner.nextLine();
            if (search == 4) {
                rs = stmt.executeQuery("SELECT * FROM borrowed_books WHERE " + n_st + " = '" + st + "';");
            } else if (search == 1 || search == 2) {
                rs = stmt.executeQuery("SELECT * FROM books WHERE " + n_st + " = '" + st + "';");
            } else {
                rs = stmt.executeQuery("SELECT * FROM books WHERE " + n_st + " = " + st + ";");
            }


        }
        //запись в файл
        try (FileWriter fw = new FileWriter(result_file)) {
            // построчно
            while (rs.next()) {
                String title = rs.getString("title");
                String author = rs.getString("author");
                int published = rs.getInt("published");
                int quantity = rs.getInt("quantity");
                if (k == 1 || (k == 2 && search == 4)) {
                    String reader = rs.getString("reader");
                    fw.write(title + ", " + author + ", " + published + ", " + quantity + ", " + reader + "\n");
                } else if (k == 0 || (k == 2 && search != 4)) {
                    int in_stock = rs.getInt("in_stock");
                    fw.write(title + ", " + author + ", " + published + ", " + quantity + ", " + in_stock + "\n");
                }

            }
            fw.flush();
            System.out.println("Список найденных книг получен!");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    private void writeFile(File file, int criteria, int search, ResultSet request) throws IOException, SQLException {
        FileWriter fileWriter = new FileWriter(file);
        // построчно
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
        System.out.println("Список найденных книг получен!");
    }
}
