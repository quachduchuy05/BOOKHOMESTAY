package BookingHomeStay.BookingHomeStay.controller;

import BookingHomeStay.BookingHomeStay.entity.Homestay;
import BookingHomeStay.BookingHomeStay.service.HomestayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HomestayService homestayService;

    // Ho tro ca "/" va "/trang-chu" tro ve cung 1 noi dung (yeu cau task 2:
    // nguoi dung muon thay ro URL tieng Viet dang "localhost:8080/trang-chu").
    @GetMapping({"/", "/trang-chu"})
    public String home(@RequestParam(required = false) String province,
                       @RequestParam(required = false) String district,
                       @RequestParam(required = false) Integer guests,
                       @RequestParam(required = false) BigDecimal minPrice,
                       @RequestParam(required = false) BigDecimal maxPrice,
                       Model model,
                       org.springframework.security.core.Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            for (org.springframework.security.core.GrantedAuthority authority : authentication.getAuthorities()) {
                String role = authority.getAuthority();
                if ("ROLE_ADMIN".equals(role)) return "redirect:/quan-tri/tong-quan";
                if ("ROLE_COLLABORATOR".equals(role)) return "redirect:/cong-tac-vien/tong-quan";
                if ("ROLE_HOST".equals(role)) return "redirect:/chu-nha/tong-quan";
            }
        }
        List<Homestay> featuredHomestays = homestayService.search(province, district, guests, minPrice, maxPrice);
        model.addAttribute("featuredHomestays", featuredHomestays);
        model.addAttribute("activeProvinces", homestayService.getActiveProvinces());
        model.addAttribute("allProvinces", BookingHomeStay.BookingHomeStay.dto.VietnamProvinces.getAll());
        model.addAttribute("featuredProvinces", BookingHomeStay.BookingHomeStay.dto.VietnamProvinces.getFeatured());
        model.addAttribute("province", province);
        model.addAttribute("district", district);
        model.addAttribute("guests", guests);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        if (province != null && !province.isBlank()) {
            model.addAttribute("districtsOfProvince", homestayService.getDistrictsOfProvince(province));
        }
        return "trang-chu";
    }

    @GetMapping("/tim-kiem")
    public String search(@RequestParam(required = false) String province,
                          @RequestParam(required = false) String district,
                          @RequestParam(required = false) Integer guests,
                          @RequestParam(required = false) BigDecimal minPrice,
                          @RequestParam(required = false) BigDecimal maxPrice,
                          org.springframework.security.core.Authentication authentication,
                          Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            for (org.springframework.security.core.GrantedAuthority authority : authentication.getAuthorities()) {
                String role = authority.getAuthority();
                if ("ROLE_ADMIN".equals(role)) return "redirect:/quan-tri/tong-quan";
                if ("ROLE_COLLABORATOR".equals(role)) return "redirect:/cong-tac-vien/tong-quan";
                if ("ROLE_HOST".equals(role)) return "redirect:/chu-nha/tong-quan";
            }
        }
        List<Homestay> results = homestayService.search(province, district, guests, minPrice, maxPrice);
        model.addAttribute("results", results);
        model.addAttribute("activeProvinces", homestayService.getActiveProvinces());
        model.addAttribute("allProvinces", BookingHomeStay.BookingHomeStay.dto.VietnamProvinces.getAll());
        model.addAttribute("featuredProvinces", BookingHomeStay.BookingHomeStay.dto.VietnamProvinces.getFeatured());
        model.addAttribute("province", province);
        model.addAttribute("district", district);
        model.addAttribute("guests", guests);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        // Task 12: neu da co san 1 tinh/thanh (vd bam tu "Diem den pho bien"),
        // nap san danh sach quan/huyen THAT SU co homestay trong tinh do de
        // nguoi dung loc tiep theo quan/huyen thay vi phai chon lai tinh/thanh.
        if (province != null && !province.isBlank()) {
            model.addAttribute("districtsOfProvince", homestayService.getDistrictsOfProvince(province));
        }
        return "tim-kiem";
    }

    // AJAX: tra ve danh sach quan/huyen theo tinh da chon (dung o ca trang chu va trang tim-kiem)
    @GetMapping({"/tim-kiem/api/quan-huyen", "/trang-chu/api/quan-huyen", "/api/quan-huyen"})
    @ResponseBody
    public List<String> quanHuyenTheoTinh(@RequestParam String province) {
        return homestayService.getDistrictsOfProvince(province);
    }
}
