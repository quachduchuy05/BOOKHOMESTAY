-- ===================================================================================
-- SCRIPT SQL: DỮ LIỆU MẪU HOMESTAY VÀ PHÒNG THEO 25+ TỈNH THÀNH (BOOKINGHOMESTAY)
-- Chạy trực tiếp trên MySQL nếu muốn nạp thủ công vào cơ sở dữ liệu `homestay_booking_db`
-- ===================================================================================

USE `homestay_booking_db`;

-- 1. Tiện nghi (Amenities)
INSERT IGNORE INTO `amenities` (`id`, `name`, `icon`) VALUES
(1, 'Wifi tốc độ cao', 'fa-solid fa-wifi'),
(2, 'Điều hòa nhiệt độ', 'fa-solid fa-snowflake'),
(3, 'Bếp nấu ăn đầy đủ', 'fa-solid fa-kitchen-set'),
(4, 'Bể bơi ngoài trời', 'fa-solid fa-person-swimming'),
(5, 'Chỗ đỗ xe ô tô', 'fa-solid fa-square-parking'),
(6, 'Bếp nướng BBQ sân vườn', 'fa-solid fa-fire-burner'),
(7, 'Máy sấy tóc & bàn là', 'fa-solid fa-shirt'),
(8, 'Máy giặt sấy', 'fa-solid fa-soap');

-- 2. Tài khoản Chủ nhà mẫu (Host)
INSERT IGNORE INTO `users` (`id`, `email`, `password`, `full_name`, `phone`, `status`, `provider`, `created_at`) VALUES
(2, 'host@bookinghomestay.local', '$2a$10$w8uD94D2P4e42Y7G4N0eI.KzFq8l7b8v0P8i6a9L0h5z8x7c6v5b4', 'Đối Tác Chủ Nhà Mẫu', '0987654321', 'ACTIVE', 'LOCAL', NOW());

INSERT IGNORE INTO `user_roles` (`user_id`, `role_id`)
SELECT 2, id FROM `roles` WHERE `name` IN ('ROLE_HOST', 'ROLE_CUSTOMER');

INSERT IGNORE INTO `hosts` (`id`, `user_id`, `business_name`, `status`, `created_at`) VALUES
(1, 2, 'Hệ Thống Nghỉ Dưỡng Homestay Việt Nam', 'APPROVED', NOW());

-- 3. Danh sách Homestay theo các Tỉnh/Thành trên Toàn Quốc
-- ===================================================================================
INSERT IGNORE INTO `homestays` (`id`, `host_id`, `slug`, `name`, `description`, `province`, `district`, `address`, `latitude`, `longitude`, `status`, `created_at`) VALUES
-- 1. Đà Lạt
(1, 1, 'homestay-da-lat-mong-mo', 'Homestay Đà Lạt Mộng Mơ', 'Tọa lạc giữa sườn đồi thông xanh ngát của thành phố ngàn hoa, Homestay Đà Lạt Mộng Mơ mang đến không gian nghỉ dưỡng tĩnh lặng, ban công ngắm trọn hoàng hôn và thung lũng sương mây bồng bềnh mỗi sớm mai.', 'Đà Lạt', 'Phường 10', 'Đường Khởi Nghĩa Bắc Sơn, Phường 10, TP. Đà Lạt, Lâm Đồng', 11.9360, 108.4550, 'ACTIVE', NOW()),
(2, 1, 'villa-rung-thong-khoi-sac', 'Villa Rừng Thông Khởi Sắc', 'Khu biệt thự nghỉ dưỡng nguyên căn phong cách vintage gần Hồ Tuyền Lâm. Sân vườn rộng lớn ngập tràn hoa tươi và khu vực tiệc nướng BBQ ngoài trời ấm áp.', 'Đà Lạt', 'Phường 3', 'Khu du lịch Hồ Tuyền Lâm, Phường 3, TP. Đà Lạt, Lâm Đồng', 11.8988, 108.4320, 'ACTIVE', NOW()),

-- 2. Sa Pa
(3, 1, 'sapa-may-village-homestay', 'Sapa Mây Village & Homestay', 'Nằm lưng chừng bản Tả Van với tầm nhìn bao trọn thung lũng Mường Hoa và ruộng bậc thang tầng tầng lớp lớp. Nơi lý tưởng để săn biển mây bồng bềnh và hòa mình vào thiên nhiên Tây Bắc.', 'Sa Pa', 'Tả Van', 'Bản Tả Van Dáy, Xã Tả Van, Thị xã Sa Pa, Lào Cai', 22.3039, 103.8732, 'ACTIVE', NOW()),

-- 3. Hà Nội
(4, 1, 'old-quarter-heritage-homestay', 'Old Quarter Heritage Homestay', 'Căn homestay mang đậm nét hoài cổ của 36 phố phường Hà Nội nhưng đầy đủ tiện nghi hiện đại. Chỉ 3 phút đi bộ là chạm tới Hồ Gươm, chợ đêm và các quán ăn đường phố trứ danh.', 'Hà Nội', 'Hoàn Kiếm', '18 Phố Hàng Gai, Quận Hoàn Kiếm, TP. Hà Nội', 21.0333, 105.8500, 'ACTIVE', NOW()),
(5, 1, 'ba-vi-green-retreat-villa', 'Ba Vì Green Retreat Villa', 'Khu biệt thự nghỉ dưỡng ngoại ô Ba Vì nép mình dưới chân núi Ba Vì. Khuôn viên rộng 3.000m2 có hồ bơi nước khoáng trong vắt, bãi cỏ cắm trại và khu nướng BBQ lý tưởng.', 'Hà Nội', 'Ba Vì', 'Thôn Yên Bài, Xã Vân Hòa, Huyện Ba Vì, TP. Hà Nội', 21.0543, 105.3722, 'ACTIVE', NOW()),

