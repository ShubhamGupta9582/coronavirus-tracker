package com.virus.app.controllers;


import com.virus.app.models.LocationStats;
import com.virus.app.services.CoronavirusDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class HomeController {

    @Autowired
    private CoronavirusDataService coronavirusDataService;

    @GetMapping(value = "/all")  // Not in use
    public String home(Model model) {
        List<LocationStats> allStats = coronavirusDataService.getAllStats();
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
        Page<LocationStats> paginatedResp = coronavirusDataService.getPaginatedData(PageRequest.of(page, size));
        model.addAttribute("paginatedResp", paginatedResp);

        int totalPages = paginatedResp.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        List<LocationStats> allStats = coronavirusDataService.getAllStats();
        int totalCasesReported = allStats.stream().mapToInt(stat -> stat.getLatestTotalCases()).sum();
        int totalNewCases = allStats.stream().mapToInt(stat -> stat.getDiffFromPrevDay()).sum();
        model.addAttribute("totalCasesReported", totalCasesReported);
        model.addAttribute("totalNewCases", totalNewCases);

        List<LocationStats> chartData = new ArrayList<>(allStats);

        chartData.sort(Comparator.comparing(o -> ((LocationStats) o).getLatestTotalCases()).reversed());
        List<String> barChartLabels = chartData.stream().limit(10).map(o -> o.getCountry()).collect(Collectors.toList());
        List<Integer> barChartData = chartData.stream().limit(10).map(o -> o.getLatestTotalCases()).collect(Collectors.toList());
        barChartLabels.add(chartData.stream().filter(p -> p.getCountry().equalsIgnoreCase("india")).findFirst().orElse(new LocationStats()).getCountry());
        barChartData.add(chartData.stream().filter(p -> p.getCountry().equalsIgnoreCase("india")).findFirst().orElse(new LocationStats()).getLatestTotalCases());
        model.addAttribute("barChartLabels", barChartLabels);
        model.addAttribute("barChartData", barChartData);

        return "home";
    }
}
