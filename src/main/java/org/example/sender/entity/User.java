package org.example.sender.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class User {

    private final String firstName;
    private final String lastName;
    private final List<String> tasks;

}
