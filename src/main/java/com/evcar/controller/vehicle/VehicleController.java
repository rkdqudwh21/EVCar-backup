package com.evcar.controller.vehicle;

import com.evcar.dto.vehicle.VehicleDetailDto;
import com.evcar.dto.vehicle.VehicleImageResponseDto;
import com.evcar.dto.vehicle.VehicleListDto;
import com.evcar.service.vehicle.VehicleImageService;
import com.evcar.service.vehicle.VehicleService;
import com.evcar.service.vehicle.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/vehicle")
public class VehicleController {

    private final VehicleService vehicleService;
    private final WishlistService wishlistService;
    private final VehicleImageService vehicleImageService;

    // 임시 사용자 ID
    // 나중에는 로그인 사용자 정보로 교체
    private static final String TEST_USER_ID = "user0001";

    @GetMapping
    public String vehicleHome(Model model) {

        List<VehicleListDto> vehicleList = vehicleService.getVehicleList(null, null);

        model.addAttribute("vehicleList", vehicleList);
        model.addAttribute("selectedBrand", "전체");
        model.addAttribute("selectedClass", "전체");
        model.addAttribute("totalCount", vehicleList.size());

        return "vehicle/list";
    }

    @GetMapping("/list")
    public String vehicleList(@RequestParam(name = "brand", defaultValue = "전체") String brand,
                              @RequestParam(name = "vehicleClass", defaultValue = "전체") String vehicleClass,
                              Model model) {

        if ("전체".equals(brand)) {
            brand = null;
        }
        if ("전체".equals(vehicleClass)) {
            vehicleClass = null;
        }

        List<VehicleListDto> vehicleList = vehicleService.getVehicleList(brand, vehicleClass);

        model.addAttribute("vehicleList", vehicleList);
        model.addAttribute("selectedBrand", brand == null ? "전체" : brand);
        model.addAttribute("selectedClass", vehicleClass == null ? "전체" : vehicleClass);
        model.addAttribute("totalCount", vehicleList.size());

        return "vehicle/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") String id, Model model) {

        VehicleDetailDto dto = vehicleService.getDetail(id);
        dto.setWished(wishlistService.isWished(TEST_USER_ID, id));

        /*List<VehicleImageResponseDto> images = vehicleImageService.getImages(id);

        System.out.println("이미지 개수: " + images.size());*/

        model.addAttribute("vehicle", dto);
        /*model.addAttribute("images", images);*/

        return "vehicle/detail";
    }
}