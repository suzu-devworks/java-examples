package jp.kogenet.example;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.JapaneseDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


public class DateAndTimeTests {

    @Test
    @Tag("datetime-generators")
    void testGenerateByNow() {

        LocalDateTime now = LocalDateTime.now(); // 2018-01-12T13:14:15.167
        LocalDate today = LocalDate.now(); // 2018-01-12
        LocalTime nowOfTimeOnly = LocalTime.now(); // 13:14:15.167

        // 2018-01-12T13:14:15.167+09:00
        OffsetDateTime nowByOffsetDateTime01 = OffsetDateTime.now();
        OffsetDateTime nowByOffsetDateTime02 =
                OffsetDateTime.now(ZoneId.of("Asia/Tokyo"));
        OffsetDateTime nowByOffsetDateTime03 =
                OffsetDateTime.now(ZoneId.systemDefault());
        OffsetDateTime nowByOffsetDateTime04 =
                OffsetDateTime.now(ZoneOffset.ofHours(9));
        // 2018-01-12T04:14:15.167Z
        OffsetDateTime nowByOffsetDateTimeUTC =
                OffsetDateTime.now(ZoneOffset.UTC);

        // 13:14:15.167+09:00
        OffsetTime nowByOffsetTime01 = OffsetTime.now();
        OffsetTime nowByOffsetTime02 = OffsetTime.now(ZoneId.of("Asia/Tokyo"));
        OffsetTime nowByOffsetTime03 = OffsetTime.now(ZoneId.systemDefault());
        OffsetTime nowByOffsetTime04 = OffsetTime.now(ZoneOffset.ofHours(9));
        // 04:14:15.167Z
        OffsetTime nowByOffsetTimeUTC = OffsetTime.now(ZoneOffset.UTC);

        // 2018-01-12T13:14:15.167+09:00[Asia/Tokyo]
        ZonedDateTime nowByZoneDateTime01 = ZonedDateTime.now();
        ZonedDateTime nowByZoneDateTime02 =
                ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime nowByZoneDateTime03 =
                ZonedDateTime.now(ZoneId.of("Asia/Tokyo"));
        // 2018-01-12T13:14:15.167+09:00
        ZonedDateTime nowByZoneDateTime04 =
                ZonedDateTime.now(ZoneOffset.ofHours(9));
        // 2018-01-12T04:14:15.167Z
        ZonedDateTime nowByZoneDateTimeUTC = ZonedDateTime.now(ZoneOffset.UTC);

        // @formatter:off
        assertAll(
            () -> assertNotNull(now), 
            () -> assertNotNull(today),
            () -> assertNotNull(nowOfTimeOnly),
            () -> assertNotNull(nowByOffsetDateTime01),
            () -> assertNotNull(nowByOffsetDateTime02),
            () -> assertNotNull(nowByOffsetDateTime03),
            () -> assertNotNull(nowByOffsetDateTime04),
            () -> assertNotNull(nowByOffsetDateTimeUTC),
            () -> assertNotNull(nowByOffsetTime01),
            () -> assertNotNull(nowByOffsetTime02),
            () -> assertNotNull(nowByOffsetTime03),
            () -> assertNotNull(nowByOffsetTime04),
            () -> assertNotNull(nowByOffsetTimeUTC),
            () -> assertNotNull(nowByZoneDateTime01),
            () -> assertNotNull(nowByZoneDateTime02),
            () -> assertNotNull(nowByZoneDateTime03),
            () -> assertNotNull(nowByZoneDateTime04),
            () -> assertNotNull(nowByZoneDateTimeUTC)
        );
        // @formatter:on
    }