-- 4. Đà Nẵng
(6, 1, 'my-khe-ocean-breeze-homestay', 'Mỹ Khê Ocean Breeze Homestay', 'Cách bãi biển Mỹ Khê chỉ 150m đi bộ. Thiết kế trẻ trung, hiện đại theo phong cách biển nhiệt đới, xung quanh bạt ngàn nhà hàng hải sản tươi ngon và quán cà phê đẹp.', 'Đà Nẵng', 'Sơn Trà', 'Đường Võ Văn Kiệt, Phường Phước Mỹ, Quận Sơn Trà, TP. Đà Nẵng', 16.0610, 108.2435, 'ACTIVE', NOW()),

-- 5. Vũng Tàu
(7, 1, 'vung-tau-sunset-sea-homestay', 'Vũng Tàu Sunset Sea Homestay', 'Tọa lạc ngay mặt tiền Bãi Sau Vũng Tàu, bước qua đường là tới bãi cát vàng. Căn homestay trang bị đầy đủ bếp nướng BBQ, dụng cụ nấu ăn hải sản và ban công ngắm biển.', 'Vũng Tàu', 'Phường 2', '128 Đường Thùy Vân, Phường 2, TP. Vũng Tàu, Bà Rịa - Vũng Tàu', 10.3392, 107.0843, 'ACTIVE', NOW()),

-- 6. Nha Trang
(8, 1, 'nha-trang-coastal-haven', 'Nha Trang Coastal Haven Homestay', 'Nằm trên cung đường biển Trần Phú sầm uất, nội thất phong cách Địa Trung Hải tinh tế, ban công ngắm trọn vịnh biển Nha Trang trong xanh.', 'Nha Trang', 'Lộc Thọ', '72 Đường Trần Phú, Phường Lộc Thọ, TP. Nha Trang, Khánh Hòa', 12.2388, 109.1967, 'ACTIVE', NOW()),

-- 7. Phú Quốc
(9, 1, 'phu-quoc-tropical-beach-homestay', 'Phú Quốc Tropical Beach Homestay', 'Nép mình dưới rặng dừa xanh ngát tại đảo ngọc Phú Quốc, các căn bungalow nhiệt đới mái lá mộc mạc bên bờ cát trắng đón hoàng hôn rực rỡ nhất Việt Nam.', 'Phú Quốc', 'Dương Đông', 'Đường Trần Hưng Đạo, Phường Dương Đông, TP. Phú Quốc, Kiên Giang', 10.2249, 103.9572, 'ACTIVE', NOW()),

-- 8. Ninh Bình
(10, 1, 'ninh-binh-trang-an-mountain-side', 'Tràng An Mountain Side Homestay', 'Nằm nép mình bên chân núi đá vôi hùng vĩ của quần thể danh thắng Tràng An, homestay sở hữu hồ sen tuyệt đẹp, không gian thanh bình và dịch vụ chèo thuyền kayak khám phá non nước Ninh Bình.', 'Ninh Bình', 'Hoa Lư', 'Thôn Khê Hạ, Xã Ninh Xuân, Huyện Hoa Lư, Ninh Bình', 20.2520, 105.9180, 'ACTIVE', NOW()),

-- 9. Quảng Ninh
(11, 1, 'ha-long-bay-view-oasis', 'Hạ Long Bay View Oasis', 'Căn hộ nghỉ dưỡng cao cấp với tầm nhìn trực diện vịnh di sản Hạ Long kỳ vĩ. Chỉ vài bước chân tới bãi biển Bãi Cháy, phố cổ Hạ Long và khu vui chơi Sun World náo nhiệt.', 'Quảng Ninh', 'Hạ Long', 'Đường Hoàng Quốc Việt, Phường Bãi Cháy, TP. Hạ Long, Quảng Ninh', 20.9520, 107.0370, 'ACTIVE', NOW()),

-- 10. Thành phố Hồ Chí Minh
(12, 1, 'sai-gon-retro-house-loft', 'Sài Gòn Retro House & Loft', 'Mang đậm phong cách kiến trúc Indochine giao thoa hoài cổ của Sài Gòn thập niên 90, tọa lạc ngay trung tâm Quận 1, thuận tiện dạo bộ tới Nhà hát Lớn, chợ Bến Thành và phố đi bộ Nguyễn Huệ.', 'Thành phố Hồ Chí Minh', 'Quận 1', '158 Pasteur, Phường Bến Nghé, Quận 1, Thành phố Hồ Chí Minh', 10.7780, 106.6980, 'ACTIVE', NOW()),

-- 11. Quảng Nam
(13, 1, 'hoi-an-riverside-bamboo-home', 'Hội An Riverside Bamboo Home', 'Nằm giữa rừng dừa Bảy Mẫu thanh bình bên bờ sông Thu Bồn thơ mộng, không gian mộc mạc với tre trúc, lồng đèn Hội An lung linh và các hoạt động chèo thuyền thúng trải nghiệm bản địa.', 'Quảng Nam', 'Hội An', 'Cẩm Thanh, TP. Hội An, Quảng Nam', 15.8790, 108.3580, 'ACTIVE', NOW()),

