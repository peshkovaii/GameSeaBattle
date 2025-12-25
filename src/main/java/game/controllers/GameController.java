package game.controllers;

import game.models.*;
import game.config.GameConfig;
import game.ui.BattleGameFrame;
import game.utils.TimerManager;

import javax.swing.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

/**
 * Основной контроллер игры, управляющий логикой игрового процесса.
 * Координирует взаимодействие между игроком, ботом и игровыми полями.
 * Отвечает за расстановку кораблей, обработку ходов и определение победителя.
 * Реализует основные правила игры "Морской бой с минами и подлодками".
 * <p>
 * Основные функции контроллера:
 * <p>
 * Инициализация игровых полей для игрока и бота
 * Расстановка кораблей, подлодок, тральщиков и мин
 * Обработка атак игрока и бота
 * Управление очередностью ходов
 * Обработка специальных событий (попадание в мину, тральщик)
 * Определение конца игры и победителя
 */
public class GameController {
    /**
     * Доска игрока с размещенными кораблями и минами.
     */
    private Board playerBoard;

    /**
     * Доска бота с размещенными кораблями и минами.
     */
    private Board botBoard;

    /**
     * Флаг, указывающий, чей сейчас ход (true - игрок, false - бот).
     */
    private boolean playerTurn;

    /**
     * Счетчик выполненных ходов в текущей игре.
     */
    private int turnCount;

    /**
     * Менеджер времени для отслеживания продолжительности игры.
     */
    private TimerManager timerManager;

    /**
     * Генератор случайных чисел для действий бота и случайной расстановки.
     */
    private Random random;

    /**
     * Флаг, указывающий на завершение игры.
     */
    private boolean gameOver;

    /**
     * Имя победителя ("Player", "Bot" или "Timeout").
     */
    private String winner;

    /**
     * Список целей, раскрытых игроку (например, при попадании в мину).
     */
    private List<RevealedTarget> playerRevealedTargets;

    /**
     * Список целей, раскрытых боту (например, при попадании в мину игрока).
     */
    private List<RevealedTarget> botRevealedTargets;

    /**
     * Ссылка на игровое окно для отображения сообщений пользователю.
     */
    private BattleGameFrame gameFrame;

    /**
     * Создает новый экземпляр контроллера игры со стандартной конфигурацией.
     * Инициализирует две игровые доски, расставляет на них корабли, подлодки,
     * минные тральщики и мины согласно настройкам
     * Устанавливает начальные значения: ход игрока, счетчик ходов, состояние игры.
     */
    public GameController() {
        int boardSize = GameConfig.getBoardSize();
        this.playerBoard = new Board(boardSize);
        this.botBoard = new Board(boardSize);
        this.playerTurn = true;
        this.turnCount = 0;
        this.timerManager = new TimerManager();
        this.random = new Random();
        this.gameOver = false;
        this.winner = "";
        this.playerRevealedTargets = new ArrayList<>();
        this.botRevealedTargets = new ArrayList<>();
        setupShips(playerBoard);
        setupShips(botBoard);
    }

    /**
     * Создает контроллер с заданной доской игрока.
     * Используется при загрузке сохраненной игры или при переходе с экрана расстановки.
     * Создает новую доску для бота и размещает на ней корабли.
     *
     * @param playerBoard предварительно подготовленная доска игрока
     */
    public GameController(Board playerBoard) {
        int boardSize = GameConfig.getBoardSize();
        this.playerBoard = playerBoard;
        this.botBoard = new Board(boardSize);
        this.playerTurn = true;
        this.turnCount = 0;
        this.timerManager = new TimerManager();
        this.random = new Random();
        this.gameOver = false;
        this.winner = "";
        this.playerRevealedTargets = new ArrayList<>();
        this.botRevealedTargets = new ArrayList<>();
        setupShips(botBoard);
    }

