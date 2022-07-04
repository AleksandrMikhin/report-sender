package org.example.sender.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Team {

    private final String color;
    private final List<User> users;

}