    @Test
    @Tag("datetime-generators")
    void testGenerateByOf() {

        LocalDateTime localDateTime =
                LocalDateTime.of(2018, 1, 12, 13, 14, 15, 167000000);

        LocalDate localDate = LocalDate.of(2018, 1, 12);
        LocalDate localDateByLocalDateTime = localDateTime.toLocalDate();
        LocalTime localTime = LocalTime.of(13, 14, 15, 167000000);
        LocalTime localTimeByLocalDateTime = localDateTime.toLocalTime();

        OffsetDateTime offsetDateTime = OffsetDateTime.of(2018, 1, 12, 13, 14,
                15, 167000000, ZoneOffset.ofHours(9));
        OffsetDateTime offsetDateTimeByLocalDateTime =
                OffsetDateTime.of(localDateTime, ZoneOffset.ofHours(9));

        OffsetTime offsetTime =
                OffsetTime.of(13, 14, 15, 167000000, ZoneOffset.ofHours(9));
        OffsetTime offsetTimeByLocalTime =
                OffsetTime.of(localTime, ZoneOffset.ofHours(9));

        ZonedDateTime zonedDateTime = ZonedDateTime.of(2018, 1, 12, 13, 14, 15,
                167000000, ZoneId.systemDefault());
        ZonedDateTime zonedDateTimeByLocalDateTime =
                ZonedDateTime.of(localDateTime, ZoneId.systemDefault());

        // @formatter:off
        assertAll(
            () -> assertNotNull(localDateTime), 
            () -> assertNotNull(localDate),
            () -> assertNotNull(localDateByLocalDateTime),
            () -> assertNotNull(localTime),
            () -> assertNotNull(localTimeByLocalDateTime),
            () -> assertNotNull(offsetDateTime),
            () -> assertNotNull(offsetDateTimeByLocalDateTime),
            () -> assertNotNull(offsetTime),
            () -> assertNotNull(offsetTimeByLocalTime),
            () -> assertNotNull(zonedDateTime),
            () -> assertNotNull(zonedDateTimeByLocalDateTime)
        );
        // @formatter:on
    }


    @Test
    @Tag("datetime-formatters")
    void testFormatting() {

        ZonedDateTime date1 = ZonedDateTime.of(2018, 1, 12, 13, 14, 15,
                167890123, ZoneId.systemDefault());

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
                "yyyy-MM-dd EEEE HH:mm:ss.SSSSSSSSS a XXX z '['VV']' ");
        assertEquals(
                "2018-01-12 金曜日 13:14:15.167890123 午後 +09:00 JST [Asia/Tokyo] ",
                date1.format(dateTimeFormatter));

        DateTimeFormatter dateTimeFormatterEn = DateTimeFormatter.ofPattern(
                "yyyy-MM-dd EEEE HH:mm:ss.SSSSSSSSS a XXX z '['VV']' ",
                Locale.ENGLISH);
        assertEquals(
                "2018-01-12 Friday 13:14:15.167890123 PM +09:00 JST [Asia/Tokyo] ",
                date1.format(dateTimeFormatterEn));

        assertEquals("2018-01-12T13:14:15.167890123",
                date1.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        assertEquals("2018-01-12T13:14:15.167890123+09:00",
                date1.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        assertEquals("2018-01-12T13:14:15.167890123+09:00[Asia/Tokyo]",
                date1.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));

        DateTimeFormatter dateTimeFormatterJa =
                DateTimeFormatter.ofPattern("G yy-MM-dd");
        assertEquals("平成 31-04-30",
                JapaneseDate.of(2019, 4, 30).format(dateTimeFormatterJa));
        assertEquals("令和 01-05-01",
                JapaneseDate.of(2019, 5, 1).format(dateTimeFormatterJa));

    }


