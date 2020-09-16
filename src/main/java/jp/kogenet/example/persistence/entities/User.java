package jp.kogenet.example.persistence.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {

    private Integer id;

    private String name;

    private String email;

    private String password;

}