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
import java.util.stream.IntStream;

@Service
public class HomeService {

    @Autowired
    private ScheduledTask scheduledTask;

    public HashMap<String, Object> home() {
        HashMap<String, Object> resp = new HashMap<>();
        List<LocationStats> allStats = scheduledTask.getAllStats();
        int totalCasesReported = allStats.stream().mapToInt(stat -> stat.getLatestTotalCases()).sum();
        int totalNewCases = allStats.stream().mapToInt(stat -> stat.getDiffFromPrevDay()).sum();
        resp.put("locationStats", allStats);
        resp.put("totalCasesReported", totalCasesReported);
        resp.put("totalNewCases", totalNewCases);

        return resp;
    }

    public HashMap<String, Object> getDataForHome(int page, int size) {
        HashMap<String, Object> resp = new HashMap<>();
        Page<LocationStats> paginatedResp = getPaginatedData(PageRequest.of(page, size));
        resp.put("paginatedResp", paginatedResp);

        int totalPages = paginatedResp.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
            resp.put("pageNumbers", pageNumbers);
        }

        List<LocationStats> allStats = scheduledTask.getAllStats();
        int totalCasesReported = allStats.stream().mapToInt(stat -> stat.getLatestTotalCases()).sum();
        int totalNewCases = allStats.stream().mapToInt(stat -> stat.getDiffFromPrevDay()).sum();
        resp.put("totalCasesReported", totalCasesReported);
        resp.put("totalNewCases", totalNewCases);

        resp.put("barChart", getBarChartData());
        resp.put("lineChart", getLineChartData());

        return resp;
    }

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
        barChartLabels.add("India");
        List<Integer> barChartData = new ArrayList<>();
        for (String label : barChartLabels) {
            barChartData.add(chartData.stream().filter(p -> p.getCountry().equalsIgnoreCase(label)).mapToInt(o -> o.getLatestTotalCases()).sum());
        }

        HashMap<String, Object> resp = new HashMap<>();
        resp.put("labels", barChartLabels);
        resp.put("data", barChartData);

        return resp;
    }

    public HashMap<String, Object> getLineChartData() {
        HashMap<String, Object> resp = new HashMap<>();
        List<String> keys = new ArrayList<>();
        List<HashMap<String, Object>> lineChartData = scheduledTask.getLineChartData();
        resp.put("data", lineChartData);

        if (Objects.nonNull(lineChartData) && !lineChartData.isEmpty()) {
            keys = new ArrayList<>(lineChartData.get(0).keySet());
            keys.remove("timestamp");
        }
        resp.put("attributes", keys);

        return resp;
    }
}
