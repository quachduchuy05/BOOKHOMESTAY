package BookingHomeStay.BookingHomeStay.component;

import BookingHomeStay.BookingHomeStay.dto.ai.BookingFilterExtracted;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class BookingSessionStore {

    // session ID -> Accumulated Filters
    private final ConcurrentHashMap<String, BookingFilterExtracted> store = new ConcurrentHashMap<>();

    public BookingFilterExtracted getSession(String sessionId) {
        return store.getOrDefault(sessionId, new BookingFilterExtracted());
    }

    public void updateSession(String sessionId, BookingFilterExtracted filters) {
        store.put(sessionId, filters);
    }

    public void clearSession(String sessionId) {
        store.remove(sessionId);
    }
}
