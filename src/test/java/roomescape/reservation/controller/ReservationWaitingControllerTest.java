package roomescape.reservation.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.RestClientControllerTest;
import roomescape.auth.token.TokenProvider;
import roomescape.member.model.MemberRole;
import roomescape.reservation.dto.SaveReservationWaitingRequest;

class ReservationWaitingControllerTest extends RestClientControllerTest {

    @Autowired
    private TokenProvider tokenProvider;

    @DisplayName("예약 대기 정보를 모두 조회한다.")
    @Test
    void getReservationWaitingTest() {
        // When & Then
        RestAssured.given(spec).log().all()
                .filter(document("findAll-waiting-reservations"))
                .contentType(ContentType.JSON)
                .cookie("token", createAccessToken(1L, MemberRole.ADMIN))
                .when().get("/admin/reservation-waiting")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(6));
    }

    @DisplayName("인증된 사용자의 예약 대기 정보를 모두 조회한다.")
    @Test
    void getMyReservationWaitingTest() {
        // When & Then
        RestAssured.given(spec).log().all()
                .filter(document("findAll-my-waiting-reservations"))
                .contentType(ContentType.JSON)
                .cookie("token", createAccessToken(1L, MemberRole.ADMIN))
                .when().get("/reservation-waiting-mine")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }

    @DisplayName("예약 대기 정보를 저장한다.")
    @Test
    void saveReservationWaitingTest() {
        // Given
        final SaveReservationWaitingRequest saveReservationWaitingRequest = new SaveReservationWaitingRequest(
                LocalDate.now().plusDays(3), 2L, 1L, 10L);

        // When & Then
        RestAssured.given(spec).log().all()
                .filter(document("save-waiting-reservation"))
                .contentType(ContentType.JSON)
                .cookie("token", createAccessToken(2L, MemberRole.USER))
                .body(saveReservationWaitingRequest)
                .when().post("/reservation-waiting")
                .then().log().all()
                .statusCode(201)
                .body("id", is(7));
    }

    @DisplayName("존재하지 않는 예약에 대해 대기 신청을 하면 에러 코드가 반환된다.")
    @Test
    void saveNotExistReservationWaitingTest() {
        // Given
        final SaveReservationWaitingRequest saveReservationWaitingRequest = new SaveReservationWaitingRequest(
                LocalDate.now(), 1L, 1L, 2L);

        // When & Then
        RestAssured.given(spec).log().all()
                .filter(document("fail-save-waiting-reservation-non-exist-reservation"))
                .contentType(ContentType.JSON)
                .cookie("token", createAccessToken(2L, MemberRole.USER))
                .body(saveReservationWaitingRequest)
                .when().post("/reservation-waiting")
                .then().log().all()
                .statusCode(400)
                .body("message", is("존재하지 않는 예약에 대한 대기 신청을 할 수 없습니다."));
    }

    @DisplayName("중복된 예약 대기 신청을 하면 에러 코드가 반환된다.")
    @Test
    void saveDuplicateReservationWaitingTest() {
        // Given
        final SaveReservationWaitingRequest saveReservationWaitingRequest = new SaveReservationWaitingRequest(
                LocalDate.now().plusDays(6), 2L, 3L, 8L);

        // When & Then
        RestAssured.given(spec).log().all()
                .filter(document("fail-save-waiting-reservation-already-exist"))
                .contentType(ContentType.JSON)
                .cookie("token", createAccessToken(2L, MemberRole.USER))
                .body(saveReservationWaitingRequest)
                .when().post("/reservation-waiting")
                .then().log().all()
                .statusCode(400)
                .body("message", is("이미 해당 예약 대기가 존재합니다."));
    }

    @DisplayName("예약 대기 정보를 삭제한다.")
    @Test
    void deleteReservationWaitingTest() {
        // When & Then
        RestAssured.given(spec).log().all()
                .filter(document("delete-waiting-reservations"))
                .contentType(ContentType.JSON)
                .cookie("token", createAccessToken(2L, MemberRole.USER))
                .when().delete("/reservation-waiting/" + 2L)
                .then().log().all()
                .statusCode(204);
    }

    private String createAccessToken(final Long memberId, final MemberRole role) {
        return tokenProvider.createToken(memberId, role);
    }
}
