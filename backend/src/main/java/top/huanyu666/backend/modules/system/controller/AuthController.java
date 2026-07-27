package top.huanyu666.backend.modules.system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.modules.system.dto.LoginRequest;
import top.huanyu666.backend.modules.system.dto.UserInfoResponse;
import top.huanyu666.backend.modules.system.service.AuthService;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<String> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request);
        return ApiResponse.ok(token);
    }

    @GetMapping("/user-info")
    public ApiResponse<UserInfoResponse> userInfo() {
        return ApiResponse.ok(authService.getUserInfo());
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        cn.dev33.satoken.stp.StpUtil.logout();
        return ApiResponse.ok();
    }
}
