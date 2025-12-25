package game.models;

import game.config.GameConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Класс, представляющий игровое поле для игры "Морской бой с минами и подлодками".
 * Управляет сеткой клеток, кораблями, минами и тральщиками.
 * Обеспечивает логику размещения объектов на поле и обработки атак.
 *
 * Поле представляет собой квадратную сетку заданного размера, где каждая клетка
 * может содержать корабль, мину, тральщик или быть пустой.
 */
public class Board {
    /**
     * Размер игрового поля (количество клеток по вертикали и горизонтали).
     */
    private int size;

    /**
     * Двумерный массив клеток, представляющий игровое поле.
     */
    private Cell[][] grid;

    /**
     * Список всех кораблей, размещенных на поле.
     * Включает стандартные корабли, подлодки и тральщики.
     */
    private List<Ship> ships;

    /**
     * Список всех мин, размещенных на поле.
     */
    private List<Mine> mines;

    /**
     * Список всех минных тральщиков, размещенных на поле.
     */
    private List<Minesweeper> minesweepers;

    /**
     * Генератор случайных чисел для случайного размещения объектов.
     */
    private Random random;

    /**
     * Создает новое игровое поле указанного размера.
     * Инициализирует сетку клеток и списки объектов.
     *
     * @param size размер поля в клетках (например, 10 для поля 10x10)
     */
    public Board(int size) {
        this.size = size;
        this.grid = new Cell[size][size];
        this.ships = new ArrayList<>();
        this.mines = new ArrayList<>();
        this.minesweepers = new ArrayList<>();
        this.random = new Random();
        initializeBoard();
    }

