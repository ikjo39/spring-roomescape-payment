package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.config.TestPaymentGatewayConfig;
import roomescape.payment.dto.SavePaymentCredentialRequest;
import roomescape.payment.service.PaymentService;
import roomescape.reservation.dto.ReservationDto;
import roomescape.reservation.dto.SaveReservationRequest;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationWaiting;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationWaitingRepository;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import(TestPaymentGatewayConfig.class)
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationWaitingRepository reservationWaitingRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private PaymentService paymentService;

    @DisplayName("전체 예약 정보를 조회한다.")
    @Test
    void getReservationsTest() {
        // When
        final List<ReservationDto> reservations = reservationService.getReservations();

        // Then
        assertThat(reservations).hasSize(16);
    }

    @DisplayName("예약 정보를 저장한다.")
    @Test
    void saveReservationTest() {
        // Given
        final LocalDate date = LocalDate.now().plusDays(10);
        String orderId = "orderId";
        long amount = 1000L;
        final SaveReservationRequest saveReservationRequest = new SaveReservationRequest(
                date,
                3L,
                1L,
                1L,
                orderId,
                amount,
                "paymentKey"
        );
        paymentService.saveCredential(new SavePaymentCredentialRequest(orderId, amount));

        // When
        final ReservationDto reservation = reservationService.saveReservation(saveReservationRequest);

        // Then
        final List<ReservationDto> reservations = reservationService.getReservations();
        assertAll(
                () -> assertThat(reservations).hasSize(17),
                () -> assertThat(reservation.id()).isEqualTo(17L),
                () -> assertThat(reservation.member().id()).isEqualTo(3L),
                () -> assertThat(reservation.date().getValue()).isEqualTo(date),
                () -> assertThat(reservation.time().startAt()).isEqualTo(LocalTime.of(9, 30))
        );
    }

    @DisplayName("저장하려는 예약 시간이 존재하지 않는다면 예외를 발생시킨다.")
    @Test
    void throwExceptionWhenSaveReservationWithNotExistReservationTimeTest() {
        // Given
        final SaveReservationRequest saveReservationRequest = new SaveReservationRequest(
                LocalDate.now(),
                3L,
                9L,
                1L,
                "orderId",
                1000L,
                "paymentKey"
        );

        // When & Then
        assertThatThrownBy(() -> reservationService.saveReservation(saveReservationRequest))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("해당 id의 예약 시간이 존재하지 않습니다.");
    }

    @DisplayName("예약 정보를 삭제한다.")
    @Test
    void deleteReservationTest() {
        // When
        reservationService.deleteReservation(1L);

        // When & Then
        final List<ReservationDto> reservations = reservationService.getReservations();
        assertThat(reservations).hasSize(15);
    }

    @DisplayName("예약 정보를 삭제하면 가장 우선 순위가 빠른 예약 대기가 예약으로 등록된다.")
    @Test
    void changeWaitingToReservationWhenDeleteReservationTest() {
        // When
        final ReservationWaiting reservationWaiting = reservationWaitingRepository.findById(1L).get();
        reservationService.deleteReservation(13L);
        final Reservation newReservation = reservationRepository.findById(17L).get();

        // Then
        assertAll(
                () -> assertThat(reservationWaiting.getTheme().getId())
                        .isEqualTo(newReservation.getTheme().getId()),
                () -> assertThat(reservationWaiting.getMember().getId())
                        .isEqualTo(newReservation.getMember().getId()),
                () -> assertThat(reservationWaiting.getDate().getValue())
                        .isEqualTo(newReservation.getDate().getValue()),
                () -> assertThat(reservationWaiting.getTime().getStartAt())
                        .isEqualTo(newReservation.getTime().getStartAt())
        );
    }

    @DisplayName("현재 보다 이전 날짜/시간의 예약 정보를 저장하려고 하면 예외가 발생한다.")
    @Test
    void throwExceptionWhenPastDateOrTime() {
        // Given
        final SaveReservationRequest saveReservationRequest = new SaveReservationRequest(
                LocalDate.now().minusDays(3),
                3L,
                1L,
                1L,
                "orderId",
                1000L,
                "paymentKey"
        );

        // When & Then
        assertThatThrownBy(() -> reservationService.saveReservation(saveReservationRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재 날짜보다 이전 날짜를 예약할 수 없습니다.");
    }

    @DisplayName("이미 존재하는 예약 날짜/시간/테마가 입력되면 예외가 발생한다.")
    @Test
    void throwExceptionWhenInputDuplicateReservationDate() {
        // Given
        final SaveReservationRequest saveReservationRequest = new SaveReservationRequest(
                LocalDate.now().plusDays(2),
                3L,
                4L,
                9L,
                "orderId",
                1000L,
                "paymentKey"
        );

        // When & Then
        assertThatThrownBy(() -> reservationService.saveReservation(saveReservationRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 해당 날짜/시간의 테마 예약이 있습니다.");
    }
}
