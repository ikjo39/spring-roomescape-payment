package roomescape.reservation.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.dto.ThemeResponse;
import roomescape.reservation.service.ThemeService;

@RestController
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(final ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping("/themes")
    public List<ThemeResponse> getThemes() {
        return themeService.getThemes()
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @GetMapping("/popular-themes")
    public List<ThemeResponse> getPopularThemes() {
        return themeService.getPopularThemes()
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
