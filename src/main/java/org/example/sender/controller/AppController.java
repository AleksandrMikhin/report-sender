package org.example.sender.controller;

import lombok.RequiredArgsConstructor;
import org.example.sender.service.ReportSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Date;

@Controller
@RequiredArgsConstructor
public class AppController {

    private final ReportSender reportSender;

    @GetMapping("/app")
    public String doGet(final Model model) {
        model.addAttribute("date", new Date());
        return "index";
    }

    @PostMapping("/app")
    public String doPost() {
        reportSender.sendReport();
        return "redirect:/app";
    }
}
