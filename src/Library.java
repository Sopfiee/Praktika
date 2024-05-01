import java.io.*;
import java.sql.*;
import java.lang.String;
import java.util.Scanner;

public class Library {
    Connection con;

    private String[] str_array(String s) {
        String[] st = s.trim().split(","); // название, автор, год издания, {количество, читатель}
        for (int i = 0; i < st.length; i++) { //убираем лишние пробелы
            st[i] = st[i].trim();
        }
        return st;
    }

    private void operation_with_book(int o, String s) { // o: 0 - добавить, 1- удалить, 2- выдать, 3- вернуть
        String[] st = this.str_array(s);

        int l = 5;
        if (o < 2)
            l = 4;
        if (st.length != l || st[3].equals("0")) {
            System.out.println("ОШИБКА: неправильный формат");
            return;
        }

        try {
            con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/library", "postgres", "SonPost24");
            Statement stmt = con.createStatement();
            //rs по умолчанию: для удаления книги (проверка есть ли в базе книга)
            ResultSet rs = stmt.executeQuery("SELECT in_stock FROM books WHERE title = '" + st[0] + "' AND author = '" + st[1] + "' AND published = " + st[2] + ";");

            if (o == 0) { //добавление
                //если нет строки в таблице то insert, если есть, то update количество
                stmt.executeUpdate("DO $$ BEGIN IF NOT EXISTS (SELECT * FROM books WHERE  title = '" + st[0] + "' AND author = '" + st[1] + "' AND published = " + st[2] +
                        ") THEN INSERT INTO books VALUES ('" + st[0] + "','" + st[1] + "'," + st[2] + "," + st[3] + "," + st[3] + "); ELSE UPDATE books SET quantity = quantity + " + st[3] +
                        ", in_stock = in_stock + " + st[3] + " WHERE title = '" + st[0] + "' AND author ='" + st[1] + "' AND published = " + st[2] + "; END IF; END $$");
                System.out.println("Книга успешно добавлена!");
            }
            if (o == 1) { //удаление
                if (!rs.isBeforeFirst()) { //если нет строки
                    System.out.println("Такой книги нет в библиотеке!");
                } else {
                    rs.next();
                    //сравниваем введённое количество и сколько есть в наличии
                    if (rs.getInt("in_stock") > Integer.parseInt(st[3])) {
                        stmt.executeUpdate("UPDATE books SET quantity = quantity - " + st[3] + ", in_stock = in_stock - " + st[3] + " WHERE title = '" + st[0] + "' AND author ='" + st[1] + "' AND published = " + st[2] + ";");
                        System.out.println("Книга успешно удалена!");
                    } else if (rs.getInt("in_stock") == Integer.parseInt(st[3])) {
                        stmt.executeUpdate("DELETE FROM books WHERE title = '" + st[0] + "' AND author = '" + st[1] + "' AND published = " + st[2] + ";");
                        System.out.println("Книга успешно удалена!");
                    } else { //in_stock < st[3]
                        System.out.println("Вы не можете удалить столько книг!");
                    }
                }
            }
            if (o == 2) { // выдача
                // проверяем есть ли в библиотеке такая книга
                rs = stmt.executeQuery("SELECT * FROM books WHERE title = '" + st[0] + "' AND author = '" + st[1] + "' AND published = " + st[2] + ";");

                if (!rs.isBeforeFirst()) { //если нет строки
                    System.out.println("Такой книги нет в библиотеке!");
                } else {
                    rs.next();
                    // смотрим сколько можно выдать, сравнивая введённое кол-во и в наличии
                    if (rs.getInt("in_stock") < Integer.parseInt(st[3])) {
                        System.out.println("Вы не можете выдать столько книг!");
                    } else {
                        stmt.executeUpdate("UPDATE books SET in_stock = in_stock - " + st[3] + " WHERE title = '" + st[0] + "' AND author ='" + st[1] + "' AND published = " + st[2] + ";");
                        stmt.executeUpdate("DO $$ BEGIN IF NOT EXISTS (SELECT * FROM borrowed_books WHERE  title = '" + st[0] + "' AND author = '" + st[1] + "' AND published = " + st[2] + " AND reader = '" + st[4] +
                                "') THEN INSERT INTO borrowed_books VALUES ('" + st[4] + "','" + st[0] + "','" + st[1] + "'," + st[2] + "," + st[3] + "); ELSE UPDATE borrowed_books SET quantity = quantity + " + st[3] +
                                " WHERE title = '" + st[0] + "' AND author ='" + st[1] + "' AND published = " + st[2] + " AND reader = '" + st[4] + "'; END IF; END $$");
                        System.out.println("Книга успешно выдана!");
                    }
                }
            }
            if (o == 3) { //возврат
                //проверка есть ли такая книга у читателя (и есть ли вообще читатель)
                rs = stmt.executeQuery("SELECT * FROM borrowed_books WHERE title = '" + st[0] + "' AND author = '" + st[1] + "' AND published = " + st[2] + " AND reader = '" + st[4] + "';");

                if (!rs.isBeforeFirst()) { //если нет строки
                    System.out.println("Неправильные данные!");
                } else {
                    rs.next();
                    int q = rs.getInt("quantity");
                    if (q < Integer.parseInt(st[3])) {
                        System.out.println("Вы не можете вернуть столько книг!");
                    } else {
                        stmt.executeUpdate("UPDATE books SET in_stock = in_stock + " + st[3] + " WHERE title = '" + st[0] + "' AND author ='" + st[1] + "' AND published = " + st[2] + ";");
                        if (q == Integer.parseInt(st[3])) {
                            stmt.executeUpdate("DELETE FROM borrowed_books WHERE reader='" + st[4] + "' AND title ='" + st[0] + "' AND author ='" + st[1] + "'AND published = " + st[2] + ";");
                        } else {
                            stmt.executeUpdate("UPDATE borrowed_books SET quantity = quantity - " + st[3] + " WHERE reader ='" + st[4] + "' AND title ='" + st[0] + "' AND author ='" + st[1] + "' AND published =" + st[2] + ";");
                        }
                        System.out.println("Книга успешно возвращена!");
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void add_book(String s) {
        this.operation_with_book(0, s);
    }

    public void remove_book(String s) {
        this.operation_with_book(1, s);
    }

    public void give_book(String s) {
        this.operation_with_book(2, s);
    }

    public void return_book(String s) { //
        this.operation_with_book(3, s);
    }

    public void search_book(int c) {
        this.write_to_file(2, c);
    }

    public void show_all_books() {
        this.write_to_file(0, 0);
    }

    public void show_borrowed_books() {
        this.write_to_file(1, 0);
    }

    private void write_to_file(int k, int search) { // k: 0 - все книги, 1 - у читателей, 2 - поиск, search: критерий поиска
        String title_file = "result_last_search.txt";
        Scanner s = new Scanner(System.in); //для поиска
        String st, n_st; //для поиска: введённая строка и параметр по которому осуществляется поиск

        //название файла
        if (k == 0) {
            title_file = "result_all_books.txt";
        }
        if (k == 1) {
            title_file = "result_borrowed_books.txt";
        }
        File result_file = new File(title_file);

        try {
            con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/library", "postgres", "SonPost24");
            Statement stmt = con.createStatement();
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
                st = s.nextLine();
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
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
