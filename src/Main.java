import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;
import java.lang.String;

public class Main {
    public static void main(String[] args) throws SQLException, IOException {
        Scanner scanner = new Scanner(System.in);
        boolean is_work = true;
        Library library = new Library();
        String data;
        System.out.println("Добро пожаловать!");
        while (is_work) {
            System.out.println("Что вы хотите сделать?" +
                    "\n1.Добавить книгу;" +
                    "\n2.Удалить книгу;" +
                    "\n3.Выдать книгу;" +
                    "\n4.Вернуть книгу;" +
                    "\n5.Поиск книги;" +
                    "\n6.Отчёт по всем книгам в наличии;" +
                    "\n7.Отчёт по взятым книгам;" +
                    "\n8.Выйти.");
            switch (scanner.nextInt()) {
                case 1: {
                    System.out.println("Введите название книги, автора, год издания и количество через запятую:");
                    scanner.nextLine();
                    data = scanner.nextLine();
                    library.addBook(data);
                    break;
                }
                case 2: {
                    System.out.println("Введите название книги, автора, год издания и количество через запятую:");
                    scanner.nextLine();
                    data = scanner.nextLine();
                    library.removeBook(data);
                    break;
                }
                case 3: {
                    System.out.println("Введите название книги, автора, год издания, количество и получателя через запятую:");
                    scanner.nextLine();
                    data = scanner.nextLine();
                    library.giveBook(data);
                    break;
                }
                case 4: {
                    System.out.println("Введите название книги, автора, год издания, количество и читателя через запятую:");
                    scanner.nextLine();
                    data = scanner.nextLine();
                    library.returnBook(data);
                    break;
                }
                case 5: {
                    System.out.println("Поиск по:\n1.Название книги;\n2.Автор;\n3.Год издания;" +
                            "\n4.Читатель;\n5.Количество общее;\n6.Количество в наличии.");
                    int c = scanner.nextInt();
                    scanner.nextLine();
                    library.searchBook(c);
                    break;
                }
                case 6: {
                    library.showAllBooks();
                    break;
                }
                case 7: {
                    library.showBorrowedBooks();
                    break;
                }
                case 8: {
                    is_work = false;
                    System.out.println("Всего хорошего!");
                    break;
                }
                default:{
                    System.out.println("Попробуйте ещё раз!");
                    break;
                }
            }
        }
    }
}