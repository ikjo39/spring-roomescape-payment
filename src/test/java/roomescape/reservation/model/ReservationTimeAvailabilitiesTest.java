package roomescape.reservation.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.model.Member;
import roomescape.member.model.MemberRole;

class ReservationTimeAvailabilitiesTest {

    @DisplayName("예약이 결정된 시간인지 검증후 결과를 저장한다.")
    @Test
    void checkIsAlreadyBookedTest() {
        // Given
        final ReservationStatus reservationStatus = ReservationStatus.RESERVATION;
        final ReservationTime reservationTime1 = new ReservationTime(1L, LocalTime.of(1, 30));
        final ReservationTime reservationTime2 = new ReservationTime(2L, LocalTime.of(2, 30));
        final ReservationTime reservationTime3 = new ReservationTime(3L, LocalTime.of(4, 30));
        final ReservationTime notBookedTime = new ReservationTime(4L, LocalTime.of(3, 30));
        final List<ReservationTime> reservationTimes = List.of(reservationTime1, reservationTime2, reservationTime3,
                notBookedTime);
        final Theme theme = new Theme(1L, "켈리의 두근두근", "안녕", "사진 링크");
        final Member member = new Member(1L, MemberRole.USER, "password1111", "kelly", "kelly6bf@mail.com");

        final LocalDate date = LocalDate.now().plusDays(3);
        final List<Reservation> reservations = List.of(
                new Reservation(1L, reservationStatus, date, reservationTime1, theme, member, PaymentStatus.DONE),
                new Reservation(2L, reservationStatus, date, reservationTime2, theme, member, PaymentStatus.DONE),
                new Reservation(3L, reservationStatus, date, reservationTime3, theme, member, PaymentStatus.DONE)
        );

        // When
        final ReservationTimeAvailabilities reservationTimeAvailabilities =
                ReservationTimeAvailabilities.of(reservationTimes, reservations);

        // Then
        final Map<ReservationTime, Boolean> values = reservationTimeAvailabilities.values();
        assertAll(
                () -> assertThat(values.get(reservationTime1)).isTrue(),
                () -> assertThat(values.get(reservationTime2)).isTrue(),
                () -> assertThat(values.get(reservationTime3)).isTrue(),
                () -> assertThat(values.get(notBookedTime)).isFalse()
        );
    }
}
