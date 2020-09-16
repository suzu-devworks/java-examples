package jp.kogenet.example.persistence.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@ToString
@Builder
@Data
public class WorkItem {

    private Integer id;

    private String code;

    private String name;

    private BigDecimal price;

    private Integer quantity;

    private LocalDate purchaseDate;

    private ZonedDateTime lastUpdatedAt;
    
}
