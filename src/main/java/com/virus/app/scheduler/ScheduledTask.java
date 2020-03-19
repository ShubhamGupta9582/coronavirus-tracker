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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class ScheduledTask {

    private static final String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Confirmed.csv";
    private List<LocationStats> allStats = new ArrayList<>();

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    @PostConstruct
    @Scheduled(cron = "0 1 0 1/1 * ?", zone = "Etc/UTC")
    public void fetchVirusData() {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String resp;
        List<LocationStats> newStats = new ArrayList<>();
        try {
            HttpGet httpGet = new HttpGet(VIRUS_DATA_URL);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException("Failed with HTTP error code: " + httpResponse.getStatusLine().getStatusCode());
            }
            resp = EntityUtils.toString(httpResponse.getEntity());

            StringReader csvBodyReader = new StringReader(resp);
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
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
            newStats.sort(Comparator.comparing(o -> o.getCountry()));
            this.allStats = newStats;
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
}