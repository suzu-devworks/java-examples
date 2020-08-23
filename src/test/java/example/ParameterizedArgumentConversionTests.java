package example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.converter.JavaTimeConversionPattern;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import example.domain.Person;
import example.domain.Person.Gender;

public class ParameterizedArgumentConversionTests {

    @ParameterizedTest
    @ValueSource(strings = "SECONDS")
    void testWithImplicitArgumentConversion(ChronoUnit argument) {
        assertNotNull(argument.name());
    }


    @ParameterizedTest
    @ValueSource(strings = "42 Cats")
    void testWithImplicitFallbackArgumentConversion(Book book) {
        assertEquals("42 Cats", book.getTitle());
    }

    static public class Book {

        private final String title;

        private Book(String title) {
            this.title = title;
        }

        public static Book fromTitle(String title) {
            return new Book(title);
        }

        public String getTitle() {
            return this.title;
        }
    }


    @ParameterizedTest
    @EnumSource(ChronoUnit.class)
    void testWithExplicitArgumentConversion(
            @ConvertWith(ToStringArgumentConverter.class) String argument) {

        assertNotNull(ChronoUnit.valueOf(argument));
    }

    static public class ToStringArgumentConverter
            extends SimpleArgumentConverter {

        @Override
        protected Object convert(Object source, Class<?> targetType) {
            assertEquals(String.class, targetType,
                    "Can only convert to String");
            if (source instanceof Enum<?>) {
                return ((Enum<?>) source).name();
            }
            return String.valueOf(source);
        }
    }


    @ParameterizedTest
    @ValueSource(strings = {"01.01.2017", "31.12.2017"})
    void testWithExplicitJavaTimeConverter(
            @JavaTimeConversionPattern("dd.MM.yyyy") LocalDate argument) {

        assertEquals(2017, argument.getYear());
    }

    // @formatter:off
    @ParameterizedTest
    @CsvSource({
        "Jane, Doe, F, 1990-05-20",
        "John, Doe, M, 1990-10-22"
    })
    void testWithArgumentsAccessor(ArgumentsAccessor arguments) {
        Person person = new Person(arguments.getString(0),
                                   arguments.getString(1),
                                   arguments.get(2, Gender.class),
                                   arguments.get(3, LocalDate.class));

        if (person.getFirstName().equals("Jane")) {
            assertEquals(Gender.F, person.getGender());
        }
        else {
            assertEquals(Gender.M, person.getGender());
        }
        assertEquals("Doe", person.getLastName());
        assertEquals(1990, person.getDateOfBirth().getYear());
    }
    // @formatter:on

    // @formatter:off
    @ParameterizedTest
    @CsvSource({
        "Jane, Doe, F, 1990-05-20",
        "John, Doe, M, 1990-10-22"
    })
    void testWithArgumentsAggregator(@AggregateWith(PersonAggregator.class) Person person) {
        // perform assertions against person
    }

    static
    public class PersonAggregator implements ArgumentsAggregator {
        @Override
        public Person aggregateArguments(ArgumentsAccessor arguments, ParameterContext context) {
            return new Person(arguments.getString(0),
                              arguments.getString(1),
                              arguments.get(2, Gender.class),
                              arguments.get(3, LocalDate.class));
        }
    }
    // @formatter:on


    @ParameterizedTest
    // @formatter:off
    @CsvSource({
        "Jane, Doe, F, 1990-05-20",
        "John, Doe, M, 1990-10-22"
    })
    // @formatter:on
    void testWithCustomAggregatorAnnotation(@CsvToPerson Person person) {
        // perform assertions against person
    }


    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @AggregateWith(PersonAggregator.class)
    public @interface CsvToPerson {
    }

}