    /**
     * Устанавливает ссылку на игровое окно для отображения сообщений.
     * Позволяет контроллеру взаимодействовать с пользовательским интерфейсом,
     * показывать диалоговые окна и обновлять состояние отображения.
     *
     * @param gameFrame ссылка на экземпляр игрового окна
     */
    public void setGameFrame(BattleGameFrame gameFrame) {
        this.gameFrame = gameFrame;
    }

    /**
     * Настраивает корабли на указанном игровом поле.
     * Размещает стандартные корабли, подлодки, минные тральщики и мины
     * в соответствии с конфигурацией игры.
     *
     * @param board игровое поле для размещения кораблей и специальных юнитов
     */
    private void setupShips(Board board) {
        placeStandardShips(board);
        placeSubmarine(board);
        placeMinesweeper(board);
        placeMines(board);
    }

    /**
     * Размещает стандартные корабли на указанном поле.
     * Включает линкоры, крейсеры и эсминцы согласно количеству,
     * заданному в конфигурации игры.
     *
     * @param board игровое поле для размещения стандартных кораблей
     */
    private void placeStandardShips(Board board) {
        // 1 четырёхпалубный
        placeShipRandomly(board, new Battleship());

        // 2 трёхпалубных
        for (int i = 0; i < GameConfig.getCruiserCount(); i++) {
            placeShipRandomly(board, new Cruiser());
        }

        // 3 двухпалубных
        for (int i = 0; i < GameConfig.getDestroyerCount(); i++) {
            placeShipRandomly(board, new Destroyer());
        }
    }

    /**
     * Размещает корабль случайным образом на указанном поле.
     * Делает до 100 попыток размещения корабля в случайной позиции
     * и ориентации. Если размещение не удалось, корабль не размещается.
     *
     * @param board игровое поле для размещения
     * @param ship  корабль для размещения
     * @return true если корабль успешно размещен, false в противном случае
     */
    private void placeShipRandomly(Board board, Ship ship) {
        boolean placed = false;
        int attempts = 0;

        while (!placed && attempts < 100) {
            int row = random.nextInt(board.getSize());
            int col = random.nextInt(board.getSize());
            boolean horizontal = random.nextBoolean();

            placed = board.placeShip(ship, row, col, horizontal);
            attempts++;
        }
    }

    /**
     * Размещает подводные лодки на указанном поле.
     * Количество подлодок определяется конфигурацией игры.
     * Подлодки являются одноклеточными кораблями с особыми правилами.
     *
     * @param board игровое поле для размещения подлодок
     */
    private void placeSubmarine(Board board) {
        for (int i = 0; i < GameConfig.getSubmarineCount(); i++) {
            Submarine submarine = new Submarine();
            placeShipRandomly(board, submarine);
        }
    }

    /**
     * Размещает минные тральщики на указанном поле.
     * Тральщики размещаются как одноклеточные юниты, которые могут
     * раскрывать мины противника при попадании.
     *
     * @param board игровое поле для размещения тральщиков
     */
    private void placeMinesweeper(Board board) {
        for (int i = 0; i < GameConfig.getMinesweeperCount(); i++) {
            boolean placed = false;
            int attempts = 0;

            while (!placed && attempts < 100) {
                int row = random.nextInt(board.getSize());
                int col = random.nextInt(board.getSize());

                placed = board.placeMinesweeper(row, col);
                attempts++;
            }
        }
    }

    /**
     * Размещает мины на указанном поле.
     * Мины размещаются как отдельные юниты, которые при попадании
     * раскрывают координату случайного корабля противника.
     *
     * @param board игровое поле для размещения мин
     */
    private void placeMines(Board board) {
        for (int i = 0; i < GameConfig.getMineCount(); i++) {
            boolean placed = false;
            int attempts = 0;

            while (!placed && attempts < 100) {
                int row = random.nextInt(board.getSize());
                int col = random.nextInt(board.getSize());

                placed = board.placeMine(row, col);
                attempts++;
            }
        }
    }

