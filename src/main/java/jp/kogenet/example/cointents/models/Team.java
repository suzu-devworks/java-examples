package jp.kogenet.example.cointents.models;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
@JsonDeserialize(builder = Team.TeamBuilder.class)
public class Team {
    @JsonProperty
    private final String name;
    @JsonProperty
    private final LocalDate startAt;
    @JsonProperty
    private final List<Staff> members;

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
