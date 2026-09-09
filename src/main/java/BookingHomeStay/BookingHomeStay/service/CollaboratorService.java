package BookingHomeStay.BookingHomeStay.service;

import BookingHomeStay.BookingHomeStay.entity.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CollaboratorService {

    Collaborator getMyCollaborator(Long userId);

    Map<String, Object> getDashboard(Long userId);

    List<Homestay> getShareableHomestays();

    void recordClick(String code, String homestaySlug);

    List<CollaboratorCommission> getMyCommissions(Long userId);

    List<CollaboratorWithdrawal> getMyWithdrawals(Long userId);

    CollaboratorWithdrawal requestWithdrawal(Long userId, BigDecimal amount);

    List<CollaboratorWithdrawal> getPendingWithdrawals();

    void approveWithdrawal(Long withdrawalId);

    void rejectWithdrawal(Long withdrawalId, String note);

    void markWithdrawalPaid(Long withdrawalId);

    void createCommissionForBooking(Booking booking);

    BigDecimal getAvailableBalance(Long userId);

    void updateBankInfo(Long userId, String tenNganHang, String soTaiKhoanNhanHoaHong);
}
