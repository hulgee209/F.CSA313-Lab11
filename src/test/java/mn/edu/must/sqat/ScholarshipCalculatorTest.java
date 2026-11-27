package mn.edu.must.sqat;

// ЛАБ 11 — B1 + B2.
// B1: assertion-уудыг ЯГ УТГЫН assertEquals болгосон.
// B2: зааг утгын тестүүд (90/89.99, 80/79.99, 70/69.99, 60/59.99, 0, 100;
//     scholarship-ийн 3.8/15, 3.4/12, 3.0 заагууд, gpa 0 ба 4, credits 0),
//     мөн && нөхцөлийн аль нэг хэсэг нь л биелэх хослолууд, gpa-гийн
//     switch-ийн бүх үсгэн дүн.

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class ScholarshipCalculatorTest {

    private static final double EPS = 1e-9;

    ScholarshipCalculator calc;

    @BeforeEach
    void setUp() { calc = new ScholarshipCalculator(); }

    // =====================================================================
    // B1 — яг утгын assertion
    // =====================================================================

    @Test
    void letterGrade_returnsExactLetterForAllRanges() {
        assertEquals("A", calc.letterGrade(95));
        assertEquals("B", calc.letterGrade(85));
        assertEquals("C", calc.letterGrade(75));
        assertEquals("D", calc.letterGrade(65));
        assertEquals("F", calc.letterGrade(30));
    }

    @Test
    void letterGrade_invalidInputs_throw() {
        assertThrows(IllegalArgumentException.class, () -> calc.letterGrade(-1));
        assertThrows(IllegalArgumentException.class, () -> calc.letterGrade(101));
    }

    @Test
    void gpa_returnsExactAverage() {
        assertEquals(3.5, calc.gpa(new double[]{95, 85}), EPS); // (4+3)/2
        assertEquals(1.5, calc.gpa(new double[]{75, 65}), EPS); // (2+1)/2
        assertEquals(0.0, calc.gpa(new double[]{40}), EPS);     // F = 0
    }

    @Test
    void gpa_invalidInputs_throw() {
        assertThrows(IllegalArgumentException.class, () -> calc.gpa(null));
        assertThrows(IllegalArgumentException.class, () -> calc.gpa(new double[]{}));
    }

    @Test
    void scholarship_returnsExactPercent() {
        assertEquals(100, calc.scholarship(3.9, 16, false));
        assertEquals(70,  calc.scholarship(3.5, 13, false));
        assertEquals(50,  calc.scholarship(3.1, 5, false));
        assertEquals(0,   calc.scholarship(2.0, 1, false));
        assertEquals(0,   calc.scholarship(4.0, 20, true));
    }

    @Test
    void scholarship_invalidInputs_throw() {
        assertThrows(IllegalArgumentException.class, () -> calc.scholarship(4.1, 10, false));
        assertThrows(IllegalArgumentException.class, () -> calc.scholarship(3.0, -1, false));
    }

    // =====================================================================
    // B2 — letterGrade-ийн зааг утгууд
    // =====================================================================

    @ParameterizedTest(name = "letterGrade({0}) = {1}")
    @CsvSource({
        "100,   A",   // дээд хязгаар (score > 100 → >= 100 мутантыг алдаг)
        "90,    A",   // A-ийн доод зааг
        "89.99, B",
        "80,    B",   // B-ийн доод зааг
        "79.99, C",
        "70,    C",   // C-ийн доод зааг
        "69.99, D",
        "60,    D",   // D-ийн доод зааг
        "59.99, F",
        "0,     F"    // доод хязгаар (score < 0 → <= 0 мутантыг алдаг)
    })
    void letterGrade_boundaries(double score, String expected) {
        assertEquals(expected, calc.letterGrade(score));
    }

    @Test
    void letterGrade_justOutsideRange_throws() {
        assertThrows(IllegalArgumentException.class, () -> calc.letterGrade(-0.01));
        assertThrows(IllegalArgumentException.class, () -> calc.letterGrade(100.01));
    }

    // =====================================================================
    // B2 — gpa: switch-ийн бүх үсэг тус бүрдээ, нэмэх үйлдлийг яг шалгана
    // =====================================================================

    @ParameterizedTest(name = "gpa([{0}]) = {1}")
    @CsvSource({
        "95, 4.0",   // A
        "85, 3.0",   // B
        "75, 2.0",   // C
        "65, 1.0",   // D
        "40, 0.0"    // F (default)
    })
    void gpa_singleLetter_exactPoints(double score, double expected) {
        assertEquals(expected, calc.gpa(new double[]{score}), EPS);
    }

    @Test
    void gpa_mixedAllLetters_exactAverage() {
        // (4 + 3 + 2 + 1 + 0) / 5 = 2.0
        assertEquals(2.0, calc.gpa(new double[]{90, 80, 70, 60, 59.99}), EPS);
        // (4 + 4 + 3) / 3
        assertEquals(11.0 / 3, calc.gpa(new double[]{100, 90, 80}), EPS);
    }

    @Test
    void gpa_invalidScoreInside_throws() {
        assertThrows(IllegalArgumentException.class, () -> calc.gpa(new double[]{90, 120}));
    }

    // =====================================================================
    // B2 — scholarship-ийн заагууд ба && нөхцөлийн хослолууд
    // =====================================================================

    @ParameterizedTest(name = "scholarship({0}, {1}, false) = {2}")
    @CsvSource({
        // 100%-ийн дүрэм: gpa >= 3.8 && credits >= 15
        "3.8,  15, 100",   // хоёулаа яг зааг дээр
        "3.79, 15,  70",   // gpa заагаас доош, кредит хангалттай
        "3.8,  14,  70",   // gpa хангалттай, кредит заагаас доош
        "3.5,  16,  70",   // зөвхөн кредитийн нөхцөл биелнэ
        "3.9,  14,  70",   // зөвхөн gpa-гийн нөхцөл биелнэ
        // 70%-ийн дүрэм: gpa >= 3.4 && credits >= 12
        "3.4,  12,  70",   // хоёулаа яг зааг дээр
        "3.39, 12,  50",   // gpa заагаас доош
        "3.4,  11,  50",   // кредит заагаас доош
        "3.1,  13,  50",   // зөвхөн кредитийн нөхцөл биелнэ
        "3.5,  11,  50",   // зөвхөн gpa-гийн нөхцөл биелнэ
        // 50%-ийн дүрэм: gpa >= 3.0
        "3.0,   0,  50",   // яг зааг дээр, кредит 0 (хүчинтэй доод хязгаар)
        "2.99, 20,   0",   // заагаас доош
        // оролтын хязгаарууд (хүчинтэй)
        "0.0,   0,   0",   // gpa = 0, credits = 0
        "4.0,   0,  50",   // gpa = 4, кредит хангалтгүй
        "4.0,  15, 100"    // gpa = 4, дээд дүрэм
    })
    void scholarship_boundariesAndCombinations(double gpa, int credits, int expected) {
        assertEquals(expected, calc.scholarship(gpa, credits, false));
    }

    @Test
    void scholarship_violationAlwaysZero() {
        assertEquals(0, calc.scholarship(3.8, 15, true));
        assertEquals(0, calc.scholarship(3.4, 12, true));
        assertEquals(0, calc.scholarship(3.0, 0, true));
    }

    @Test
    void scholarship_outOfRangeInputs_throw() {
        assertThrows(IllegalArgumentException.class, () -> calc.scholarship(-0.01, 10, false));
        assertThrows(IllegalArgumentException.class, () -> calc.scholarship(4.01, 10, false));
        assertThrows(IllegalArgumentException.class, () -> calc.scholarship(3.0, -1, false));
        // хүчингүй оролт сахилгын шалгалтаас ӨМНӨ барина
        assertThrows(IllegalArgumentException.class, () -> calc.scholarship(-1, 10, true));
    }
}