-- 12. Thừa Thiên Huế
(14, 1, 'hue-co-do-garden-homestay', 'Huế Cố Đô Garden Homestay', 'Không gian nhà rường truyền thống đậm chất hoàng cung xứ Huế, ẩn mình trong khu vườn thanh trà xanh mát ngát hương. Chỉ cách Đại Nội và sông Hương 5 phút di chuyển.', 'Thừa Thiên Huế', 'Huế', '24 Lê Thánh Tôn, Phường Thuận Thành, TP. Huế, Thừa Thiên Huế', 16.4710, 107.5780, 'ACTIVE', NOW()),

-- 13. Bình Định
(15, 1, 'quy-nhon-sunny-sea-homestay', 'Quy Nhơn Sunny Sea Homestay', 'Tọa lạc tại làng chài Nhơn Lý bình dị, ngay gần Kỳ Co và Eo Gió hoang sơ. Đón bình minh rực rỡ nhất miền Trung, tận hưởng hải sản tươi sống và biển xanh trong vắt.', 'Bình Định', 'Quy Nhơn', 'Làng Chài Nhơn Lý, TP. Quy Nhơn, Bình Định', 13.9050, 109.2810, 'ACTIVE', NOW()),

-- 14. Hà Giang
(16, 1, 'ha-giang-cao-nguyen-da-lodge', 'Hà Giang Cao Nguyên Đá Lodge', 'Nằm ngay Làng văn hóa du lịch cộng đồng Pả Vi dưới chân đèo Mã Pí Lèng huyền thoại. Kiến trúc nhà trình tường mái ngói âm dương của đồng bào H''Mông nhìn thẳng ra hẻm vực sông Nho Quế.', 'Hà Giang', 'Mèo Vạc', 'Làng Văn Hóa Pả Vi, Huyện Mèo Vạc, Hà Giang', 23.1610, 105.4120, 'ACTIVE', NOW()),

-- 15. Sơn La
(17, 1, 'moc-chau-doi-che-trai-tim', 'Mộc Châu Đồi Chè Trái Tim Homestay', 'Bao quanh bởi bạt ngàn đồi chè xanh mướt của cao nguyên Mộc Châu lộng gió. Không khí trong lành, view thung lũng mận mơ bung nở hoa trắng muốt vào mùa xuân.', 'Sơn La', 'Mộc Châu', 'Tiểu khu Mía Đường, TT. Nông Trường Mộc Châu, Sơn La', 20.8420, 104.6640, 'ACTIVE', NOW()),

-- 16. Cần Thơ
(18, 1, 'can-tho-riverside-mekong-home', 'Cần Thơ Riverside Mekong Home', 'Trải nghiệm cuộc sống sông nước miền Tây đích thực bên bờ sông Hậu hiền hòa. Miệt vườn rợp bóng cây ăn trái, tiếng đờn ca tài tử và thuyền đón đi chợ nổi Cái Răng sáng sớm.', 'Cần Thơ', 'Cái Răng', 'Cồn Ấu, Phường Hưng Phú, Quận Cái Răng, Cần Thơ', 10.0210, 105.7950, 'ACTIVE', NOW()),

-- 17. Bình Thuận
(19, 1, 'mui-ne-sandy-beach-house', 'Mũi Né Sandy Beach House', 'Tọa lạc ngay cung đường ven biển Huỳnh Thúc Kháng rực rỡ hoa giấy, gần Đồi Cát Bay và Làng chài Mũi Né. Bãi cát vàng mịn màng cùng làn gió biển trong lành quanh năm.', 'Bình Thuận', 'Phan Thiết', 'Đường Huỳnh Thúc Kháng, Phường Mũi Né, TP. Phan Thiết, Bình Thuận', 10.9380, 108.2870, 'ACTIVE', NOW()),

-- 18. Quảng Bình
(20, 1, 'phong-nha-cave-explorer-homestay', 'Phong Nha Cave Explorer Homestay', 'Tọa lạc bên dòng sông Son thơ mộng, là điểm xuất phát lý tưởng để khám phá kỳ quan Động Phong Nha, Thiên Đường và hang Sơn Đoòng hùng vĩ.', 'Quảng Bình', 'Bố Trạch', 'Thôn Sơn Trạch, Huyện Bố Trạch, Quảng Bình', 17.5870, 106.2840, 'ACTIVE', NOW()),

-- 19. Hải Phòng
(21, 1, 'cat-ba-emerald-island-retreat', 'Cát Bà Emerald Island Retreat', 'Tọa lạc trên sườn đồi nhìn thẳng ra vịnh Lan Hạ thơ mộng của quần đảo Cát Bà. Không khí mát mẻ, dịch vụ chèo thuyền kayak và du thuyền ngắm vịnh riêng tư.', 'Hải Phòng', 'Cát Hải', 'Thị trấn Cát Bà, Huyện Cát Hải, Hải Phòng', 20.7280, 107.0490, 'ACTIVE', NOW()),

-- 20. Đắk Lắk
(22, 1, 'ban-me-coffee-farm-stay', 'Ban Mê Coffee Farm Stay', 'Nằm giữa trang trại cà phê Robusta bạt ngàn của thủ phủ Tây Nguyên. Cùng thưởng thức cà phê nguyên chất bên bếp lửa bập bùng và ngắm hoàng hôn hồ Ea Kao.', 'Đắk Lắk', 'Buôn Ma Thuột', 'Xã Ea Kao, TP. Buôn Ma Thuột, Đắk Lắk', 12.6200, 108.0600, 'ACTIVE', NOW()),

