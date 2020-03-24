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
        model.addAttribute("totalConfirmedCases", resp.get("totalConfirmedCases"));
        model.addAttribute("totalNewConfirmedCases", resp.get("totalNewConfirmedCases"));

        return "home";
    }

    @GetMapping(value = "/")
    public String getDataForHome(Model model, @RequestParam(name = "page", defaultValue = "0") int page,
                                 @RequestParam(name = "size", defaultValue = "10") int size) {
        HashMap<String, Object> resp = homeService.getDataForHome(page, size);
        model.addAttribute("paginatedResp", resp.get("paginatedResp"));
        model.addAttribute("pageNumbers", resp.get("pageNumbers"));
        model.addAttribute("totalConfirmedCases", resp.get("totalConfirmedCases"));
        model.addAttribute("totalNewConfirmedCases", resp.get("totalNewConfirmedCases"));
        model.addAttribute("totalDeathCases", resp.get("totalDeathCases"));
        model.addAttribute("totalNewDeathCases", resp.get("totalNewDeathCases"));
        model.addAttribute("totalRecoveredCases", resp.get("totalRecoveredCases"));
        model.addAttribute("totalNewRecoveredCases", resp.get("totalNewRecoveredCases"));
        model.addAttribute("barChart", resp.get("barChart"));
        model.addAttribute("lineChart", resp.get("lineChart"));

        return "home";
    }
}
