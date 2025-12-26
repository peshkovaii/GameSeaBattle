package game.controllers;

import game.models.*;
import game.config.GameConfig;
import game.utils.CoordinateConverter;
import javax.swing.*;
import java.awt.*;
import java.util.Scanner;

/**
 * Контроллер для консольного режима игры "Морской бой".
 * Предоставляет текстовый интерфейс для игры через командную строку.
 * Управляет игровым процессом, обрабатывает ввод пользователя и отображает игровое состояние.
 *
 * Этот класс предназначен для отладки и тестирования игровой логики без графического интерфейса.
 */
public class InteractiveController {
    /**
     * Основной контроллер игры, управляющий игровой логикой.
     */
    private GameController gameController;

    /**
     * Сканер для чтения ввода пользователя из консоли.
     */
    private Scanner scanner;

    /**
     * Флаг, указывающий, запущена ли игра в консольном режиме.
     */
    private boolean gameRunning;

    /**
     * Создает новый контроллер для консольного режима игры.
     * Инициализирует игровой контроллер и сканер для ввода пользователя.
     */
    public InteractiveController() {
        this.gameController = new GameController();
        this.scanner = new Scanner(System.in);
        this.gameRunning = true;
    }

    /**
     * Запускает консольный режим игры.
     * Отображает приветственное сообщение, настраивает игру и начинает игровой цикл.
     * Пользователь может выйти из игры в любой момент, введя "выход".
     */
    public void startConsoleMode() {
        System.out.println("=== КОНСОЛЬНЫЙ РЕЖИМ ИГРЫ ===");
        System.out.println("В этом режиме вы можете играть через консоль.");
        System.out.println("Для выхода введите 'выход'.");
        System.out.println("Формат ввода координат: A1, B5, C10 и т.д.");

        setupGame();
        playGame();
    }

    /**
     * Настраивает начальное состояние игры.
     * Автоматически расставляет корабли на поле игрока и отображает начальное поле.
     */
    private void setupGame() {
        System.out.println("\n=== РАССТАНОВКА КОРАБЛЕЙ ===");

        System.out.println("Корабли расставлены автоматически.");
        System.out.println("Ваше поле:");
        printBoard(gameController.getPlayerBoard(), true);
    }

    /**
     * Запускает основной игровой цикл.
     * Чередует ходы игрока и бота до завершения игры или выхода пользователя.
     * После каждого хода отображает текущее состояние игры.
     */
    private void playGame() {
        gameController.startGame();

        while (gameRunning && !gameController.isGameOver()) {
            if (gameController.isPlayerTurn()) {
                playerTurn();
            } else {
                botTurn();
            }


            showGameStatus();
        }

        endGame();
    }

