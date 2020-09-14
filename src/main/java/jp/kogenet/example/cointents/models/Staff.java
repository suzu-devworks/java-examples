package jp.kogenet.example.cointents.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;

import jp.kogenet.example.cointents.utils.gson.Exclude;
import lombok.Data;

@Data
public class Staff {
    private String name;
    private int numOfYears;
    private String[] position; // array
    private List<String> skills; // list
    private Map<String, BigDecimal> salary; // map
    private LocalDate birthday;
    private ZonedDateTime lastUpdateAt;

    @JsonIgnore
    @Exclude
    @Expose(serialize = false, deserialize = false) // not work.
    private TimeZone localeTimeZone;
}
