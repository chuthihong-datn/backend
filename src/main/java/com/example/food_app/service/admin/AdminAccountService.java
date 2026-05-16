package com.example.food_app.service.admin;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.food_app.dto.request.admin.AccountUpdateRequest;
import com.example.food_app.dto.response.admin.AccountResponse;
import com.example.food_app.entity.Account;
import com.example.food_app.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminAccountService {
    private final AccountRepository accountRepository;
    private final Cloudinary cloudinary;

    public List<AccountResponse> getAll() {
        return accountRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AccountResponse getDetail(BigInteger id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tài khoản"
                ));

        return mapToResponse(account);
    }

    @Transactional
    public AccountResponse update(
            BigInteger id,
            AccountUpdateRequest request,
            MultipartFile avatarFile
    ) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tài khoản"
                ));

        if (request.getFullName() != null) {
            if (request.getFullName().trim().isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Họ tên không được để trống"
                );
            }

            account.setFullName(request.getFullName().trim());
        }

        if (request.getRole() != null) {
            account.setRole(request.getRole());
        }

        if (avatarFile != null && !avatarFile.isEmpty()) {
            account.setAvtUrl(uploadImage(avatarFile));
        }

        accountRepository.save(account);

        return mapToResponse(account);
    }

    @Transactional
    public void disable(BigInteger id) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tài khoản"
                ));

        account.setIsActive(false);

        accountRepository.save(account);
    }

    private String uploadImage(MultipartFile file) {

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File phải là ảnh"
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
                    ObjectUtils.asMap("folder", "accounts")
            );

            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Upload ảnh thất bại"
            );
        }
    }

    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .accountId(account.getAccountId())
                .fullName(account.getFullName())
                .email(account.getEmail())
                .phone(account.getPhone())
                .role(account.getRole())
                .isActive(account.getIsActive())
                .avtUrl(account.getAvtUrl())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}