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
        for (int i = 0; i < records.size(); i++) {
            LocationStats locationStat = new LocationStats();
            CSVRecord record = null;
            CSVRecord deathRecord = null;
            CSVRecord recoveredRecord = null;
            int confirmedCases, prevDayConfirmedCases, deathCases, prevDayDeathCases, recoveredCases, prevDayRecoveredCases;

            try {
                record = records.get(i);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            if (Objects.nonNull(record)) {
                confirmedCases = record.get(record.size() - 1).equals(EMPTY_STRING) ? 0 : Integer.parseInt(record.get(record.size() - 1));
                prevDayConfirmedCases = record.get(record.size() - 2).equals(EMPTY_STRING) ? 0 : Integer.parseInt(record.get(record.size() - 2));
                locationStat.setState(record.get("Province/State"))
                        .setCountry(record.get("Country/Region"))
                        .setConfirmedCases(confirmedCases)
                        .setNewConfirmedCases(confirmedCases - prevDayConfirmedCases);
            }
            try {
                deathRecord = deathRecords.get(i);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            if (Objects.nonNull(deathRecord)) {
                deathCases = deathRecord.get(deathRecord.size() - 1).equals(EMPTY_STRING) ? 0 : Integer.parseInt(deathRecord.get(deathRecord.size() - 1));
                prevDayDeathCases = deathRecord.get(deathRecord.size() - 2).equals(EMPTY_STRING) ? 0 : Integer.parseInt(deathRecord.get(deathRecord.size() - 2));
                locationStat.setDeathCases(deathCases)
                        .setNewDeathCases(deathCases - prevDayDeathCases);
            }
            try {
                recoveredRecord = recoveredRecords.get(i);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            if (Objects.nonNull(recoveredRecord)) {
                recoveredCases = recoveredRecord.get(recoveredRecord.size() - 1).equals(EMPTY_STRING) ? 0 : Integer.parseInt(recoveredRecord.get(recoveredRecord.size() - 1));
                prevDayRecoveredCases = recoveredRecord.get(recoveredRecord.size() - 2).equals(EMPTY_STRING) ? 0 : Integer.parseInt(recoveredRecord.get(recoveredRecord.size() - 2));
                locationStat.setRecoveredCases(recoveredCases)
                        .setNewRecoveredCases(recoveredCases - prevDayRecoveredCases);
            }
            newStats.add(locationStat);
        }
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
