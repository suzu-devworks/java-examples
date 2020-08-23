package example;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofMinutes;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Test;
import example.domain.Person;
import example.util.Calculator;

class AssertionsDemo {

    private final Calculator calculator = new Calculator();

    private final Person person = new Person("Jane", "Doe");

    @Test
    void standardAssertions() {
        assertEquals(2, calculator.add(1, 1));
        assertEquals(4, calculator.multiply(2, 2),
                "省略可能なアサーションメッセージは最後のパラメーター");
        assertTrue('a' < 'b', () -> "アサーションメッセージは遅延評価できる -- "
                + "不必要に複雑なメッセージを構築するコストを割けるために");
    }

    @Test
    void groupedAssertions() {
        assumeTrue(false, "suppress fail.");

        // アサーションをグループ化すると、すべてのアサーションが一度に実行され、
        // すべての失敗がまとめて報告される。
        // @formatter:off
        assertAll("person", 
            () -> assertEquals("John", person.getFirstName()),
            () -> assertEquals("doe", person.getLastName())
        );
        // @formatter:on
    }

    @Test
    void dependentAssertions() {
        // コードブロック内でアサーションが失敗すると、同じブロック内の後続のコードはスキップされる。
        assertAll("properties", () -> {
            String firstName = person.getFirstName();
            assertNotNull(firstName);

            // 上のアサーションが成功した場合のみ実行される。
            // @formatter:off
            assertAll("first name", 
                () -> assertTrue(firstName.startsWith("J")),
                () -> assertTrue(firstName.endsWith("e")));
            // @formatter:on
        }, () -> {
            // グループ化されたアサーションは、first name のアサーションとは独立して実行される。
            String lastName = person.getLastName();
            assertNotNull(lastName);

            // 上のアサーションが成功した場合のみ実行される。
            // @formatter:off
            assertAll("last name",
                () -> assertTrue(lastName.startsWith("D")),
                () -> assertTrue(lastName.endsWith("e")));
            // @formatter:on
        });
    }

    @Test
    void exceptionTesting() {
        Exception exception = assertThrows(ArithmeticException.class,
                () -> calculator.divide(1, 0));
        assertEquals("/ by zero", exception.getMessage());
    }

    @Test
    void timeoutNotExceeded() {
        // 次のアサーションは成功する。
        assertTimeout(ofMinutes(2), () -> {
            // 2分未満で終わるタスクを実行する。
        });
    }

    @Test
    void timeoutNotExceededWithResult() {
        // 次のアサーションは成功し、指定されたオブジェクトを返す。
        String actualResult = assertTimeout(ofMinutes(2), () -> {
            return "a result";
        });
        assertEquals("a result", actualResult);
    }

    @Test
    void timeoutNotExceededWithMethod() {
        // 次のアサーションは、メソッド参照を実行してオブジェクトを返す。
        String actualGreeting =
                assertTimeout(ofMinutes(2), AssertionsDemo::greeting);
        assertEquals("Hello, World!", actualGreeting);
    }

    @Test
    void timeoutExceeded() {
        assumeTrue(false, "suppress fail.");

        // 次のアサーションは、以下のようなエラーメッセージを出して失敗する:
        // execution exceeded timeout of 10 ms by 91 ms
        assertTimeout(ofMillis(10), () -> {
            // 10ミリ秒より時間のかかるタスクをシミュレートする。
            Thread.sleep(100);
        });
    }

    @Test
    void timeoutExceededWithPreemptiveTermination() {
        assumeTrue(false, "suppress fail.");

        // 次のアサーションは、以下のようなエラーメッセージを出して失敗する:
        // execution timed out after 10 ms
        assertTimeoutPreemptively(ofMillis(10), () -> {
            // 10ミリ秒より時間のかかるタスクをシミュレートする。
            new CountDownLatch(1).await();
        });
    }

    private static String greeting() {
        return "Hello, World!";
    }

}
