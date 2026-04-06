'use strict';

// 지도 생성
const map = new kakao.maps.Map(document.getElementById('map'), {
    center: new kakao.maps.LatLng(36.5, 127.5),
    level: 13
});

let markers = [];

// 마커 제거
function clearMarkers() {
    markers.forEach(m => m.setMap(null));
    markers = [];
}

// 타입 변환
function convertType(type) {
    switch (type) {
        case "06": return "급속";
        case "04": return "급속(콤보)";
        case "05": return "완속";
        default: return "기타";
    }
}

// 상태 변환
function convertStatus(status) {
    switch (status) {
        case "1": return "사용가능";
        case "2": return "충전중";
        case "3": return "고장";
        case "4": return "점검중";
        default: return "알수없음";
    }
}

// DOM
const sidoSelect = document.getElementById('sido');
const sigunguSelect = document.getElementById('sigungu');

// 지역 데이터 (유지)
const regionData = {
    "서울특별시": ["강남구","강동구","강북구","강서구","관악구","광진구","구로구","금천구",
        "노원구","도봉구","동대문구","동작구","마포구","서대문구","서초구",
        "성동구","성북구","송파구","양천구","영등포구","용산구","은평구",
        "종로구","중구","중랑구"]
};

// 시/도 초기화
function initSido() {
    sidoSelect.innerHTML = '<option value="">시/도 선택</option>';

    Object.keys(regionData).forEach(sido => {
        const option = document.createElement('option');
        option.value = sido;
        option.textContent = sido;
        sidoSelect.appendChild(option);
    });
}

// 시군구 변경
function updateSigungu() {
    const sido = sidoSelect.value;

    sigunguSelect.innerHTML = '<option value="">시/군/구 선택</option>';

    if (!sido) return;

    regionData[sido].forEach(sigungu => {
        const option = document.createElement('option');
        option.value = sigungu;
        option.textContent = sigungu;
        sigunguSelect.appendChild(option);
    });
}

sidoSelect.addEventListener('change', updateSigungu);
initSido();

// 검색
document.getElementById('searchBtn').addEventListener('click', () => {

    const sido = sidoSelect.value;
    const sigungu = sigunguSelect.value;

    if (!sido) {
        alert("시/도 선택해라");
        return;
    }

    fetch(`/charging/stations?sido=${sido}&sigungu=${sigungu}`)
        .then(res => res.json())
        .then(data => {

            clearMarkers();

            const listDiv = document.getElementById('chargerList');
            listDiv.innerHTML = "";

            const bounds = new kakao.maps.LatLngBounds();

            data.forEach(station => {

                if (!station.lat || !station.lng) return;

                const pos = new kakao.maps.LatLng(station.lat, station.lng);

                bounds.extend(pos);

                const marker = new kakao.maps.Marker({
                    map: map,
                    position: pos
                });

                markers.push(marker);

                // 마커 클릭
                kakao.maps.event.addListener(marker, 'click', () => {

                    // 상세 정보
                    document.getElementById('stationName').innerText = station.stationName;
                    document.getElementById('address').innerText = station.address;

                    // 충전기 조회
                    fetch(`/charging/chargers?stationId=${station.stationId}`)
                        .then(res => res.json())
                        .then(chargers => {

                            listDiv.innerHTML = "";

                            chargers.forEach((c, i) => {

                                const div = document.createElement('div');
                                div.className = "ev-charger-item";

                                div.innerHTML = `
                                    ${i + 1}번 ${convertType(c.chargerType)} 
                                    <span class="ev-badge">${convertStatus(c.status)}</span>
                                `;

                                listDiv.appendChild(div);
                            });
                        });
                });
            });

            map.setBounds(bounds);
        });
});