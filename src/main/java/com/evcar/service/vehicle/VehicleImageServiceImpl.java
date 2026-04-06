
package com.evcar.service.vehicle;

import com.evcar.dto.vehicle.VehicleImageResponseDto;
import com.evcar.repository.vehicle.VehicleImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleImageServiceImpl implements VehicleImageService {

    private final VehicleImageRepository vehicleImageRepository;

    @Override
    public List<VehicleImageResponseDto> getImages(String vehicleId) {
        return vehicleImageRepository.findByVehicleIdOrderByImageOrderAsc(vehicleId)
                .stream()
                .map(img -> VehicleImageResponseDto.from(img.getImageUrl()))
                .toList();
    }
}
