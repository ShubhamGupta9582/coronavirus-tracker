package com.virus.app.controllers;


import com.virus.app.services.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;

@Controller
public class HomeController {

    @Autowired
    private HomeService homeService;

    @GetMapping(value = "/all")  // Not in use
    public String home(Model model) {
        HashMap<String, Object> resp = homeService.home();
        model.addAttribute("locationStats", resp.get("locationStats"));
        model.addAttribute("totalCasesReported", resp.get("totalCasesReported"));
        model.addAttribute("totalNewCases", resp.get("totalNewCases"));

        return "home";
    }

    @GetMapping(value = "/")
    public String getDataForHome(Model model, @RequestParam(name = "page", defaultValue = "0") int page,
                                 @RequestParam(name = "size", defaultValue = "10") int size) {
        HashMap<String, Object> resp = homeService.getDataForHome(page, size);
        model.addAttribute("paginatedResp", resp.get("paginatedResp"));
        model.addAttribute("pageNumbers", resp.get("pageNumbers"));
        model.addAttribute("totalCasesReported", resp.get("totalCasesReported"));
        model.addAttribute("totalNewCases", resp.get("totalNewCases"));
        model.addAttribute("barChart", resp.get("barChart"));
        model.addAttribute("lineChart", resp.get("lineChart"));

        return "home";
    }
}
