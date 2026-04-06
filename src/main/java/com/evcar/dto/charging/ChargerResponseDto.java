package com.evcar.dto.charging;

import com.evcar.domain.charging.Charger;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChargerResponseDto {

    private String chargerType;
    private String status;

    public static ChargerResponseDto from(Charger charger) {
        return new ChargerResponseDto(
                convertType(charger.getChargerType()),
                convertStatus(charger.getStatus())
        );
    }

    private static String convertType(String type) {
        return switch (type) {
            case "01" -> "완속";
            case "02" -> "급속";
            case "03" -> "급속(차데모)";
            case "04" -> "급속(AC3상)";
            case "05" -> "급속(DC콤보)";
            case "06" -> "급속(콤보)";
            default -> "기타";
        };
    }

    // 🔥 여기 추가된 핵심
    private static String convertStatus(String status) {
        return switch (status) {
            case "1", "01" -> "사용가능";
            case "2", "02" -> "충전중";
            case "3", "03" -> "고장";
            case "4", "04" -> "통신장애";
            case "5", "05" -> "점검중";
            default -> "알수없음";
        };
    }
}