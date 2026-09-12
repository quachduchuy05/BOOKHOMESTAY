package BookingHomeStay.BookingHomeStay.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VietnamProvinces {

    private static final List<ProvinceDto> ALL_PROVINCES = new ArrayList<>();

    static {
        // ==========================================
        // 1. MIỀN BẮC (25 TỈNH/THÀNH)
        // ==========================================
        add("Hà Nội", "bac", "Miền Bắc", "Thủ đô ngàn năm văn hiến, 36 phố phường & ẩm thực tinh hoa", "/images/gallery2.png", true);
        add("Hải Phòng", "bac", "Miền Bắc", "Thành phố hoa phượng đỏ & quần đảo Cát Bà thơ mộng", "/images/gallery6.png", true);
        add("Quảng Ninh", "bac", "Miền Bắc", "Kỳ quan thiên nhiên thế giới Vịnh Hạ Long & phố cổ Bãi Cháy", "/images/gallery7.png", true);
        add("Ninh Bình", "bac", "Miền Bắc", "Quần thể danh thắng Tràng An non nước hữu tình & Tam Cốc", "/images/gallery8.png", true);
        add("Lào Cai", "bac", "Miền Bắc", "Sa Pa bồng bềnh mây ngàn, đỉnh Fansipan & ruộng bậc thang", "/images/gallery4.png", true);
        add("Hà Giang", "bac", "Miền Bắc", "Cao nguyên đá Đồng Văn hùng vĩ, hẻm vực Tu Sản & hoa tam giác mạch", "/images/gallery1.png", true);
        add("Sơn La", "bac", "Miền Bắc", "Cao nguyên Mộc Châu đồi chè xanh ngát & mùa mận mơ bung nở", "/images/gallery5.png", true);
        add("Hòa Bình", "bac", "Miền Bắc", "Thung lũng Mai Châu nhà sàn bình yên & hồ Hòa Bình xanh biếc", "/images/gallery3.png", true);
        add("Bắc Giang", "bac", "Miền Bắc", "Vùng đất vải thiều Lục Ngạn ngọt ngào & rừng nguyên sinh Khe Rỗ", null, false);
        add("Bắc Kạn", "bac", "Miền Bắc", "Hồ Ba Bể - Viên ngọc xanh giữa đại ngàn Đông Bắc hoang sơ", null, false);
        add("Bắc Ninh", "bac", "Miền Bắc", "Cái nôi văn hóa Kinh Bắc & những làn điệu Dân ca Quan họ ngọt ngào", null, false);
        add("Cao Bằng", "bac", "Miền Bắc", "Thác Bản Giốc kỳ vĩ bọt nước trắng xóa & suối Lê-nin trong vắt", null, false);
        add("Điện Biên", "bac", "Miền Bắc", "Mảnh đất lịch sử oai hùng & thung lũng Mường Thanh trù phú", null, false);
        add("Hà Nam", "bac", "Miền Bắc", "Chùa Tam Chúc ngôi chùa lớn nhất thế giới & cảnh quan sơn thủy", null, false);
        add("Hải Dương", "bac", "Miền Bắc", "Bánh đậu xanh truyền thống & đảo Cò Chi Lăng Nam thanh bình", null, false);
        add("Hưng Yên", "bac", "Miền Bắc", "Phố Hiến vang bóng một thời & vùng đất nhãn lồng thơm ngon", null, false);
        add("Lai Châu", "bac", "Miền Bắc", "Đèo Ô Quy Hồ hùng vĩ bậc nhất & bản Sì Thâu Chải mộc mạc", null, false);
        add("Lạng Sơn", "bac", "Miền Bắc", "Ải Chi Lăng lịch sử, động Tam Thanh & chợ biên giới sầm uất", null, false);
        add("Nam Định", "bac", "Miền Bắc", "Đền Trần linh thiêng, nhà thờ đổ Hải Lý & biển Thịnh Long", null, false);
        add("Phú Thọ", "bac", "Miền Bắc", "Đền Hùng cội nguồn non sông dân tộc Việt Nam ngàn đời", null, false);
        add("Thái Bình", "bac", "Miền Bắc", "Miền quê lúa yên bình, bãi biển Cồn Đen & chùa Keo cổ kính", null, false);
        add("Thái Nguyên", "bac", "Miền Bắc", "Thủ phủ chè xanh Tân Cương thơm ngát & hồ Núi Cốc thơ mộng", null, false);
        add("Tuyên Quang", "bac", "Miền Bắc", "Lễ hội Thành Tuyên rực rỡ sắc màu & hồ thủy điện Na Hang", null, false);
        add("Vĩnh Phúc", "bac", "Miền Bắc", "Thị trấn trong mây Tam Đảo mát lạnh & thiền viện Trúc Lâm Tây Thiên", null, false);
        add("Yên Bái", "bac", "Miền Bắc", "Kỳ quan ruộng bậc thang Mù Cang Chải & hồ Thác Bà mênh mông", null, false);

        // ==========================================
        // 2. MIỀN TRUNG & TÂY NGUYÊN (19 TỈNH/THÀNH)
        // ==========================================
        add("Đà Nẵng", "trung", "Miền Trung & Tây Nguyên", "Thành phố biển đáng sống nhất Việt Nam, Cầu Vàng & biển Mỹ Khê", "/images/gallery3.png", true);
        add("Thừa Thiên Huế", "trung", "Miền Trung & Tây Nguyên", "Cố đô thơ mộng, lăng tẩm hoàng gia, chùa Thiên Mụ & sông Hương", "/images/gallery2.png", true);
        add("Quảng Nam", "trung", "Miền Trung & Tây Nguyên", "Phố cổ Hội An lung linh lồng đèn, rừng dừa Bảy Mẫu & Cù Lao Chàm", "/images/gallery5.png", true);
        add("Khánh Hòa", "trung", "Miền Trung & Tây Nguyên", "Vịnh biển Nha Trang ngọc ngà, biển Bãi Dài & lặn ngắm san hô", "/images/gallery7.png", true);
        add("Lâm Đồng", "trung", "Miền Trung & Tây Nguyên", "Đà Lạt xứ sở ngàn hoa, thung lũng thông reo & khí hậu mát lành", "/images/gallery1.png", true);
        add("Bình Định", "trung", "Miền Trung & Tây Nguyên", "Quy Nhơn biển xanh ngắt, Eo Gió lộng gió & đảo Kỳ Co hoang sơ", "/images/gallery6.png", true);
        add("Bình Thuận", "trung", "Miền Trung & Tây Nguyên", "Mũi Né - Phan Thiết đồi cát bay rực rỡ, bãi biển lướt ván diều", "/images/gallery8.png", true);
        add("Quảng Bình", "trung", "Miền Trung & Tây Nguyên", "Vương quốc hang động thế giới Phong Nha - Kẻ Bàng kỳ ảo", "/images/gallery4.png", true);
        add("Phú Yên", "trung", "Miền Trung & Tây Nguyên", "Xứ sở hoa vàng trên cỏ xanh, Gành Đá Đĩa kỳ quan thiên nhiên", null, false);
        add("Đắk Lắk", "trung", "Miền Trung & Tây Nguyên", "Thủ phủ cà phê Buôn Ma Thuột, thác Dray Nur & buôn Đôn hoang sơ", null, false);
        add("Gia Lai", "trung", "Miền Trung & Tây Nguyên", "Biển Hồ T'Nưng 'đôi mắt Pleiku' trong biếc & núi lửa Chư Đăng Ya", null, false);
        add("Kon Tum", "trung", "Miền Trung & Tây Nguyên", "Nhà thờ gỗ Kon Tum trăm năm tuổi, ngã ba Đông Dương & Măng Đen", null, false);
        add("Đắk Nông", "trung", "Miền Trung & Tây Nguyên", "Hồ Tà Đùng - 'Vịnh Hạ Long trên cạn' của đại ngàn Tây Nguyên", null, false);
        add("Hà Tĩnh", "trung", "Miền Trung & Tây Nguyên", "Biển Thiên Cầm trong xanh, ngã ba Đồng Lộc & khu lưu niệm Nguyễn Du", null, false);
        add("Nghệ An", "trung", "Miền Trung & Tây Nguyên", "Quê Bác Nam Đàn ấm áp, bãi biển Cửa Lò náo nhiệt", null, false);
        add("Ninh Thuận", "trung", "Miền Trung & Tây Nguyên", "Vịnh Vĩnh Hy hoang sơ tuyệt mỹ, vườn nho bạt ngàn & đồi cừu An Hòa", null, false);
        add("Quảng Ngãi", "trung", "Miền Trung & Tây Nguyên", "Đảo Lý Sơn - Thiên đường núi lửa giữa đại dương xanh ngắt", null, false);
        add("Quảng Trị", "trung", "Miền Trung & Tây Nguyên", "Địa đạo Vịnh Mốc kiên cường, sông Bến Hải & cầu Hiền Lương lịch sử", null, false);
        add("Thanh Hóa", "trung", "Miền Trung & Tây Nguyên", "Bãi biển Sầm Sơn rực rỡ nắng hè & khu bảo tồn Pù Luông xanh ngát", null, false);

        // ==========================================
        // 3. MIỀN NAM (19 TỈNH/THÀNH)
        // ==========================================
        add("Thành phố Hồ Chí Minh", "nam", "Miền Nam", "Trung tâm kinh tế sầm uất, nhịp sống trẻ trung & văn hóa ẩm thực đa dạng", "/images/gallery2.png", true);
        add("Bà Rịa - Vũng Tàu", "nam", "Miền Nam", "Bãi Sau Vũng Tàu, ngọn hải đăng cổ kính & hải sản tươi sống", "/images/gallery3.png", true);
        add("Kiên Giang", "nam", "Miền Nam", "Đảo ngọc Phú Quốc biển xanh cát trắng & quần đảo Nam Du hoang sơ", "/images/gallery1.png", true);
        add("Cần Thơ", "nam", "Miền Nam", "Thủ phủ Tây Đô sông nước, chợ nổi Cái Răng rộn ràng sáng sớm", "/images/gallery5.png", true);
        add("An Giang", "nam", "Miền Nam", "Rừng tràm Trà Sư xanh mướt, miếu Bà Chúa Xứ núi Sam linh thiêng", null, false);
        add("Bạc Liêu", "nam", "Miền Nam", "Nhà công tử Bạc Liêu hào hoa & cánh đồng quạt gió khổng lồ ven biển", null, false);
        add("Bến Tre", "nam", "Miền Nam", "Xứ dừa thanh bình, kênh rạch rợp bóng dừa nước & miệt vườn trĩu quả", null, false);
        add("Bình Dương", "nam", "Miền Nam", "Đô thị công nghiệp hiện đại, làng gốm Lái Thiêu & chùa Bà Thiên Hậu", null, false);
        add("Bình Phước", "nam", "Miền Nam", "Rừng cao su bạt ngàn thay lá mùa thu & trảng cỏ Bù Lạch xanh rì", null, false);
        add("Cà Mau", "nam", "Miền Nam", "Đất Mũi Cà Mau - Cột mốc tọa độ cực Nam thiêng liêng của Tổ quốc", null, false);
        add("Đồng Nai", "nam", "Miền Nam", "Vườn quốc gia Cát Tiên hoang dã & khu du lịch Bửu Long hữu tình", null, false);
        add("Đồng Tháp", "nam", "Miền Nam", "Đầm sen Tháp Mười ngan ngát hương thơm & làng hoa Sa Đéc rực rỡ", null, false);
        add("Hậu Giang", "nam", "Miền Nam", "Chợ nổi Ngã Bảy tấp nập thuyền bè & cánh đồng khóm Cầu Đúc bạt ngàn", null, false);
        add("Long An", "nam", "Miền Nam", "Làng nổi Tân Lập rừng tràm ma mị & cửa ngõ miền Tây trù phú", null, false);
        add("Sóc Trăng", "nam", "Miền Nam", "Chùa Dơi cổ kính linh thiêng, lễ hội Ooc Om Boc & bánh pía thơm bùi", null, false);
        add("Tây Ninh", "nam", "Miền Nam", "Núi Bà Đen nóc nhà Đông Nam Bộ & Tòa thánh Cao Đài uy nghiêm", null, false);
        add("Tiền Giang", "nam", "Miền Nam", "Cù lao Thới Sơn xum xuê hoa trái, chợ nổi Cái Bè sông Tiền", null, false);
        add("Trà Vinh", "nam", "Miền Nam", "Vương quốc chùa Khmer cổ kính, ao Bà Om rợp bóng cây sao nghìn năm", null, false);
        add("Vĩnh Long", "nam", "Miền Nam", "Miệt vườn cù lao An Bình sông nước & làng gốm Mang Thít trầm mặc", null, false);
    }

    private static void add(String name, String region, String regionName, String description, String imageUrl, boolean featured) {
        ALL_PROVINCES.add(ProvinceDto.builder()
                .name(name)
                .region(region)
                .regionName(regionName)
                .description(description)
                .imageUrl(imageUrl != null ? imageUrl : "/images/gallery" + ((ALL_PROVINCES.size() % 8) + 1) + ".png")
                .featured(featured)
                .build());
    }

    public static List<ProvinceDto> getAll() {
        return Collections.unmodifiableList(ALL_PROVINCES);
    }

    public static List<ProvinceDto> getFeatured() {
        return ALL_PROVINCES.stream().filter(ProvinceDto::isFeatured).toList();
    }
}
