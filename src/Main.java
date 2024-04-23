import java.util.Scanner;
import java.io.*;
import java.lang.String;


public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Добро пожаловать!");
        boolean t = true;
        Library library = new Library();
        String s;
        while (t) {
            System.out.println("Что вы хотите сделать?" +
                    "\n1.Добавить книгу;" +
                    "\n2.Удалить книгу;" +
                    "\n3.Выдать книгу;" +
                    "\n4.Вернуть книгу;" +
                    "\n5.Поиск книги;" +
                    "\n6.Отчёт по всем книгам;" +
                    "\n7.Отчёт по взятым книгам;" +
                    "\n8.Выйти.");
            switch (sc.nextInt()) {
                case 1: {
                    System.out.println("Введите название книги, автора, год издания и количество через запятую:");
                    s = sc.nextLine();
                    library.add_book(s);
                    System.out.println("Книга успешно добавлена!");
                    break;
                }
                case 2: {
                    System.out.println("Введите название книги, автора, год издания и количество через запятую:");
                    s = sc.nextLine();
                    library.remove_book(s);
                    System.out.println("Книга успешно удалена!");
                    break;
                }
                case 3: {
                    System.out.println("Введите название книги, автора, год издания, количество и получателя через запятую:");
                    s = sc.nextLine();
                    library.give_book(s);
                    System.out.println("Книга успешно выдана!");
                    break;
                }
                case 4: {
                    System.out.println("Введите название книги, автора, год издания, количество и читателя через запятую:");
                    s = sc.nextLine();
                    library.return_book(s);
                    System.out.println("Книга успешно возвращена!");
                    break;
                }
                case 5: {
                    System.out.println("Поиск по:\n1.Название книги;\n2.Автор;\n3.Год издания;" +
                            "\n4.Читатель;\n5.Количество общее;\n6.Количество в наличии.");
                    int c = sc.nextInt();
                    library.search_book(c);
                    System.out.println("Список найденных книг получен!");
                    break;
                }
                case 6: {
                    library.show_all_books();
                    System.out.println("Список всех книг получен!");
                    break;
                }
                case 7: {
                    library.show_borrowed_books();
                    System.out.println("Список книг у читателей получен!");
                    break;
                }
                case 8: {
                    t = false;
                    System.out.println("Всего хорошего!");
                    break;
                }
            }
        }
    }
}