    /**
     * Начинает игровой процесс.
     * Запускает таймер игры, сбрасывает флаг завершения игры
     * и устанавливает очередь хода игроку.
     */
    public void startGame() {
        timerManager.start();
        gameOver = false;
        playerTurn = true;
    }

    /**
     * Выполняет атаку игрока по указанным координатам на поле бота.
     * Проверяет возможность атаки (ход игрока, игра не завершена),
     * выполняет атаку и обрабатывает результат.
     *
     * @param row строка для атаки (0-based индексация)
     * @param col столбец для атаки (0-based индексация)
     * @return true если атака успешно выполнена, false если атака невозможна
     * (не ход игрока, игра завершена, неверные координаты)
     */
    public boolean playerAttack(int row, int col) {
        if (!playerTurn || gameOver) return false;

        Cell cell = botBoard.getCell(row, col);
        CellState previousState = (cell != null) ? cell.getState() : CellState.EMPTY;

        Board.AttackResult result = botBoard.attack(row, col);
        turnCount++;

        if (result == Board.AttackResult.MINE_HIT) {
            handleMineHit(true, row, col);
            return true;
        } else if (result == Board.AttackResult.MINESWEEPER_HIT) {
            handleMinesweeperHit(true, row, col);
        }

        checkShipSunk(botBoard, row, col);

        checkGameOver();

        if (result != Board.AttackResult.MINE_HIT) {
            playerTurn = false;
        }

        return true;
    }

    /**
     * Выполняет атаку бота по указанным координатам на поле игрока.
     * Используется для автоматических ходов бота. Сохраняет предыдущее
     * состояние клетки, выполняет атаку и обрабатывает специальные события.
     *
     * @param row строка для атаки (0-based индексация)
     * @param col столбец для атаки (0-based индексация)
     * @return true если атака успешно выполнена, false если атака невозможна
     * (не ход бота, игра завершена)
     */
    public boolean botAttack(int row, int col) {
        if (playerTurn || gameOver) return false;

        Cell cell = playerBoard.getCell(row, col);
        cell.setPreviousState(cell.getState());

        Board.AttackResult result = playerBoard.attack(row, col);
        turnCount++;

        if (result == Board.AttackResult.MINE_HIT) {
            handleMineHit(false, row, col);
        } else if (result == Board.AttackResult.MINESWEEPER_HIT) {
            handleMinesweeperHit(false, row, col);
        }

        checkShipSunk(playerBoard, row, col);

        checkGameOver();

        if (result != Board.AttackResult.MINE_HIT) {
            playerTurn = true;
        }

        return true;
    }

    /**
     * Обрабатывает попадание в мину.
     * В зависимости от того, кто попал в мину (игрок или бот),
     * выполняет соответствующие действия:
     * - При попадании игрока в мину бота: бот получает координату корабля игрока
     * - При попадании бота в мину игрока: мгрок получает координату корабля бота
     * Также деактивирует мину после срабатывания.
     *
     * @param isPlayer true если в мину попал игрок, false если бот
     * @param row      строка, где находится мина
     * @param col      столбец, где находится мина
     */
    private void handleMineHit(boolean isPlayer, int row, int col) {
        if (isPlayer) {
            RevealedTarget target = findRandomBotShipForReveal();
            if (target != null) {
                playerRevealedTargets.add(target);

                Cell botCell = botBoard.getCell(target.getRow(), target.getCol());
                if (botCell != null) {
                    botCell.setState(CellState.REVEALED);
                    botCell.setRevealedByMine(true);
                }

                showMineMessageToPlayer(true, target);
            }
            deactivateMine(botBoard, row, col);
        } else {
            RevealedTarget target = findRandomPlayerShipForReveal();
            if (target != null) {
                botRevealedTargets.add(target);

                Cell playerCell = playerBoard.getCell(target.getRow(), target.getCol());
                if (playerCell != null) {
                    playerCell.setState(CellState.REVEALED);
                    playerCell.setRevealedByMine(true);
                }

                showMineMessageToPlayer(false, target);
            }
            deactivateMine(playerBoard, row, col);
        }
    }

