package com.evcar.service.charging;

import com.evcar.domain.charging.ChargingStation;
import com.evcar.dto.charging.ChargingStationResponseDto;
import com.evcar.repository.charging.ChargingStationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChargingStationServiceImpl implements ChargingStationService {

    private final ChargingStationRepository chargingStationRepository;

    @Override
    public List<ChargingStation> findByMapBounds(double swLat, double neLat, double swLng, double neLng) {
        return List.of();
    }

    @Override
    public List<ChargingStation> findByRegion(String sido, String sigungu) {
        return List.of();
    }

    // 🔥 공통 변환 메서드 (핵심)
    private ChargingStationResponseDto toDto(ChargingStation c) {
        return ChargingStationResponseDto.builder()
                .stationId(c.getStationId())
                .stationName(c.getStationName())
                .address(c.getAddress())
                .lat(c.getLat())
                .lng(c.getLng())
                .operatorName(c.getOperatorName())
                .operatorCall(c.getOperatorCall())
                .useTime(c.getUseTime())
                .parkingFree(
                        c.getParkingFree() != null && c.getParkingFree().equalsIgnoreCase("Y")
                                ? "무료"
                                : "유료"
                )
                .note(
                        (c.getNote() == null || c.getNote().isBlank())
                                ? "이용 안내 없음"   // 🔥 핵심 수정
                                : c.getNote()
                )
                .build();
    }

    // 🔥 지역 검색
    @Override
    public List<ChargingStationResponseDto> getStationsByRegion(String sido, String sigungu) {

        String keyword = sido + " " + sigungu;

        List<ChargingStation> list = chargingStationRepository.findByAddressContaining(keyword);

        return list.stream()
                .map(this::toDto)
                .toList();
    }

    // 🔥 zcode 검색
    @Override
    public List<ChargingStationResponseDto> getStationsByZcode(String zcode) {

        List<ChargingStation> list = chargingStationRepository.findByZcodeWithChargers(zcode);

        return list.stream()
                .map(this::toDto)
                .toList();
    }

    // 🔥 지역 목록
    @Override
    public Map<String, List<String>> getAllRegions() {

        List<ChargingStation> list = chargingStationRepository.findAll();

        Map<String, Set<String>> temp = new HashMap<>();

        for (ChargingStation s : list) {

            if (s.getAddress() == null) continue;

            String addr = s.getAddress().trim().replaceAll("\\s+", " ");

            if (addr.length() < 5) continue;

            String[] parts = addr.split(" ");

            String sido;
            String sigungu;

            if (parts.length >= 2) {
                sido = parts[0];
                sigungu = parts[1];
            } else {
                if (addr.startsWith("서울")) {
                    sido = "서울특별시";
                    sigungu = addr.substring(5, Math.min(8, addr.length()));
                } else if (addr.startsWith("경기")) {
                    sido = "경기도";
                    sigungu = addr.substring(3, Math.min(6, addr.length()));
                } else if (addr.startsWith("인천")) {
                    sido = "인천광역시";
                    sigungu = addr.substring(5, Math.min(8, addr.length()));
                } else if (addr.startsWith("부산")) {
                    sido = "부산광역시";
                    sigungu = addr.substring(5, Math.min(8, addr.length()));
                } else {
                    continue;
                }
            }

            temp.putIfAbsent(sido, new HashSet<>());
            temp.get(sido).add(sigungu);
        }

        Map<String, List<String>> result = new HashMap<>();

        for (String key : temp.keySet()) {
            result.put(key, new ArrayList<>(temp.get(key)));
        }

        return result;
    }
}