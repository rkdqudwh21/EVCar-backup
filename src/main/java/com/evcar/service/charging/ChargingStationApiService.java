package com.evcar.service.charging;

import com.evcar.domain.charging.Charger;
import com.evcar.domain.charging.ChargingStation;
import com.evcar.repository.charging.ChargerRepository;
import com.evcar.repository.charging.ChargingStationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ChargingStationApiService {

    private final ChargingStationRepository repository;
    private final ChargerRepository chargerRepository;

    private final String SERVICE_KEY = "031a71b2c07d3e248408ec1aed5c98b545bc8a4db1b41e4c48a34be451e036d3";

    public void loadData() throws Exception {

        String serviceKey = URLEncoder.encode(SERVICE_KEY, "UTF-8");

        String url = "https://apis.data.go.kr/B552584/EvCharger/getChargerInfo"
                + "?serviceKey=" + serviceKey
                + "&numOfRows=1000&pageNo=1";

        RestTemplate restTemplate = new RestTemplate();
        String json = restTemplate.getForObject(url, String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        JsonNode items = root
                .path("response")
                .path("body")
                .path("items")
                .path("item");

        for (JsonNode node : items) {

            String stationId = node.path("statId").asText();

            // 🔥 비고 처리
            String note = node.path("limitDetail").asText();
            if (note == null || note.isBlank()) {
                note = "이용 안내 없음";
            }

            ChargingStation station = ChargingStation.builder()
                    .stationId(stationId)
                    .stationName(node.path("statNm").asText())
                    .address(node.path("addr").asText())
                    .lat(node.path("lat").asDouble())
                    .lng(node.path("lng").asDouble())
                    .useTime(node.path("useTime").asText())
                    .zcode(node.path("zcode").asText())
                    .operatorName(node.path("busiNm").asText())
                    .operatorCall(node.path("busiCall").asText())
                    .parkingFree(node.path("parkingFree").asText())
                    .note(note)
                    .build();

            repository.save(station);

            Charger charger = Charger.builder()
                    .chargerId(node.path("chgerId").asText())
                    .chargerType(node.path("chgerType").asText())
                    .powerType(node.path("powerType").asText())
                    .status(node.path("stat").asText())
                    .statusUpdatedAt(parseDateTime(node.path("statUpdDt").asText()))
                    .chargingStation(station)
                    .build();

            chargerRepository.save(charger);
        }
    }

    private LocalDateTime parseDateTime(String val) {
        try {
            if (val == null || val.isEmpty()) return null;

            return LocalDateTime.parse(val,
                    DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        } catch (Exception e) {
            return null;
        }
    }
}