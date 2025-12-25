package game.controllers;

import game.models.*;
import game.config.GameConfig;
import java.util.Random;

/**
 * Контроллер для наблюдения за игрой двух ботов.
 * Режим наблюдения позволяет двум ботам играть друг против друга
 * без участия человека. Полезен для тестирования игровой логики
 * и стратегий искусственного интеллекта.
 */
public class ObserverController {
    /**
     * Игровой контроллер для первого бота.
     * Управляет доской первого бота и его атаками.
     */
    private GameController bot1Game;

    /**
     * Игровой контроллер для второго бота.
     * Управляет доской второго бота и его атаками.
     */
    private GameController bot2Game;

    /**
     * Генератор случайных чисел для ходов ботов.
     */
    private Random random;

    /**
     * Создает новый контроллер для режима наблюдения.
     * Инициализирует два игровых контроллера для ботов.
     */
    public ObserverController() {
        this.bot1Game = new GameController();
        this.bot2Game = new GameController();
        this.random = new Random();
    }

    /**
     * Запускает режим наблюдения за игрой двух ботов.
     * Отображает начальное расположение кораблей, затем поочередно
     * выполняет ходы ботов до завершения игры или достижения максимального
     * количества ходов.
     */
    public void start() {
        System.out.println("НАЧАЛО ИГРЫ БОТОВ");
        printBoards();

        int turn = 0;
        boolean gameOver = false;

        while (!gameOver && turn < GameConfig.getMaxTurns()) {
            turn++;
            System.out.println("\n ХОД " + turn + " ===");

            botTurn(bot1Game, bot2Game, "Бот 1", "Бот 2");

            if (checkGameOver()) {
                gameOver = true;
                break;
            }

            System.out.println("Бот 2 атакует:");
            botTurn(bot2Game, bot1Game, "Бот 2", "Бот 1");

            if (checkGameOver()) {
                gameOver = true;
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        printResult();
    }

    /**
     * Выполняет ход одного бота против другого.
     * Бот использует случайную стратегию атаки.
     *
     * @param attacker контроллер атакующего бота
     * @param defender контроллер защищающегося бота
     * @param attackerName имя атакующего бота для отображения
     * @param defenderName имя защищающегося бота для отображения
     */
    private void botTurn(GameController attacker, GameController defender,
                         String attackerName, String defenderName) {
        Board defenderBoard = defender.getPlayerBoard();

        int row, col;
        Board.AttackResult result;

        do {
            row = random.nextInt(defenderBoard.getSize());
            col = random.nextInt(defenderBoard.getSize());
            result = defenderBoard.attack(row, col);
        } while (result == Board.AttackResult.INVALID ||
                result == Board.AttackResult.ALREADY_ATTACKED);

        System.out.println(attackerName + " атакует клетку " +
                (col + 1) + "," + (row + 1));

        switch (result) {
            case SHIP_HIT:
                System.out.println("Попадание в корабль!");
                break;
            case SHIP_SUNK:
                System.out.println("Корабль потоплен!");
                break;
            case MINE_HIT:
                System.out.println("Попадание в мину!");
                break;
            case MINESWEEPER_HIT:
                System.out.println("Попадание в минный тральщик!");
                break;
            case MISS:
                System.out.println("Промах!");
                break;
        }
    }

    /**
     * Проверяет, завершена ли игра.
     * Игра считается завершенной, если один из ботов проиграл.
     *
     * @return true если игра завершена, false если игра продолжается
     */
    private boolean checkGameOver() {
        return bot1Game.isGameOver() || bot2Game.isGameOver();
    }

    /**
     * Отображает начальное расположение кораблей на полях обоих ботов.
     * Показывает поля двух ботов рядом для удобства сравнения.
     */
    private void printBoards() {
        System.out.println("\nНачальное расположение:");
        System.out.println("Бот 1 - слева, Бот 2 - справа");

        int size = GameConfig.getBoardSize();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Cell cell = bot1Game.getPlayerBoard().getCell(i, j);
                System.out.print(getCellSymbol(cell) + " ");
            }

            System.out.print("   ");

            for (int j = 0; j < size; j++) {
                Cell cell = bot2Game.getPlayerBoard().getCell(i, j);
                System.out.print(getCellSymbol(cell) + " ");
            }

            System.out.println();
        }
    }

    /**
     * Возвращает символ для отображения состояния клетки в консоли.
     * Используется для визуализации игровых полей в текстовом режиме.
     *
     * @param cell клетка для отображения
     * @return символ, представляющий состояние клетки
     */
    private char getCellSymbol(Cell cell) {
        if (cell == null) return '?';

        switch (cell.getState()) {
            case EMPTY: return '.';
            case SHIP: return 'S';
            case SUBMARINE: return 'U';
            case MINE: return '*';
            case MINESWEEPER: return 'T';
            case HIT: return 'X';
            case MISS: return 'O';
            case DESTROYED: return '#';
            default: return '.';
        }
    }

    /**
     * Отображает результат игры между ботами.
     * Определяет победителя на основе состояния игровых контроллеров.
     * Если оба бота проиграли или никто не проиграл до конца ходов,
     * объявляется ничья.
     */
    private void printResult() {
        System.out.println("\n ИГРА ОКОНЧЕНА");

        if (bot1Game.isGameOver() && bot2Game.isGameOver()) {
            System.out.println("НИЧЬЯ!");
        } else if (bot1Game.isGameOver()) {
            System.out.println("ПОБЕДИТЕЛЬ: БОТ 2");
        } else if (bot2Game.isGameOver()) {
            System.out.println("ПОБЕДИТЕЛЬ: БОТ 1");
        } else {
            System.out.println("ПРЕВЫШЕНО МАКСИМАЛЬНОЕ ЧИСЛО ХОДОВ");
        }
    }
}