-- 21. Hòa Bình
(23, 1, 'mai-chau-thung-lung-xanh', 'Mai Châu Thung Lũng Xanh', 'Trải nghiệm nhà sàn truyền thống của người Thái tại Bản Lác, ngắm cánh đồng lúa xanh ngút ngàn và tham gia các điệu múa xòe, đốt lửa trại đặc sắc.', 'Hòa Bình', 'Mai Châu', 'Bản Lác, Xã Chiềng Châu, Huyện Mai Châu, Hòa Bình', 20.6620, 105.0850, 'ACTIVE', NOW()),

-- 22. Phú Yên
(24, 1, 'phu-yen-hoa-vang-co-xanh', 'Phú Yên Hoa Vàng Trên Cỏ Xanh', 'Vùng đất hoa vàng cỏ xanh với biển trời trong vắt, gần Bãi Xép và Gành Đá Đĩa kỳ quan thiên nhiên. Không gian thoáng đãng, lộng gió biển miền Trung.', 'Phú Yên', 'Tuy Hòa', 'Đường Độc Lập, Phường 9, TP. Tuy Hòa, Phú Yên', 13.1020, 109.3100, 'ACTIVE', NOW()),

-- 23. Thanh Hóa
(25, 1, 'pu-luong-retreat-may-ngan', 'Pù Luông Retreat Mây Ngàn', 'Nằm giữa thung lũng Pù Luông nguyên sơ với những thửa ruộng bậc thang trùng điệp. Sở hữu hồ bơi vô cực ngắm mây vờn đỉnh núi và dòng suối mát lành.', 'Thanh Hóa', 'Bá Thước', 'Bản Đôn, Xã Thành Lâm, Huyện Bá Thước, Thanh Hóa', 20.4560, 105.1840, 'ACTIVE', NOW()),

-- 24. An Giang
(26, 1, 'an-giang-that-son-river-view', 'An Giang Thất Sơn River View', 'Khám phá vùng đất Thất Sơn huyền bí và rừng tràm Trà Sư xanh ngắt bèo tây. Homestay có ban công ngắm dòng kênh Vĩnh Tế và thưởng thức bún cá Châu Đốc nức tiếng.', 'An Giang', 'Châu Đốc', 'Đường Lê Lợi, Phường Châu Phú B, TP. Châu Đốc, An Giang', 10.7050, 105.1200, 'ACTIVE', NOW()),

-- 25. Bến Tre
(27, 1, 'ben-tre-dua-xanh-eco-lodge', 'Bến Tre Dừa Xanh Eco Lodge', 'Về xứ dừa Bến Tre thanh bình, chèo xuồng len lỏi rặng dừa nước, thưởng thức kẹo dừa béo ngậy và món cá lóc nướng trui mộc mạc thơm lừng.', 'Bến Tre', 'Châu Thành', 'Xã Tân Thạch, Huyện Châu Thành, Bến Tre', 10.3340, 106.3520, 'ACTIVE', NOW());

-- 4. Hình ảnh cho từng Homestay
INSERT IGNORE INTO `homestay_images` (`homestay_id`, `image_url`) VALUES
(1, '/images/gallery1-1.jpg'), (1, '/images/gallery2-2.jpg'), (1, '/images/gallery3-3.jpg'),
(2, '/images/gallery4-4.jpg'), (2, '/images/gallery5-5.jpg'),
(3, '/images/gallery6-6.jpg'), (3, '/images/gallery7-7.jpg'),
(4, '/images/gallery8-8.jpg'), (4, '/images/gallery9-9.jpg'),
(5, '/images/gallery1-1.jpg'), (5, '/images/gallery3-3.jpg'),
(6, '/images/gallery2-2.jpg'), (6, '/images/gallery5-5.jpg'),
(7, '/images/gallery4-4.jpg'), (7, '/images/gallery6-6.jpg'),
(8, '/images/gallery7-7.jpg'), (8, '/images/gallery8-8.jpg'),
(9, '/images/gallery1-1.jpg'), (9, '/images/gallery5-5.jpg'),
(10, '/images/gallery1-1.jpg'), (10, '/images/gallery2-2.jpg'),
(11, '/images/gallery3-3.jpg'), (11, '/images/gallery4-4.jpg'),
(12, '/images/gallery2-2.jpg'), (12, '/images/gallery5-5.jpg'),
(13, '/images/gallery1-1.jpg'), (13, '/images/gallery3-3.jpg'),
(14, '/images/gallery4-4.jpg'), (14, '/images/gallery2-2.jpg'),
(15, '/images/gallery5-5.jpg'), (15, '/images/gallery1-1.jpg'),
(16, '/images/gallery2-2.jpg'), (16, '/images/gallery4-4.jpg'),
(17, '/images/gallery3-3.jpg'), (17, '/images/gallery1-1.jpg'),
(18, '/images/gallery5-5.jpg'), (18, '/images/gallery2-2.jpg'),
(19, '/images/gallery4-4.jpg'), (19, '/images/gallery1-1.jpg'),
(20, '/images/gallery1-1.jpg'), (20, '/images/gallery5-5.jpg'),
(21, '/images/gallery3-3.jpg'), (21, '/images/gallery2-2.jpg'),
(22, '/images/gallery4-4.jpg'), (22, '/images/gallery5-5.jpg'),
(23, '/images/gallery1-1.jpg'), (23, '/images/gallery2-2.jpg'),
(24, '/images/gallery3-3.jpg'), (24, '/images/gallery4-4.jpg'),
(25, '/images/gallery5-5.jpg'), (25, '/images/gallery1-1.jpg'),
(26, '/images/gallery2-2.jpg'), (26, '/images/gallery3-3.jpg'),
(27, '/images/gallery4-4.jpg'), (27, '/images/gallery5-5.jpg');