    /**
     * Показывает сообщение игроку о срабатывании мины.
     * Отображает диалоговое окно с информацией о том, кто попал на мину
     * и какую координату корабля был раскрыт.
     *
     * @param isPlayerHitBotMine true если игрок попал в мину бота,
     *                           false если бот попал в мину игрока
     * @param target             раскрытая цель (координата корабля)
     */
    private void showMineMessageToPlayer(boolean isPlayerHitBotMine, RevealedTarget target) {
        if (gameFrame != null) {
            SwingUtilities.invokeLater(() -> {
                String message;
                if (isPlayerHitBotMine) {
                    message = String.format(
                            "Вы попали на мину противника!\n" +
                                    "В награду вы узнали координату корабля противника: %s%d\n" +
                                    "Эта координата отмечена голубым цветом с буквой 'R' на поле противника.",
                            (char) ('A' + target.getCol()), (target.getRow() + 1)
                    );
                } else {
                    message = String.format(
                            "Противник попал на вашу мину!\n" +
                                    "Теперь он знает координату вашего корабля: %s%d\n" +
                                    "Эта координата отмечена на вашем поле голубым цветом с буквой 'R'.",
                            (char) ('A' + target.getCol()), (target.getRow() + 1)
                    );
                }

                JOptionPane.showMessageDialog(
                        gameFrame,
                        message,
                        isPlayerHitBotMine ? "Мина!" : "Ваша мина сработала!",
                        isPlayerHitBotMine ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE
                );
            });
        }
    }

    /**
     * Деактивирует мину после срабатывания.
     * Находит мину по координатам на доске, активирует ее и удаляет с клетки.
     * Помечает клетку как пораженную (HIT).
     *
     * @param board доска, на которой находится мина
     * @param row   строка, где находится мина
     * @param col   столбец, где находится мина
     */
    private void deactivateMine(Board board, int row, int col) {
        for (Mine mine : board.getMines()) {
            if (mine.getRow() == row && mine.getCol() == col) {
                mine.activate();
                Cell cell = board.getCell(row, col);
                if (cell != null) {
                    cell.removeMine();
                    cell.setState(CellState.HIT);
                }
                break;
            }
        }
    }

    /**
     * Находит случайный неповрежденный корабль бота для выдачи игроку.
     * Исключает из поиска минные тральщики и подлодки, а также уже
     * поврежденные или раскрытые клетки кораблей.
     *
     * @return {@link RevealedTarget} с координатами корабля или null,
     * если нет доступных кораблей
     */
    private RevealedTarget findRandomBotShipForReveal() {
        List<Ship> availableShips = new ArrayList<>();

        for (Ship ship : botBoard.getShips()) {
            if (!ship.isSunk() && !(ship instanceof Minesweeper) && !(ship instanceof Submarine)) {
                availableShips.add(ship);
            }
        }

        if (!availableShips.isEmpty()) {
            Ship randomShip = availableShips.get(random.nextInt(availableShips.size()));

            List<Cell> availableCells = new ArrayList<>();
            for (Cell cell : randomShip.getCells()) {
                if (cell.getState() != CellState.HIT && cell.getState() != CellState.REVEALED) {
                    availableCells.add(cell);
                }
            }

            if (!availableCells.isEmpty()) {
                Cell targetCell = availableCells.get(random.nextInt(availableCells.size()));
                return new RevealedTarget(targetCell.getRow(), targetCell.getCol(),
                        RevealedTarget.TargetType.SHIP);
            }
        }

        return null;
    }

