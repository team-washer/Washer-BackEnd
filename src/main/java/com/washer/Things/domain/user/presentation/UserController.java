package com.washer.Things.domain.user.presentation;

import com.washer.Things.domain.user.entity.enums.Gender;
import com.washer.Things.domain.user.presentation.dto.request.RestrictUserRequest;
import com.washer.Things.domain.user.presentation.dto.response.AdminUserInfoResponse;
import com.washer.Things.domain.user.presentation.dto.response.UserResponse;
import com.washer.Things.domain.user.service.UserService;
import com.washer.Things.global.exception.dto.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/user")
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @Operation(summary = "마이페이지")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 정보 조회 성공")
    })
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserResponse>> me() {
        UserResponse userResponse = userService.getUserInfo();
        return ResponseEntity.ok(BaseResponse.success(userResponse, "내 정보 조회 성공"));
    }

    @Operation(summary = "유저 목록 조회 (관리자)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "유저 목록 조회 성공")
    })
    @GetMapping("/admin/user/info")
    public ResponseEntity<BaseResponse<List<AdminUserInfoResponse>>> getUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) String floor
    ) {
        List<AdminUserInfoResponse> result = userService.getUsers(name, gender, floor);
        return ResponseEntity.ok(BaseResponse.success(result, "유저 목록 조회 성공"));
    }

    @Operation(summary = "유저 정지 (관리자)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "유저 정지 완료"),
            @ApiResponse(responseCode = "400", description = "잘못된 정지 기간일 경우"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없을 경우")
    })
    @PostMapping("/admin/{userId}/restrict")
    public ResponseEntity<BaseResponse<Void>> restrictUser(
            @PathVariable Long userId,
            @RequestBody @Valid RestrictUserRequest request
    ) {
        userService.restrictUser(userId, request);
        return ResponseEntity.ok(BaseResponse.success("유저 정지 완료"));
    }

    @Operation(summary = "유저 정지 해제 (관리자)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "유저 정지 해제 완료"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없을 경우")
    })
    @PostMapping("/admin/{userId}/unrestrict")
    public ResponseEntity<BaseResponse<Void>> unrestrictUser(@PathVariable Long userId) {
        userService.unrestrictUser(userId);
        return ResponseEntity.ok(BaseResponse.success("유저 정지 해제 완료"));
    }
}