-- 5. Danh sách các phòng (Rooms)
INSERT IGNORE INTO `rooms` (`homestay_id`, `name`, `price_per_night`, `max_guests`, `description`, `status`) VALUES
-- Homestay Đà Lạt Mộng Mơ (id=1)
(1, 'Phòng Deluxe Ban Công View Rừng Thông', 450000.00, 2, 'Phòng đôi 28m2 ngắm trọn thung lũng thông, giường King êm ái, đầy đủ vệ sinh khép kín.', 'ACTIVE'),
(1, 'Phòng Suite Áp Mái Săn Mây', 750000.00, 2, 'Thiết kế áp mái ấm cúng phong cách Bắc Âu, bồn tắm gỗ ngắm hoàng hôn, trà cà phê miễn phí.', 'ACTIVE'),
(1, 'Phòng Gia Đình Sunset Panorama', 1200000.00, 4, 'Phòng gia đình 2 giường đôi King size rộng rãi, ban công lớn ngắm toàn cảnh thung lũng.', 'ACTIVE'),

-- Villa Rừng Thông Khởi Sắc (id=2)
(2, 'Phòng Superior Double Hướng Vườn', 550000.00, 2, 'Cửa sổ kính lớn nhìn ra vườn hoa cẩm tú cầu, nội thất gỗ tự nhiên mộc mạc.', 'ACTIVE'),
(2, 'Phòng VIP Ban Công Hướng Hồ', 950000.00, 2, 'View trực diện hồ nước và đồi thông, không gian cực kỳ lãng mạn cho cặp đôi.', 'ACTIVE'),
(2, 'Nguyên Căn Villa Nghỉ Dưỡng 3 Phòng Ngủ', 2800000.00, 8, 'Bao trọn căn biệt thự gồm phòng khách, bếp đầy đủ dụng cụ và 3 phòng ngủ tiện nghi.', 'ACTIVE'),

-- Sapa Mây Village (id=3)
(3, 'Bungalow Gỗ Pơ-Mu View Ruộng Bậc Thang', 550000.00, 2, 'Bungalow gỗ ấm áp view trực diện thung lũng Mường Hoa, trà thảo mộc miễn phí.', 'ACTIVE'),
(3, 'Phòng Đôi Săn Mây Đỉnh Núi', 850000.00, 2, 'Tầng cao nhất với ban công kính 360 độ ngắm trọn mây trời và đỉnh Fansipan.', 'ACTIVE'),
(3, 'Phòng Tập Thể Bạn Bè Dorm 4 Giường', 900000.00, 4, 'Thiết kế giường tầng tiện lợi, tủ khóa riêng biệt, phù hợp nhóm bạn trẻ khám phá.', 'ACTIVE'),

-- Old Quarter Heritage Homestay (id=4)
(4, 'Phòng Cozy Studio Ban Công Phố Cổ', 480000.00, 2, 'Căn studio xinh xắn ban công nhìn xuống góc phố cổ rêu phong yên bình.', 'ACTIVE'),
(4, 'Phòng Suite Gia Đình Heritage', 950000.00, 4, '2 giường đôi lớn, không gian bếp nhỏ và bàn trà tiếp khách đậm chất Tràng An.', 'ACTIVE'),

-- Ba Vì Green Retreat Villa (id=5)
(5, 'Phòng Superior Vườn Xanh Sinh Thái', 650000.00, 2, 'Cửa kính lớn bao quanh nhìn ra đồi cỏ xanh ngút ngàn, không khí mát lành.', 'ACTIVE'),
(5, 'Nguyên Căn Villa Ba Vì Sân Vườn & Hồ Bơi', 2500000.00, 8, 'Biệt thự 4 phòng ngủ sang trọng dành cho đại gia đình hoặc team building cuối tuần.', 'ACTIVE'),

-- Mỹ Khê Ocean Breeze Homestay (id=6)
(6, 'Phòng Deluxe Hướng Biển Ban Công', 420000.00, 2, 'Ban công đón gió biển mát rượi, ngắm bình minh trên biển Mỹ Khê tuyệt đẹp.', 'ACTIVE'),
(6, 'Căn Hộ Studio Bếp Riêng View Cầu Rồng', 680000.00, 3, 'Có khu vực bếp riêng nấu ăn hải sản, view ngắm pháo hoa và Cầu Rồng.', 'ACTIVE'),
(6, 'Phòng Gia Đình Ocean Penthouse', 1350000.00, 5, 'Căn penthouse tầng thượng rộng rãi với 2 phòng ngủ và ban công ngắm toàn cảnh vịnh.', 'ACTIVE'),

-- Vũng Tàu Sunset Sea Homestay (id=7)
(7, 'Phòng Double Ban Công Gió Biển Bãi Sau', 380000.00, 2, 'Phòng giường đôi ấm áp, tiện nghi tiêu chuẩn khách sạn cao cấp.', 'ACTIVE'),
(7, 'Phòng Suite Panorama Biển Bãi Sau', 720000.00, 2, 'Góc nhìn biển 180 độ đón ánh hoàng hôn lãng mạn, bồn tắm nằm thư giãn.', 'ACTIVE'),
(7, 'Căn Hộ 2 Phòng Ngủ Cho Nhóm Bạn', 1400000.00, 6, '2 phòng ngủ riêng biệt, phòng khách rộng có sofa bed và bếp nấu nướng thoải mái.', 'ACTIVE'),