    /**
     * Находит случайный неповрежденный корабль игрока для выдачи боту.
     * Исключает из поиска минные тральщики и подлодки, а также уже
     * поврежденные или раскрытые клетки кораблей.
     *
     * @return {@link RevealedTarget} с координатами корабля или null,
     * если нет доступных кораблей
     */
    private RevealedTarget findRandomPlayerShipForReveal() {
        List<Ship> availableShips = new ArrayList<>();

        for (Ship ship : playerBoard.getShips()) {
            if (!ship.isSunk() && !(ship instanceof Minesweeper) && !(ship instanceof Submarine)) {
                availableShips.add(ship);
            }
        }

        if (!availableShips.isEmpty()) {
            Ship randomShip = availableShips.get(random.nextInt(availableShips.size()));

            List<Cell> availableCells = new ArrayList<>();
            for (Cell cell : randomShip.getCells()) {
                if (cell.getState() != CellState.HIT && cell.getState() != CellState.REVEALED) {
                    availableCells.add(cell);
                }
            }

            if (!availableCells.isEmpty()) {
                Cell targetCell = availableCells.get(random.nextInt(availableCells.size()));
                return new RevealedTarget(targetCell.getRow(), targetCell.getCol(),
                        RevealedTarget.TargetType.SHIP);
            }
        }

        return null;
    }

    /**
     * Обрабатывает попадание в минный тральщик.
     * При попадании в тральщик противника, атакующий получает координату
     * случайной мины противника. Раскрытая мина помечается как REVEALED.
     *
     * @param isPlayer true если в тральщик попал игрок, false если бот
     * @param row      строка, где находится тральщик
     * @param col      столбец, где находится тральщик
     */
    private void handleMinesweeperHit(boolean isPlayer, int row, int col) {
        if (isPlayer) {
            RevealedTarget target = findRandomBotMineForReveal();
            if (target != null) {
                playerRevealedTargets.add(target);

                Cell botCell = botBoard.getCell(target.getRow(), target.getCol());
                if (botCell != null) {
                    botCell.setState(CellState.REVEALED);
                    botCell.setRevealedByMine(true);
                }
            }
        } else {
            RevealedTarget target = findRandomPlayerMineForReveal();
            if (target != null) {
                botRevealedTargets.add(target);

                Cell playerCell = playerBoard.getCell(target.getRow(), target.getCol());
                if (playerCell != null) {
                    playerCell.setState(CellState.REVEALED);
                    playerCell.setRevealedByMine(true);
                }
            }
        }
    }

    /**
     * Находит случайную мину бота для выдачи игроку.
     * Ищет неактивированные мины на поле бота.
     *
     * @return {@link RevealedTarget} с координатами мины или null,
     * если нет доступных мин
     */
    private RevealedTarget findRandomBotMineForReveal() {
        List<Mine> availableMines = new ArrayList<>();

        for (Mine mine : botBoard.getMines()) {
            if (!mine.isActive()) {
                availableMines.add(mine);
            }
        }

        if (!availableMines.isEmpty()) {
            Mine randomMine = availableMines.get(random.nextInt(availableMines.size()));
            return new RevealedTarget(randomMine.getRow(), randomMine.getCol(),
                    RevealedTarget.TargetType.MINE);
        }

        return null;
    }

    /**
     * Находит случайную мину игрока для выдачи боту.
     * Ищет неактивированные мины на поле игрока.
     *
     * @return {@link RevealedTarget} с координатами мины или null,
     * если нет доступных мин
     */
    private RevealedTarget findRandomPlayerMineForReveal() {
        List<Mine> availableMines = new ArrayList<>();

        for (Mine mine : playerBoard.getMines()) {
            if (!mine.isActive()) {
                availableMines.add(mine);
            }
        }

        if (!availableMines.isEmpty()) {
            Mine randomMine = availableMines.get(random.nextInt(availableMines.size()));
            return new RevealedTarget(randomMine.getRow(), randomMine.getCol(),
                    RevealedTarget.TargetType.MINE);
        }

        return null;
    }


