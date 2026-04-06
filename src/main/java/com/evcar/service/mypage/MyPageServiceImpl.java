package com.evcar.service.mypage;

import com.evcar.domain.consultation.Consultation;
import com.evcar.domain.inquiry.Inquiry;
import com.evcar.domain.user.User;
import com.evcar.domain.vehicle.Wishlist;
import com.evcar.dto.mypage.MyConsultationResponseDto;
import com.evcar.dto.mypage.MyInquiryResponseDto;
import com.evcar.dto.mypage.MyPageInfoResponseDto;
import com.evcar.dto.mypage.MyPageInfoUpdateRequestDto;
import com.evcar.dto.mypage.MyPageSummaryResponseDto;
import com.evcar.dto.mypage.MyWishlistResponseDto;
import com.evcar.dto.mypage.WithdrawRequestDto;
import com.evcar.repository.consultation.ConsultationRepository;
import com.evcar.repository.inquiry.InquiryRepository;
import com.evcar.repository.user.UserRepository;
import com.evcar.repository.vehicle.VehicleRepository;
import com.evcar.repository.vehicle.WishlistRepository;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageServiceImpl implements MyPageService {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final int MIN_VEHICLE_YEAR = 1900;

    private final UserRepository userRepository;
    private final ConsultationRepository consultationRepository;
    private final InquiryRepository inquiryRepository;
    private final WishlistRepository wishlistRepository;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public MyPageInfoResponseDto getMyPageInfo(String userId) {
        User user = getUserByUserId(userId);
        return MyPageInfoResponseDto.from(user);
    }

    @Override
    public MyPageSummaryResponseDto getMyPageSummary(String userId) {
        User user = getUserByUserId(userId);

        List<MyConsultationResponseDto> consultations = getMyConsultations(user.getUserId());
        List<MyInquiryResponseDto> inquiries = getMyInquiries(user.getUserId());
        List<MyWishlistResponseDto> wishlist = getMyWishlist(user.getUserId());

        int waitingInquiryCount = (int) inquiries.stream()
                .filter(inquiry -> !inquiry.isAnswered())
                .count();

        return MyPageSummaryResponseDto.builder()
                .wishlistCount(wishlist.size())
                .consultationCount(consultations.size())
                .inquiryCount(inquiries.size())
                .waitingInquiryCount(waitingInquiryCount)
                .build();
    }

    @Override
    @Transactional
    public void updateMyPageInfo(String userId, MyPageInfoUpdateRequestDto requestDto) {
        User user = getUserByUserId(userId);

        validateCurrentPassword(user, requestDto.getCurrentPassword());

        String name = getOrDefault(requestDto.getName(), user.getName());
        LocalDate birthDate = requestDto.getBirthDate() != null ? requestDto.getBirthDate() : user.getBirthDate();
        String gender = getOrDefault(requestDto.getGender(), user.getGender());
        String phone = getOrDefault(requestDto.getPhone(), user.getPhone());
        String address = getOrDefault(requestDto.getAddress(), user.getAddress());
        String addressDetail = getTrimmedOrDefault(requestDto.getAddressDetail(), user.getAddressDetail());
        String email = getOrDefault(requestDto.getEmail(), user.getEmail());

        if (requestDto.isInvalidPhone()) {
            throw new IllegalArgumentException("전화번호는 숫자 11자리로 입력해주세요.");
        }

        validateBasicInfo(name, birthDate, gender, phone, address, addressDetail, email);

        VehicleInfo vehicleInfo = resolveVehicleInfo(requestDto);
        validatePasswordChangeRequest(requestDto);

        if (isPasswordChangeRequested(requestDto)) {
            user.changePassword(passwordEncoder.encode(requestDto.getNewPassword().trim()));
        }

        user.updateMyPageInfo(
                name,
                birthDate,
                gender,
                phone,
                address,
                addressDetail,
                email,
                vehicleInfo.hasVehicle(),
                vehicleInfo.vehicleModel(),
                vehicleInfo.vehicleYear(),
                vehicleInfo.drivingDistance()
        );

        userRepository.saveAndFlush(user);
    }

    @Override
    public List<MyConsultationResponseDto> getMyConsultations(String userId) {
        User user = getUserByUserId(userId);

        return consultationRepository.findByUserUserIdOrderByCreatedAtDesc(user.getUserId()).stream()
                .map(MyConsultationResponseDto::from)
                .toList();
    }

    @Override
    @Transactional
    public void cancelMyConsultation(String userId, String consultId) {
        User user = getUserByUserId(userId);

        Consultation consultation = consultationRepository.findById(consultId)
                .orElseThrow(() -> new IllegalArgumentException("상담 정보를 찾을 수 없습니다."));

        if (!consultation.getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("본인의 상담만 취소할 수 있습니다.");
        }

        consultation.cancel();
    }

    @Override
    public List<MyInquiryResponseDto> getMyInquiries(String userId) {
        User user = getUserByUserId(userId);

        return inquiryRepository.findByUserUserIdOrderByCreatedAtDesc(user.getUserId()).stream()
                .map(MyInquiryResponseDto::from)
                .toList();
    }

    @Override
    public MyInquiryResponseDto getMyInquiryDetail(String userId, String inquiryId) {
        User user = getUserByUserId(userId);

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의 정보를 찾을 수 없습니다."));

        if (!inquiry.getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("본인의 문의만 조회할 수 있습니다.");
        }

        return MyInquiryResponseDto.from(inquiry);
    }

    @Override
    @Transactional
    public void withdraw(String userId, WithdrawRequestDto withdrawRequestDto) {
        if (withdrawRequestDto.isInvalid()) {
            throw new IllegalArgumentException("회원탈퇴 입력값을 확인해주세요.");
        }

        User user = getUserByUserId(userId);

        if ("WITHDRAWN".equalsIgnoreCase(user.getUserStatus())) {
            throw new IllegalArgumentException("이미 탈퇴 처리된 회원입니다.");
        }

        if (!passwordEncoder.matches(withdrawRequestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호를 확인하세요.");
        }

        boolean hasPendingOrInProgressConsultation =
                consultationRepository.existsByUserUserIdAndConsultStatusIn(
                        user.getUserId(),
                        List.of("PENDING", "IN_PROGRESS")
                );

        if (hasPendingOrInProgressConsultation) {
            throw new IllegalArgumentException("대기중 또는 진행중인 상담이 있어 탈퇴가 불가능합니다. 상담 완료 또는 취소 후 다시 시도해주세요.");
        }

        boolean hasWaitingInquiry =
                inquiryRepository.existsByUserUserIdAndReplyStatus(user.getUserId(), "WAITING");

        if (hasWaitingInquiry) {
            throw new IllegalArgumentException("답변 대기 중인 문의가 있어 탈퇴가 불가능합니다.");
        }

        user.withdraw();
    }

    @Override
    public List<MyWishlistResponseDto> getMyWishlist(String userId) {
        getUserByUserId(userId);

        return wishlistRepository.findByUserId(userId).stream()
                .map(wishlist -> vehicleRepository.findById(wishlist.getVehicleId())
                        .map(vehicle -> MyWishlistResponseDto.builder()
                                .wishlistId(wishlist.getWishlistId())
                                .brand(vehicle.getBrand())
                                .modelName(vehicle.getModelName())
                                .vehicleClass(vehicle.getVehicleClass())
                                .priceBasic(vehicle.getPriceBasic())
                                .imageUrl(vehicle.getImageUrl())
                                .detailUrl("/vehicle/" + vehicle.getVehicleId())
                                .build())
                        .orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional
    public void deleteWishlist(String userId, String wishlistId) {
        getUserByUserId(userId);

        if (wishlistId == null || wishlistId.isBlank()) {
            throw new IllegalArgumentException("관심차량 식별값이 없습니다.");
        }

        Wishlist wishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> new IllegalArgumentException("관심차량 정보를 찾을 수 없습니다."));

        if (!wishlist.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 관심차량만 삭제할 수 있습니다.");
        }

        wishlistRepository.delete(wishlist);
    }

    private User getUserByUserId(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원정보를 찾을 수 없습니다."));
    }

    private void validateCurrentPassword(User user, String currentPassword) {
        if (!hasText(currentPassword)) {
            throw new IllegalArgumentException("회원정보 수정을 위해 현재 비밀번호를 입력해주세요.");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
    }

    private VehicleInfo resolveVehicleInfo(MyPageInfoUpdateRequestDto requestDto) {
        String normalizedHasVehicle = normalizeHasVehicle(requestDto.getHasVehicle());

        if ("yes".equals(normalizedHasVehicle)) {
            String vehicleModel = trimToNull(requestDto.getVehicleModel());
            String vehicleYear = trimToNull(requestDto.getVehicleYear());
            Integer drivingDistance = requestDto.getDrivingDistance();

            validateOwnedVehicleInfo(vehicleModel, vehicleYear, drivingDistance);

            return new VehicleInfo(
                    "yes",
                    vehicleModel,
                    vehicleYear,
                    drivingDistance
            );
        }

        return new VehicleInfo(
                "no",
                "",
                "",
                0
        );
    }

    private String normalizeHasVehicle(String hasVehicle) {
        if (!hasText(hasVehicle)) {
            return "no";
        }

        String normalized = hasVehicle.trim().toLowerCase();
        return ("yes".equals(normalized) || "y".equals(normalized)) ? "yes" : "no";
    }

    private void validatePasswordChangeRequest(MyPageInfoUpdateRequestDto requestDto) {
        if (!isPasswordChangeRequested(requestDto)) {
            return;
        }

        if (!hasText(requestDto.getNewPassword()) || !hasText(requestDto.getNewPasswordConfirm())) {
            throw new IllegalArgumentException("새 비밀번호와 새 비밀번호 확인을 모두 입력해주세요.");
        }

        if (requestDto.isNewPasswordMismatch()) {
            throw new IllegalArgumentException("새 비밀번호와 새 비밀번호 확인이 일치하지 않습니다.");
        }
    }

    private boolean isPasswordChangeRequested(MyPageInfoUpdateRequestDto requestDto) {
        return hasText(requestDto.getNewPassword()) || hasText(requestDto.getNewPasswordConfirm());
    }

    private void validateBasicInfo(
            String name,
            LocalDate birthDate,
            String gender,
            String phone,
            String address,
            String addressDetail,
            String email
    ) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름을 입력해주세요.");
        }

        if (birthDate == null) {
            throw new IllegalArgumentException("생년월일을 입력해주세요.");
        }

        if (gender == null || gender.isBlank()) {
            throw new IllegalArgumentException("성별 정보를 확인해주세요.");
        }

        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("전화번호를 입력해주세요.");
        }

        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("주소를 입력해주세요.");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }
    }

    private void validateOwnedVehicleInfo(String vehicleModel, String vehicleYear, Integer drivingDistance) {
        if (vehicleModel == null || vehicleModel.isBlank()) {
            throw new IllegalArgumentException("보유 차량명을 입력해주세요.");
        }

        if (vehicleYear == null || vehicleYear.isBlank()) {
            throw new IllegalArgumentException("보유차량 연식을 입력해주세요.");
        }

        if (!vehicleYear.matches("^\\d{4}$")) {
            throw new IllegalArgumentException("보유차량 연식은 4자리 숫자(YYYY)로 입력해주세요.");
        }

        int currentYear = Year.now(KOREA_ZONE_ID).getValue();
        int year = Integer.parseInt(vehicleYear);

        if (year < MIN_VEHICLE_YEAR) {
            throw new IllegalArgumentException("보유차량 연식이 올바르지 않습니다.");
        }

        if (year > currentYear) {
            throw new IllegalArgumentException("보유차량 연식은 현재 연도보다 클 수 없습니다.");
        }

        if (drivingDistance == null) {
            throw new IllegalArgumentException("주행거리를 입력해주세요.");
        }

        if (drivingDistance < 0) {
            throw new IllegalArgumentException("주행거리는 0 이상이어야 합니다.");
        }
    }

    private String getOrDefault(String value, String defaultValue) {
        return hasText(value) ? value.trim() : defaultValue;
    }

    private String getTrimmedOrDefault(String value, String defaultValue) {
        if (value == null) {
            return defaultValue == null ? "" : defaultValue;
        }

        return value.trim();
    }

    private String trimToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private record VehicleInfo(
            String hasVehicle,
            String vehicleModel,
            String vehicleYear,
            Integer drivingDistance
    ) {
    }
}