package com.virus.app.services;

import com.virus.app.models.LocationStats;
import com.virus.app.scheduler.ScheduledTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HomeService {

    @Autowired
    private ScheduledTask scheduledTask;

    public Page<LocationStats> getPaginatedData(Pageable pageable) {
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<LocationStats> paginatedStats;
        List<LocationStats> allStats = new ArrayList<>(scheduledTask.getAllStats());
        allStats.sort(Comparator.comparing(o -> o.getCountry()));

        if (allStats.size() < startItem) {
            paginatedStats = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, allStats.size());
            paginatedStats = allStats.subList(startItem, toIndex);
        }

        Page<LocationStats> page = new PageImpl<>(paginatedStats, PageRequest.of(currentPage, pageSize), allStats.size());

        return page;
    }

    public HashMap<String, Object> getBarChartData() {
        List<LocationStats> chartData = scheduledTask.getAllStats();

        List<String> barChartLabels = chartData.stream().limit(10).map(o -> o.getCountry()).collect(Collectors.toList());
        List<Integer> barChartData = chartData.stream().limit(10).map(o -> o.getLatestTotalCases()).collect(Collectors.toList());
        barChartLabels.add("India");
        barChartData.add(chartData.stream().filter(p -> p.getCountry().equalsIgnoreCase("india")).findFirst().orElse(new LocationStats()).getLatestTotalCases());

        HashMap<String, Object> barChartResp = new HashMap<>();
        barChartResp.put("labels", barChartLabels);
        barChartResp.put("data", barChartData);

        return barChartResp;
    }

    public HashMap<String, Object> getLineChartData() {
        return scheduledTask.getLineChartData();
    }
}
