package jp.kogenet.example.persistence.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class User {

    private Integer Id;

    private String name;

    private String email;

    private String password;

}