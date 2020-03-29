package com.virus.app.scheduler;

import com.virus.app.models.LocationStats;
import com.virus.app.services.HttpService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.virus.app.constants.ApplicationConstants.*;

@Component
public class ScheduledTask {

    @Autowired
    private HttpService httpService;

    private List<LocationStats> allStats = new ArrayList<>();
    private List<HashMap<String, Object>> lineChartData = new ArrayList<>();

    @PostConstruct
    @Scheduled(cron = "0 1 0 1/1 * ?", zone = "Etc/UTC")
    public void fetchVirusData() throws Exception {
        List<LocationStats> newStats = new ArrayList<>();
        HashMap<String, Object> locationStatsMap = new HashMap<>();
        String pattern = "M/d/yy";
        String resp = httpService.doGet(CONFIRMED_CASES_URL, null, null);
        StringReader csvBodyReader = new StringReader(resp);
        List<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader).getRecords();
        List<String> headers = new ArrayList<>();

        if (Objects.nonNull(records) && !records.isEmpty()) {
            headers = new ArrayList<>(records.get(0).toMap().keySet());
        }
        headers.forEach(item -> {
            if (item.matches("([0-9]{1,2})/([0-9]{1,2})/([0-9]{2,4})")) {
                Long timestamp = 0L;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                try {
                    timestamp = simpleDateFormat.parse(item).getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                HashMap<String, Object> map = new HashMap<>();
                map.put("timestamp", timestamp);
                lineChartData.add(map);
            }
        });

        resp = httpService.doGet(DEATH_CASES_URL, null, null);
        csvBodyReader = new StringReader(resp);
        List<CSVRecord> deathRecords = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader).getRecords();
        resp = httpService.doGet(RECOVERED_CASES_URL, null, null);
        csvBodyReader = new StringReader(resp);
        List<CSVRecord> recoveredRecords = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader).getRecords();

        for (CSVRecord record : records) {
            int confirmedCases = record.get(record.size() - 1).equals(EMPTY_STRING) ? 0 : Integer.parseInt(record.get(record.size() - 1));
            int prevDayConfirmedCases = record.get(record.size() - 2).equals(EMPTY_STRING) ? 0 : Integer.parseInt(record.get(record.size() - 2));
            HashMap<String, Object> stats = new HashMap<>();
            stats.put("state", record.get("Province/State"));
            stats.put("country", record.get("Country/Region"));
            stats.put("confirmedCases", confirmedCases);
            stats.put("newConfirmedCases", confirmedCases - prevDayConfirmedCases);
            locationStatsMap.put(record.get("Country/Region").replace(SPACE, EMPTY_STRING) + record.get("Province/State").replace(SPACE, EMPTY_STRING), stats);
        }
        for (CSVRecord deathRecord : deathRecords) {
            int deathCases = deathRecord.get(deathRecord.size() - 1).equals(EMPTY_STRING) ? 0 : Integer.parseInt(deathRecord.get(deathRecord.size() - 1));
            int prevDayDeathCases = deathRecord.get(deathRecord.size() - 2).equals(EMPTY_STRING) ? 0 : Integer.parseInt(deathRecord.get(deathRecord.size() - 2));
            HashMap<String, Object> stats = (HashMap<String, Object>) locationStatsMap.get(deathRecord.get("Country/Region").replace(SPACE, EMPTY_STRING) + deathRecord.get("Province/State").replace(SPACE, EMPTY_STRING));
            if (Objects.nonNull(stats) && !stats.isEmpty()) {
                stats.put("deathCases", deathCases);
                stats.put("newDeathCases", deathCases - prevDayDeathCases);
                locationStatsMap.put(deathRecord.get("Country/Region").replace(SPACE, EMPTY_STRING) + deathRecord.get("Province/State").replace(SPACE, EMPTY_STRING), stats);
            }
        }
        for (CSVRecord recoveredRecord : recoveredRecords) {
            int recoveredCases = recoveredRecord.get(recoveredRecord.size() - 1).equals(EMPTY_STRING) ? 0 : Integer.parseInt(recoveredRecord.get(recoveredRecord.size() - 1));
            int prevDayRecoveredCases = recoveredRecord.get(recoveredRecord.size() - 2).equals(EMPTY_STRING) ? 0 : Integer.parseInt(recoveredRecord.get(recoveredRecord.size() - 2));
            HashMap<String, Object> stats = (HashMap<String, Object>) locationStatsMap.get(recoveredRecord.get("Country/Region").replace(SPACE, EMPTY_STRING) + recoveredRecord.get("Province/State").replace(SPACE, EMPTY_STRING));
            if (Objects.nonNull(stats) && !stats.isEmpty()) {
                stats.put("recoveredCases", recoveredCases);
                stats.put("newRecoveredCases", recoveredCases - prevDayRecoveredCases);
                locationStatsMap.put(recoveredRecord.get("Country/Region").replace(SPACE, EMPTY_STRING) + recoveredRecord.get("Province/State").replace(SPACE, EMPTY_STRING), stats);
            }
        }
        locationStatsMap.forEach((k, v) -> {
            HashMap<String, Object> stats = (HashMap<String, Object>) v;
            LocationStats locationStats = new LocationStats();
            locationStats.setState(String.valueOf(stats.get("state")))
                    .setCountry(String.valueOf(stats.get("country")))
                    .setConfirmedCases(Integer.parseInt(String.valueOf(stats.get("confirmedCases"))))
                    .setNewConfirmedCases(Integer.parseInt(String.valueOf(stats.get("newConfirmedCases"))))
                    .setDeathCases(stats.get("deathCases") == null ? 0 : Integer.parseInt(String.valueOf(stats.get("deathCases"))))
                    .setNewDeathCases(stats.get("newDeathCases") == null ? 0 : Integer.parseInt(String.valueOf(stats.get("newDeathCases"))))
                    .setRecoveredCases(stats.get("recoveredCases") == null ? 0 : Integer.parseInt(String.valueOf(stats.get("recoveredCases"))))
                    .setNewRecoveredCases(stats.get("newRecoveredCases") == null ? 0 : Integer.parseInt(String.valueOf(stats.get("newRecoveredCases"))));
            newStats.add(locationStats);
        });
        newStats.sort(Comparator.comparing(o -> ((LocationStats) o).getConfirmedCases()).reversed());
        this.allStats = new ArrayList<>(newStats);
        List<String> topElevenCountries = newStats.stream().limit(10).map(o -> o.getCountry().toLowerCase()).collect(Collectors.toList());
        topElevenCountries.add("india");
        for (CSVRecord record : records) {
            if (topElevenCountries.contains(record.get("Country/Region").toLowerCase())) {
                int j = 0;
                for (int i = 0; i < record.size(); i++) {
                    if (headers.get(i).matches("([0-9]{1,2})/([0-9]{1,2})/([0-9]{2,4})")) {
                        if (Objects.nonNull(lineChartData.get(j).get(record.get("Country/Region")))) {
                            Integer val = Integer.parseInt(String.valueOf(lineChartData.get(j).get(record.get("Country/Region"))));
                            val += record.get(i).equals(EMPTY_STRING) ? 0 : Integer.parseInt(record.get(i));
                            lineChartData.get(j).put(record.get("Country/Region"), val);
                        } else {
                            lineChartData.get(j).put(record.get("Country/Region"), record.get(i).equals(EMPTY_STRING) ? 0 : Integer.parseInt(record.get(i)));
                        }
                        j++;
                    }
                }
            }
        }
    }

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    public List<HashMap<String, Object>> getLineChartData() {
        return lineChartData;
    }
}
