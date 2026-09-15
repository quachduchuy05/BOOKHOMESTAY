package BookingHomeStay.BookingHomeStay.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AiBookingViewController {

    @GetMapping("/ai-booking")
    public String showAiBookingPage() {
        return "khach-hang/ai-booking";
    }
}
