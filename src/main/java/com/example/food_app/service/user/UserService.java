package com.example.food_app.service.user;

import com.example.food_app.dto.request.user.ProfileUpdateRequest;
import com.example.food_app.dto.response.user.ProfileResponse;
import com.example.food_app.entity.Account;
import com.example.food_app.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AccountRepository accountRepository;
    private final Cloudinary cloudinary;

    public ProfileResponse getProfile(Account account) {

        if (account == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Chưa đăng nhập"
            );
        }

        Account acc = accountRepository.findByEmailAndIsActiveIsTrue(account.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tài khoản"
                ));

        return mapToResponse(acc);
    }

    public ProfileResponse updateProfile(
            Account account,
            ProfileUpdateRequest request,
            MultipartFile file
    ) {

        if (account == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Chưa đăng nhập"
            );
        }

        Account acc = accountRepository.findById(account.getAccountId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tài khoản"
                ));

        if (request.getFullName() != null) {
            if (request.getFullName().trim().isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Tên không được để trống"
                );
            }
            acc.setFullName(request.getFullName());
        }

        if (request.getPhone() != null) {
            if (!request.getPhone().matches("^(0[0-9]{9})$")) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Số điện thoại không hợp lệ"
                );
            }
            acc.setPhone(request.getPhone());
        }

        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "File phải là ảnh"
                );
            }

            if (file.getSize() > 2 * 1024 * 1024) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Ảnh tối đa 2MB"
                );
            }

            try {
                Map uploadResult = cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "folder", "avatars"
                        )
                );

                String imageUrl = uploadResult.get("secure_url").toString();

                acc.setAvtUrl(imageUrl);

            } catch (IOException e) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Upload ảnh thất bại"
                );
            }
        }

        try {
            accountRepository.save(acc);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Lỗi khi cập nhật dữ liệu"
            );
        }

        return mapToResponse(acc);
    }

    private ProfileResponse mapToResponse(Account account) {
        return ProfileResponse.builder()
                .accountId(account.getAccountId())
                .fullName(account.getFullName())
                .email(account.getEmail())
                .phone(account.getPhone())
                .avtUrl(account.getAvtUrl())
                .createdAt(account.getCreatedAt())
                .build();
    }
}