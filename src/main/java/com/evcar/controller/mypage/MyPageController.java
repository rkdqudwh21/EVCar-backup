package com.evcar.controller.mypage;

import com.evcar.domain.user.User;
import com.evcar.domain.user.UserStatus;
import com.evcar.dto.mypage.MyConsultationResponseDto;
import com.evcar.dto.mypage.MyInquiryResponseDto;
import com.evcar.dto.mypage.MyPageInfoResponseDto;
import com.evcar.dto.mypage.MyPageInfoUpdateRequestDto;
import com.evcar.dto.mypage.MyPageSummaryResponseDto;
import com.evcar.dto.mypage.MyWishlistResponseDto;
import com.evcar.dto.mypage.WithdrawRequestDto;
import com.evcar.service.mypage.MyPageService;
import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private static final String ACCESS_DENIED_VIEW = "mypage/memberAccessDenied";

    private final MyPageService myPageService;

    @GetMapping("")
    public String myPageRedirect() {
        return "redirect:/mypage/main";
    }

    @GetMapping("/main")
    public String myPageMain(HttpSession session, Model model) {
        return handlePage(session, model, userId -> {
            MyPageSummaryResponseDto summary = myPageService.getMyPageSummary(userId);
            model.addAttribute("summary", summary);
            return "mypage/myPageMain";
        });
    }

    @GetMapping("/info")
    public String myInfo(HttpSession session, Model model) {
        return handlePage(session, model, userId -> {
            MyPageInfoResponseDto info = getRequiredMyPageInfo(model, userId);

            if (!model.containsAttribute("myPageInfoUpdateRequestDto")) {
                model.addAttribute("myPageInfoUpdateRequestDto", toUpdateRequestDto(info));
            }

            return "mypage/myInfo";
        });
    }

    @PostMapping("/info")
    public String updateMyInfo(HttpSession session,
                               @ModelAttribute MyPageInfoUpdateRequestDto dto,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        String userId = getUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            myPageService.updateMyPageInfo(userId, dto);
            redirectAttributes.addFlashAttribute("message", "내 정보가 정상적으로 수정되었습니다.");
            return "redirect:/mypage/info";
        } catch (IllegalArgumentException e) {
            try {
                MyPageInfoResponseDto info = myPageService.getMyPageInfo(userId);
                model.addAttribute("currentUserId", userId);
                model.addAttribute("myPageInfo", info);
            } catch (IllegalArgumentException ignored) {
                model.addAttribute("currentUserId", userId);
            }

            model.addAttribute("myPageInfoUpdateRequestDto", dto);
            model.addAttribute("infoErrorMessage", e.getMessage());
            return "mypage/myInfo";
        }
    }

    @GetMapping("/wishlist")
    public String myWishlist(HttpSession session, Model model) {
        return handlePage(session, model, userId -> "mypage/myWishlist");
    }

    @GetMapping("/wishlist/api")
    @ResponseBody
    public List<MyWishlistResponseDto> wishlistApi(HttpSession session) {
        String userId = getUserId(session);
        if (userId == null) {
            return Collections.emptyList();
        }

        try {
            return myPageService.getMyWishlist(userId);
        } catch (IllegalArgumentException e) {
            return Collections.emptyList();
        }
    }

    @PostMapping("/wishlist/delete")
    @ResponseBody
    public void deleteWishlist(HttpSession session,
                               @RequestParam("wishlistId") String wishlistId) {

        String userId = getUserId(session);
        if (userId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        myPageService.deleteWishlist(userId, wishlistId);
    }

    @GetMapping("/consultation")
    public String myConsultation(HttpSession session, Model model) {
        return handlePage(session, model, userId -> {
            List<MyConsultationResponseDto> consultations = myPageService.getMyConsultations(userId);
            model.addAttribute("consultations", consultations);
            return "mypage/myConsultation";
        });
    }

    @GetMapping("/consultation/{id}")
    public String myConsultationDetail(@PathVariable("id") String id,
                                       HttpSession session,
                                       Model model) {

        return handlePage(session, model, userId -> {
            MyConsultationResponseDto consultation = myPageService.getMyConsultationDetail(userId, id);
            model.addAttribute("consultation", consultation);
            return "mypage/myConsultationDetail";
        });
    }

    @PostMapping("/consultation/cancel")
    public String cancelConsult(HttpSession session,
                                @RequestParam("consultId") String consultId,
                                RedirectAttributes redirectAttributes) {

        String userId = getUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            myPageService.cancelMyConsultation(userId, consultId);
            redirectAttributes.addFlashAttribute("message", "상담이 정상적으로 취소되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }

        return "redirect:/mypage/consultation";
    }

    @GetMapping("/inquiry")
    public String myInquiry(HttpSession session, Model model) {
        return handlePage(session, model, userId -> {
            List<MyInquiryResponseDto> inquiries = myPageService.getMyInquiries(userId);
            model.addAttribute("inquiries", inquiries);
            return "mypage/myInquiry";
        });
    }

    @GetMapping("/inquiry/{id}")
    public String myInquiryDetail(@PathVariable("id") String id,
                                  HttpSession session,
                                  Model model) {

        return handlePage(session, model, userId -> {
            MyInquiryResponseDto inquiry = myPageService.getMyInquiryDetail(userId, id);
            model.addAttribute("inquiry", inquiry);
            return "mypage/myInquiryDetail";
        });
    }

    @GetMapping("/withdraw")
    public String myWithdraw(HttpSession session, Model model) {
        return handlePage(session, model, userId -> {
            if (!model.containsAttribute("withdrawRequestDto")) {
                model.addAttribute("withdrawRequestDto", WithdrawRequestDto.builder().build());
            }
            return "mypage/myWithdraw";
        });
    }

    @PostMapping("/withdraw")
    public String withdraw(HttpSession session,
                           @ModelAttribute WithdrawRequestDto dto,
                           RedirectAttributes redirectAttributes) {

        String userId = getUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            myPageService.withdraw(userId, dto);
            session.invalidate();
            return "redirect:/mypage/withdraw/success";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("withdrawErrorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("withdrawRequestDto", dto);
            return "redirect:/mypage/withdraw";
        }
    }

    @GetMapping("/withdraw/success")
    public String withdrawSuccess() {
        return "mypage/withdrawSuccess";
    }

    private String handlePage(HttpSession session, Model model, PageHandler handler) {
        String userId = getUserId(session);

        if (userId == null) {
            return "redirect:/login";
        }

        try {
            MyPageInfoResponseDto info = myPageService.getMyPageInfo(userId);

            if (UserStatus.WITHDRAWN.name().equalsIgnoreCase(info.getUserStatus())) {
                session.invalidate();
                model.addAttribute("message", "탈퇴한 회원은 이용할 수 없습니다.");
                return ACCESS_DENIED_VIEW;
            }

            model.addAttribute("currentUserId", userId);
            model.addAttribute("myPageInfo", info);

            return handler.handle(userId);
        } catch (IllegalArgumentException e) {
            model.addAttribute("message", "존재하지 않는 회원입니다.");
            return ACCESS_DENIED_VIEW;
        }
    }

    private String getUserId(HttpSession session) {
        Object loginUser = session.getAttribute("loginUser");
        if (!(loginUser instanceof User user)) {
            return null;
        }

        return user.getUserId();
    }
    private MyPageInfoResponseDto getRequiredMyPageInfo(Model model, String userId) {
        Object info = model.getAttribute("myPageInfo");
        if (info instanceof MyPageInfoResponseDto responseDto) {
            return responseDto;
        }
        return myPageService.getMyPageInfo(userId);
    }

    private MyPageInfoUpdateRequestDto toUpdateRequestDto(MyPageInfoResponseDto dto) {
        return MyPageInfoUpdateRequestDto.builder()
                .name(dto.getName())
                .gender(dto.getGender())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .addressDetail(dto.getAddressDetail())
                .email(dto.getEmail())
                .hasVehicle(dto.getHasVehicle())
                .vehicleModel(dto.getVehicleModel())
                .vehicleYear(dto.getVehicleYear())
                .drivingDistance(dto.getDrivingDistance())
                .build();
    }

    @FunctionalInterface
    private interface PageHandler {
        String handle(String userId);
    }
}