package roomescape.reservation.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.dto.AvailableReservationTimeResponse;
import roomescape.reservation.dto.ReservationTimeResponse;
import roomescape.reservation.service.ReservationTimeService;

@RestController
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(final ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping("/times")
    public List<ReservationTimeResponse> getReservationTimes() {
        return reservationTimeService.getReservationTimes()
                .stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    @GetMapping("/available-reservation-times")
    public List<AvailableReservationTimeResponse> getAvailableReservationTimes(
            @RequestParam("date") final LocalDate date,
            @RequestParam("theme-id") final Long themeId
    ) {
        return reservationTimeService.getAvailableReservationTimes(date, themeId)
                .values()
                .entrySet()
                .stream()
                .map(entry -> AvailableReservationTimeResponse.of(entry.getKey(), entry.getValue()))
                .toList();
    }
}
