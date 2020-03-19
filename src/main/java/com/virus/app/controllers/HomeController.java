package com.virus.app.controllers;


import com.virus.app.models.LocationStats;
import com.virus.app.scheduler.ScheduledTask;
import com.virus.app.services.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class HomeController {

    @Autowired
    private ScheduledTask scheduledTask;

    @Autowired
    private HomeService homeService;

    @GetMapping(value = "/all")  // Not in use
    public String home(Model model) {
        List<LocationStats> allStats = scheduledTask.getAllStats();
        int totalCasesReported = allStats.stream().mapToInt(stat -> stat.getLatestTotalCases()).sum();
        int totalNewCases = allStats.stream().mapToInt(stat -> stat.getDiffFromPrevDay()).sum();
        model.addAttribute("locationStats", allStats);
        model.addAttribute("totalCasesReported", totalCasesReported);
        model.addAttribute("totalNewCases", totalNewCases);

        return "home";
    }

    @GetMapping(value = "/")
    public String getPaginatedData(Model model, @RequestParam(name = "page", defaultValue = "0") int page,
                                   @RequestParam(name = "size", defaultValue = "10") int size) {
        Page<LocationStats> paginatedResp = homeService.getPaginatedData(PageRequest.of(page, size));
        model.addAttribute("paginatedResp", paginatedResp);

        int totalPages = paginatedResp.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        List<LocationStats> allStats = scheduledTask.getAllStats();
        int totalCasesReported = allStats.stream().mapToInt(stat -> stat.getLatestTotalCases()).sum();
        int totalNewCases = allStats.stream().mapToInt(stat -> stat.getDiffFromPrevDay()).sum();
        model.addAttribute("totalCasesReported", totalCasesReported);
        model.addAttribute("totalNewCases", totalNewCases);

        model.addAttribute("barChart", homeService.getBarChartData());
        model.addAttribute("lineChart", homeService.getLineChartData());

        return "home";
    }
}
