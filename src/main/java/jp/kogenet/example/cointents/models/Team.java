package jp.kogenet.example.cointents.models;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Team {
    private String name;
    private LocalDate startAt;
    private List<Staff> members;

    public static Team create(String name, LocalDate startAt,
            Supplier<List<Staff>> staffSupplier) {
        // @formatter:off
        var team = Team.builder()
                .name(name)
                .startAt(startAt)
                .members(staffSupplier.get())
                .build();
        // @formatter:on

        return team;
    }
}
