package com.example.food_app.service.user;

import com.example.food_app.dto.request.user.LoginRequest;
import com.example.food_app.dto.request.user.LogoutRequest;
import com.example.food_app.dto.request.user.RefreshTokenRequest;
import com.example.food_app.dto.request.user.RegisterRequest;
import com.example.food_app.dto.response.user.LoginResponse;
import com.example.food_app.dto.response.user.RegisterResponse;
import com.example.food_app.entity.*;
import com.example.food_app.entity.enums.Role;
import com.example.food_app.repository.*;
import com.example.food_app.security.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AccountRepository accountRepository;
    private final CartRepository cartRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final VoucherRepository voucherRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    public ResponseEntity<?> login(LoginRequest request) {
        try {
            Optional<Account> optionalAccount =
                    accountRepository.findByEmail(
                            request.getEmail()
                    );

            if (optionalAccount.isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body("Email không tồn tại");
            }

            Account account = optionalAccount.get();

            if (!Boolean.TRUE.equals(account.getIsActive())) {

                return ResponseEntity
                        .badRequest()
                        .body("Tài khoản đã bị vô hiệu hóa");
            }

            if (!account.getPassword()
                    .equals(request.getPassword())) {

                return ResponseEntity
                        .badRequest()
                        .body("Mật khẩu không đúng");
            }

            // ACCESS TOKEN
            String accessToken =
                    jwtUtil.generateAccessToken(
                            account.getEmail(),
                            account.getRole()
                    );

            // REFRESH TOKEN
            String refreshToken =
                    jwtUtil.generateRefreshToken(
                            account.getEmail()
                    );

            RefreshToken refreshTokenEntity =
                    RefreshToken.builder()
                            .token(refreshToken)
                            .account(account)
                            .revoked(false)
                            .expiryDate(
                                    LocalDateTime.now()
                                            .plusDays(7)
                            )
                            .build();

            refreshTokenRepository.save(refreshTokenEntity);
            LoginResponse response = LoginResponse.builder()
                    .id(account.getAccountId())
                    .fullName(account.getFullName())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .phone(account.getPhone())
                    .email(account.getEmail())
                    .role(account.getRole())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<?> register(RegisterRequest request) {
        try {

            if (accountRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity
                        .badRequest()
                        .body(RegisterResponse.builder()
                                .success(false)
                                .message("Email đã tồn tại")
                                .build());
            }

            if (!request.getPhone().matches("^0\\d{9}$")) {
                return ResponseEntity
                        .badRequest()
                        .body(RegisterResponse.builder()
                                .success(false)
                                .message("Số điện thoại không hợp lệ")
                                .build());
            }

            if (request.getPassword().length() < 6 ||
                    !request.getPassword().matches(
                            "^(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>?,./]).+$")) {

                return ResponseEntity
                        .badRequest()
                        .body(RegisterResponse.builder()
                                .success(false)
                                .message("Mật khẩu phải >=6 ký tự và có chữ hoa, chữ thường, ký tự đặc biệt")
                                .build());
            }

            Account account = new Account();
            account.setFullName(request.getFullName());
            account.setEmail(request.getEmail());
            account.setPhone(request.getPhone());
            account.setPassword(request.getPassword());
            account.setRole(Role.CUSTOMER);
            account.setIsActive(true);
            accountRepository.save(account);

            Cart cart = new Cart();
            cart.setAccount(account);
            cartRepository.save(cart);
            Optional<Voucher> optionalVoucher = voucherRepository.findByCode("NEW50");

            if (optionalVoucher.isPresent()) {
                Voucher voucher = optionalVoucher.get();

                UserVoucher userVoucher = new UserVoucher();
                userVoucher.setAccount(account);
                userVoucher.setVoucher(voucher);
                userVoucher.setUsed(false);
                userVoucher.setUsedAt(null);

                userVoucherRepository.save(userVoucher);
            }

            return ResponseEntity.ok(
                    RegisterResponse.builder()
                            .success(true)
                            .message("Đăng ký thành công")
                            .build()
            );

        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .body(RegisterResponse.builder()
                            .success(false)
                            .message("Lỗi hệ thống")
                            .build());
        }
    }

    public ResponseEntity<?> refreshToken(
            RefreshTokenRequest request
    ) {

        try {

            String refreshToken =
                    request.getRefreshToken();

            // validate token
            if (!jwtUtil.validateToken(refreshToken)) {

                return ResponseEntity
                        .badRequest()
                        .body("Refresh token không hợp lệ");
            }

            Optional<RefreshToken> optionalRefreshToken =
                    refreshTokenRepository.findByToken(
                            refreshToken
                    );

            if (optionalRefreshToken.isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body("Refresh token không tồn tại");
            }

            RefreshToken refreshTokenEntity =
                    optionalRefreshToken.get();

            if (Boolean.TRUE.equals(
                    refreshTokenEntity.getRevoked()
            )) {

                return ResponseEntity
                        .badRequest()
                        .body("Refresh token đã bị thu hồi");
            }

            if (!jwtUtil.getTokenType(refreshToken)
                    .equals("REFRESH")) {

                return ResponseEntity
                        .badRequest()
                        .body("Token không hợp lệ");
            }

            String email =
                    jwtUtil.getEmailFromToken(refreshToken);

            Optional<Account> optionalAccount =
                    accountRepository
                            .findByEmailAndIsActiveIsTrue(email);

            if (optionalAccount.isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body("Tài khoản không tồn tại");
            }

            Account account = optionalAccount.get();

            String newAccessToken =
                    jwtUtil.generateAccessToken(
                            account.getEmail(),
                            account.getRole()
                    );

            return ResponseEntity.ok(
                    LoginResponse.builder()
                            .accessToken(newAccessToken)
                            .refreshToken(refreshToken)
                            .build()
            );

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body("Lỗi hệ thống");
        }
    }

    public ResponseEntity<?> logout(
            LogoutRequest request
    ) {

        try {

            String refreshToken =
                    request.getRefreshToken();

            Optional<RefreshToken> optionalRefreshToken =
                    refreshTokenRepository.findByToken(
                            refreshToken
                    );

            if (optionalRefreshToken.isPresent()) {

                RefreshToken token =
                        optionalRefreshToken.get();

                token.setRevoked(true);

                refreshTokenRepository.save(token);
            }

            return ResponseEntity.ok(
                    "Đăng xuất thành công"
            );

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body("Lỗi hệ thống");
        }
    }
}
