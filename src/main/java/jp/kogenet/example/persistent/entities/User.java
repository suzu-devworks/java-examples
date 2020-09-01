package jp.kogenet.example.persistent.entities;

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