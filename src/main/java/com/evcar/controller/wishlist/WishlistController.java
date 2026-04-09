package com.evcar.controller.wishlist;

import com.evcar.service.vehicle.WishlistService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/add")
    public String add(HttpSession session,
                      @RequestParam("vehicleId") String vehicleId) {

        Object userId = session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        wishlistService.addWishlist(String.valueOf(userId), vehicleId);
        return "redirect:/vehicle/list";
    }

    @PostMapping("/delete")
    public String delete(HttpSession session,
                         @RequestParam("vehicleId") String vehicleId) {

        Object userId = session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        wishlistService.removeWishlist(String.valueOf(userId), vehicleId);
        return "redirect:/vehicle";
    }
}