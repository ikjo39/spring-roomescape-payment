package roomescape.reservation.dto;

import java.time.LocalDate;
import roomescape.member.dto.MemberResponse;

public record ReservationWaitingResponse(
        Long id,
        MemberResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {

    public static ReservationWaitingResponse from(final ReservationWaitingDto reservationWaitingDto) {
        return new ReservationWaitingResponse(
                reservationWaitingDto.id(),
                new MemberResponse(
                        reservationWaitingDto.member().id(),
                        reservationWaitingDto.member().name().getValue(),
                        reservationWaitingDto.member().email().getValue()
                ),
                reservationWaitingDto.date().getValue(),
                ReservationTimeResponse.from(reservationWaitingDto.time()),
                ThemeResponse.from(reservationWaitingDto.theme())
        );
    }
}
