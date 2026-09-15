package BookingHomeStay.BookingHomeStay.repository.specification;

import BookingHomeStay.BookingHomeStay.dto.ai.BookingFilterExtracted;
import BookingHomeStay.BookingHomeStay.entity.*;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class HomestayRoomSpecification {

    public static Specification<Room> buildSpecification(BookingFilterExtracted filters, List<String> relaxedFields) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Room must be active
            predicates.add(cb.equal(root.get("status"), RoomStatus.ACTIVE));

            // Join Homestay
            Join<Room, Homestay> homestayJoin = root.join("homestay");
            predicates.add(cb.equal(homestayJoin.get("status"), HomestayStatus.ACTIVE));

            // Flexible Location Search
            boolean hasProvinces = filters.getProvinces() != null && !filters.getProvinces().isEmpty();
            boolean hasDistricts = filters.getDistricts() != null && !filters.getDistricts().isEmpty();
            
            if (hasProvinces || hasDistricts) {
                List<Predicate> locPreds = new ArrayList<>();
                if (hasProvinces) {
                    for (String province : filters.getProvinces()) {
                        String p = "%" + province.toLowerCase() + "%";
                        locPreds.add(cb.like(cb.lower(homestayJoin.get("province")), p));
                        locPreds.add(cb.like(cb.lower(homestayJoin.get("district")), p));
                    }
                }
                if (hasDistricts) {
                    for (String district : filters.getDistricts()) {
                        String d = "%" + district.toLowerCase() + "%";
                        locPreds.add(cb.like(cb.lower(homestayJoin.get("province")), d));
                        locPreds.add(cb.like(cb.lower(homestayJoin.get("district")), d));
                    }
                }
                predicates.add(cb.or(locPreds.toArray(new Predicate[0])));
            }

            // Guest count is evaluated in Java post-processing to support multi-room bookings
            // We just ensure we don't filter out small rooms here.

            // Price Min/Max (ignore if relaxed)
            if (!relaxedFields.contains("price")) {
                if (filters.getPriceMin() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("pricePerNight"), filters.getPriceMin()));
                }
                if (filters.getPriceMax() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("pricePerNight"), filters.getPriceMax()));
                }
            }

            // Amenities (ignore if relaxed)
            if (!relaxedFields.contains("amenities") && filters.getAmenityGroups() != null && !filters.getAmenityGroups().isEmpty()) {
                for (List<String> amenityGroup : filters.getAmenityGroups()) {
                    if (amenityGroup.isEmpty()) continue;
                    
                    // For each required amenity GROUP, ensure there is a join that matches AT LEAST ONE synonym
                    Subquery<Long> amenitySubquery = query.subquery(Long.class);
                    Root<Homestay> subHomestay = amenitySubquery.from(Homestay.class);
                    Join<Homestay, Amenity> subAmenityJoin = subHomestay.join("amenities");
                    amenitySubquery.select(subHomestay.get("id"));
                    
                    List<Predicate> synonymPreds = new ArrayList<>();
                    for (String synonym : amenityGroup) {
                        synonymPreds.add(cb.like(cb.lower(subAmenityJoin.get("name")), "%" + synonym.toLowerCase() + "%"));
                    }
                    
                    amenitySubquery.where(
                        cb.equal(subHomestay.get("id"), homestayJoin.get("id")),
                        cb.or(synonymPreds.toArray(new Predicate[0]))
                    );
                    predicates.add(cb.exists(amenitySubquery));
                }
            }

            // Date overlap
            if (filters.getCheckInDate() != null && filters.getCheckOutDate() != null) {
                Subquery<Long> overlapSubquery = query.subquery(Long.class);
                Root<BookingDetail> bdRoot = overlapSubquery.from(BookingDetail.class);
                Join<BookingDetail, Booking> bookingJoin = bdRoot.join("booking");

                overlapSubquery.select(bdRoot.get("id"));
                
                // Conditions for overlap:
                // bd.checkin < filters.checkOut AND bd.checkout > filters.checkIn
                Predicate dateOverlap = cb.and(
                    cb.lessThan(bdRoot.get("checkinDate"), filters.getCheckOutDate()),
                    cb.greaterThan(bdRoot.get("checkoutDate"), filters.getCheckInDate())
                );
                
                // Booking status is NOT cancelled/rejected/pending_refund
                CriteriaBuilder.In<BookingStatus> statusNotIn = cb.in(bookingJoin.get("status"));
                statusNotIn.value(BookingStatus.CANCELLED);
                statusNotIn.value(BookingStatus.REJECTED);
                statusNotIn.value(BookingStatus.PENDING_REFUND);
                
                Predicate statusActive = cb.not(statusNotIn);

                // Room matches
                Predicate roomMatches = cb.equal(bdRoot.get("room").get("id"), root.get("id"));

                overlapSubquery.where(dateOverlap, statusActive, roomMatches);

                // If overlap exists, the room is NOT available. So we need NOT EXISTS
                predicates.add(cb.not(cb.exists(overlapSubquery)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
