package com.washer.Things.domain.user.presentation;

import com.washer.Things.domain.user.entity.enums.Gender;
import com.washer.Things.domain.user.presentation.dto.request.RestrictUserRequest;
import com.washer.Things.domain.user.presentation.dto.response.AdminUserInfoResponse;
import com.washer.Things.domain.user.presentation.dto.response.UserResponse;
import com.washer.Things.global.exception.dto.response.ApiResponse;
import com.washer.Things.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/user")
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me() {
        UserResponse userResponse = userService.getUserInfo();
        return ResponseEntity.ok(ApiResponse.success(userResponse, "내 정보 조회 성공"));
    }

    @GetMapping("/admin/user/info")
    public ResponseEntity<ApiResponse<List<AdminUserInfoResponse>>> getUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) String floor
    ) {
        List<AdminUserInfoResponse> result = userService.getUsers(name, gender, floor);
        return ResponseEntity.ok(ApiResponse.success(result, "유저 목록 조회 성공"));
    }

    @PostMapping("/admin/{userId}/restrict")
    public ResponseEntity<ApiResponse<Void>> restrictUser(
            @PathVariable Long userId,
            @RequestBody RestrictUserRequest request
            ) {
        userService.restrictUser(userId, request);
        return ResponseEntity.ok(ApiResponse.success("유저 정지 완료"));
    }

    @PostMapping("/admin/{userId}/unrestrict")
    public ResponseEntity<ApiResponse<Void>> unrestrictUser(@PathVariable Long userId) {
        userService.unrestrictUser(userId);
        return ResponseEntity.ok(ApiResponse.success("유저 정지 해제 완료"));
    }
}
