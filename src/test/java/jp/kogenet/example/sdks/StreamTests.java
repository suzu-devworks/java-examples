package jp.kogenet.example.sdks;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StreamTests {

    @Test
    void testFlatMap() {

        final int[][] values = { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } };
        System.out.println("do map");
        var arrayOfArray = Arrays.stream(values).map(x -> x).toArray();
        Assertions.assertEquals(3, arrayOfArray.length);

        System.out.println("do flatMap");
        var arrayOfInt = Arrays.stream(values)
                .flatMapToInt(x -> Arrays.stream(x)).toArray();

        Assertions.assertEquals(9, arrayOfInt.length);
        final int[] expected = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        Assertions.assertArrayEquals(expected, arrayOfInt);
    }
}
