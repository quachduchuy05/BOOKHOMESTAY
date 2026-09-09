package BookingHomeStay.BookingHomeStay.service;

import BookingHomeStay.BookingHomeStay.entity.Homestay;

import java.util.List;

public interface FavoriteService {
    boolean toggleFavorite(Long userId, Long homestayId);

    boolean isFavorite(Long userId, Long homestayId);

    List<Homestay> getFavoriteHomestays(Long userId);
}
