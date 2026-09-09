package BookingHomeStay.BookingHomeStay.service.impl;

import BookingHomeStay.BookingHomeStay.entity.Favorite;
import BookingHomeStay.BookingHomeStay.entity.Homestay;
import BookingHomeStay.BookingHomeStay.entity.User;
import BookingHomeStay.BookingHomeStay.exception.ResourceNotFoundException;
import BookingHomeStay.BookingHomeStay.repository.FavoriteRepository;
import BookingHomeStay.BookingHomeStay.repository.HomestayRepository;
import BookingHomeStay.BookingHomeStay.repository.UserRepository;
import BookingHomeStay.BookingHomeStay.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final HomestayRepository homestayRepository;

    @Override
    @Transactional
    public boolean toggleFavorite(Long userId, Long homestayId) {
        Optional<Favorite> existing = favoriteRepository.findByUserIdAndHomestayId(userId, homestayId);
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            log.info("[FAVORITE] User id={} đã xóa homestay id={} khỏi danh sách yêu thích", userId, homestayId);
            return false; // Da go khoi yeu thich
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng id=" + userId));
            Homestay homestay = homestayRepository.findById(homestayId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy homestay id=" + homestayId));

            Favorite favorite = Favorite.builder()
                    .user(user)
                    .homestay(homestay)
                    .build();
            favoriteRepository.save(favorite);
            log.info("[FAVORITE] User id={} đã lưu homestay id={} vào danh sách yêu thích", userId, homestayId);
            return true; // Da them vao yeu thich
        }
    }

    @Override
    public boolean isFavorite(Long userId, Long homestayId) {
        if (userId == null || homestayId == null) return false;
        return favoriteRepository.existsByUserIdAndHomestayId(userId, homestayId);
    }

    @Override
    public List<Homestay> getFavoriteHomestays(Long userId) {
        return favoriteRepository.findByUserIdOrderByIdDesc(userId).stream()
                .map(Favorite::getHomestay)
                .toList();
    }
}