    @Test
    @Tag("datetime-formatters")
    void testPerse() {

        ZonedDateTime date1 = ZonedDateTime.of(2018, 1, 12, 13, 14, 15,
                167890123, ZoneId.systemDefault());

        String zonedFormatted =
                date1.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
        assertEquals(date1, ZonedDateTime.parse(zonedFormatted,
                DateTimeFormatter.ISO_ZONED_DATE_TIME));

        String offsetFormatted =
                date1.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        assertEquals(date1.toOffsetDateTime(), OffsetDateTime.parse(
                offsetFormatted, DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        String localFormatted =
                date1.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        assertEquals(date1.toLocalDateTime(), LocalDateTime
                .parse(localFormatted, DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // fall throw
        assertEquals(LocalDate.of(2000, 2, 29), LocalDate.parse("2000-02-30",
                DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        assertEquals(LocalDate.of(2000, 2, 29), LocalDate.parse("2000-02-30",
                DateTimeFormatter.ofPattern("uuuu-MM-dd")));

        // STRICT
        assertThrows(DateTimeParseException.class,
                () -> LocalDate.parse("2000-02-30",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                .withResolverStyle(ResolverStyle.STRICT)));
        assertThrows(DateTimeParseException.class,
                () -> LocalDate.parse("2000-02-30",
                        DateTimeFormatter.ofPattern("uuuu-MM-dd")
                                .withResolverStyle(ResolverStyle.STRICT)));
        assertThrows(DateTimeParseException.class, () -> LocalDate
                .parse("2000-02-30", DateTimeFormatter.ISO_LOCAL_DATE));
        assertThrows(DateTimeParseException.class,
                () -> LocalDate.parse("2000-02-29",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                .withResolverStyle(ResolverStyle.STRICT)));
        assertEquals(LocalDate.of(2000, 2, 29),
                LocalDate.parse("2000-02-29",
                        DateTimeFormatter.ofPattern("uuuu-MM-dd")
                                .withResolverStyle(ResolverStyle.STRICT)));
        assertEquals(LocalDate.of(2000, 2, 29), LocalDate.parse("2000-02-29",
                DateTimeFormatter.ISO_LOCAL_DATE));

        // yyyy は G(era) と Locale を指定する必要があります。
        assertEquals(LocalDate.of(2000, 2, 29),
                LocalDate.parse("2000-02-29 AD",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd G")
                                .withLocale(Locale.ENGLISH)
                                .withResolverStyle(ResolverStyle.STRICT)));
    }



    @Test
    @Tag("datetime-calclation")
    void testPlusMinusWithTruncate() {

        // 2018-01-12T13:14:15.167890123+09:00[Asia/Tokyo]
        ZonedDateTime date1 = ZonedDateTime.of(2018, 1, 12, 13, 14, 15,
                167890123, ZoneId.systemDefault());

        assertEquals(
                ZonedDateTime.parse(
                        "2018-01-23T09:44:15.167890123+09:00[Asia/Tokyo]",
                        DateTimeFormatter.ISO_ZONED_DATE_TIME),
                date1.plusDays(10L).plusHours(20L).plusMinutes(30L));

        assertEquals(
                ZonedDateTime.parse(
                        "2018-01-23T09:44:15.167890123+09:00[Asia/Tokyo]",
                        DateTimeFormatter.ISO_ZONED_DATE_TIME),
                date1.minusDays(-10L).minusHours(-20L).minusMinutes(-30L));

        assertEquals(
                ZonedDateTime.parse("2018-01-31T01:00:00+09:00[Asia/Tokyo]",
                        DateTimeFormatter.ISO_ZONED_DATE_TIME),
                date1.plusMonths(1L).withDayOfMonth(1).minusDays(1L).withHour(1)
                        .truncatedTo(ChronoUnit.HOURS));

    }


    @Test
    @Tag("datetime-compareation")
    void testBetween() {

        LocalDateTime date1 =
                LocalDateTime.of(2018, 1, 12, 13, 14, 15, 167890123);
        LocalDateTime date2 =
                LocalDateTime.of(2020, 5, 1, 21, 22, 23, 167890123);

        Period period =
                Period.between(date1.toLocalDate(), date2.toLocalDate());

        Duration duration =
                Duration.between(date1.toLocalTime(), date2.toLocalTime());

        assertEquals(Period.of(2, 3, 19), period);
        assertEquals(Duration.ofSeconds(29288L), duration);
        assertEquals(29288L, duration.toSeconds());

    }


    @Test
    @Tag("datetime-compareation")
    void testComparatioon() {

        ZonedDateTime date1 = ZonedDateTime.of(2018, 1, 12, 13, 14, 15,
                167890123, ZoneId.systemDefault());
        ZonedDateTime date2 = ZonedDateTime.of(2020, 5, 1, 21, 22, 23,
                167890123, ZoneId.systemDefault());
        ZonedDateTime date3 = ZonedDateTime.of(2018, 1, 12, 13, 14, 15,
                167890123, ZoneId.systemDefault());

        assertEquals(-1, date1.compareTo(date2));
        assertEquals(0, date1.compareTo(date3));
        assertEquals(1, date2.compareTo(date1));

        assertTrue(date1.isBefore(date2));
        assertTrue(date1.isEqual(date3));
        assertTrue(date2.isAfter(date1));

    }


    @Test
    @Tag("datetime-convertors")
    void testToLocalDateTime() {

        LocalDateTime expected =
                LocalDateTime.of(2018, 1, 12, 13, 14, 15, 167890123);

        LocalDate localDate = LocalDate.of(2018, 1, 12);
        assertEquals(expected,
                localDate.atTime(LocalTime.of(13, 14, 15, 167890123)));

        LocalTime localTime = LocalTime.of(13, 14, 15, 167890123);
        assertEquals(expected, localTime.atDate(LocalDate.of(2018, 1, 12)));

        OffsetDateTime offsetDateTime = OffsetDateTime.of(2018, 1, 12, 13, 14,
                15, 167890123, ZoneOffset.ofHours(9));
        assertEquals(expected, offsetDateTime.toLocalDateTime());

        OffsetTime offsetTime =
                OffsetTime.of(13, 14, 15, 167890123, ZoneOffset.ofHours(9));
        assertEquals(expected,
                offsetTime.toLocalTime().atDate(LocalDate.of(2018, 1, 12)));

        ZonedDateTime zonedDateTime = ZonedDateTime.of(2018, 1, 12, 13, 14, 15,
                167890123, ZoneId.systemDefault());
        assertEquals(expected, zonedDateTime.toLocalDateTime());

    }


    @Test
    @Tag("datetime-convertors")
    void testToOffsetDateTime() {

        OffsetDateTime expected = OffsetDateTime.of(2018, 1, 12, 13, 14, 15,
                167890123, ZoneOffset.ofHours(9));

        LocalDateTime localDateTime =
                LocalDateTime.of(2018, 1, 12, 13, 14, 15, 167890123);
        assertEquals(expected, localDateTime.atOffset(ZoneOffset.ofHours(9)));

        LocalDate localDate = LocalDate.of(2018, 1, 12);
        assertEquals(expected, localDate.atTime(
                OffsetTime.of(13, 14, 15, 167890123, ZoneOffset.ofHours(9))));

        LocalTime localTime = LocalTime.of(13, 14, 15, 167890123);
        assertEquals(expected, localTime.atOffset(ZoneOffset.ofHours(9))
                .atDate(LocalDate.of(2018, 1, 12)));

        OffsetTime offsetTime =
                OffsetTime.of(13, 14, 15, 167890123, ZoneOffset.ofHours(9));
        assertEquals(expected, offsetTime.atDate(LocalDate.of(2018, 1, 12)));

        ZonedDateTime zonedDateTime = ZonedDateTime.of(2018, 1, 12, 13, 14, 15,
                167890123, ZoneId.systemDefault());
        assertEquals(expected, zonedDateTime.toOffsetDateTime());

    }


    @Test
    @Tag("datetime-convertors")
    void testToZonedDateTime() {

        ZonedDateTime expected = ZonedDateTime.of(2018, 1, 12, 13, 14, 15,
                167890123, ZoneId.systemDefault());

        LocalDateTime localDateTime =
                LocalDateTime.of(2018, 1, 12, 13, 14, 15, 167890123);
        assertEquals(expected, localDateTime.atZone(ZoneId.systemDefault()));

        LocalDate localDate = LocalDate.of(2018, 1, 12);
        assertEquals(expected,
                localDate.atTime(LocalTime.of(13, 14, 15, 167890123))
                        .atZone(ZoneId.systemDefault()));

        LocalTime localTime = LocalTime.of(13, 14, 15, 167890123);
        assertEquals(expected, localTime.atDate(LocalDate.of(2018, 1, 12))
                .atZone(ZoneId.systemDefault()));

        OffsetDateTime offsetDateTime = OffsetDateTime.of(2018, 1, 12, 13, 14,
                15, 167890123, ZoneOffset.ofHours(9));
        assertEquals(expected,
                offsetDateTime.atZoneSameInstant(ZoneId.systemDefault()));
        // atZoneSimilarLocal(ZoneId zone) ... 今のローカル日時を維持して作成する。
        // toZonedDateTime() ... ZoneId が +09:00 のみになる。
        // もあるが、用途としてはInstantで設定する用途が多そうだ。

        OffsetTime offsetTime =
                OffsetTime.of(13, 14, 15, 167890123, ZoneOffset.ofHours(9));
        assertEquals(expected, offsetTime.atDate(LocalDate.of(2018, 1, 12))
                .atZoneSameInstant(ZoneId.systemDefault()));

    }


    @Test
    @Tag("datetime-compatibility")
    void testConvertJavaSQLDateTime() {

        LocalDateTime expected =
                LocalDateTime.of(2018, 1, 12, 13, 14, 15, 167890123);

        java.sql.Timestamp javaSqlTimestamp =
                java.sql.Timestamp.valueOf(expected);
        assertEquals(expected, javaSqlTimestamp.toLocalDateTime());

        java.sql.Date javaSqlDate =
                java.sql.Date.valueOf(expected.toLocalDate());
        assertEquals(expected.toLocalDate(), javaSqlDate.toLocalDate());

        // java.sql.Time へ秒以下が渡らない？
        java.sql.Time javaSqlTime =
                java.sql.Time.valueOf(expected.toLocalTime());
        assertEquals(expected.toLocalTime().truncatedTo(ChronoUnit.SECONDS),
                javaSqlTime.toLocalTime());

    }


    @Test
    @Tag("datetime-compatibility")
    void testConvertJavaUtilDate() {

        LocalDateTime expected =
                LocalDateTime.of(2018, 1, 12, 13, 14, 15, 167890123);

        ZonedDateTime zonedDateTime = expected.atZone(ZoneId.systemDefault());
        Instant instant = zonedDateTime.toInstant();
        java.util.Date date = java.util.Date.from(instant);

        // ミリ秒までしか復元できない？
        assertEquals(expected.truncatedTo(ChronoUnit.MILLIS), LocalDateTime
                .ofInstant(date.toInstant(), ZoneId.systemDefault()));

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(date);
        assertEquals(expected.truncatedTo(ChronoUnit.MILLIS), LocalDateTime
                .ofInstant(calendar.toInstant(), ZoneId.systemDefault()));

    }


    @Test
    @Tag("datetime-compatibility")
    void testConvertFileTime() {

        ClassLoader classLoader = getClass().getClassLoader();
        Path filePath =
                Paths.get(classLoader.getResource("somefile.txt").getFile());

        try {
            FileTime filetime = Files.getLastModifiedTime(filePath);

            LocalDateTime actual = LocalDateTime.ofInstant(filetime.toInstant(),
                    ZoneId.systemDefault());

            ZonedDateTime zonedDateTime = actual.atZone(ZoneId.systemDefault());
            Instant instant = zonedDateTime.toInstant();
            assertEquals(filetime, FileTime.from(instant));

        } catch (IOException ex) {
            fail(ex.toString());
        }

    }


    @Test
    @Tag("datetime-compatibility")
    void testConvertEpoch() {

        LocalDateTime expected =
                LocalDateTime.of(2018, 1, 12, 13, 14, 15, 167890123);

        ZonedDateTime zonedDateTime = expected.atZone(ZoneId.systemDefault());
        Instant instant = zonedDateTime.toInstant();

        long epochMilli = instant.toEpochMilli();
        long epochSecond = instant.getEpochSecond();

        // ミリ秒までの精度しかない。
        assertEquals(expected.truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli),
                        ZoneId.systemDefault()));

        // そもそも秒精度。
        assertEquals(expected.truncatedTo(ChronoUnit.SECONDS),
                LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSecond),
                        ZoneId.systemDefault()));

        //ちなみに java.util.Date はミリ秒精度。
        assertDoesNotThrow(() -> new java.util.Date(epochMilli));
        assertDoesNotThrow(() -> new java.util.Date(epochSecond * 1000L));

    }
}
