package game.models;

/**
 * Класс, представляющий клетку игрового поля в игре "Морской бой".
 * Каждая клетка может содержать корабль, мину, минный тральщик или быть пустой.
 * Управляет состоянием клетки и обрабатывает взаимодействия с ней.
 *
 * Клетка является основной единицей игрового поля и хранит информацию
 * о своем содержимом, текущем состоянии и истории изменений.
 */
public class Cell {
    /**
     * Номер строки клетки на игровом поле.
     */
    private int row;

    /**
     * Номер столбца клетки на игровом поле.
     */
    private int col;

    /**
     * Корабль, расположенный в этой клетке (если есть).
     */
    private Ship ship;

    /**
     * Мина, расположенная в этой клетке (если есть).
     */
    private Mine mine;

    /**
     * Минный тральщик, расположенный в этой клетке (если есть).
     */
    private Minesweeper minesweeper;

    /**
     * Текущее состояние клетки (пустая, корабль, мина, попадание и т.д.).
     */
    private CellState state;

    /**
     * Предыдущее состояние клетки до последнего изменения.
     * Используется для отслеживания изменений и отката.
     */
    private CellState previousState;

    /**
     * Флаг, указывающий что координата этой клетки была раскрыта миной.
     */
    private boolean revealedByMine;

    /**
     * Флаг, указывающий что клетка содержит подводную лодку.
     */
    private boolean containsSubmarine;

    /**
     * Флаг, указывающий что клетка была поражена атакой.
     */
    private boolean isHit;