-- Nha Trang Coastal Haven (id=8)
(8, 'Phòng Deluxe Queen Hướng Biển', 400000.00, 2, 'Cửa kính lớn hướng biển đón gió lành, trang thiết bị vệ sinh hiện đại.', 'ACTIVE'),
(8, 'Studio Ban Công Hoàng Hôn Vịnh Nha Trang', 650000.00, 3, 'Không gian mở khoáng đạt, bàn làm việc và ban công view vịnh biển lộng gió.', 'ACTIVE'),

-- Phú Quốc Tropical Beach Homestay (id=9)
(9, 'Bungalow Vườn Dừa Nhiệt Đới', 600000.00, 2, 'Bungalow mộc mạc ẩn mình giữa khu vườn nhiệt đới xanh mát, cực kỳ yên tĩnh.', 'ACTIVE'),
(9, 'Bungalow Bãi Biển Hướng Hoàng Hôn', 1100000.00, 2, 'Chỉ cách mép sóng 20m, ban công gỗ ngắm trọn vẹn hoàng hôn đảo ngọc.', 'ACTIVE'),
(9, 'Family Villa 2 Phòng Ngủ Hồ Bơi Riêng', 2200000.00, 6, 'Biệt thự hồ bơi riêng tư với 2 phòng ngủ khép kín, khu BBQ bãi biển.', 'ACTIVE'),

-- Ninh Bình (id=10)
(10, 'Bungalow Hướng Núi Non Nước', 550000.00, 2, 'Bungalow mái ngói hướng thẳng vách núi đá vôi hùng vĩ, ban công thưởng trà buổi sớm.', 'ACTIVE'),
(10, 'Phòng Gia Đình View Hồ Sen Tràng An', 950000.00, 4, 'Phòng rộng 45m2 view trọn đầm sen ngát hương, đầy đủ tiện nghi cho gia đình.', 'ACTIVE'),
(10, 'Phòng Đôi Deluxe Ban Công Đá Vôi', 680000.00, 2, 'Thiết kế mộc mạc tinh tế, cửa kính chạm trần đón ánh sáng thiên nhiên.', 'ACTIVE'),

-- Quảng Ninh (id=11)
(11, 'Căn Hộ Panorama Ngắm Vịnh Kỳ Quan', 1100000.00, 2, 'Tầng cao view trọn vẹn vịnh Hạ Long và vòng quay Mặt Trời, nội thất sang trọng.', 'ACTIVE'),
(11, 'Phòng Gia Đình 2 Phòng Ngủ Hướng Biển', 1850000.00, 5, 'Căn hộ 2 phòng ngủ 2 WC riêng biệt, phòng khách ban công biển cực thoáng đãng.', 'ACTIVE'),
(11, 'Phòng Studio Ấm Cúng Gần Bãi Cháy', 600000.00, 2, 'Tiện nghi hiện đại, vị trí đắc địa ngay trung tâm Bãi Cháy ẩm thực sầm uất.', 'ACTIVE'),

-- TP. Hồ Chí Minh (id=12)
(12, 'Studio Vintage Ban Công Phố Đi Bộ', 690000.00, 2, 'Nội thất gỗ xưa ấm cúng, ban công ngắm nhịp sống Sài Gòn rực rỡ ánh đèn đêm.', 'ACTIVE'),
(12, 'Căn Hộ Loft Gác Xép Bohemian', 880000.00, 3, 'Thiết kế gác lửng độc đáo tràn ngập ánh sáng, máy pha cà phê và gian bếp nhỏ tiện ích.', 'ACTIVE'),
(12, 'Master Suite Sài Gòn Xưa', 1250000.00, 4, 'Phòng rộng rãi phong cách quý phái, bồn tắm chân rồng cổ điển thư giãn.', 'ACTIVE'),

-- Quảng Nam (id=13)
(13, 'Bungalow Tre Hướng Sông Thu Bồn', 520000.00, 2, 'Bungalow tre mát mẻ bên mép sông, đón gió mát rượi và ngắm thuyền hoa đăng.', 'ACTIVE'),
(13, 'Phòng Deluxe Đèn Lồng Sân Vườn', 450000.00, 2, 'Không gian tĩnh lặng ngập tràn sắc hoa và ánh đèn lồng lãng mạn.', 'ACTIVE'),
(13, 'Villa Gia Đình Mộc Mạc Ven Sông', 1300000.00, 6, 'Khu nhà nguyên căn với sân vườn rộng, bể bơi ngoài trời và tiệc nướng BBQ bên sông.', 'ACTIVE'),

-- Thừa Thiên Huế (id=14)
(14, 'Phòng Nhà Rường Cổ Cố Đô', 480000.00, 2, 'Cột gỗ chạm khắc hoa văn tinh xảo, không gian thanh tịnh bình yên đặc trưng xứ Huế.', 'ACTIVE'),
(14, 'Phòng Thượng Uyển View Vườn Thanh Trà', 700000.00, 2, 'Cửa sổ nhìn ra vườn cây sum suê trĩu quả, phục vụ trà cung đình Huế miễn phí.', 'ACTIVE'),
(14, 'Phòng Gia Đình Hoàng Cung Rộng Rãi', 1150000.00, 4, 'Phòng rộng cho gia đình, nội thất gỗ ấm cúng đầy đủ tiện nghi sinh hoạt.', 'ACTIVE'),

