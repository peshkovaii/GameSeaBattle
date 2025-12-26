package game;

import game.controllers.InteractiveController;
import game.controllers.ObserverController;
import game.ui.BattleGameFrame;
import game.controllers.GameController;

import javax.swing.*;

/**
 * Главный класс игры "Морской бой с минами и подлодками".
 * Предоставляет точку входа в приложение и управление режимами игры.
 */
public class Main {

    /**
     * Основной метод запуска приложения.
     * Запрашивает у пользователя выбор режима игры и запускает соответствующий контроллер.
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        System.out.println("     МОРСКОЙ БОЙ    ");
        System.out.println("\nДоступные режимы:");
        System.out.println("1. Графический режим - введите 'Хочу играть!' или нажмите Enter");
        System.out.println("2. Консольный режим - введите 'Я наблюдатель'");
        System.out.print("\nВыберите режим: ");

        java.util.Scanner scanner = new java.util.Scanner(System.in);
        String command = scanner.nextLine().trim();

        if (command.equalsIgnoreCase("Хочу играть!")) {
            scanner.close();
            startGraphicalMode();

        } else if (command.equalsIgnoreCase("Я наблюдатель")) {
            scanner.close();
            startObserverMode();
        } else {
            System.out.println("Запускаю графический режим...");
            scanner.close();
            startGraphicalMode();
        }
    }

    /**
     * Запускает графический режим игры с интерфейсом Swing.
     * Создает основной контроллер игры и главное игровое окно.
     */
    private static void startGraphicalMode() {
        SwingUtilities.invokeLater(() -> {
            try {
                GameController controller = new GameController();
                new BattleGameFrame(controller);
                System.out.println("Графический интерфейс запущен!");
                System.out.println("\n ИНСТРУКЦИЯ");
                System.out.println("1. Расставьте свои корабли на поле и Нажмите 'Готово'");
                System.out.println("2. Кликайте по полю противника для атаки");
                System.out.println("3. Ваши корабли видны вам, корабли противника скрыты");
            } catch (Exception e) {
                System.err.println("Ошибка: " + e.getMessage());
                e.printStackTrace();
                startConsoleInteractiveMode();
            }
        });
    }

    /**
     * Запускает консольный интерактивный режим игры.
     * Взаимодействие с игроком происходит через текстовый интерфейс в консоли.
     */
    private static void startConsoleInteractiveMode() {
        System.out.println("\nЗапуск консольного режима...");
        InteractiveController interactive = new InteractiveController();
        interactive.startConsoleMode();
    }

    /**
     * Запускает режим наблюдателя, в котором два компьютерных игрока сражаются друг с другом.
     * Ход игры отображается в консоли в реальном времени.
     */
    private static void startObserverMode() {
        System.out.println("\nЗапуск режима наблюдения...");
        ObserverController observer = new ObserverController();
        observer.start();
    }
}
