package BookingHomeStay.BookingHomeStay.service;

import BookingHomeStay.BookingHomeStay.entity.Favorite;
import BookingHomeStay.BookingHomeStay.entity.Homestay;
import BookingHomeStay.BookingHomeStay.entity.User;
import BookingHomeStay.BookingHomeStay.repository.FavoriteRepository;
import BookingHomeStay.BookingHomeStay.repository.HomestayRepository;
import BookingHomeStay.BookingHomeStay.repository.UserRepository;
import BookingHomeStay.BookingHomeStay.service.impl.FavoriteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HomestayRepository homestayRepository;

    @InjectMocks
    private FavoriteServiceImpl favoriteService;

    private User user;
    private Homestay homestay;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).fullName("Nguyễn Khách").build();
        homestay = Homestay.builder().id(10L).name("Homestay View Hồ").build();
    }

    @Test
    void testToggleFavoriteAddWhenNotExists() {
        when(favoriteRepository.findByUserIdAndHomestayId(1L, 10L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(homestayRepository.findById(10L)).thenReturn(Optional.of(homestay));

        boolean result = favoriteService.toggleFavorite(1L, 10L);

        assertTrue(result, "Should return true when newly favorited");
        verify(favoriteRepository, times(1)).save(any(Favorite.class));
        verify(favoriteRepository, never()).delete(any());
    }

    @Test
    void testToggleFavoriteRemoveWhenExists() {
        Favorite existing = Favorite.builder().id(99L).user(user).homestay(homestay).build();
        when(favoriteRepository.findByUserIdAndHomestayId(1L, 10L)).thenReturn(Optional.of(existing));

        boolean result = favoriteService.toggleFavorite(1L, 10L);

        assertFalse(result, "Should return false when un-favorited");
        verify(favoriteRepository, times(1)).delete(existing);
        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void testIsFavorite() {
        when(favoriteRepository.existsByUserIdAndHomestayId(1L, 10L)).thenReturn(true);
        when(favoriteRepository.existsByUserIdAndHomestayId(1L, 20L)).thenReturn(false);

        assertTrue(favoriteService.isFavorite(1L, 10L));
        assertFalse(favoriteService.isFavorite(1L, 20L));
        assertFalse(favoriteService.isFavorite(null, 10L));
    }

    @Test
    void testGetFavoriteHomestays() {
        Favorite f1 = Favorite.builder().id(1L).user(user).homestay(homestay).build();
        when(favoriteRepository.findByUserIdOrderByIdDesc(1L)).thenReturn(List.of(f1));

        List<Homestay> favorites = favoriteService.getFavoriteHomestays(1L);

        assertEquals(1, favorites.size());
        assertEquals("Homestay View Hồ", favorites.get(0).getName());
    }
}
