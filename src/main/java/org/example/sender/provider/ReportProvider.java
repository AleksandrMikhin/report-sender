package org.example.sender.provider;

import org.example.sender.entity.Team;

import java.io.File;
import java.util.Date;
import java.util.List;

public interface ReportProvider {

    File createReport(List<Team> teamList, Date reportDate) throws Exception;

}
