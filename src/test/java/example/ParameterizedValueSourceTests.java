package example;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import example.util.StringUtils;

public class ParameterizedValueSourceTests {

    @ParameterizedTest
    @ValueSource(strings = {"racecar", "radar", "able was I ere I saw elba"})
    void palindromes(String candidate) {
        assertTrue(StringUtils.isPalindrome(candidate));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void testWithValueSource(int argument) {
        assertTrue(argument > 0 && argument < 4);
    }

    @Nested
    class NullAndEmptySource_1 {

        @ParameterizedTest
        @NullSource
        @EmptySource
        @ValueSource(strings = {" ", "   ", "\t", "\n"})
        void nullEmptyAndBlankStrings(String text) {
            assertTrue(text == null || text.trim().isEmpty());
        }
    }

    @Nested
    class NullAndEmptySource_2 {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t", "\n"})
        void nullEmptyAndBlankStrings(String text) {
            assertTrue(text == null || text.trim().isEmpty());
        }
    }

}
