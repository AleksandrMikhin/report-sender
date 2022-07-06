package org.example.sender.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SingleReport {

    private String firstName;
    private String lastName;
    private List<TrackMinInfo> tasks;

}