    /**
     * Создает новую клетку с указанными координатами.
     * Инициализирует все поля значениями по умолчанию.
     *
     * @param row номер строки клетки
     * @param col номер столбца клетки
     */
    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.state = CellState.EMPTY;
        this.previousState = CellState.EMPTY;
        this.revealedByMine = false;
        this.containsSubmarine = false;
        this.isHit = false;
    }

    /**
     * Размещает корабль в этой клетке.
     * Обновляет состояние клетки в зависимости от типа корабля.
     *
     * @param ship корабль для размещения
     * @return true если корабль успешно размещен, false если клетка уже занята
     */
    public boolean placeShip(Ship ship) {
        if (this.ship != null) {
            return false;
        }

        this.ship = ship;

        if (ship instanceof Submarine) {
            this.state = CellState.SUBMARINE;
            this.containsSubmarine = true;
        } else if (ship instanceof Minesweeper) {
            this.state = CellState.MINESWEEPER;
        } else {
            this.state = CellState.SHIP;
        }

        return true;
    }

    /**
     * Размещает мину в этой клетке.
     * Обновляет состояние клетки только если в ней нет корабля.
     *
     * @param mine мина для размещения
     * @return true если мина успешно размещена, false если клетка уже содержит мину
     */
    public boolean placeMine(Mine mine) {
        if (this.mine != null) {
            return false;
        }

        this.mine = mine;

        if (!this.hasShip()) {
            this.state = CellState.MINE;
        }

        return true;
    }

    /**
     * Размещает минный тральщик в этой клетке.
     * Обновляет состояние только если клетка не занята кораблем или миной.
     *
     * @param minesweeper тральщик для размещения
     * @return true если тральщик успешно размещен, false если клетка уже занята
     */
    public boolean placeMinesweeper(Minesweeper minesweeper) {
        if (this.minesweeper != null) {
            return false;
        }

        this.minesweeper = minesweeper;

        if (!this.hasShip() && !this.hasMine()) {
            this.state = CellState.MINESWEEPER;
        }

        return true;
    }

    /**
     * Удаляет корабль из этой клетки.
     * Восстанавливает состояние клетки в зависимости от оставшегося содержимого.
     *
     * @return true если корабль был удален, false если в клетке не было корабля
     */
    public boolean removeShip() {
        if (this.ship == null) {
            return false;
        }

        this.ship = null;
        this.containsSubmarine = false;

        if (this.hasMine()) {
            this.state = CellState.MINE;
        } else if (this.hasMinesweeper()) {
            this.state = CellState.MINESWEEPER;
        } else {
            this.state = CellState.EMPTY;
        }

        return true;
    }

    /**
     * Удаляет минный тральщик из этой клетки.
     * Восстанавливает состояние клетки в зависимости от оставшегося содержимого.
     */
    public void removeMinesweeper() {
        this.minesweeper = null;

        if (this.hasShip()) {
            this.state = this.containsSubmarine ? CellState.SUBMARINE : CellState.SHIP;
        } else if (this.hasMine()) {
            this.state = CellState.MINE;
        } else {
            this.state = CellState.EMPTY;
        }
    }

    /**
     * Удаляет мину из этой клетки.
     * Восстанавливает состояние клетки в зависимости от оставшегося содержимого.
     */
    public void removeMine() {
        this.mine = null;

        if (this.hasShip()) {
            this.state = this.containsSubmarine ? CellState.SUBMARINE : CellState.SHIP;
        } else if (this.hasMinesweeper()) {
            this.state = CellState.MINESWEEPER;
        } else {
            this.state = CellState.EMPTY;
        }
    }

    /**
     * Выполняет атаку на эту клетку.
     * Сохраняет предыдущее состояние, помечает клетку как пораженную
     * и возвращает результат атаки.
     *
     * @return результат атаки (тип пораженного объекта или промах)
     */
    public CellState attack() {
        this.previousState = this.state;
        this.isHit = true;

        if (this.hasShip()) {
            if (this.ship instanceof Submarine) {
                this.state = CellState.HIT;
                return CellState.SUBMARINE;
            } else if (this.ship instanceof Minesweeper) {
                this.state = CellState.HIT;
                return CellState.MINESWEEPER;
            } else {
                this.state = CellState.HIT;
                return CellState.SHIP;
            }
        } else if (this.hasMine()) {
            this.state = CellState.HIT;
            return CellState.MINE;
        } else if (this.hasMinesweeper()) {
            this.state = CellState.HIT;
            return CellState.MINESWEEPER;
        } else {
            this.state = CellState.MISS;
            return CellState.MISS;
        }
    }

    /**
     * Проверяет, можно ли атаковать эту клетку.
     * Клетка не может быть атакована, если уже была поражена
     * или если ее координаты были раскрыты миной.
     *
     * @return true если клетку можно атаковать, false в противном случае
     */
    public boolean canBeAttacked() {
        return !isHit && this.state != CellState.REVEALED;
    }

    /**
     * Полностью сбрасывает состояние клетки к начальным значениям.
     * Удаляет все объекты и сбрасывает флаги.
     */
    public void reset() {
        this.ship = null;
        this.mine = null;
        this.minesweeper = null;
        this.state = CellState.EMPTY;
        this.previousState = CellState.EMPTY;
        this.revealedByMine = false;
        this.containsSubmarine = false;
        this.isHit = false;
    }

    /**
     * Проверяет, является ли клетка частью потопленного корабля.
     *
     * @return true если клетка содержит корабль и он потоплен, false в противном случае
     */
    public boolean isPartOfSunkShip() {
        return this.hasShip() && this.ship.isSunk();
    }

    /**
     * Обновляет состояние клетки, если содержащийся в ней корабль потоплен.
     * Устанавливает состояние клетки в DESTROYED.
     */
    public void updateIfShipSunk() {
        if (this.hasShip() && this.ship.isSunk()) {
            this.state = CellState.DESTROYED;
        }
    }

    /**
     * Возвращает предыдущее состояние клетки.
     *
     * @return предыдущее состояние клетки
     */
    public CellState getPreviousState() {
        return previousState;
    }

    /**
     * Устанавливает предыдущее состояние клетки.
     *
     * @param previousState новое предыдущее состояние
     */
    public void setPreviousState(CellState previousState) {
        this.previousState = previousState;
    }

    /**
     * Проверяет, была ли координата этой клетки раскрыта миной.
     *
     * @return true если координата была раскрыта миной, false в противном случае
     */
    public boolean isRevealedByMine() {
        return revealedByMine;
    }

    /**
     * Устанавливает флаг раскрытия координаты миной.
     *
     * @param revealedByMine новое значение флага
     */
    public void setRevealedByMine(boolean revealedByMine) {
        this.revealedByMine = revealedByMine;
    }

    /**
     * Возвращает номер строки клетки.
     *
     * @return номер строки
     */
    public int getRow() {
        return row;
    }

    /**
     * Возвращает номер столбца клетки.
     *
     * @return номер столбца
     */
    public int getCol() {
        return col;
    }

    /**
     * Возвращает корабль, расположенный в клетке.
     *
     * @return корабль или null если клетка не содержит корабль
     */
    public Ship getShip() {
        return ship;
    }

    /**
     * Устанавливает корабль в клетку.
     *
     * @param ship корабль для размещения
     */
    public void setShip(Ship ship) {
        this.ship = ship;
    }

    /**
     * Проверяет, содержит ли клетка корабль.
     *
     * @return true если клетка содержит корабль, false в противном случае
     */
    public boolean hasShip() {
        return ship != null;
    }

    /**
     * Возвращает мину, расположенную в клетке.
     *
     * @return мина или null если клетка не содержит мину
     */
    public Mine getMine() {
        return mine;
    }

    /**
     * Устанавливает мину в клетку.
     *
     * @param mine мина для размещения
     */
    public void setMine(Mine mine) {
        this.mine = mine;
    }

    /**
     * Проверяет, содержит ли клетка мину.
     *
     * @return true если клетка содержит мину, false в противном случае
     */
    public boolean hasMine() {
        return mine != null;
    }

    /**
     * Возвращает минный тральщик, расположенный в клетке.
     *
     * @return тральщик или null если клетка не содержит тральщик
     */
    public Minesweeper getMinesweeper() {
        return minesweeper;
    }

    /**
     * Устанавливает минный тральщик в клетку.
     *
     * @param minesweeper тральщик для размещения
     */
    public void setMinesweeper(Minesweeper minesweeper) {
        this.minesweeper = minesweeper;
    }

    /**
     * Проверяет, содержит ли клетка минный тральщик.
     *
     * @return true если клетка содержит тральщик, false в противном случае
     */
    public boolean hasMinesweeper() {
        return minesweeper != null;
    }

    /**
     * Возвращает текущее состояние клетки.
     *
     * @return текущее состояние
     */
    public CellState getState() {
        return state;
    }

    /**
     * Устанавливает текущее состояние клетки.
     * Сохраняет предыдущее состояние перед изменением.
     *
     * @param state новое состояние
     */
    public void setState(CellState state) {
        this.previousState = this.state;
        this.state = state;
    }

    /**
     * Проверяет, содержит ли клетка подводную лодку.
     *
     * @return true если клетка содержит подводную лодку, false в противном случае
     */
    public boolean containsSubmarine() {
        return containsSubmarine;
    }

    /**
     * Устанавливает флаг наличия подводной лодки в клетке.
     *
     * @param containsSubmarine новое значение флага
     */
    public void setContainsSubmarine(boolean containsSubmarine) {
        this.containsSubmarine = containsSubmarine;
    }

    /**
     * Проверяет, была ли клетка поражена атакой.
     *
     * @return true если клетка была поражена, false в противном случае
     */
    public boolean isHit() {
        return isHit;
    }

    /**
     * Устанавливает флаг поражения клетки.
     *
     * @param hit новое значение флага
     */
    public void setHit(boolean hit) {
        this.isHit = hit;
    }

    /**
     * Возвращает строковое представление клетки для отладки.
     * Содержит координаты, состояние и информацию о содержимом.
     *
     * @return строковое представление клетки
     */
    @Override
    public String toString() {
        return String.format("Cell[%d,%d] state=%s ship=%s mine=%s minesweeper=%s",
                row, col, state,
                ship != null ? ship.getClass().getSimpleName() : "null",
                mine != null ? "yes" : "no",
                minesweeper != null ? "yes" : "no");
    }
}