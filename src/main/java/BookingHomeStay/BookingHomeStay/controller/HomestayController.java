package BookingHomeStay.BookingHomeStay.controller;

import BookingHomeStay.BookingHomeStay.entity.Homestay;
import BookingHomeStay.BookingHomeStay.entity.Review;
import BookingHomeStay.BookingHomeStay.security.CustomUserDetails;
import BookingHomeStay.BookingHomeStay.service.CollaboratorService;
import BookingHomeStay.BookingHomeStay.service.FavoriteService;
import BookingHomeStay.BookingHomeStay.service.HomestayService;
import BookingHomeStay.BookingHomeStay.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomestayController {

    private final HomestayService homestayService;
    private final ReviewService reviewService;
    private final FavoriteService favoriteService;
    private final CollaboratorService collaboratorService;

    @GetMapping("/homestay/{slug}")
    public String detail(@PathVariable String slug,
                         @RequestParam(required = false) String ref,
                         @AuthenticationPrincipal CustomUserDetails currentUser,
                         Model model) {
        if (ref != null && !ref.isBlank()) {
            collaboratorService.recordClick(ref, slug);
            model.addAttribute("ref", ref);
        }

        Homestay homestay = homestayService.getActiveBySlug(slug);
        model.addAttribute("homestay", homestay);

        List<Review> reviews = reviewService.getReviewsOfHomestay(homestay.getId());
        Double avgRating = reviewService.getAverageRating(homestay.getId());
        long reviewCount = reviewService.getReviewCount(homestay.getId());

        boolean isFavorite = currentUser != null && favoriteService.isFavorite(currentUser.getId(), homestay.getId());

        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", avgRating);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("isFavorite", isFavorite);

        return "chi-tiet-homestay";
    }

    @GetMapping("/homestay/gan-day")
    public String nearbyPage() { return "homestay-gan-day"; }

    @GetMapping("/homestay/api/gan-day")
    @ResponseBody
    public List<Map<String, Object>> nearbyApi(@RequestParam double lat, @RequestParam double lng) {
        return homestayService.findNearby(lat, lng, 15).stream()
                .map(h -> Map.<String, Object>of(
                        "slug", h.getSlug(), "name", h.getName(), "province", h.getProvince()))
                .collect(Collectors.toList());
    }
}