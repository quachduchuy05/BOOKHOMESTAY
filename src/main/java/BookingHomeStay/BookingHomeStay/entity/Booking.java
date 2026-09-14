package BookingHomeStay.BookingHomeStay.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String bookingCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private BookingHomeStay.BookingHomeStay.entity.User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private BookingHomeStay.BookingHomeStay.entity.Promotion promotion;

    @Column(nullable = false, length = 100)
    private String customerName;

    @Column(nullable = false, length = 20)
    private String customerPhone;

    @Column(nullable = false, length = 100)
    private String customerEmail;

    @Column(nullable = false, precision = 13, scale = 2)
    private BigDecimal totalAmount;

    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal serviceFee = BigDecimal.ZERO;

    @Column(nullable = false, precision = 13, scale = 2)
    private BigDecimal finalAmount;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingHomeStay.BookingHomeStay.entity.PaymentPolicy paymentPolicy = BookingHomeStay.BookingHomeStay.entity.PaymentPolicy.PAY_AT_PROPERTY;

    @Column(nullable = false, precision = 13, scale = 2)
    @Builder.Default
    private BigDecimal requiredDeposit = BigDecimal.ZERO;

    @Column(nullable = false, precision = 13, scale = 2)
    @Builder.Default
    private BigDecimal remainingBalance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BookingHomeStay.BookingHomeStay.entity.BookingStatus status = BookingHomeStay.BookingHomeStay.entity.BookingStatus.PENDING;

    @Builder.Default
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingHomeStay.BookingHomeStay.entity.BookingDetail> details = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingHomeStay.BookingHomeStay.entity.Payment> payments = new ArrayList<>();

    public boolean isFullyPaid() {
        if (payments == null || payments.isEmpty()) return false;
        BigDecimal paidAmount = payments.stream()
                .filter(p -> p.getStatus() == BookingHomeStay.BookingHomeStay.entity.PaymentStatus.PAID)
                .map(BookingHomeStay.BookingHomeStay.entity.Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return paidAmount.compareTo(finalAmount) >= 0;
    }

    public boolean hasPaid() {
        if (payments == null || payments.isEmpty()) return false;
        return payments.stream()
                .anyMatch(p -> p.getStatus() == BookingHomeStay.BookingHomeStay.entity.PaymentStatus.PAID);
    }

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Thời hạn thanh toán (giữ phòng 2 tiếng). Áp dụng cho đơn PENDING_PAYMENT.
     */
    private LocalDateTime expiredAt;

    // ===== TASK 4: nguon goc don hang, dung ve bieu do tron o Dashboard Admin
    // =====
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BookingHomeStay.BookingHomeStay.entity.BookingSource source = BookingHomeStay.BookingHomeStay.entity.BookingSource.DIRECT;

    // Ma gioi thieu CTV da dung khi dat don nay (null neu dat truc tiep).
    // Xem Collaborator.maGioiThieu - moi CTV co 1 ma rieng do Admin cap khi duyet.
    private String collaboratorCode;
}