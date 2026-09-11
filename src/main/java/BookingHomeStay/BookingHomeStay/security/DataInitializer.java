package BookingHomeStay.BookingHomeStay.security;

import BookingHomeStay.BookingHomeStay.entity.*;
import BookingHomeStay.BookingHomeStay.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final HostRepository hostRepository;
    private final HomestayRepository homestayRepository;
    private final AmenityRepository amenityRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        initRolesAndAdmin();
        Host defaultHost = initDefaultHost();
        Map<String, Amenity> amenities = initAmenities();
        initHomestaysAndRooms(defaultHost, amenities);
    }

    private void initRolesAndAdmin() {
        for (String roleName : List.of("ROLE_CUSTOMER", "ROLE_HOST", "ROLE_ADMIN", "ROLE_COLLABORATOR")) {
            roleRepository.findByName(roleName)
                    .orElseGet(() -> {
                        Role r = new Role();
                        r.setName(roleName);
                        return roleRepository.save(r);
                    });
        }

        if (!userRepository.existsByEmail("admin@bookinghomestay.local")) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);

            User admin = User.builder()
                    .fullName("System Admin")
                    .email("admin@bookinghomestay.local")
                    .password(passwordEncoder.encode("Admin@123"))
                    .roles(roles)
                    .build();
            userRepository.save(admin);
            log.info(">>> Đã khởi tạo Admin mặc định: admin@bookinghomestay.local / Admin@123");
        }
    }

    private Host initDefaultHost() {
        String hostEmail = "host@bookinghomestay.local";
        User hostUser = userRepository.findByEmail(hostEmail).orElseGet(() -> {
            Role hostRole = roleRepository.findByName("ROLE_HOST").orElseThrow();
            Role customerRole = roleRepository.findByName("ROLE_CUSTOMER").orElseThrow();
            Set<Role> roles = new HashSet<>(List.of(hostRole, customerRole));

            User user = User.builder()
                    .fullName("Đối Tác Chủ Nhà Mẫu")
                    .email(hostEmail)
                    .phone("0987654321")
                    .password(passwordEncoder.encode("Host@123"))
                    .roles(roles)
                    .build();
            return userRepository.save(user);
        });

        return hostRepository.findByUserId(hostUser.getId()).orElseGet(() -> {
            Host host = Host.builder()
                    .user(hostUser)
                    .businessName("Hệ Thống Nghỉ Dưỡng Homestay Việt Nam")
                    .status(HostStatus.APPROVED)
                    .build();
            return hostRepository.save(host);
        });
    }

    private Map<String, Amenity> initAmenities() {
        Map<String, String> amenityList = Map.of(
                "Wifi tốc độ cao", "fa-solid fa-wifi",
                "Điều hòa nhiệt độ", "fa-solid fa-snowflake",
                "Bếp nấu ăn đầy đủ", "fa-solid fa-kitchen-set",
                "Bể bơi ngoài trời", "fa-solid fa-person-swimming",
                "Chỗ đỗ xe ô tô", "fa-solid fa-square-parking",
                "Bếp nướng BBQ sân vườn", "fa-solid fa-fire-burner",
                "Máy sấy tóc & bàn là", "fa-solid fa-shirt",
                "Máy giặt sấy", "fa-solid fa-soap"
        );

        Map<String, Amenity> resultMap = new HashMap<>();
        for (Map.Entry<String, String> entry : amenityList.entrySet()) {
            Amenity amenity = amenityRepository.findByName(entry.getKey()).orElseGet(() -> {
                Amenity a = Amenity.builder()
                        .name(entry.getKey())
                        .icon(entry.getValue())
                        .build();
                return amenityRepository.save(a);
            });
            resultMap.put(entry.getKey(), amenity);
        }
        return resultMap;
    }

    private void initHomestaysAndRooms(Host host, Map<String, Amenity> amenities) {
        // 1. Đà Lạt: Homestay Đà Lạt Mộng Mơ
        createHomestayIfNotExist(
                host,
                "homestay-da-lat-mong-mo",
                "Homestay Đà Lạt Mộng Mơ",
                "Tọa lạc giữa sườn đồi thông xanh ngát của thành phố ngàn hoa, Homestay Đà Lạt Mộng Mơ mang đến không gian nghỉ dưỡng tĩnh lặng, ban công ngắm trọn hoàng hôn và thung lũng sương mây bồng bềnh mỗi sớm mai.",
                "Đà Lạt",
                "Phường 10",
                "Đường Khởi Nghĩa Bắc Sơn, Phường 10, TP. Đà Lạt, Lâm Đồng",
                11.9360, 108.4550,
                List.of(
                        "/images/gallery1-1.jpg",
                        "/images/gallery2-2.jpg",
                        "/images/gallery3-3.jpg",
                        "https://images.unsplash.com/photo-1582719508461-905c673771fd?w=800"
                ),
                Set.of(amenities.get("Wifi tốc độ cao"), amenities.get("Điều hòa nhiệt độ"), amenities.get("Bếp nấu ăn đầy đủ"), amenities.get("Bếp nướng BBQ sân vườn")),
                List.of(
                        new RoomData("Phòng Deluxe Ban Công View Rừng Thông", new BigDecimal("450000"), 2, "Phòng đôi 28m2 ngắm trọn thung lũng thông, giường King êm ái, đầy đủ vệ sinh khép kín."),
                        new RoomData("Phòng Suite Áp Mái Săn Mây", new BigDecimal("750000"), 2, "Thiết kế áp mái ấm cúng phong cách Bắc Âu, bồn tắm gỗ ngắm hoàng hôn, trà cà phê miễn phí."),
                        new RoomData("Phòng Gia Đình Sunset Panorama", new BigDecimal("1200000"), 4, "Phòng gia đình 2 giường đôi King size rộng rãi, ban công lớn ngắm toàn cảnh thung lũng.")
                )
        );

        // 2. Đà Lạt: Villa Rừng Thông Khởi Sắc
        createHomestayIfNotExist(
                host,
                "villa-rung-thong-khoi-sac",
                "Villa Rừng Thông Khởi Sắc",
                "Khu biệt thự nghỉ dưỡng nguyên căn phong cách vintage gần Hồ Tuyền Lâm. Sân vườn rộng lớn ngập tràn hoa tươi và khu vực tiệc nướng BBQ ngoài trời ấm áp.",
                "Đà Lạt",
                "Phường 3",
                "Khu du lịch Hồ Tuyền Lâm, Phường 3, TP. Đà Lạt, Lâm Đồng",
                11.8988, 108.4320,
                List.of(
                        "/images/gallery4-4.jpg",
                        "/images/gallery5-5.jpg",
                        "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800"
                ),
                Set.of(amenities.get("Wifi tốc độ cao"), amenities.get("Chỗ đỗ xe ô tô"), amenities.get("Bếp nướng BBQ sân vườn"), amenities.get("Máy giặt sấy")),
                List.of(
                        new RoomData("Phòng Superior Double Hướng Vườn", new BigDecimal("550000"), 2, "Cửa sổ kính lớn nhìn ra vườn hoa cẩm tú cầu, nội thất gỗ tự nhiên mộc mạc."),
                        new RoomData("Phòng VIP Ban Công Hướng Hồ", new BigDecimal("950000"), 2, "View trực diện hồ nước và đồi thông, không gian cực kỳ lãng mạn cho cặp đôi."),
                        new RoomData("Nguyên Căn Villa Nghỉ Dưỡng 3 Phòng Ngủ", new BigDecimal("2800000"), 8, "Bao trọn căn biệt thự gồm phòng khách, bếp đầy đủ dụng cụ và 3 phòng ngủ tiện nghi.")
                )
        );

        // 3. Sa Pa: Sapa Mây Village & Homestay
        createHomestayIfNotExist(
                host,
                "sapa-may-village-homestay",
                "Sapa Mây Village & Homestay",
                "Nằm lưng chừng bản Tả Van với tầm nhìn bao trọn thung lũng Mường Hoa và ruộng bậc thang tầng tầng lớp lớp. Nơi lý tưởng để săn biển mây bồng bềnh và hòa mình vào thiên nhiên Tây Bắc.",
                "Sa Pa",
                "Tả Van",
                "Bản Tả Van Dáy, Xã Tả Van, Thị xã Sa Pa, Lào Cai",
                22.3039, 103.8732,
                List.of(
                        "/images/gallery6-6.jpg",
                        "/images/gallery7-7.jpg",
                        "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800"
                ),
                Set.of(amenities.get("Wifi tốc độ cao"), amenities.get("Điều hòa nhiệt độ"), amenities.get("Bếp nướng BBQ sân vườn")),
                List.of(
                        new RoomData("Bungalow Gỗ Pơ-Mu View Ruộng Bậc Thang", new BigDecimal("550000"), 2, "Bungalow gỗ ấm áp view trực diện thung lũng Mường Hoa, trà thảo mộc miễn phí."),
                        new RoomData("Phòng Đôi Săn Mây Đỉnh Núi", new BigDecimal("850000"), 2, "Tầng cao nhất với ban công kính 360 độ ngắm trọn mây trời và đỉnh Fansipan."),
                        new RoomData("Phòng Tập Thể Bạn Bè Dorm 4 Giường", new BigDecimal("900000"), 4, "Thiết kế giường tầng tiện lợi, tủ khóa riêng biệt, phù hợp nhóm bạn trẻ khám phá.")
                )
        );

        // 4. Hà Nội: Old Quarter Heritage Homestay
        createHomestayIfNotExist(
                host,
                "old-quarter-heritage-homestay",
                "Old Quarter Heritage Homestay",
                "Căn homestay mang đậm nét hoài cổ của 36 phố phường Hà Nội nhưng đầy đủ tiện nghi hiện đại. Chỉ 3 phút đi bộ là chạm tới Hồ Gươm, chợ đêm và các quán ăn đường phố trứ danh.",
                "Hà Nội",
                "Hoàn Kiếm",
                "18 Phố Hàng Gai, Quận Hoàn Kiếm, TP. Hà Nội",
                21.0333, 105.8500,
                List.of(
                        "/images/gallery8-8.jpg",
                        "/images/gallery9-9.jpg",
                        "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800"
                ),
                Set.of(amenities.get("Wifi tốc độ cao"), amenities.get("Điều hòa nhiệt độ"), amenities.get("Máy sấy tóc & bàn là")),
                List.of(
                        new RoomData("Phòng Cozy Studio Ban Công Phố Cổ", new BigDecimal("480000"), 2, "Căn studio xinh xắn ban công nhìn xuống góc phố cổ rêu phong yên bình."),
                        new RoomData("Phòng Suite Gia Đình Heritage", new BigDecimal("950000"), 4, "2 giường đôi lớn, không gian bếp nhỏ và bàn trà tiếp khách đậm chất Tràng An.")
                )
        );

        // 5. Hà Nội: Ba Vì Green Retreat Villa
        createHomestayIfNotExist(
                host,
                "ba-vi-green-retreat-villa",
                "Ba Vì Green Retreat Villa",
                "Khu biệt thự nghỉ dưỡng ngoại ô Ba Vì nép mình dưới chân núi Ba Vì. Khuôn viên rộng 3.000m2 có hồ bơi nước khoáng trong vắt, bãi cỏ cắm trại và khu nướng BBQ lý tưởng.",
                "Hà Nội",
                "Ba Vì",
                "Thôn Yên Bài, Xã Vân Hòa, Huyện Ba Vì, TP. Hà Nội",
                21.0543, 105.3722,
                List.of(
                        "/images/gallery1-1.jpg",
                        "/images/gallery3-3.jpg",
                        "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800"
                ),
                Set.of(amenities.get("Wifi tốc độ cao"), amenities.get("Bể bơi ngoài trời"), amenities.get("Chỗ đỗ xe ô tô"), amenities.get("Bếp nướng BBQ sân vườn")),
                List.of(
                        new RoomData("Phòng Superior Vườn Xanh Sinh Thái", new BigDecimal("650000"), 2, "Cửa kính lớn bao quanh nhìn ra đồi cỏ xanh ngút ngàn, không khí mát lành."),
                        new RoomData("Nguyên Căn Villa Ba Vì Sân Vườn & Hồ Bơi", new BigDecimal("2500000"), 8, "Biệt thự 4 phòng ngủ sang trọng dành cho đại gia đình hoặc team building cuối tuần.")
                )
        );

        // 6. Đà Nẵng: Mỹ Khê Ocean Breeze Homestay
        createHomestayIfNotExist(
                host,
                "my-khe-ocean-breeze-homestay",
                "Mỹ Khê Ocean Breeze Homestay",
                "Cách bãi biển Mỹ Khê chỉ 150m đi bộ. Thiết kế trẻ trung, hiện đại theo phong cách biển nhiệt đới, xung quanh bạt ngàn nhà hàng hải sản tươi ngon và quán cà phê đẹp.",
                "Đà Nẵng",
                "Sơn Trà",
                "Đường Võ Văn Kiệt, Phường Phước Mỹ, Quận Sơn Trà, TP. Đà Nẵng",
                16.0610, 108.2435,
                List.of(
                        "/images/gallery2-2.jpg",
                        "/images/gallery5-5.jpg",
                        "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800"
                ),
                Set.of(amenities.get("Wifi tốc độ cao"), amenities.get("Điều hòa nhiệt độ"), amenities.get("Máy giặt sấy"), amenities.get("Chỗ đỗ xe ô tô")),
                List.of(
                        new RoomData("Phòng Deluxe Hướng Biển Ban Công", new BigDecimal("420000"), 2, "Ban công đón gió biển mát rượi, ngắm bình minh trên biển Mỹ Khê tuyệt đẹp."),
                        new RoomData("Căn Hộ Studio Bếp Riêng View Cầu Rồng", new BigDecimal("680000"), 3, "Có khu vực bếp riêng nấu ăn hải sản, view ngắm pháo hoa và Cầu Rồng."),
                        new RoomData("Phòng Gia Đình Ocean Penthouse", new BigDecimal("1350000"), 5, "Căn penthouse tầng thượng rộng rãi với 2 phòng ngủ và ban công ngắm toàn cảnh vịnh.")
                )
        );

        // 7. Vũng Tàu: Vũng Tàu Sunset Sea Homestay
        createHomestayIfNotExist(
                host,
                "vung-tau-sunset-sea-homestay",
                "Vũng Tàu Sunset Sea Homestay",
                "Tọa lạc ngay mặt tiền Bãi Sau Vũng Tàu, bước qua đường là tới bãi cát vàng. Căn homestay trang bị đầy đủ bếp nướng BBQ, dụng cụ nấu ăn hải sản và ban công ngắm biển.",
                "Vũng Tàu",
                "Phường 2",
                "128 Đường Thùy Vân, Phường 2, TP. Vũng Tàu, Bà Rịa - Vũng Tàu",
                10.3392, 107.0843,
                List.of(
                        "/images/gallery4-4.jpg",
                        "/images/gallery6-6.jpg",
                        "https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=800"
                ),
                Set.of(amenities.get("Wifi tốc độ cao"), amenities.get("Điều hòa nhiệt độ"), amenities.get("Bếp nấu ăn đầy đủ"), amenities.get("Chỗ đỗ xe ô tô")),
                List.of(
                        new RoomData("Phòng Double Ban Công Gió Biển Bãi Sau", new BigDecimal("380000"), 2, "Phòng giường đôi ấm áp, tiện nghi tiêu chuẩn khách sạn cao cấp."),
                        new RoomData("Phòng Suite Panorama Biển Bãi Sau", new BigDecimal("720000"), 2, "Góc nhìn biển 180 độ đón ánh hoàng hôn lãng mạn, bồn tắm nằm thư giãn."),
                        new RoomData("Căn Hộ 2 Phòng Ngủ Cho Nhóm Bạn", new BigDecimal("1400000"), 6, "2 phòng ngủ riêng biệt, phòng khách rộng có sofa bed và bếp nấu nướng thoải mái.")
                )
        );

        // 8. Nha Trang: Nha Trang Coastal Haven Homestay
        createHomestayIfNotExist(
                host,
                "nha-trang-coastal-haven",
                "Nha Trang Coastal Haven Homestay",
                "Nằm trên cung đường biển Trần Phú sầm uất, nội thất phong cách Địa Trung Hải tinh tế, ban công ngắm trọn vịnh biển Nha Trang trong xanh.",
                "Nha Trang",
                "Lộc Thọ",
                "72 Đường Trần Phú, Phường Lộc Thọ, TP. Nha Trang, Khánh Hòa",
                12.2388, 109.1967,
                List.of(
                        "/images/gallery7-7.jpg",
                        "/images/gallery8-8.jpg",
                        "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800"
                ),
                Set.of(amenities.get("Wifi tốc độ cao"), amenities.get("Điều hòa nhiệt độ"), amenities.get("Bể bơi ngoài trời")),
                List.of(
                        new RoomData("Phòng Deluxe Queen Hướng Biển", new BigDecimal("400000"), 2, "Cửa kính lớn hướng biển đón gió lành, trang thiết bị vệ sinh hiện đại."),
                        new RoomData("Studio Ban Công Hoàng Hôn Vịnh Nha Trang", new BigDecimal("650000"), 3, "Không gian mở khoáng đạt, bàn làm việc và ban công view vịnh biển lộng gió.")
                )
        );

        // 9. Phú Quốc: Phú Quốc Tropical Beach Homestay
        createHomestayIfNotExist(
                host,
                "phu-quoc-tropical-beach-homestay",
                "Phú Quốc Tropical Beach Homestay",
                "Nép mình dưới rặng dừa xanh ngát tại đảo ngọc Phú Quốc, các căn bungalow nhiệt đới mái lá mộc mạc bên bờ cát trắng đón hoàng hôn rực rỡ nhất Việt Nam.",
                "Phú Quốc",
                "Dương Đông",
                "Đường Trần Hưng Đạo, Phường Dương Đông, TP. Phú Quốc, Kiên Giang",
                10.2249, 103.9572,
                List.of(
                        "/images/gallery1-1.jpg",
                        "/images/gallery5-5.jpg",
                        "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=800"
                ),
                Set.of(amenities.get("Wifi tốc độ cao"), amenities.get("Điều hòa nhiệt độ"), amenities.get("Bể bơi ngoài trời"), amenities.get("Bếp nướng BBQ sân vườn")),
                List.of(
                        new RoomData("Bungalow Vườn Dừa Nhiệt Đới", new BigDecimal("600000"), 2, "Bungalow mộc mạc ẩn mình giữa khu vườn nhiệt đới xanh mát, cực kỳ yên tĩnh."),
                        new RoomData("Bungalow Bãi Biển Hướng Hoàng Hôn", new BigDecimal("1100000"), 2, "Chỉ cách mép sóng 20m, ban công gỗ ngắm trọn vẹn hoàng hôn đảo ngọc."),
                        new RoomData("Family Villa 2 Phòng Ngủ Hồ Bơi Riêng", new BigDecimal("2200000"), 6, "Biệt thự hồ bơi riêng tư với 2 phòng ngủ khép kín, khu BBQ bãi biển.")
                )
        );

        // 10. Ninh Bình: Tràng An Mountain Side Homestay
        createHomestayIfNotExist(
                host,
                "ninh-binh-trang-an-mountain-side",
                "Tràng An Mountain Side Homestay",
                "Nằm nép mình bên chân núi đá vôi hùng vĩ của quần thể danh thắng Tràng An, homestay sở hữu hồ sen tuyệt đẹp, không gian thanh bình và dịch vụ chèo thuyền kayak khám phá non nước Ninh Bình.",
                "Ninh Bình",
                "Hoa Lư",
                "Thôn Khê Hạ, Xã Ninh Xuân, Huyện Hoa Lư, Ninh Bình",
                20.2520, 105.9180,
                List.of(
                        "/images/gallery1-1.jpg",
                        "/images/gallery2-2.jpg",
                        "https://images.unsplash.com/photo-1528127269322-539801943592?w=800"
                ),
                safeAmenities(amenities.get("Wifi tốc độ cao"), amenities.get("Điều hòa nhiệt độ"), amenities.get("Bếp nướng BBQ sân vườn"), amenities.get("Chỗ đỗ xe ô tô")),
                List.of(
                        new RoomData("Bungalow Hướng Núi Non Nước", new BigDecimal("550000"), 2, "Bungalow mái ngói hướng thẳng vách núi đá vôi hùng vĩ, ban công thưởng trà buổi sớm."),
                        new RoomData("Phòng Gia Đình View Hồ Sen Tràng An", new BigDecimal("950000"), 4, "Phòng rộng 45m2 view trọn đầm sen ngát hương, đầy đủ tiện nghi cho gia đình."),
                        new RoomData("Phòng Đôi Deluxe Ban Công Đá Vôi", new BigDecimal("680000"), 2, "Thiết kế mộc mạc tinh tế, cửa kính chạm trần đón ánh sáng thiên nhiên.")
                )
        );

        // 11. Quảng Ninh: Hạ Long Bay View Oasis
        createHomestayIfNotExist(
                host,
                "ha-long-bay-view-oasis",
                "Hạ Long Bay View Oasis",
                "Căn hộ nghỉ dưỡng cao cấp với tầm nhìn trực diện vịnh di sản Hạ Long kỳ vĩ. Chỉ vài bước chân tới bãi biển Bãi Cháy, phố cổ Hạ Long và khu vui chơi Sun World náo nhiệt.",
                "Quảng Ninh",
                "Hạ Long",
                "Đường Hoàng Quốc Việt, Phường Bãi Cháy, TP. Hạ Long, Quảng Ninh",
                20.9520, 107.0370,
                List.of(
                        "/images/gallery3-3.jpg",
                        "/images/gallery4-4.jpg",
                        "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?w=800"
                ),
                safeAmenities(amenities.get("Wifi tốc độ cao"), amenities.get("Bể bơi ngoài trời"), amenities.get("Điều hòa nhiệt độ"), amenities.get("Máy giặt sấy")),
                List.of(
                        new RoomData("Căn Hộ Panorama Ngắm Vịnh Kỳ Quan", new BigDecimal("1100000"), 2, "Tầng cao view trọn vẹn vịnh Hạ Long và vòng quay Mặt Trời, nội thất sang trọng."),
                        new RoomData("Phòng Gia Đình 2 Phòng Ngủ Hướng Biển", new BigDecimal("1850000"), 5, "Căn hộ 2 phòng ngủ 2 WC riêng biệt, phòng khách ban công biển cực thoáng đãng."),
                        new RoomData("Phòng Studio Ấm Cúng Gần Bãi Cháy", new BigDecimal("600000"), 2, "Tiện nghi hiện đại, vị trí đắc địa ngay trung tâm Bãi Cháy ẩm thực sầm uất.")
                )
        );

        // 12. Thành phố Hồ Chí Minh: Sài Gòn Retro House & Loft
        createHomestayIfNotExist(
                host,
                "sai-gon-retro-house-loft",
                "Sài Gòn Retro House & Loft",
                "Mang đậm phong cách kiến trúc Indochine giao thoa hoài cổ của Sài Gòn thập niên 90, tọa lạc ngay trung tâm Quận 1, thuận tiện dạo bộ tới Nhà hát Lớn, chợ Bến Thành và phố đi bộ Nguyễn Huệ.",
                "Thành phố Hồ Chí Minh",
                "Quận 1",
                "158 Pasteur, Phường Bến Nghé, Quận 1, Thành phố Hồ Chí Minh",
                10.7780, 106.6980,
                List.of(
                        "/images/gallery2-2.jpg",
                        "/images/gallery5-5.jpg",
                        "https://images.unsplash.com/photo-1583417319070-4a69db38a482?w=800"
                ),
                safeAmenities(amenities.get("Wifi tốc độ cao"), amenities.get("Điều hòa nhiệt độ"), amenities.get("Bếp nấu ăn đầy đủ"), amenities.get("Máy giặt sấy")),
                List.of(
                        new RoomData("Studio Vintage Ban Công Phố Đi Bộ", new BigDecimal("690000"), 2, "Nội thất gỗ xưa ấm cúng, ban công ngắm nhịp sống Sài Gòn rực rỡ ánh đèn đêm."),
                        new RoomData("Căn Hộ Loft Gác Xép Bohemian", new BigDecimal("880000"), 3, "Thiết kế gác lửng độc đáo tràn ngập ánh sáng, máy pha cà phê và gian bếp nhỏ tiện ích."),
                        new RoomData("Master Suite Sài Gòn Xưa", new BigDecimal("1250000"), 4, "Phòng rộng rãi phong cách quý phái, bồn tắm chân rồng cổ điển thư giãn.")
                )
        );

        // 13. Quảng Nam: Hội An Riverside Bamboo Home
        createHomestayIfNotExist(
                host,
                "hoi-an-riverside-bamboo-home",
                "Hội An Riverside Bamboo Home",
                "Nằm giữa rừng dừa Bảy Mẫu thanh bình bên bờ sông Thu Bồn thơ mộng, không gian mộc mạc với tre trúc, lồng đèn Hội An lung linh và các hoạt động chèo thuyền thúng trải nghiệm bản địa.",
                "Quảng Nam",
                "Hội An",
                "Cẩm Thanh, TP. Hội An, Quảng Nam",
                15.8790, 108.3580,
                List.of(
                        "/images/gallery1-1.jpg",
                        "/images/gallery3-3.jpg",
                        "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=800"
                ),
                safeAmenities(amenities.get("Wifi tốc độ cao"), amenities.get("Bể bơi ngoài trời"), amenities.get("Điều hòa nhiệt độ"), amenities.get("Bếp nướng BBQ sân vườn")),
                List.of(
                        new RoomData("Bungalow Tre Hướng Sông Thu Bồn", new BigDecimal("520000"), 2, "Bungalow tre mát mẻ bên mép sông, đón gió mát rượi và ngắm thuyền hoa đăng."),
                        new RoomData("Phòng Deluxe Đèn Lồng Sân Vườn", new BigDecimal("450000"), 2, "Không gian tĩnh lặng ngập tràn sắc hoa và ánh đèn lồng lãng mạn."),
                        new RoomData("Villa Gia Đình Mộc Mạc Ven Sông", new BigDecimal("1300000"), 6, "Khu nhà nguyên căn với sân vườn rộng, bể bơi ngoài trời và tiệc nướng BBQ bên sông.")
                )
        );

        // 14. Thừa Thiên Huế: Huế Cố Đô Garden Homestay
        createHomestayIfNotExist(
                host,
                "hue-co-do-garden-homestay",
                "Huế Cố Đô Garden Homestay",
                "Không gian nhà rường truyền thống đậm chất hoàng cung xứ Huế, ẩn mình trong khu vườn thanh trà xanh mát ngát hương. Chỉ cách Đại Nội và sông Hương 5 phút di chuyển.",
                "Thừa Thiên Huế",
                "Huế",
                "24 Lê Thánh Tôn, Phường Thuận Thành, TP. Huế, Thừa Thiên Huế",
                16.4710, 107.5780,
                List.of(
                        "/images/gallery4-4.jpg",
                        "/images/gallery2-2.jpg",
                        "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=800"
                ),
                safeAmenities(amenities.get("Wifi tốc độ cao"), amenities.get("Điều hòa nhiệt độ"), amenities.get("Bếp nấu ăn đầy đủ"), amenities.get("Chỗ đỗ xe ô tô")),
                List.of(
                        new RoomData("Phòng Nhà Rường Cổ Cố Đô", new BigDecimal("480000"), 2, "Cột gỗ chạm khắc hoa văn tinh xảo, không gian thanh tịnh bình yên đặc trưng xứ Huế."),
                        new RoomData("Phòng Thượng Uyển View Vườn Thanh Trà", new BigDecimal("700000"), 2, "Cửa sổ nhìn ra vườn cây sum suê trĩu quả, phục vụ trà cung đình Huế miễn phí."),
                        new RoomData("Phòng Gia Đình Hoàng Cung Rộng Rãi", new BigDecimal("1150000"), 4, "Phòng rộng cho gia đình, nội thất gỗ ấm cúng đầy đủ tiện nghi sinh hoạt.")
                )
        );

        // 15. Bình Định: Quy Nhơn Sunny Sea Homestay
        createHomestayIfNotExist(
                host,
                "quy-nhon-sunny-sea-homestay",
                "Quy Nhơn Sunny Sea Homestay",
                "Tọa lạc tại làng chài Nhơn Lý bình dị, ngay gần Kỳ Co và Eo Gió hoang sơ. Đón bình minh rực rỡ nhất miền Trung, tận hưởng hải sản tươi sống và biển xanh trong vắt.",
                "Bình Định",
                "Quy Nhơn",
                "Làng Chài Nhơn Lý, TP. Quy Nhơn, Bình Định",
                13.9050, 109.2810,
                List.of(
                        "/images/gallery5-5.jpg",
                        "/images/gallery1-1.jpg",
                        "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800"
                ),
                safeAmenities(amenities.get("Wifi tốc độ cao"), amenities.get("Điều hòa nhiệt độ"), amenities.get("Bếp nấu ăn đầy đủ"), amenities.get("Bếp nướng BBQ sân vườn")),
                List.of(
                        new RoomData("Phòng Đôi Hướng Biển Làng Chài", new BigDecimal("450000"), 2, "Mở cửa là nhìn thấy biển cả bao la cùng những đoàn thuyền thúng đi biển về."),
                        new RoomData("Phòng VIP Ban Công Bình Minh Eo Gió", new BigDecimal("780000"), 2, "Ban công đón ánh bình minh tuyệt mỹ, phòng ốc sáng sủa hiện đại."),
                        new RoomData("Phòng Tập Thể Cho Nhóm Phượt", new BigDecimal("320000"), 4, "Giường tầng tiện nghi, sạch sẽ, giá cả hợp lý cho các bạn trẻ mê khám phá.")
                )
        );

        // 16. Hà Giang: Hà Giang Cao Nguyên Đá Lodge
        createHomestayIfNotExist(
                host,
                "ha-giang-cao-nguyen-da-lodge",
                "Hà Giang Cao Nguyên Đá Lodge",
                "Nằm ngay Làng văn hóa du lịch cộng đồng Pả Vi dưới chân đèo Mã Pí Lèng huyền thoại. Kiến trúc nhà trình tường mái ngói âm dương của đồng bào H'Mông nhìn thẳng ra hẻm vực sông Nho Quế.",
                "Hà Giang",
                "Mèo Vạc",
                "Làng Văn Hóa Pả Vi, Huyện Mèo Vạc, Hà Giang",
                23.1610, 105.4120,
                List.of(
                        "/images/gallery2-2.jpg",
                        "/images/gallery4-4.jpg",
                        "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800"
                ),
                safeAmenities(amenities.get("Wifi tốc độ cao"), amenities.get("Bếp nướng BBQ sân vườn"), amenities.get("Chỗ đỗ xe ô tô"), amenities.get("Máy sấy tóc")),
                List.of(
                        new RoomData("Phòng Nhà Trình Tường Bản Địa", new BigDecimal("380000"), 2, "Tường đất ấm vào mùa đông mát vào mùa hè, nệm êm chăn thổ cẩm bản địa."),
                        new RoomData("Bungalow Gỗ View Đèo Mã Pí Lèng", new BigDecimal("650000"), 2, "Cửa kính lớn ngắm nhìn dãy núi đá tai mèo tráng lệ và mây vờn đỉnh núi."),
                        new RoomData("Phòng Gia Đình Sưởi Ấm Củi Gỗ", new BigDecimal("900000"), 4, "Phòng rộng có lò sưởi củi ấm cúng, trải nghiệm ẩm thực thắng cố và rượu ngô.")
                )
        );

        // 17. Sơn La: Mộc Châu Đồi Chè Trái Tim Homestay
        createHomestayIfNotExist(
                host,
                "moc-chau-doi-che-trai-tim",
                "Mộc Châu Đồi Chè Trái Tim Homestay",
                "Bao quanh bởi bạt ngàn đồi chè xanh mướt của cao nguyên Mộc Châu lộng gió. Không khí trong lành, view thung lũng mận mơ bung nở hoa trắng muốt vào mùa xuân.",
                "Sơn La",
                "Mộc Châu",
                "Tiểu khu Mía Đường, TT. Nông Trường Mộc Châu, Sơn La",
                20.8420, 104.6640,
                List.of(
                        "/images/gallery3-3.jpg",
                        "/images/gallery1-1.jpg",
                        "https://images.unsplash.com/photo-1513836279014-a89f7a76ae86?w=800"
                ),
                safeAmenities(amenities.get("Wifi tốc độ cao"), amenities.get("Bếp nấu ăn đầy đủ"), amenities.get("Bếp nướng BBQ sân vườn"), amenities.get("Chỗ đỗ xe ô tô")),
                List.of(
                        new RoomData("Bungalow Hình Vòm Ngắm Đồi Chè", new BigDecimal("480000"), 2, "Thiết kế vòm độc đáo nằm giữa luống chè Ô Long xanh mướt mắt."),
                        new RoomData("Nhà Tam Giác Gỗ Giữa Vườn Mận", new BigDecimal("550000"), 2, "Nhà gỗ chữ A xinh xắn, hoa mận nở rực rỡ quanh hiên nhà."),
                        new RoomData("Phòng Gia Đình Sum Họp", new BigDecimal("1050000"), 5, "Phòng sinh hoạt chung rộng rãi, bếp nướng BBQ ngoài trời giữa đồi chè mát rượi.")
                )
        );

        // 18. Cần Thơ: Cần Thơ Riverside Mekong Home
        createHomestayIfNotExist(
                host,
                "can-tho-riverside-mekong-home",
                "Cần Thơ Riverside Mekong Home",
                "Trải nghiệm cuộc sống sông nước miền Tây đích thực bên bờ sông Hậu hiền hòa. Miệt vườn rợp bóng cây ăn trái, tiếng đờn ca tài tử và thuyền đón đi chợ nổi Cái Răng sáng sớm.",
                "Cần Thơ",
                "Cái Răng",
                "Cồn Ấu, Phường Hưng Phú, Quận Cái Răng, Cần Thơ",
                10.0210, 105.7950,
                List.of(
                        "/images/gallery5-5.jpg",
                        "/images/gallery2-2.jpg",
                        "https://images.unsplash.com/photo-1544644181-1484b3fdfc62?w=800"
                ),
                safeAmenities(amenities.get("Wifi tốc độ cao"), amenities.get("Điều hòa nhiệt độ"), amenities.get("Bể bơi ngoài trời"), amenities.get("Bếp nấu ăn đầy đủ")),
                List.of(
                        new RoomData("Bungalow Mái Lá Ven Sông Tiền", new BigDecimal("420000"), 2, "Mái lá dừa mát rượi, ban công nhìn ra xuồng ghe tấp nập trên sông."),
                        new RoomData("Phòng Deluxe Vườn Cây Trái Nam Bộ", new BigDecimal("620000"), 2, "Bao quanh bởi vườn xoài cát, chôm chôm và sầu riêng sum suê trĩu cành."),
                        new RoomData("Căn Hộ Miệt Vườn Gia Đình", new BigDecimal("1100000"), 4, "Căn nhà vườn phong cách Nam Bộ ấm áp, đầy đủ gian bếp và bàn ăn ngoài trời.")
                )
        );

        // 19. Bình Thuận: Mũi Né Sandy Beach House
        createHomestayIfNotExist(
                host,
                "mui-ne-sandy-beach-house",
                "Mũi Né Sandy Beach House",
                "Tọa lạc ngay cung đường ven biển Huỳnh Thúc Kháng rực rỡ hoa giấy, gần Đồi Cát Bay và Làng chài Mũi Né. Bãi cát vàng mịn màng cùng làn gió biển trong lành quanh năm.",
                "Bình Thuận",
                "Phan Thiết",
                "Đường Huỳnh Thúc Kháng, Phường Mũi Né, TP. Phan Thiết, Bình Thuận",
                10.9380, 108.2870,
                List.of(
                        "/images/gallery4-4.jpg",
                        "/images/gallery1-1.jpg",
                        "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800"
                ),
                safeAmenities(amenities.get("Wifi tốc độ cao"), amenities.get("Điều hòa nhiệt độ"), amenities.get("Bể bơi ngoài trời"), amenities.get("Bếp nướng BBQ sân vườn")),
                List.of(
                        new RoomData("Phòng Superior Sân Vườn Nhiệt Đới", new BigDecimal("460000"), 2, "Lối đi ngập tràn dừa xiêm và hoa giấy rực rỡ, cách biển chỉ vài chục mét."),
                        new RoomData("Bungalow View Biển Sóng Vỗ", new BigDecimal("850000"), 2, "Nghe tiếng sóng biển rì rào suốt đêm ngày, ngắm hoàng hôn đỏ ối trên biển."),
                        new RoomData("Villa Gia Đình BBQ Bãi Cát", new BigDecimal("1500000"), 6, "Khu nghỉ dưỡng khép kín với sân vườn nướng hải sản tươi sống Mũi Né.")
                )
        );

        // 20. Quảng Bình: Phong Nha Cave Explorer Homestay
        createHomestayIfNotExist(
                host,
                "phong-nha-cave-explorer-homestay",
                "Phong Nha Cave Explorer Homestay",
                "Tọa lạc bên dòng sông Son thơ mộng, là điểm xuất phát lý tưởng để khám phá kỳ quan Động Phong Nha, Thiên Đường và hang Sơn Đoòng hùng vĩ.",
                "Quảng Bình",
                "Bố Trạch",
                "Thôn Sơn Trạch, Huyện Bố Trạch, Quảng Bình",
                17.5870, 106.2840,
                List.of(
                        "/images/gallery1-1.jpg",
                        "/images/gallery5-5.jpg",
                        "https://images.unsplash.com/photo-1518684079-3c830dcef090?w=800"
                ),
                safeAmenities(amenities.get("Wifi tốc độ cao"), amenities.get("Điều hòa nhiệt độ"), amenities.get("Bếp nướng BBQ sân vườn"), amenities.get("Chỗ đỗ xe ô tô")),
                List.of(
                        new RoomData("Phòng Đôi View Sông Son Thơ Mộng", new BigDecimal("390000"), 2, "Sông Son xanh ngắt như tranh vẽ, làn gió mát lành thổi từ rặng núi đá vôi."),
                        new RoomData("Bungalow Núi Đá Kẻ Bàng", new BigDecimal("620000"), 2, "Thiết kế vật liệu tự nhiên đá và gỗ, hài hòa cùng thiên nhiên di sản thế giới."),
                        new RoomData("Phòng Tập Thể Cho Nhóm Thám Hiểm", new BigDecimal("280000"), 4, "Tiện nghi và chi phí hợp lý cho du khách trekking hang động kỳ vĩ.")
                )
        );

        // 21. Hải Phòng: Cát Bà Emerald Island Retreat
        createHomestayIfNotExist(
                host,
                "cat-ba-emerald-island-retreat",
                "Cát Bà Emerald Island Retreat",
                "Tọa lạc trên sườn đồi nhìn thẳng ra vịnh Lan Hạ thơ mộng của quần đảo Cát Bà. Không khí mát mẻ, dịch vụ chèo thuyền kayak và du thuyền ngắm vịnh riêng tư.",
                "Hải Phòng",
                "Cát Hải",
                "Thị trấn Cát Bà, Huyện Cát Hải, Hải Phòng",
                20.7280, 107.0490,
                List.of(
                        "/images/gallery3-3.jpg",
                        "/images/gallery2-2.jpg",
                        "https://images.unsplash.com/photo-1544644181-1484b3fdfc62?w=800"
                ),
                safeAmenities(amenities.get("Wifi tốc độ cao"), amenities.get("Điều hòa nhiệt độ"), amenities.get("Bếp nấu ăn đầy đủ"), amenities.get("Chỗ đỗ xe ô tô")),
                List.of(
                        new RoomData("Phòng Hướng Biển Đảo Lan Hạ", new BigDecimal("580000"), 2, "Tầm nhìn ôm trọn vịnh nước trong xanh như ngọc và hàng trăm đảo đá nhấp nhô."),
                        new RoomData("Phòng Deluxe Ban Công Núi & Biển", new BigDecimal("820000"), 2, "Phòng rộng rãi ban công đón trọn gió vịnh Cát Bà mát rượi quanh năm."),
                        new RoomData("Suite Nghỉ Dưỡng Cát Bà", new BigDecimal("1400000"), 4, "Căn suite cao cấp có bồn tắm ngắm cảnh biển và khu tiếp khách sang trọng.")
                )
        );

        // 22. Đắk Lắk: Ban Mê Coffee Farm Stay
        createHomestayIfNotExist(
                host,
                "ban-me-coffee-farm-stay",
                "Ban Mê Coffee Farm Stay",
                "Nằm giữa trang trại cà phê Robusta bạt ngàn của thủ phủ Tây Nguyên. Cùng thưởng thức cà phê nguyên chất bên bếp lửa bập bùng và ngắm hoàng hôn hồ Ea Kao.",
                "Đắk Lắk",
                "Buôn Ma Thuột",
                "Xã Ea Kao, TP. Buôn Ma Thuột, Đắk Lắk",
                12.6200, 108.0600,
                List.of(
                        "/images/gallery4-4.jpg",
                        "/images/gallery5-5.jpg",
                        "https://images.unsplash.com/photo-1447752875215-b2761acb3c5d?w=800"
                ),
                safeAmenities(amenities.get("Wifi tốc độ cao"), amenities.get("Bếp nướng BBQ sân vườn"), amenities.get("Chỗ đỗ xe ô tô"), amenities.get("Bếp nấu ăn đầy đủ")),
                List.of(
                        new RoomData("Nhà Rông Gỗ Tây Nguyên", new BigDecimal("450000"), 2, "Kiến trúc nhà rông cách điệu, mái cao vút thoáng mát, nệm êm thổ cẩm Ê-đê."),
                        new RoomData("Bungalow Hương Cà Phê Ban Mê", new BigDecimal("600000"), 2, "Mỗi sáng thức giấc ngập tràn mùi hoa cà phê trắng muốt hoặc hương hạt rang xay."),
                        new RoomData("Căn Hộ Gia Đình Sân Lửa Trại", new BigDecimal("1100000"), 5, "Khoảng sân riêng tổ chức tiệc nướng BBQ và đốt lửa trại ấm áp vùng cao nguyên.")
                )
        );

        // 23. Hòa Bình: Mai Châu Thung Lũng Xanh
        createHomestayIfNotExist(
                host,
                "mai-chau-thung-lung-xanh",
                "Mai Châu Thung Lũng Xanh",
                "Trải nghiệm nhà sàn truyền thống của người Thái tại Bản Lác, ngắm cánh đồng lúa xanh ngút ngàn và tham gia các điệu múa xòe, đốt lửa trại đặc sắc.",
                "Hòa Bình",
                "Mai Châu",
                "Bản Lác, Xã Chiềng Châu, Huyện Mai Châu, Hòa Bình",
                20.6620, 105.0850,
                List.of(
                        "/images/gallery1-1.jpg",
                        "/images/gallery2-2.jpg",
                        "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800"
                ),
                safeAmenities(amenities.get("Wifi tốc độ cao"), amenities.get("Bếp nướng BBQ sân vườn"), amenities.get("Chỗ đỗ xe ô tô")),
                List.of(
                        new RoomData("Phòng Nhà Sàn Thái Bản Lác", new BigDecimal("360000"), 2, "Sàn gỗ bóng sạch, nệm gấm truyền thống người Thái, cửa sổ nhìn ra đồng lúa."),
                        new RoomData("Bungalow View Ruộng Lúa Mai Châu", new BigDecimal("580000"), 2, "Bungalow riêng tư giữa lòng thung lũng, mộc mạc và cực kỳ thư thái."),
                        new RoomData("Căn Sàn Gỗ Đoàn Đông Người", new BigDecimal("1200000"), 8, "Không gian nhà sàn lớn rộng rãi cho nhóm bạn phượt hoặc đại gia đình sum vầy.")
                )
        );

        // 24. Phú Yên: Phú Yên Hoa Vàng Trên Cỏ Xanh
        createHomestayIfNotExist(
                host,
                "phu-yen-hoa-vang-co-xanh",
                "Phú Yên Hoa Vàng Trên Cỏ Xanh",
                "Vùng đất hoa vàng cỏ xanh với biển trời trong vắt, gần Bãi Xép và Gành Đá Đĩa kỳ quan thiên nhiên. Không gian thoáng đãng, lộng gió biển miền Trung.",
                "Phú Yên",
                "Tuy Hòa",
                "Đường Độc Lập, Phường 9, TP. Tuy Hòa, Phú Yên",
                13.1020, 109.3100,
                List.of(
                        "/images/gallery3-3.jpg",
                        "/images/gallery4-4.jpg",
                        "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800"
                ),
                safeAmenities(amenities.get("Wifi tốc độ cao"), amenities.get("Điều hòa nhiệt độ"), amenities.get("Bếp nấu ăn đầy đủ"), amenities.get("Chỗ đỗ xe ô tô")),
                List.of(
                        new RoomData("Phòng Đôi View Biển Tuy Hòa", new BigDecimal("420000"), 2, "Phòng rộng mở cửa đón gió biển trong lành, cách bãi cát chỉ 50 mét."),
                        new RoomData("Bungalow Sân Vườn Cỏ Xanh", new BigDecimal("650000"), 2, "Bungalow xinh xắn giữa khu vườn ngập tràn hoa vàng và cây xanh nhiệt đới.")
                )
        );

        // 25. Thanh Hóa: Pù Luông Retreat Mây Ngàn
        createHomestayIfNotExist(
                host,
                "pu-luong-retreat-may-ngan",
                "Pù Luông Retreat Mây Ngàn",
                "Nằm giữa thung lũng Pù Luông nguyên sơ với những thửa ruộng bậc thang trùng điệp. Sở hữu hồ bơi vô cực ngắm mây vờn đỉnh núi và dòng suối mát lành.",
                "Thanh Hóa",
                "Bá Thước",
                "Bản Đôn, Xã Thành Lâm, Huyện Bá Thước, Thanh Hóa",
                20.4560, 105.1840,
                List.of(
                        "/images/gallery5-5.jpg",
                        "/images/gallery1-1.jpg",
                        "https://images.unsplash.com/photo-1528127269322-539801943592?w=800"
                ),
                safeAmenities(amenities.get("Wifi tốc độ cao"), amenities.get("Bể bơi ngoài trời"), amenities.get("Bếp nướng BBQ sân vườn"), amenities.get("Chỗ đỗ xe ô tô")),
                List.of(
                        new RoomData("Bungalow Đôi View Ruộng Bậc Thang", new BigDecimal("750000"), 2, "Ngắm nhìn từng tầng sóng lúa vàng rực rỡ mùa gặt ngay tại giường ngủ."),
                        new RoomData("Phòng Deluxe Hồ Bơi Vô Cực", new BigDecimal("1050000"), 2, "Phòng cao cấp sát hồ bơi vô cực nhìn thẳng ra thung lũng mây Pù Luông."),
                        new RoomData("Phòng Gia Đình Bản Đôn", new BigDecimal("1400000"), 5, "Nhà sàn gỗ tiện nghi hiện đại cho gia đình có trẻ nhỏ cùng nghỉ dưỡng.")
                )
        );

        // 26. An Giang: An Giang Thất Sơn River View
        createHomestayIfNotExist(
                host,
                "an-giang-that-son-river-view",
                "An Giang Thất Sơn River View",
                "Khám phá vùng đất Thất Sơn huyền bí và rừng tràm Trà Sư xanh ngắt bèo tây. Homestay có ban công ngắm dòng kênh Vĩnh Tế và thưởng thức bún cá Châu Đốc nức tiếng.",
                "An Giang",
                "Châu Đốc",
                "Đường Lê Lợi, Phường Châu Phú B, TP. Châu Đốc, An Giang",
                10.7050, 105.1200,
                List.of(
                        "/images/gallery2-2.jpg",
                        "/images/gallery3-3.jpg",
                        "https://images.unsplash.com/photo-1544644181-1484b3fdfc62?w=800"
                ),
                safeAmenities(amenities.get("Wifi tốc độ cao"), amenities.get("Điều hòa nhiệt độ"), amenities.get("Bếp nấu ăn đầy đủ"), amenities.get("Chỗ đỗ xe ô tô")),
                List.of(
                        new RoomData("Phòng Tiêu Chuẩn View Kênh Vĩnh Tế", new BigDecimal("350000"), 2, "Không gian sạch sẽ, gió sông thổi lồng lộng, gần Miếu Bà Chúa Xứ."),
                        new RoomData("Phòng Deluxe Thất Sơn Hướng Núi", new BigDecimal("520000"), 2, "Tầm nhìn hướng về dãy núi Sam linh thiêng và cánh đồng biên giới bao la.")
                )
        );

        // 27. Bến Tre: Bến Tre Dừa Xanh Eco Lodge
        createHomestayIfNotExist(
                host,
                "ben-tre-dua-xanh-eco-lodge",
                "Bến Tre Dừa Xanh Eco Lodge",
                "Về xứ dừa Bến Tre thanh bình, chèo xuồng len lỏi rặng dừa nước, thưởng thức kẹo dừa béo ngậy và món cá lóc nướng trui mộc mạc thơm lừng.",
                "Bến Tre",
                "Châu Thành",
                "Xã Tân Thạch, Huyện Châu Thành, Bến Tre",
                10.3340, 106.3520,
                List.of(
                        "/images/gallery4-4.jpg",
                        "/images/gallery5-5.jpg",
                        "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800"
                ),
                safeAmenities(amenities.get("Wifi tốc độ cao"), amenities.get("Bếp nấu ăn đầy đủ"), amenities.get("Chỗ đỗ xe ô tô"), amenities.get("Bếp nướng BBQ sân vườn")),
                List.of(
                        new RoomData("Phòng Mái Lá Mộc Mạc Ven Sông", new BigDecimal("380000"), 2, "Nội thất từ gỗ dừa tự nhiên, võng đung đưa bên hiên nhà rợp bóng cây."),
                        new RoomData("Bungalow Vườn Dừa Sinh Thái", new BigDecimal("560000"), 2, "Bungalow tiện nghi hiện đại ẩn mình giữa rặng dừa xanh mướt mát lành.")
                )
        );
    }

    private Set<Amenity> safeAmenities(Amenity... ams) {
        Set<Amenity> set = new HashSet<>();
        if (ams != null) {
            for (Amenity a : ams) {
                if (a != null) {
                    set.add(a);
                }
            }
        }
        return set;
    }

    private void createHomestayIfNotExist(
            Host host,
            String slug,
            String name,
            String description,
            String province,
            String district,
            String address,
            Double lat,
            Double lng,
            List<String> imageUrls,
            Set<Amenity> amenitySet,
            List<RoomData> roomsData
    ) {
        if (homestayRepository.existsBySlug(slug)) {
            return;
        }

        Homestay homestay = Homestay.builder()
                .host(host)
                .slug(slug)
                .name(name)
                .description(description)
                .province(province)
                .district(district)
                .address(address)
                .latitude(lat)
                .longitude(lng)
                .status(HomestayStatus.ACTIVE)
                .amenities(new HashSet<>(amenitySet))
                .build();

        for (String url : imageUrls) {
            HomestayImage img = HomestayImage.builder()
                    .homestay(homestay)
                    .imageUrl(url)
                    .build();
            homestay.getImages().add(img);
        }

        for (RoomData rd : roomsData) {
            Room room = Room.builder()
                    .homestay(homestay)
                    .name(rd.name)
                    .pricePerNight(rd.price)
                    .maxGuests(rd.guests)
                    .description(rd.desc)
                    .status(RoomStatus.ACTIVE)
                    .build();
            homestay.getRooms().add(room);
        }

        homestayRepository.save(homestay);
        log.info(">>> Đã khởi tạo Homestay mẫu: {} (Tỉnh/Thành: {}, Số phòng: {})", name, province, roomsData.size());
    }

    private static class RoomData {
        String name;
        BigDecimal price;
        int guests;
        String desc;

        RoomData(String name, BigDecimal price, int guests, String desc) {
            this.name = name;
            this.price = price;
            this.guests = guests;
            this.desc = desc;
        }
    }
}