-- Bình Định (id=15)
(15, 'Phòng Đôi Hướng Biển Làng Chài', 450000.00, 2, 'Mở cửa là nhìn thấy biển cả bao la cùng những đoàn thuyền thúng đi biển về.', 'ACTIVE'),
(15, 'Phòng VIP Ban Công Bình Minh Eo Gió', 780000.00, 2, 'Ban công đón ánh bình minh tuyệt mỹ, phòng ốc sáng sủa hiện đại.', 'ACTIVE'),
(15, 'Phòng Tập Thể Cho Nhóm Phượt', 320000.00, 4, 'Giường tầng tiện nghi, sạch sẽ, giá cả hợp lý cho các bạn trẻ mê khám phá.', 'ACTIVE'),

-- Hà Giang (id=16)
(16, 'Phòng Nhà Trình Tường Bản Địa', 380000.00, 2, 'Tường đất ấm vào mùa đông mát vào mùa hè, nệm êm chăn thổ cẩm bản địa.', 'ACTIVE'),
(16, 'Bungalow Gỗ View Đèo Mã Pí Lèng', 650000.00, 2, 'Cửa kính lớn ngắm nhìn dãy núi đá tai mèo tráng lệ và mây vờn đỉnh núi.', 'ACTIVE'),
(16, 'Phòng Gia Đình Sưởi Ấm Củi Gỗ', 900000.00, 4, 'Phòng rộng có lò sưởi củi ấm cúng, trải nghiệm ẩm thực thắng cố và rượu ngô.', 'ACTIVE'),

-- Sơn La (id=17)
(17, 'Bungalow Hình Vòm Ngắm Đồi Chè', 480000.00, 2, 'Thiết kế vòm độc đáo nằm giữa luống chè Ô Long xanh mướt mắt.', 'ACTIVE'),
(17, 'Nhà Tam Giác Gỗ Giữa Vườn Mận', 550000.00, 2, 'Nhà gỗ chữ A xinh xắn, hoa mận nở rực rỡ quanh hiên nhà.', 'ACTIVE'),
(17, 'Phòng Gia Đình Sum Họp', 1050000.00, 5, 'Phòng sinh hoạt chung rộng rãi, bếp nướng BBQ ngoài trời giữa đồi chè mát rượi.', 'ACTIVE'),

-- Cần Thơ (id=18)
(18, 'Bungalow Mái Lá Ven Sông Tiền', 420000.00, 2, 'Mái lá dừa mát rượi, ban công nhìn ra xuồng ghe tấp nập trên sông.', 'ACTIVE'),
(18, 'Phòng Deluxe Vườn Cây Trái Nam Bộ', 620000.00, 2, 'Bao quanh bởi vườn xoài cát, chôm chôm và sầu riêng sum suê trĩu cành.', 'ACTIVE'),
(18, 'Căn Hộ Miệt Vườn Gia Đình', 1100000.00, 4, 'Căn nhà vườn phong cách Nam Bộ ấm áp, đầy đủ gian bếp và bàn ăn ngoài trời.', 'ACTIVE'),

-- Bình Thuận (id=19)
(19, 'Phòng Superior Sân Vườn Nhiệt Đới', 460000.00, 2, 'Lối đi ngập tràn dừa xiêm và hoa giấy rực rỡ, cách biển chỉ vài chục mét.', 'ACTIVE'),
(19, 'Bungalow View Biển Sóng Vỗ', 850000.00, 2, 'Nghe tiếng sóng biển rì rào suốt đêm ngày, ngắm hoàng hôn đỏ ối trên biển.', 'ACTIVE'),
(19, 'Villa Gia Đình BBQ Bãi Cát', 1500000.00, 6, 'Khu nghỉ dưỡng khép kín với sân vườn nướng hải sản tươi sống Mũi Né.', 'ACTIVE'),

-- Quảng Bình (id=20)
(20, 'Phòng Đôi View Sông Son Thơ Mộng', 390000.00, 2, 'Sông Son xanh ngắt như tranh vẽ, làn gió mát lành thổi từ rặng núi đá vôi.', 'ACTIVE'),
(20, 'Bungalow Núi Đá Kẻ Bàng', 620000.00, 2, 'Thiết kế vật liệu tự nhiên đá và gỗ, hài hòa cùng thiên nhiên di sản thế giới.', 'ACTIVE'),
(20, 'Phòng Tập Thể Cho Nhóm Thám Hiểm', 280000.00, 4, 'Tiện nghi và chi phí hợp lý cho du khách trekking hang động kỳ vĩ.', 'ACTIVE'),

-- Hải Phòng (id=21)
(21, 'Phòng Hướng Biển Đảo Lan Hạ', 580000.00, 2, 'Tầm nhìn ôm trọn vịnh nước trong xanh như ngọc và hàng trăm đảo đá nhấp nhô.', 'ACTIVE'),
(21, 'Phòng Deluxe Ban Công Núi & Biển', 820000.00, 2, 'Phòng rộng rãi ban công đón trọn gió vịnh Cát Bà mát rượi quanh năm.', 'ACTIVE'),
(21, 'Suite Nghỉ Dưỡng Cát Bà', 1400000.00, 4, 'Căn suite cao cấp có bồn tắm ngắm cảnh biển và khu tiếp khách sang trọng.', 'ACTIVE'),

