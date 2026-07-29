package com.aiinterview.user.controller;

import com.aiinterview.common.code.ResultCode;
import com.aiinterview.common.dto.ApiResponse;
import com.aiinterview.user.dto.LoginRequest;
import com.aiinterview.user.dto.LoginResponse;
import com.aiinterview.user.dto.SignupRequest;
import com.aiinterview.user.dto.SignupResponse;
import com.aiinterview.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 관련 REST API Controller.
 *
 * <p>현재 Sprint (User Domain): 회원가입 및 로그인 API를 구현한다.</p>
 *
 * <p>추후 Auth Sprint에서 아래 엔드포인트를 추가한다:</p>
 * <ul>
 *     <li>POST /api/v1/auth/refresh   - Access Token 재발급</li>
 *     <li>POST /api/v1/auth/logout    - 로그아웃</li>
 * </ul>
 *
 * <p>Controller 책임 범위:</p>
 * <ul>
 *     <li>HTTP 요청 수신</li>
 *     <li>@Valid를 통한 입력값 검증</li>
 *     <li>Service 호출</li>
 *     <li>HTTP 응답 반환</li>
 * </ul>
 *
 * <p>비즈니스 로직은 절대 작성하지 않는다.
 * 예외 처리는 {@link com.aiinterview.common.exception.GlobalExceptionHandler}가 담당한다.</p>
 */
@Slf4j
@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 회원가입 API.
     *
     * <p>입력값 검증은 {@code @Valid}가 수행한다.
     * 검증 실패 시 {@link com.aiinterview.common.exception.GlobalExceptionHandler}가
     * {@code 400 Bad Request}를 반환한다.</p>
     *
     * <p>이메일 중복 시 {@link com.aiinterview.user.service.UserServiceImpl}이
     * {@code BusinessException(DUPLICATE_EMAIL)}을 던지고,
     * {@link com.aiinterview.common.exception.GlobalExceptionHandler}가
     * {@code 409 Conflict}를 반환한다.</p>
     *
     * @param request 회원가입 요청 DTO (이메일, 비밀번호, 닉네임)
     * @return 201 Created + 가입된 사용자 정보 (id, 이메일, 닉네임)
     */
    @Operation(
            summary = "회원가입",
            description = "이메일, 비밀번호, 닉네임으로 신규 회원을 등록합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = SignupResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "입력값 검증 실패 (이메일 형식, 비밀번호 조건, 닉네임 길이)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이메일 중복"
            )
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(
            @RequestBody @Valid SignupRequest request) {

        log.info("Signup request received - email: {}", request.getEmail());

        SignupResponse response = userService.signup(request);

        log.info("Signup response returned - userId: {}", response.getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(ResultCode.USER_CREATED, response));
    }

    /**
     * 로그인 API.
     *
     * <p>입력값 검증은 {@code @Valid}가 수행한다.
     * 비밀번호 검증 및 사용자 조회 실패 시 각각 알맞은 예외를 던집니다.</p>
     *
     * @param request 로그인 요청 DTO (이메일, 비밀번호)
     * @return 200 OK + 로그인 완료된 사용자 정보 (id, 이메일, 닉네임, role)
     */
    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호로 로그인을 요청합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "비밀번호 불일치 또는 검증 실패"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 회원"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @RequestBody @Valid LoginRequest request) {

        log.info("Login request received - email: {}", request.getEmail());

        LoginResponse response = userService.login(request);

        log.info("Login response returned - userId: {}", response.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(ResultCode.LOGIN_SUCCESS, response));
    }
}
