package game.models;

import java.util.Random;

/**
 * Класс, представляющий мину в игре "Морской бой с минами и подлодками".
 * Мина является специальным объектом, который при активации раскрывает
 * координату случайного корабля противника. Мины размещаются на игровом поле
 * и активируются при попадании в них атакой.
 *
 * Мина может находиться в двух состояниях: активная (сработавшая) и неактивная.
 * При срабатывании мина раскрывает информацию о корабле противника.
 */
public class Mine {
    /**
     * Номер строки, в которой расположена мина на игровом поле.
     */
    private int row;

    /**
     * Номер столбца, в котором расположена мина на игровом поле.
     */
    private int col;

    /**
     * Флаг, указывающий активирована ли мина.
     * true - мина сработала, false - мина неактивна.
     */
    private boolean activated;

    /**
     * Флаг, указывающий раскрыла ли мина координату корабля.
     */
    private boolean revealedShip;

    /**
     * Генератор случайных чисел для выбора целей при срабатывании мины.
     */
    private Random random;

    /**
     * Создает новую мину с указанными координатами.
     * Инициализирует мину как неактивную и без раскрытых кораблей.
     *
     * @param row номер строки для размещения мины
     * @param col номер столбца для размещения мины
     */
    public Mine(int row, int col) {
        this.row = row;
        this.col = col;
        this.activated = false;
        this.revealedShip = false;
        this.random = new Random();
    }

    /**
     * Активирует мину.
     * Переводит мину из неактивного состояния в активное.
     * Мина может быть активирована только один раз.
     *
     * @return true если мина была успешно активирована,
     *         false если мина уже была активна
     */
    public boolean activate() {
        if (!activated) {
            activated = true;
            return true;
        }
        return false;
    }

    /**
     * Деактивирует мину.
     * Возвращает мину в неактивное состояние.
     * Используется при сбросе состояния или перезапуске игры.
     */
    public void deactivate() {
        this.activated = false;
    }

    /**
     * Проверяет, находится ли мина в указанных координатах.
     *
     * @param row проверяемая строка
     * @param col проверяемый столбец
     * @return true если мина находится в указанных координатах,
     *         false в противном случае
     */
    public boolean isAt(int row, int col) {
        return this.row == row && this.col == col;
    }

    /**
     * Устанавливает флаг раскрытия корабля миной.
     *
     * @param revealed true если мина раскрыла координату корабля,
     *                 false в противном случае
     */
    public void setRevealedShip(boolean revealed) {
        this.revealedShip = revealed;
    }

    /**
     * Возвращает случайную координату корабля в заданном радиусе от мины.
     * Используется при срабатывании мины для выбора цели для раскрытия.
     *
     * @param board игровое поле для поиска целей
     * @param range радиус поиска целей от мины
     * @return клетка с кораблем в радиусе или null если цель не найдена
     */
    public Cell getRandomTargetInRange(Board board, int range) {
        int boardSize = board.getSize();

        for (int attempt = 0; attempt < 50; attempt++) {
            int offsetRow = random.nextInt(range * 2 + 1) - range;
            int offsetCol = random.nextInt(range * 2 + 1) - range;

            int targetRow = row + offsetRow;
            int targetCol = col + offsetCol;

            if (targetRow >= 0 && targetRow < boardSize &&
                    targetCol >= 0 && targetCol < boardSize) {

                Cell targetCell = board.getCell(targetRow, targetCol);
                if (targetCell != null && targetCell.hasShip() &&
                        !targetCell.getShip().isSunk()) {
                    return targetCell;
                }
            }
        }

        return null;
    }

    /**
     * Возвращает случайную координату корабля на всей доске.
     * Используется, если не удалось найти цель в радиусе от мины.
     *
     * @param board игровое поле для поиска целей
     * @return клетка с кораблем или null если корабли не найдены
     */
    public Cell getRandomShipCell(Board board) {
        int boardSize = board.getSize();

        for (int attempt = 0; attempt < 100; attempt++) {
            int randomRow = random.nextInt(boardSize);
            int randomCol = random.nextInt(boardSize);

            Cell cell = board.getCell(randomRow, randomCol);
            if (cell != null && cell.hasShip() && !cell.getShip().isSunk()) {
                return cell;
            }
        }

        return null;
    }

    /**
     * Проверяет, может ли мина быть активирована.
     * Мина может быть активирована только если она еще не активна.
     *
     * @return true если мина может быть активирована,
     *         false если мина уже активна
     */
    public boolean canActivate() {
        return !activated;
    }

    /**
     * Полностью сбрасывает состояние мины.
     * Устанавливает все флаги в значения по умолчанию.
     */
    public void reset() {
        this.activated = false;
        this.revealedShip = false;
    }

    /**
     * Вычисляет расстояние от мины до указанной клетки.
     * Используется манхэттенское расстояние (сумма разностей по осям).
     *
     * @param targetRow строка целевой клетки
     * @param targetCol столбец целевой клетки
     * @return расстояние в клетках от мины до цели
     */
    public int distanceTo(int targetRow, int targetCol) {
        return Math.abs(row - targetRow) + Math.abs(col - targetCol);
    }

    /**
     * Проверяет, находится ли клетка в пределах заданного радиуса от мины.
     *
     * @param targetRow строка целевой клетки
     * @param targetCol столбец целевой клетки
     * @param range радиус проверки
     * @return true если клетка находится в радиусе, false в противном случае
     */
    public boolean isInRange(int targetRow, int targetCol, int range) {
        return distanceTo(targetRow, targetCol) <= range;
    }

    /**
     * Возвращает номер строки, в которой расположена мина.
     *
     * @return номер строки
     */
    public int getRow() {
        return row;
    }

    /**
     * Возвращает номер столбца, в котором расположена мина.
     *
     * @return номер столбца
     */
    public int getCol() {
        return col;
    }

    /**
     * Проверяет, активна ли мина.
     *
     * @return true если мина активна, false если неактивна
     */
    public boolean isActive() {
        return activated;
    }

    /**
     * Проверяет, раскрыла ли мина координату корабля.
     *
     * @return true если мина раскрыла корабль, false в противном случае
     */
    public boolean hasRevealedShip() {
        return revealedShip;
    }

    /**
     * Устанавливает новые координаты для мины.
     * Используется при перемещении мины или изменении ее положения.
     *
     * @param row новый номер строки
     * @param col новый номер столбца
     */
    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Возвращает строковое представление мины для отладки.
     * Содержит координаты и текущее состояние мины.
     *
     * @return строковое представление мины
     */
    @Override
    public String toString() {
        return String.format("Mine[%d,%d] activated=%s revealedShip=%s",
                row, col, activated, revealedShip);
    }

    /**
     * Сравнивает эту мину с другим объектом.
     * Мины считаются равными, если они находятся в одинаковых координатах.
     *
     * @param obj объект для сравнения
     * @return true если объекты равны, false в противном случае
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Mine mine = (Mine) obj;
        return row == mine.row && col == mine.col;
    }

    /**
     * Возвращает хэш-код мины.
     * Хэш-код вычисляется на основе координат мины.
     *
     * @return хэш-код мины
     */
    @Override
    public int hashCode() {
        return 31 * row + col;
    }
}