package com.virus.app.scheduler;

import com.virus.app.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ScheduledTask {

    private static final String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Confirmed.csv";
    private List<LocationStats> allStats = new ArrayList<>();
    private List<HashMap<String, Object>> lineChartData = new ArrayList<>();

    @PostConstruct
    @Scheduled(cron = "0 1 0 1/1 * ?", zone = "Etc/UTC")
    public void fetchVirusData() {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String resp;
        List<LocationStats> newStats = new ArrayList<>();
        String pattern = "M/d/yy";
        try {
            HttpGet httpGet = new HttpGet(VIRUS_DATA_URL);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException("Failed with HTTP error code: " + httpResponse.getStatusLine().getStatusCode());
            }
            resp = EntityUtils.toString(httpResponse.getEntity());

            StringReader csvBodyReader = new StringReader(resp);
            List<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader).getRecords();
            List<String> headers = new ArrayList<>();
            if (Objects.nonNull(records) && !records.isEmpty()) {
                headers = new ArrayList<>(records.get(0).toMap().keySet());
            }
            headers.forEach(item -> {
                if (item.matches("([0-9]{1,2})/([0-9]{1,2})/([0-9]{2})")) {
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
            for (CSVRecord record : records) {
                LocationStats locationStat = new LocationStats();
                int latestCases = Integer.parseInt(record.get(record.size() - 1));
                int prevDayCases = Integer.parseInt(record.get(record.size() - 2));
                locationStat.setState(record.get("Province/State"))
                        .setCountry(record.get("Country/Region"))
                        .setLatestTotalCases(latestCases)
                        .setDiffFromPrevDay(latestCases - prevDayCases);
                newStats.add(locationStat);
            }
            newStats.sort(Comparator.comparing(o -> ((LocationStats) o).getLatestTotalCases()).reversed());
            this.allStats = newStats;
            List<String> topElevenCountries = newStats.stream().limit(10).map(o -> o.getCountry().toLowerCase()).collect(Collectors.toList());
            topElevenCountries.add("india");
            for (CSVRecord record : records) {
                if (topElevenCountries.contains(record.get("Country/Region").toLowerCase())) {
                    int j = 0;
                    for (int i = 0; i < record.size(); i++) {
                        if (headers.get(i).matches("([0-9]{1,2})/([0-9]{1,2})/([0-9]{2})")) {
                            if (Objects.nonNull(lineChartData.get(j).get(record.get("Country/Region")))) {
                                Integer val = Integer.parseInt(String.valueOf(lineChartData.get(j).get(record.get("Country/Region"))));
                                val += Integer.parseInt(record.get(i));
                                lineChartData.get(j).put(record.get("Country/Region"), val);
                            } else {
                                lineChartData.get(j).put(record.get("Country/Region"), Integer.parseInt(record.get(i)));
                            }
                            j++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    public HashMap<String, Object> getLineChartData() {
        List<String> keys = new ArrayList<>();
        HashMap<String, Object> resp = new HashMap<>();
        resp.put("data", lineChartData);

        if (Objects.nonNull(lineChartData) && !lineChartData.isEmpty()) {
            keys = new ArrayList<>(lineChartData.get(0).keySet());
            keys.remove("timestamp");
        }
        resp.put("attributes", keys);

        return resp;
    }
}
