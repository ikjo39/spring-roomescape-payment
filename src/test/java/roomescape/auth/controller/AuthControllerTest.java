package roomescape.auth.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.RestClientControllerTest;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.token.TokenProvider;
import roomescape.member.model.MemberRole;

class AuthControllerTest extends RestClientControllerTest {

    @Autowired
    private TokenProvider tokenProvider;

    @DisplayName("로그인에 성공하면 인증 토큰이 담긴 쿠키를 반환한다.")
    @Test
    void loginTest() {
        // Given
        final String email = "user@mail.com";
        final String password = "userPw1234!";
        final LoginRequest loginRequest = new LoginRequest(email, password);

        // When && Then
        RestAssured.given(spec).log().all()
                .filter(document("login-success"))
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .cookie("token");
    }

    @DisplayName("존재하지 않는 이메일로 로그인 요청을 하면 에러 코드가 반환된다.")
    @Test
    void loginFailWithUnknownEmail() {
        // Given
        final String email = "hacker@mail.com";
        final String password = "userPw1234!";
        final LoginRequest loginRequest = new LoginRequest(email, password);

        // When & Then
        RestAssured.given(spec).log().all()
                .filter(document("fail-login-not-exist-id"))
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(400)
                .body("message", is("해당 이메일 정보와 일치하는 회원 정보가 없습니다."));
    }

    @DisplayName("일치하지 않는 비밀번호로 로그인 요청을 하면 에러 코드가 반환된다.")
    @Test
    void loginFailWithInvalidPassword() {
        // Given
        final String email = "user@mail.com";
        final String password = "hackerPw1234!";
        final LoginRequest loginRequest = new LoginRequest(email, password);

        // When & Then
        RestAssured.given(spec).log().all()
                .filter(document("fail-login-not-exist-pw"))
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(400)
                .body("message", is("일치하지 않는 비밀번호입니다."));
    }

    @DisplayName("인증 토큰이 포함된 쿠키를 전송하면 인증된 사용자 이름이 반환된다.")
    @Test
    void loginCheckTest() {
        // Given
        final Long memberId = 3L;
        final MemberRole role = MemberRole.USER;
        final String accessToken = tokenProvider.createToken(memberId, role);

        // When & Then
        RestAssured.given(spec).log().all()
                .filter(document("login-check"))
                .cookie("token", accessToken)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(200)
                .body("name", is("켈리"));
    }

    @DisplayName("존재하지 않은 사용자 아이디 기반의 인증 토큰이 포함된 쿠키를 전송하면 에러 코드가 반환된다.")
    @Test
    void loginCheckWithUnknownEmailTest() {
        // Given
        final Long memberId = 10L;
        final MemberRole role = MemberRole.USER;
        final String accessToken = tokenProvider.createToken(memberId, role);

        // When & Then
        RestAssured.given(spec).log().all()
                .filter(document("fail-login-check-invalid-member"))
                .cookie("token", accessToken)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(400)
                .body("message", is("해당 회원 아이디와 일치하는 회원 정보가 없습니다."));
    }

    @DisplayName("유효하지 않은 쿠키를 포함하여 로그인 확인 요청을 하면 에러 코드가 반환된다.")
    @Test
    void loginCheckWithInvalidCookie() {
        // When & Then
        RestAssured.given(spec).log().all()
                .filter(document("fail-login-check-invalid-cookie"))
                .cookie("invalid-cookie", "그냥 좀 해주면 안되요?ㅋ")
                .when().get("/login/check")
                .then().log().all()
                .statusCode(401)
                .body("message", is("인증되지 않은 요청입니다."));
    }

    @DisplayName("쿠키를 포함하지 않고 로그인 확인 요청을 하면 에러 코드가 반환된다.")
    @Test
    void loginCheckWithoutCookie() {
        // When & Then
        RestAssured.given(spec).log().all()
                .filter(document("fail-login-check-non-contains-token"))
                .when().get("/login/check")
                .then().log().all()
                .statusCode(401)
                .body("message", is("인증되지 않은 요청입니다."));
    }

    @DisplayName("유효하지 않은 값의 인증 토큰으로 요청하면 에러 코드가 반환된다.")
    @Test
    void loginCheckWithInvalidTokenTest() {
        // When & Then
        RestAssured.given(spec).log().all()
                .filter(document("fail-login-check-invalid-token"))
                .cookie("token", "invalid-token")
                .when().get("/login/check")
                .then().log().all()
                .statusCode(401)
                .body("message", is("유효하지 않은 인증 토큰입니다."));
    }

    @DisplayName("만료된 인증 토큰으로 요청하면 에러 코드가 반환된다.")
    @Test
    void loginCheckWithExpiredTokenTest() {
        // Given
        final String expiredToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyIiwiaWF0I" +
                                    "joxNzE1MzY1ODI2LCJleHAiOjE3MTUzNjU4MjYsInJvbGUiOiJVU0VSIn0." +
                                    "mLgs2dqD9oCOUtleHtpcmf4tTw39bC9pmqFaUBPQZy9ADPsgRXEu3qhLS8qqs3UiV6MPmP_03FaZHX8UrieK4A";

        // When & Then
        RestAssured.given(spec).log().all()
                .filter(document("fail-login-check-expired-token"))
                .cookie("token", expiredToken)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(401)
                .body("message", is("만료된 인증 토큰입니다."));
    }

    @DisplayName("로그아웃을 하면 200ok와 쿠키가 삭제된다.")
    @Test
    void logout() {
        // Given
        final Long memberId = 10L;
        final MemberRole role = MemberRole.USER;
        final String accessToken = tokenProvider.createToken(memberId, role);

        // When & Then
        RestAssured.given(spec).log().all()
                .filter(document("logout"))
                .cookie("token", accessToken)
                .when().post("/logout")
                .then().log().all()
                .statusCode(200)
                .cookie("token", is(""));
    }
}
