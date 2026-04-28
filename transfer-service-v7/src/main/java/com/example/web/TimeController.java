package com.example.web;

import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/time")
public class TimeController {

    // @RequestMapping(method = RequestMethod.GET)
    @GetMapping
    public String getCurrentTime(Model model) {
        LocalDateTime now = LocalDateTime.now();
        model.addAttribute("currentTime", now);
        model.addAttribute("timeZone", "Asia/Kolkata");
        return "time-now"; // This will resolve to time-now.html
    }

}
