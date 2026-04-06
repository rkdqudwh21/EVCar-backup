package com.evcar.controller.charging;

import com.evcar.dto.charging.ChargerResponseDto;
import com.evcar.dto.charging.ChargingStationResponseDto;
import com.evcar.repository.charging.ChargerRepository;
import com.evcar.service.charging.ChargingStationApiService;
import com.evcar.service.charging.ChargingStationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/charging")
@RequiredArgsConstructor
public class ChargingStationController {

    private final ChargingStationApiService apiService;
    private final ChargingStationService chargingStationService;
    private final ChargerRepository chargerRepository;

    @GetMapping("/load")
    @ResponseBody
    public String load() throws Exception {
        apiService.loadData();
        return "데이터 적재 완료";
    }

    @GetMapping("/map")
    public String map() {
        return "charging/map";
    }

    @GetMapping("/regions")
    @ResponseBody
    public Map<String, List<String>> getRegions() {
        return chargingStationService.getAllRegions();
    }

    @GetMapping("/stations")
    @ResponseBody
    public List<ChargingStationResponseDto> getStations(
            @RequestParam("sido") String sido,
            @RequestParam(value = "sigungu", required = false, defaultValue = "") String sigungu) {

        if (sigungu.isEmpty()) {
            return chargingStationService.getStationsByZcode(sido);
        }
        return chargingStationService.getStationsByRegion(sido, sigungu);
    }

    // 🔥🔥🔥 여기 수정 완료 버전
    @GetMapping("/chargers")
    @ResponseBody
    public List<ChargerResponseDto> getChargers(@RequestParam("stationId") String stationId) {

        return chargerRepository.findByChargingStation_StationId(stationId)
                .stream()
                .map(ChargerResponseDto::from) // ⭐ 핵심
                .toList();
    }
}