-- Đắk Lắk (id=22)
(22, 'Nhà Rông Gỗ Tây Nguyên', 450000.00, 2, 'Kiến trúc nhà rông cách điệu, mái cao vút thoáng mát, nệm êm thổ cẩm Ê-đê.', 'ACTIVE'),
(22, 'Bungalow Hương Cà Phê Ban Mê', 600000.00, 2, 'Mỗi sáng thức giấc ngập tràn mùi hoa cà phê trắng muốt hoặc hương hạt rang xay.', 'ACTIVE'),
(22, 'Căn Hộ Gia Đình Sân Lửa Trại', 1100000.00, 5, 'Khoảng sân riêng tổ chức tiệc nướng BBQ và đốt lửa trại ấm áp vùng cao nguyên.', 'ACTIVE'),

-- Hòa Bình (id=23)
(23, 'Phòng Nhà Sàn Thái Bản Lác', 360000.00, 2, 'Sàn gỗ bóng sạch, nệm gấm truyền thống người Thái, cửa sổ nhìn ra đồng lúa.', 'ACTIVE'),
(23, 'Bungalow View Ruộng Lúa Mai Châu', 580000.00, 2, 'Bungalow riêng tư giữa lòng thung lũng, mộc mạc và cực kỳ thư thái.', 'ACTIVE'),
(23, 'Căn Sàn Gỗ Đoàn Đông Người', 1200000.00, 8, 'Không gian nhà sàn lớn rộng rãi cho nhóm bạn phượt hoặc đại gia đình sum vầy.', 'ACTIVE'),

-- Phú Yên (id=24)
(24, 'Phòng Đôi View Biển Tuy Hòa', 420000.00, 2, 'Phòng rộng mở cửa đón gió biển trong lành, cách bãi cát chỉ 50 mét.', 'ACTIVE'),
(24, 'Bungalow Sân Vườn Cỏ Xanh', 650000.00, 2, 'Bungalow xinh xắn giữa khu vườn ngập tràn hoa vàng và cây xanh nhiệt đới.', 'ACTIVE'),

-- Thanh Hóa (id=25)
(25, 'Bungalow Đôi View Ruộng Bậc Thang', 750000.00, 2, 'Ngắm nhìn từng tầng sóng lúa vàng rực rỡ mùa gặt ngay tại giường ngủ.', 'ACTIVE'),
(25, 'Phòng Deluxe Hồ Bơi Vô Cực', 1050000.00, 2, 'Phòng cao cấp sát hồ bơi vô cực nhìn thẳng ra thung lũng mây Pù Luông.', 'ACTIVE'),
(25, 'Phòng Gia Đình Bản Đôn', 1400000.00, 5, 'Nhà sàn gỗ tiện nghi hiện đại cho gia đình có trẻ nhỏ cùng nghỉ dưỡng.', 'ACTIVE'),

-- An Giang (id=26)
(26, 'Phòng Tiêu Chuẩn View Kênh Vĩnh Tế', 350000.00, 2, 'Không gian sạch sẽ, gió sông thổi lồng lộng, gần Miếu Bà Chúa Xứ.', 'ACTIVE'),
(26, 'Phòng Deluxe Thất Sơn Hướng Núi', 520000.00, 2, 'Tầm nhìn hướng về dãy núi Sam linh thiêng và cánh đồng biên giới bao la.', 'ACTIVE'),

-- Bến Tre (id=27)
(27, 'Phòng Mái Lá Mộc Mạc Ven Sông', 380000.00, 2, 'Nội thất từ gỗ dừa tự nhiên, võng đung đưa bên hiên nhà rợp bóng cây.', 'ACTIVE'),
(27, 'Bungalow Vườn Dừa Sinh Thái', 560000.00, 2, 'Bungalow tiện nghi hiện đại ẩn mình giữa rặng dừa xanh mướt mát lành.', 'ACTIVE');

-- 6. Gán tiện ích cho Homestay
INSERT IGNORE INTO `homestay_amenities` (`homestay_id`, `amenity_id`) VALUES
(1, 1), (1, 2), (1, 3), (1, 6),
(2, 1), (2, 5), (2, 6), (2, 8),
(3, 1), (3, 2), (3, 6),
(4, 1), (4, 2), (4, 7),
(5, 1), (5, 4), (5, 5), (5, 6),
(6, 1), (6, 2), (6, 5), (6, 8),
(7, 1), (7, 2), (7, 3), (7, 5),
(8, 1), (8, 2), (8, 4),
(9, 1), (9, 2), (9, 4), (9, 6),
(10, 1), (10, 2), (10, 5), (10, 6),
(11, 1), (11, 2), (11, 4), (11, 8),
(12, 1), (12, 2), (12, 3), (12, 8),
(13, 1), (13, 2), (13, 4), (13, 6),
(14, 1), (14, 2), (14, 3), (14, 5),
(15, 1), (15, 2), (15, 3), (15, 6),
(16, 1), (16, 5), (16, 6), (16, 7),
(17, 1), (17, 3), (17, 5), (17, 6),
(18, 1), (18, 2), (18, 3), (18, 4),
(19, 1), (19, 2), (19, 4), (19, 6),
(20, 1), (20, 2), (20, 5), (20, 6),
(21, 1), (21, 2), (21, 3), (21, 5),
(22, 1), (22, 3), (22, 5), (22, 6),
(23, 1), (23, 5), (23, 6),
(24, 1), (24, 2), (24, 3), (24, 5),
(25, 1), (25, 4), (25, 5), (25, 6),
(26, 1), (26, 2), (26, 3), (26, 5),
(27, 1), (27, 3), (27, 5), (27, 6);
