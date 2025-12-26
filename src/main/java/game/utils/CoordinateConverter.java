package game.utils;

import game.config.GameConfig;

/**
 * Утилитный класс для преобразования координат между числовым и буквенным форматами.
 * Используется для отображения координат на игровом поле в удобочитаемом виде.
 */
public class CoordinateConverter {

    /**
     * Преобразует числовой индекс колонки в буквенное обозначение.
     * Выбор алфавита (русский или английский) определяется конфигурацией игры.
     *
     * @param col числовой индекс колонки (начинается с 0)
     * @return буквенное обозначение колонки (например, "A", "Б") или числовое представление в виде строки,
     *         если индекс выходит за допустимые пределы
     */
    public static String toLetterCoordinate(int col) {
        if (GameConfig.useRussianAlphabet()) {
            String letters = "абвгдежзик";
            if (col >= 0 && col < letters.length()) {
                return String.valueOf(letters.charAt(col));
            }
        } else {
            if (col >= 0 && col < 26) {
                return String.valueOf((char) ('A' + col));
            }
        }
        return String.valueOf(col + 1);
    }

    /**
     * Преобразует буквенное обозначение колонки обратно в числовой индекс.
     * Учитывает регистр ввода и настройку используемого алфавита.
     *
     * @param coord строковое представление координаты, первый символ которой является буквой колонки
     * @return числовой индекс колонки (начинается с 0) или -1, если преобразование невозможно
     */
    public static int fromLetterCoordinate(String coord) {
        if (coord == null || coord.isEmpty()) return -1;

        char firstChar = coord.toUpperCase().charAt(0);
        if (GameConfig.useRussianAlphabet()) {
            String letters = "абвгдежзик";
            return letters.indexOf(Character.toLowerCase(firstChar));
        } else {
            if (firstChar >= 'A' && firstChar <= 'Z') {
                return firstChar - 'A';
            }
        }
        return -1;
    }

    /**
     * Форматирует координаты клетки в удобочитаемую строку вида "БукваНомер".
     * Пример: для row=0, col=1 вернет "B2" (при английском алфавите).
     *
     * @param row номер строки (начинается с 0)
     * @param col номер колонки (начинается с 0)
     * @return отформатированная строка координат
     */
    public static String formatCoordinate(int row, int col) {
        return toLetterCoordinate(col) + (row + 1);
    }
}