    /**
     * Проверяет, был ли потоплен корабль после атаки в указанной клетке.
     * Если корабль потоплен, помечает все его клетки как DESTROYED.
     *
     * @param board доска для проверки
     * @param row   строка атакованной клетки
     * @param col   столбец атакованной клетки
     */
    private void checkShipSunk(Board board, int row, int col) {
        Cell cell = board.getCell(row, col);
        if (cell != null && cell.hasShip()) {
            Ship ship = cell.getShip();
            if (ship.isSunk()) {
                for (Cell shipCell : ship.getCells()) {
                    shipCell.setState(CellState.DESTROYED);
                }
            }
        }
    }

    /**
     * Проверяет, завершена ли игра.
     * Игра считается завершенной, если все корабли одного из игроков потоплены.
     * Устанавливает флаг gameOver и определяет победителя.
     *
     * @see #allShipsSunk(Board)
     */
    private void checkGameOver() {
        if (allShipsSunk(botBoard)) {
            gameOver = true;
            winner = "Player";
        } else if (allShipsSunk(playerBoard)) {
            gameOver = true;
            winner = "Bot";
        }
    }

    /**
     * Проверяет, все ли корабли потоплены на доске.
     * Игнорирует минные тральщики при проверке, так как они
     * не считаются основными кораблями для победы.
     *
     * @param board доска для проверки
     * @return true если все не-тральщиковые корабли потоплены, false в противном случае
     */
    private boolean allShipsSunk(Board board) {
        for (Ship ship : board.getShips()) {
            if (!ship.isSunk() && !(ship instanceof Minesweeper)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Возвращает true, если сейчас ход игрока.
     *
     * @return true если ход игрока, false если ход бота
     */
    public boolean isPlayerTurn() {
        return playerTurn;
    }

    /**
     * Устанавливает, чей сейчас ход.
     *
     * @param playerTurn true для хода игрока, false для хода бота
     */
    public void setPlayerTurn(boolean playerTurn) {
        this.playerTurn = playerTurn;
    }

    /**
     * Возвращает true, если игра завершена.
     *
     * @return true если игра завершена, false если игра продолжается
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Устанавливает флаг завершения игры.
     *
     * @param gameOver true для завершения игры, false для продолжения
     */
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    /**
     * Возвращает имя победителя.
     * Возможные значения: "Player", "Bot", "Timeout".
     *
     * @return имя победителя или пустая строка, если игра не завершена
     */
    public String getWinner() {
        return winner;
    }

    /**
     * Устанавливает победителя игры.
     *
     * @param winner имя победителя ("Player", "Bot", "Timeout")
     */
    public void setWinner(String winner) {
        this.winner = winner;
    }

    /**
     * Возвращает количество выполненных ходов в текущей игре.
     *
     * @return количество ходов
     */
    public int getTurnCount() {
        return turnCount;
    }

    /**
     * Возвращает доску игрока.
     *
     * @return доска игрока
     */
    public Board getPlayerBoard() {
        return playerBoard;
    }

    /**
     * Возвращает доску бота.
     *
     * @return доска бота
     */
    public Board getBotBoard() {
        return botBoard;
    }

    /**
     * Возвращает менеджер таймера для управления временем игры.
     *
     * @return менеджер таймера
     * @see TimerManager
     */
    public TimerManager getTimerManager() {
        return timerManager;
    }

    /**
     * Возвращает список целей, раскрытых игроку.
     * Включает координаты кораблей и мин, раскрытые при попадании в мины или тральщики.
     *
     * @return список раскрытых целей игроку
     */
    public List<RevealedTarget> getPlayerRevealedTargets() {
        return playerRevealedTargets;
    }

    /**
     * Возвращает список целей, раскрытых боту.
     * Включает координаты кораблей и мин игрока, раскрытые боту.
     *
     * @return список раскрытых целей боту
     */
    public List<RevealedTarget> getBotRevealedTargets() {
        return botRevealedTargets;
    }
}