    /**
     * Обрабатывает ход игрока.
     * Запрашивает координаты для атаки у пользователя, парсит ввод и выполняет атаку.
     * Если пользователь вводит "выход", игра завершается.
     * Отображает результат атаки (попадание, промах, мина и т.д.).
     */
    private void playerTurn() {
        System.out.println(" ВАШ ХОД");
        System.out.print("Введите координаты для атаки (например, A5): ");

        String input = scanner.nextLine().trim();

        if (input.equalsIgnoreCase("выход")) {
            gameRunning = false;
            return;
        }

        try {
            int[] coordinates = parseCoordinates(input);
            if (coordinates != null) {
                int row = coordinates[0];
                int col = coordinates[1];

                boolean validMove = gameController.playerAttack(row, col);

                if (validMove) {
                    System.out.println("Вы атаковали клетку " + input);

                    // Показываем результат атаки
                    Cell cell = gameController.getBotBoard().getCell(row, col);
                    if (cell != null) {
                        switch (cell.getState()) {
                            case HIT:
                                if (cell.hasShip() && cell.getShip().isSunk()) {
                                    System.out.println("Вы потопили " + cell.getShip().getName() + "!");
                                } else {
                                    System.out.println("Попадание!");
                                }
                                break;
                            case MISS:
                                System.out.println("Промах!");
                                break;
                            case MINE:
                                System.out.println("Вы попали на мину! Противник получает координату вашего корабля.");
                                break;
                            case MINESWEEPER:
                                System.out.println("Вы попали на минный тральщик! Противник получает координату вашей мины.");
                                break;
                        }
                    }
                } else {
                    System.out.println("Невозможно атаковать эту клетку. Попробуйте снова.");
                }
            } else {
                System.out.println("Неверный формат координат. Используйте формат A1, B5, C10 и т.д.");
            }
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    /**
     * Обрабатывает ход бота.
     * Имитирует задержку для реалистичности и уведомляет пользователя о завершении хода бота.
     * Реальная атака выполняется через игровой контроллер.
     */
    private void botTurn() {
        System.out.println("\nХОД ПРОТИВНИКА");
        System.out.println("Противник думает...");

        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Противник совершил ход.");


        System.out.println("Проверьте свое поле на наличие попаданий.");
    }

    /**
     * Отображает текущее состояние игры.
     * Показывает оба игровых поля, текущий счетчик ходов, время и чей сейчас ход.
     */
    private void showGameStatus() {
        System.out.println("\nТЕКУЩЕЕ СОСТОЯНИЕ");

        System.out.println("Ваше поле:");
        printBoard(gameController.getPlayerBoard(), false);

        System.out.println("\nПоле противника:");
        printBoard(gameController.getBotBoard(), false);

        System.out.println("\nХод: " + gameController.getTurnCount());
        System.out.println("Время: " + gameController.getTimerManager().getFormattedTime());

        if (gameController.isPlayerTurn()) {
            System.out.println("Сейчас ваш ход.");
        } else {
            System.out.println("Сейчас ход противника.");
        }
    }

    /**
     * Выводит игровое поле в консоль в текстовом формате.
     * Может отображать поле с кораблями (при расстановке) или только результаты атак (во время игры).
     *
     * @param board игровое поле для отображения
     * @param showShips true для показа кораблей, false для показа только результатов атак
     */
    private void printBoard(Board board, boolean showShips) {
        int size = board.getSize();

        System.out.print("  ");
        for (int col = 0; col < size; col++) {
            System.out.print(CoordinateConverter.toLetterCoordinate(col) + " ");
        }
        System.out.println();

        for (int row = 0; row < size; row++) {
            System.out.print((row + 1) + " ");
            if (row < 9) System.out.print(" ");

            for (int col = 0; col < size; col++) {
                Cell cell = board.getCell(row, col);
                char symbol = getCellSymbol(cell, showShips);
                System.out.print(symbol + " ");
            }
            System.out.println();
        }
    }

    /**
     * Возвращает символ для отображения состояния клетки в консоли.
     * Использует разные наборы символов в зависимости от того, нужно ли показывать корабли.
     *
     * @param cell клетка для отображения
     * @param showShips true для показа кораблей, false для показа только результатов атак
     * @return символ, представляющий состояние клетки
     */
    private char getCellSymbol(Cell cell, boolean showShips) {
        if (cell == null) return '?';

        CellState state = cell.getState();

        if (showShips) {
            if (cell.hasShip()) {
                if (cell.getShip() instanceof Submarine) return 'U';
                return 'S';
            }
            if (cell.hasMine()) return '*';
            if (cell.hasMinesweeper()) return 'T';
            return '.';
        }

        switch (state) {
            case HIT:
                return 'X';
            case MISS:
                return 'O';
            case DESTROYED:
                return '#';
            case REVEALED:
                return 'R';
            default:
                return '.';
        }
    }

    /**
     * Парсит строку с координатами в формате "A1" в числовые индексы строки и столбца.
     * Поддерживает координаты от A1 до Z26 в зависимости от размера поля.
     *
     * @param input строка с координатами (например, "A5", "C10")
     * @return массив из двух элементов [row, col] или null при неверном формате
     */
    private int[] parseCoordinates(String input) {
        if (input == null || input.length() < 2) return null;

        try {
            String letterPart = input.substring(0, 1).toUpperCase();
            String numberPart = input.substring(1);


            int col = CoordinateConverter.fromLetterCoordinate(letterPart);
            if (col == -1) return null;

            int row = Integer.parseInt(numberPart) - 1;

            int boardSize = GameConfig.getBoardSize();
            if (row >= 0 && row < boardSize && col >= 0 && col < boardSize) {
                return new int[]{row, col};
            }
        } catch (NumberFormatException e) {
            return null;
        }

        return null;
    }

    /**
     * Завершает игру и отображает итоговую статистику.
     * Определяет победителя, показывает количество ходов и затраченное время.
     * Закрывает сканер для ввода пользователя.
     */
    private void endGame() {
        if (!gameRunning) {
            System.out.println("\nИгра прервана пользователем.");
            return;
        }

        System.out.println("\n ИГРА ОКОНЧЕНА");

        String winner = gameController.getWinner();
        if ("Player".equals(winner)) {
            System.out.println("ПОЗДРАВЛЯЕМ! ВЫ ПОБЕДИЛИ!");
        } else if ("Bot".equals(winner)) {
            System.out.println("Вы проиграли. Попробуйте еще раз!");
        } else if ("Timeout".equals(winner)) {
            System.out.println("Время вышло! Ничья.");
        } else {
            System.out.println("Игра завершена. Ничья.");
        }

        System.out.println("Всего ходов: " + gameController.getTurnCount());
        System.out.println("Затраченное время: " +
                gameController.getTimerManager().getFormattedTime());

        scanner.close();
    }
}