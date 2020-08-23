package example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assumptions.assumingThat;
import org.junit.jupiter.api.Test;

class AssumptionsDemo {

    @Test
    void testOnlyOnCiServer() {
        assumeTrue("CI".equals(System.getenv("ENV")));
        // 残りのテスト
    }

    @Test
    void testOnlyOnDeveloperWorkstation() {
        assumeTrue("DEV".equals(System.getenv("ENV")),
                () -> "テストを中断: 開発者マシンではない");
        // 残りのテスト
    }

    @Test
    void testInAllEnvironments() {
        assumingThat("CI".equals(System.getenv("ENV")), () -> {
            // これらのアサーションはCIサーバーでのみ実行する
            assertEquals(2, 2);
        });

        // これらのアサーションはすべての環境で実行する
        assertEquals("a string", "a string");
    }

}
