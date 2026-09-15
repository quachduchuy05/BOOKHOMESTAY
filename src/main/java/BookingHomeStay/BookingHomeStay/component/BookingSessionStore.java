package BookingHomeStay.BookingHomeStay.component;

import BookingHomeStay.BookingHomeStay.dto.ai.BookingFilterExtracted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BookingSessionStore {

    private static final Logger log = LoggerFactory.getLogger(BookingSessionStore.class);

    private static class SessionEntry {
        private final BookingFilterExtracted filter;
        private volatile Instant lastAccessed;

        SessionEntry(BookingFilterExtracted filter) {
            this.filter = filter;
            this.lastAccessed = Instant.now();
        }

        public BookingFilterExtracted getFilter() {
            this.lastAccessed = Instant.now();
            return filter;
        }

        public Instant getLastAccessed() {
            return lastAccessed;
        }
    }

    // session ID -> SessionEntry with TTL tracking
    private final ConcurrentHashMap<String, SessionEntry> store = new ConcurrentHashMap<>();

    public BookingFilterExtracted getSession(String sessionId) {
        SessionEntry entry = store.get(sessionId);
        if (entry == null) {
            return new BookingFilterExtracted();
        }
        return entry.getFilter();
    }

    public void updateSession(String sessionId, BookingFilterExtracted filters) {
        store.put(sessionId, new SessionEntry(filters));
    }

    public void clearSession(String sessionId) {
        store.remove(sessionId);
    }

    /**
     * Tự động dọn dẹp các session không hoạt động quá 30 phút để chống tràn RAM (Memory Leak)
     * Chạy định kỳ mỗi 10 phút một lần
     */
    @Scheduled(fixedRate = 600000)
    public void cleanupExpiredSessions() {
        Instant expiryThreshold = Instant.now().minusSeconds(1800);
        int initialSize = store.size();
        store.entrySet().removeIf(e -> e.getValue().getLastAccessed().isBefore(expiryThreshold));
        int removedCount = initialSize - store.size();
        if (removedCount > 0) {
            log.info("BookingSessionStore: Đã dọn dẹp {} session AI hết hạn. Hiện còn {} session.", removedCount, store.size());
        }
    }
}