    /**
     * Инициализирует игровое поле, создавая клетки для всех позиций.
     * Вызывается конструктором для подготовки поля к использованию.
     */
    private void initializeBoard() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = new Cell(i, j);
            }
        }
    }

    /**
     * Размещает корабль на поле в указанной позиции и ориентации.
     * Проверяет возможность размещения и добавляет корабль в список.
     *
     * @param ship корабль для размещения
     * @param row начальная строка размещения
     * @param col начальный столбец размещения
     * @param isHorizontal ориентация корабля (true - горизонтально, false - вертикально)
     * @return true если корабль успешно размещен, false в противном случае
     */
    public boolean placeShip(Ship ship, int row, int col, boolean isHorizontal) {
        if (isHorizontal) {
            if (col + ship.getSize() > size) return false;
            for (int i = 0; i < ship.getSize(); i++) {
                if (!isValidShipPosition(row, col + i)) return false;
            }
            for (int i = 0; i < ship.getSize(); i++) {
                Cell cell = grid[row][col + i];
                cell.placeShip(ship);
                ship.addCell(cell);
            }
        } else {
            if (row + ship.getSize() > size) return false;
            for (int i = 0; i < ship.getSize(); i++) {
                if (!isValidShipPosition(row + i, col)) return false;
            }
            for (int i = 0; i < ship.getSize(); i++) {
                Cell cell = grid[row + i][col];
                cell.placeShip(ship);
                ship.addCell(cell);
            }
        }

        ships.add(ship);
        return true;
    }

    /**
     * Проверяет, можно ли разместить корабль в указанной клетке.
     * Учитывает границы поля, наличие других объектов и правила касания кораблей.
     *
     * @param row строка для проверки
     * @param col столбец для проверки
     * @return true если клетка доступна для размещения корабля, false в противном случае
     */
    private boolean isValidShipPosition(int row, int col) {
        if (row < 0 || row >= size || col < 0 || col >= size) return false;

        Cell cell = grid[row][col];
        if (cell.hasShip()) {
            return false;
        }


        if (!GameConfig.isAllowTouching()) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    int newRow = row + i;
                    int newCol = col + j;
                    if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size) {
                        if (grid[newRow][newCol].hasShip()) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * Размещает мину на поле в указанной позиции.
     * Проверяет возможность размещения и добавляет мину в список.
     *
     * @param row строка для размещения мины
     * @param col столбец для размещения мины
     * @return true если мина успешно размещена, false в противном случае
     */
    public boolean placeMine(int row, int col) {
        if (row < 0 || row >= size || col < 0 || col >= size) return false;

        Cell cell = grid[row][col];
        if (cell.hasMine() || cell.hasShip() || cell.hasMinesweeper()) {
            return false;
        }

        if (!isValidMinePosition(row, col)) return false;

        Mine mine = new Mine(row, col);
        mines.add(mine);
        cell.placeMine(mine);
        return true;
    }

    /**
     * Проверяет, можно ли разместить мину в указанной клетке.
     * Мины не должны касаться других мин.
     *
     * @param row строка для проверки
     * @param col столбец для проверки
     * @return true если клетка доступна для размещения мины, false в противном случае
     */
    private boolean isValidMinePosition(int row, int col) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int newRow = row + i;
                int newCol = col + j;
                if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size) {
                    Cell cell = grid[newRow][newCol];
                    if (cell.hasMine()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Размещает минный тральщик на поле в указанной позиции.
     * Проверяет возможность размещения и добавляет тральщик в список.
     *
     * @param row строка для размещения тральщика
     * @param col столбец для размещения тральщика
     * @return true если тральщик успешно размещен, false в противном случае
     */
    public boolean placeMinesweeper(int row, int col) {
        if (row < 0 || row >= size || col < 0 || col >= size) return false;

        Cell cell = grid[row][col];
        if (cell.hasMinesweeper() || cell.hasShip() || cell.hasMine()) {
            return false;
        }

        if (!isValidMinesweeperPosition(row, col)) return false;

        Minesweeper minesweeper = new Minesweeper();
        minesweepers.add(minesweeper);
        cell.placeMinesweeper(minesweeper);
        minesweeper.addCell(cell);
        ships.add(minesweeper);
        return true;
    }

    /**
     * Проверяет, можно ли разместить тральщик в указанной клетке.
     * Тральщики не должны касаться других тральщиков.
     *
     * @param row строка для проверки
     * @param col столбец для проверки
     * @return true если клетка доступна для размещения тральщика, false в противном случае
     */
    private boolean isValidMinesweeperPosition(int row, int col) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int newRow = row + i;
                int newCol = col + j;
                if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size) {
                    Cell cell = grid[newRow][newCol];
                    if (cell.hasMinesweeper()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Возвращает клетку по указанным координатам.
     *
     * @param row строка клетки
     * @param col столбец клетки
     * @return клетка по указанным координатам или null если координаты недействительны
     */
    public Cell getCell(int row, int col) {
        if (row < 0 || row >= size || col < 0 || col >= size) return null;
        return grid[row][col];
    }

    /**
     * Выполняет атаку по указанным координатам.
     * Проверяет возможность атаки и возвращает результат.
     *
     * @param row строка для атаки
     * @param col столбец для атаки
     * @return результат атаки в виде AttackResult
     */
    public AttackResult attack(int row, int col) {
        Cell cell = getCell(row, col);
        if (cell == null) return AttackResult.INVALID;

        if (!cell.canBeAttacked()) {
            return AttackResult.ALREADY_ATTACKED;
        }

        CellState result = cell.attack();

        if (cell.hasShip()) {
            Ship ship = cell.getShip();
            ship.hit();

            if (ship.isSunk()) {
                for (Cell shipCell : ship.getCells()) {
                    shipCell.updateIfShipSunk();
                }

                if (cell.hasMine()) {
                    return AttackResult.MINE_HIT;
                } else if (cell.hasMinesweeper()) {
                    return AttackResult.MINESWEEPER_HIT;
                } else if (ship instanceof Minesweeper) {
                    return AttackResult.MINESWEEPER_HIT;
                } else {
                    return AttackResult.SHIP_SUNK;
                }
            } else {
                if (cell.hasMine()) {
                    return AttackResult.MINE_HIT;
                } else if (cell.hasMinesweeper()) {
                    return AttackResult.MINESWEEPER_HIT;
                } else {
                    return AttackResult.SHIP_HIT;
                }
            }
        } else if (cell.hasMine()) {
            Mine mine = cell.getMine();
            if (mine != null) {
                mine.activate();
            }
            return AttackResult.MINE_HIT;
        } else if (cell.hasMinesweeper()) {
            Minesweeper minesweeper = cell.getMinesweeper();
            if (minesweeper != null) {
                minesweeper.hit();
            }
            return AttackResult.MINESWEEPER_HIT;
        } else {
            return AttackResult.MISS;
        }
    }

    /**
     * Размещает все корабли, мины и тральщики случайным образом на поле.
     * Сначала очищает поле, затем размещает объекты согласно конфигурации игры.
     */
    public void placeShipsRandomly() {
        clearShips();

        placeStandardShips();

        placeSpecialShips();
    }

    /**
     * Размещает стандартные корабли на поле.
     * Включает броненосцы, крейсеры и эсминцы согласно конфигурации.
     */
    private void placeStandardShips() {
        for (int i = 0; i < GameConfig.getBattleshipCount(); i++) {
            placeShipRandomly(new Battleship());
        }

        for (int i = 0; i < GameConfig.getCruiserCount(); i++) {
            placeShipRandomly(new Cruiser());
        }

        for (int i = 0; i < GameConfig.getDestroyerCount(); i++) {
            placeShipRandomly(new Destroyer());
        }
    }

    /**
     * Размещает специальные корабли на поле.
     * Включает подлодки, минные тральщики и мины.
     */
    private void placeSpecialShips() {
        for (int i = 0; i < GameConfig.getSubmarineCount(); i++) {
            placeSubmarineRandomly();
        }

        for (int i = 0; i < GameConfig.getMinesweeperCount(); i++) {
            placeMinesweeperRandomly();
        }

        for (int i = 0; i < GameConfig.getMineCount(); i++) {
            placeMineRandomly();
        }
    }

    /**
     * Размещает корабль случайным образом на поле.
     * Делает до 100 попыток найти доступную позицию.
     *
     * @param ship корабль для размещения
     */
    private void placeShipRandomly(Ship ship) {
        boolean placed = false;
        int attempts = 0;
        int maxAttempts = 100;

        while (!placed && attempts < maxAttempts) {
            int row = random.nextInt(size);
            int col = random.nextInt(size);
            boolean horizontal = random.nextBoolean();

            placed = placeShip(ship, row, col, horizontal);
            attempts++;
        }

        if (!placed) {
            System.out.println("Не удалось разместить корабль: " + ship.getName());
        }
    }

    /**
     * Размещает подводную лодку случайным образом на поле.
     * Делает до 100 попыток найти доступную позицию.
     */
    private void placeSubmarineRandomly() {
        boolean placed = false;
        int attempts = 0;
        int maxAttempts = 100;

        while (!placed && attempts < maxAttempts) {
            int row = random.nextInt(size);
            int col = random.nextInt(size);

            Submarine submarine = new Submarine();
            placed = placeShip(submarine, row, col, true);
            attempts++;
        }
    }

    /**
     * Размещает минный тральщик случайным образом на поле.
     * Делает до 100 попыток найти доступную позицию.
     */
    private void placeMinesweeperRandomly() {
        boolean placed = false;
        int attempts = 0;
        int maxAttempts = 100;

        while (!placed && attempts < maxAttempts) {
            int row = random.nextInt(size);
            int col = random.nextInt(size);

            placed = placeMinesweeper(row, col);
            attempts++;
        }
    }

    /**
     * Размещает мину случайным образом на поле.
     * Делает до 100 попыток найти доступную позицию.
     */
    private void placeMineRandomly() {
        boolean placed = false;
        int attempts = 0;
        int maxAttempts = 100;

        while (!placed && attempts < maxAttempts) {
            int row = random.nextInt(size);
            int col = random.nextInt(size);

            placed = placeMine(row, col);
            attempts++;
        }
    }

    /**
     * Очищает поле от всех кораблей, мин и тральщиков.
     * Сбрасывает состояние всех клеток и очищает списки объектов.
     */
    public void clearShips() {
        ships.clear();
        mines.clear();
        minesweepers.clear();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j].reset();
            }
        }
    }

    /**
     * Полностью очищает поле и повторно инициализирует его.
     * Удаляет все объекты и сбрасывает состояние поля до начального.
     */
    public void clearAll() {
        clearShips();
        initializeBoard();
    }

    /**
     * Проверяет, все ли корабли потоплены на поле.
     * Игнорирует тральщики и подлодки при проверке.
     *
     * @return true если все обычные корабли потоплены, false в противном случае
     */
    public boolean allShipsSunk() {
        for (Ship ship : ships) {
            if (!(ship instanceof Minesweeper) && !(ship instanceof Submarine) && !ship.isSunk()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Проверяет, есть ли на поле активные (не сработавшие) мины.
     *
     * @return true если есть хотя бы одна активная мина, false в противном случае
     */
    public boolean hasActiveMines() {
        for (Mine mine : mines) {
            if (!mine.isActive()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Проверяет, есть ли на поле не потопленные минные тральщики.
     *
     * @return true если есть хотя бы один активный тральщик, false в противном случае
     */
    public boolean hasActiveMinesweepers() {
        for (Minesweeper minesweeper : minesweepers) {
            if (!minesweeper.isSunk()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Возвращает список всех соседних клеток для указанной позиции.
     * Включает клетки по диагонали и ортогонально.
     *
     * @param row строка центральной клетки
     * @param col столбец центральной клетки
     * @return список соседних клеток
     */
    public List<Cell> getAdjacentCells(int row, int col) {
        List<Cell> adjacent = new ArrayList<>();

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;

                int newRow = row + i;
                int newCol = col + j;

                if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size) {
                    adjacent.add(grid[newRow][newCol]);
                }
            }
        }

        return adjacent;
    }

    /**
     * Возвращает список ортогональных соседних клеток для указанной позиции.
     * Включает только клетки сверху, снизу, слева и справа.
     *
     * @param row строка центральной клетки
     * @param col столбец центральной клетки
     * @return список ортогональных соседних клеток
     */
    public List<Cell> getOrthogonalAdjacentCells(int row, int col) {
        List<Cell> adjacent = new ArrayList<>();

        if (row > 0) adjacent.add(grid[row-1][col]);
        if (row < size-1) adjacent.add(grid[row+1][col]);
        if (col > 0) adjacent.add(grid[row][col-1]);
        if (col < size-1) adjacent.add(grid[row][col+1]);

        return adjacent;
    }

    /**
     * Возвращает строковое представление игрового поля.
     * Может отображать поле с кораблями или только с результатами атак.
     *
     * @param showShips true для отображения кораблей, false для скрытия
     * @return строковое представление поля
     */
    public String getBoardAsString(boolean showShips) {
        StringBuilder sb = new StringBuilder();

        sb.append("  ");
        for (int col = 0; col < size; col++) {
            sb.append((char) ('A' + col)).append(" ");
        }
        sb.append("\n");

        for (int row = 0; row < size; row++) {
            sb.append(String.format("%2d", row + 1)).append(" ");

            for (int col = 0; col < size; col++) {
                Cell cell = grid[row][col];
                char symbol = getCellSymbol(cell, showShips);
                sb.append(symbol).append(" ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Возвращает символ для отображения состояния клетки.
     * Используется при выводе поля в текстовом формате.
     *
     * @param cell клетка для отображения
     * @param showShips true для отображения кораблей, false для скрытия
     * @return символ, представляющий состояние клетки
     */
    private char getCellSymbol(Cell cell, boolean showShips) {
        if (cell == null) return '?';

        CellState state = cell.getState();

        if (showShips) {
            if (cell.hasShip()) {
                if (cell.getShip() instanceof Submarine) return 'U';
                if (cell.getShip() instanceof Minesweeper) return 'T';
                return 'S';
            }
            if (cell.hasMine()) return '*';
            return '.';
        } else {
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
    }

    /**
     * Выводит игровое поле в консоль.
     *
     * @param showShips true для отображения кораблей, false для скрытия
     */
    public void printBoard(boolean showShips) {
        System.out.println(getBoardAsString(showShips));
    }

    /**
     * Проверяет, пусто ли поле (нет кораблей, мин и тральщиков).
     *
     * @return true если поле пустое, false если есть хотя бы один объект
     */
    public boolean isEmpty() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Cell cell = grid[i][j];
                if (cell.hasShip() || cell.hasMine() || cell.hasMinesweeper()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Проверяет, являются ли координаты валидными для данного поля.
     *
     * @param row строка для проверки
     * @param col столбец для проверки
     * @return true если координаты в пределах поля, false в противном случае
     */
    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < size && col >= 0 && col < size;
    }

    /**
     * Подсчитывает количество оставшихся не потопленных кораблей.
     * Игнорирует тральщики при подсчете.
     *
     * @return количество активных кораблей
     */
    public int countRemainingShips() {
        int count = 0;
        for (Ship ship : ships) {
            if (!(ship instanceof Minesweeper) && !ship.isSunk()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Подсчитывает количество активных (не сработавших) мин.
     *
     * @return количество активных мин
     */
    public int countRemainingMines() {
        int count = 0;
        for (Mine mine : mines) {
            if (!mine.isActive()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Возвращает список клеток, которые можно атаковать.
     * Включает клетки, которые еще не были атакованы или раскрыты.
     *
     * @return список доступных для атаки клеток
     */
    public List<Cell> getAvailableTargets() {
        List<Cell> targets = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Cell cell = grid[i][j];
                if (cell.canBeAttacked()) {
                    targets.add(cell);
                }
            }
        }
        return targets;
    }

    /**
     * Проверяет, можно ли атаковать указанную клетку.
     *
     * @param row строка клетки
     * @param col столбец клетки
     * @return true если клетку можно атаковать, false в противном случае
     */
    public boolean isCellAttackable(int row, int col) {
        Cell cell = getCell(row, col);
        return cell != null && cell.canBeAttacked();
    }

    /**
     * Возвращает размер игрового поля.
     *
     * @return размер поля в клетках
     */
    public int getSize() {
        return size;
    }

    /**
     * Возвращает двумерный массив клеток поля.
     *
     * @return массив клеток поля
     */
    public Cell[][] getGrid() {
        return grid;
    }

    /**
     * Возвращает список всех кораблей на поле.
     *
     * @return список кораблей
     */
    public List<Ship> getShips() {
        return ships;
    }

    /**
     * Возвращает список всех мин на поле.
     *
     * @return список мин
     */
    public List<Mine> getMines() {
        return mines;
    }

    /**
     * Возвращает список всех минных тральщиков на поле.
     *
     * @return список тральщиков
     */
    public List<Minesweeper> getMinesweepers() {
        return minesweepers;
    }

    /**
     * Возвращает генератор случайных чисел, используемый полем.
     *
     * @return генератор случайных чисел
     */
    public Random getRandom() {
        return random;
    }

    /**
     * Перечисление возможных результатов атаки.
     */
    public enum AttackResult {
        /**
         * Попадание в обычный корабль.
         */
        SHIP_HIT,

        /**
         * Корабль полностью потоплен.
         */
        SHIP_SUNK,

        /**
         * Попадание в мину.
         */
        MINE_HIT,

        /**
         * Попадание в минный тральщик.
         */
        MINESWEEPER_HIT,

        /**
         * Промах по пустой клетке.
         */
        MISS,

        /**
         * Неверные координаты атаки.
         */
        INVALID,

        /**
         * Клетка уже была атакована ранее.
         */
        ALREADY_ATTACKED
    }

    /**
     * Вложенный класс для сбора статистики по игровому полю.
     * Предоставляет информацию о состоянии кораблей, мин и раскрытых клеток.
     */
    public static class BoardStats {
        /**
         * Общее количество кораблей на поле.
         */
        public int totalShips;

        /**
         * Количество потопленных кораблей.
         */
        public int sunkShips;

        /**
         * Количество активных кораблей.
         */
        public int remainingShips;

        /**
         * Общее количество мин на поле.
         */
        public int totalMines;

        /**
         * Количество активных (не сработавших) мин.
         */
        public int activeMines;

        /**
         * Количество раскрытых клеток (результаты атак).
         */
        public int revealedCells;

        /**
         * Создает объект статистики для указанного поля.
         * Собирает актуальную информацию о состоянии поля.
         *
         * @param board поле для сбора статистики
         */
        public BoardStats(Board board) {
            this.totalShips = board.getShips().size();
            this.sunkShips = 0;
            this.remainingShips = 0;
            this.totalMines = board.getMines().size();
            this.activeMines = 0;
            this.revealedCells = 0;

            for (Ship ship : board.getShips()) {
                if (ship.isSunk()) {
                    sunkShips++;
                } else if (!(ship instanceof Minesweeper)) {
                    remainingShips++;
                }
            }

            for (Mine mine : board.getMines()) {
                if (!mine.isActive()) {
                    activeMines++;
                }
            }

            for (int i = 0; i < board.size; i++) {
                for (int j = 0; j < board.size; j++) {
                    Cell cell = board.getCell(i, j);
                    if (cell.getState() == CellState.HIT ||
                            cell.getState() == CellState.MISS ||
                            cell.getState() == CellState.DESTROYED ||
                            cell.getState() == CellState.REVEALED) {
                        revealedCells++;
                    }
                }
            }
        }

        /**
         * Возвращает строковое представление статистики.
         *
         * @return форматированная строка со статистикой
         */
        @Override
        public String toString() {
            return String.format("Ships: %d/%d sunk, %d active. Mines: %d/%d active. Revealed: %d/%d",
                    sunkShips, totalShips, remainingShips,
                    activeMines, totalMines, revealedCells, 100);
        }
    }
}