package BookingHomeStay.BookingHomeStay.service.impl;

import BookingHomeStay.BookingHomeStay.client.GeminiBookingClient;
import BookingHomeStay.BookingHomeStay.component.BookingSessionStore;
import BookingHomeStay.BookingHomeStay.dto.ai.BookingChatRequest;
import BookingHomeStay.BookingHomeStay.dto.ai.BookingFilterExtracted;
import BookingHomeStay.BookingHomeStay.dto.ai.BookingSuggestionResponse;
import BookingHomeStay.BookingHomeStay.dto.ai.RoomSuggestionDto;
import BookingHomeStay.BookingHomeStay.entity.Amenity;
import BookingHomeStay.BookingHomeStay.entity.BookingDetail;
import BookingHomeStay.BookingHomeStay.entity.Room;
import BookingHomeStay.BookingHomeStay.repository.BookingDetailRepository;
import BookingHomeStay.BookingHomeStay.repository.RoomRepository;
import BookingHomeStay.BookingHomeStay.repository.specification.HomestayRoomSpecification;
import BookingHomeStay.BookingHomeStay.service.AiBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class AiBookingServiceImpl implements AiBookingService {

    private final GeminiBookingClient geminiBookingClient;
    private final BookingSessionStore sessionStore;
    private final RoomRepository roomRepository;
    private final BookingDetailRepository bookingDetailRepository;

    @Override
    public BookingSuggestionResponse processChat(BookingChatRequest request) {
        // 1. Get current session
        BookingFilterExtracted currentFilters = sessionStore.getSession(request.getSessionId());

        // 2. Extract new filters from message
        BookingFilterExtracted newFilters = geminiBookingClient.extractFilters(request.getMessage());
        log.info("Gemini Extracted Filters: provinces={}, districts={}, guests={}, rooms={}, checkIn={}, checkOut={}, amenities={}", 
                 newFilters.getProvinces(), newFilters.getDistricts(), newFilters.getGuestCount(), newFilters.getRoomCount(),
                 newFilters.getCheckInDate(), newFilters.getCheckOutDate(), newFilters.getAmenityGroups());

        // 3. Merge
        currentFilters.mergeWith(newFilters);
        sessionStore.updateSession(request.getSessionId(), currentFilters);

        BookingSuggestionResponse response = new BookingSuggestionResponse();
        response.setCurrentFilters(currentFilters);
        response.setRelaxedFields(new ArrayList<>());
        response.setSuggestions(new ArrayList<>());

        boolean hasLocation = (currentFilters.getProvinces() != null && !currentFilters.getProvinces().isEmpty()) || 
                              (currentFilters.getDistricts() != null && !currentFilters.getDistricts().isEmpty());
        boolean hasGuest = currentFilters.getGuestCount() != null;
        if (!hasLocation || !hasGuest) {
            if (newFilters.getExplanation() != null && !newFilters.getExplanation().isBlank()) {
                response.setSystemPrompt(newFilters.getExplanation());
            } else {
                StringBuilder prompt = new StringBuilder("Dạ, để tìm phòng chuẩn nhất, bạn bổ sung giúp mình: ");
                if (!hasLocation) prompt.append("Khu vực bạn muốn đến? ");
                if (!hasGuest) prompt.append("Đoàn mình đi bao nhiêu người? ");
                response.setSystemPrompt(prompt.toString().trim());
            }
            return response;
        }
        
        // Default room count to 1 if not specified
        if (currentFilters.getRoomCount() == null) {
            currentFilters.setRoomCount(1);
        }

        // 5. Search with relaxation strategy
        List<Room> foundRooms = searchRooms(currentFilters, response.getRelaxedFields());
        
        // Post-filter: Group by homestay to support multi-room bookings for large groups
        java.util.Map<Long, List<Room>> roomsByHomestay = foundRooms.stream()
            .collect(Collectors.groupingBy(r -> r.getHomestay().getId()));
        
        List<Room> validRooms = new ArrayList<>();
        java.util.Map<Long, Integer> recommendedRoomCountForHomestay = new java.util.HashMap<>();
        
        int guests = currentFilters.getGuestCount() != null ? currentFilters.getGuestCount() : 1;
        int requestedRooms = currentFilters.getRoomCount() != null ? currentFilters.getRoomCount() : 1;

        for (List<Room> hRooms : roomsByHomestay.values()) {
            Room bestRoom = null;
            int bestRoomsNeeded = 0;
            int minDiff = Integer.MAX_VALUE;
            
            for (Room r : hRooms) {
                int roomsNeeded = (int) Math.ceil((double) guests / r.getMaxGuests());
                roomsNeeded = Math.max(roomsNeeded, requestedRooms);
                
                int totalCapacity = roomsNeeded * r.getMaxGuests();
                int diff = totalCapacity - guests;
                
                boolean isBetter = false;
                if (bestRoom == null) {
                    isBetter = true;
                } else if (roomsNeeded < bestRoomsNeeded) {
                    // Ưu tiên số lượng phòng ít nhất (ví dụ 2 phòng 10 người thay vì 9 phòng 2 người)
                    isBetter = true;
                } else if (roomsNeeded == bestRoomsNeeded && diff < minDiff) {
                    // Nếu số lượng phòng bằng nhau, ưu tiên loại phòng có sức chứa sát với số lượng khách nhất
                    isBetter = true;
                }

                if (isBetter) {
                    minDiff = diff;
                    bestRoom = r;
                    bestRoomsNeeded = roomsNeeded;
                }
            }
            
            if (bestRoom != null) {
                validRooms.add(bestRoom);
                recommendedRoomCountForHomestay.put(bestRoom.getHomestay().getId(), bestRoomsNeeded);
            }
        }
        
        foundRooms = validRooms.stream().limit(5).collect(Collectors.toList());

        String prefix = (newFilters.getExplanation() != null && !newFilters.getExplanation().isBlank()) ? newFilters.getExplanation() + " " : "";
        if (foundRooms.isEmpty()) {
            response.setSystemPrompt(prefix + "Hiện chưa có phòng phù hợp ở khu vực này.");
        } else {
            if (response.getRelaxedFields().isEmpty()) {
                response.setSystemPrompt(prefix + "Tuyệt vời! Mình tìm thấy " + foundRooms.size() + " phòng thỏa mãn 100% yêu cầu của bạn. Xem chi tiết bên dưới nhé!");
            } else {
                response.setSystemPrompt(prefix + "Mình tìm thấy " + foundRooms.size() + " phòng khá phù hợp. Tuy có vài tiêu chí phải nới lỏng để tìm được phòng, nhưng đây là những gợi ý tốt nhất cho bạn!");
            }
            
            // 6. Build suggestions
            List<RoomSuggestionDto> dtos = foundRooms.stream().map(room -> {
                RoomSuggestionDto dto = new RoomSuggestionDto();
                dto.setHomestayId(room.getHomestay().getId());
                dto.setRoomId(room.getId());
                dto.setHomestayName(room.getHomestay().getName());
                dto.setRoomName(room.getName());
                dto.setPrice(room.getPricePerNight());
                dto.setCheckInDate(currentFilters.getCheckInDate());
                dto.setCheckOutDate(currentFilters.getCheckOutDate());
                dto.setGuestCount(currentFilters.getGuestCount());
                
                int recommendedRooms = recommendedRoomCountForHomestay.getOrDefault(room.getHomestay().getId(), 1);
                dto.setRoomCount(recommendedRooms);
                
                // Matched amenities
                List<String> roomAmenities = room.getHomestay().getAmenities().stream()
                        .map(Amenity::getName).toList();
                List<String> matched = new ArrayList<>();
                if (currentFilters.getAmenityGroups() != null && !currentFilters.getAmenityGroups().isEmpty()) {
                    for (List<String> group : currentFilters.getAmenityGroups()) {
                        // Find which synonym matched
                        for (String synonym : group) {
                            if (roomAmenities.stream().anyMatch(a -> a.toLowerCase().contains(synonym.toLowerCase()))) {
                                matched.add(synonym);
                                break; // Only add one synonym per group
                            }
                        }
                    }
                    dto.setMatchedAmenities(matched);
                } else {
                    dto.setMatchedAmenities(matched);
                }

                return dto;
            }).collect(Collectors.toList());
            response.setSuggestions(dtos);
        }

        return response;
    }

    @Override
    public BookingSuggestionResponse processManualQuery(String sessionId, BookingFilterExtracted manualFilters) {
        // Update session
        sessionStore.updateSession(sessionId, manualFilters);
        
        BookingSuggestionResponse response = new BookingSuggestionResponse();
        response.setCurrentFilters(manualFilters);
        response.setRelaxedFields(new ArrayList<>());
        response.setSuggestions(new ArrayList<>());
        
        // Search with relaxation strategy
        List<Room> foundRooms = searchRooms(manualFilters, response.getRelaxedFields());
        
        // Group and limit
        java.util.Map<Long, List<Room>> roomsByHomestay = foundRooms.stream()
            .collect(Collectors.groupingBy(r -> r.getHomestay().getId()));
        
        List<Room> validRooms = new ArrayList<>();
        java.util.Map<Long, Integer> recommendedRoomCountForHomestay = new java.util.HashMap<>();
        
        int guests = manualFilters.getGuestCount() != null ? manualFilters.getGuestCount() : 1;
        int requestedRooms = manualFilters.getRoomCount() != null ? manualFilters.getRoomCount() : 1;

        for (List<Room> hRooms : roomsByHomestay.values()) {
            Room bestRoom = null;
            int bestRoomsNeeded = 0;
            int minDiff = Integer.MAX_VALUE;
            
            for (Room r : hRooms) {
                int roomsNeeded = (int) Math.ceil((double) guests / r.getMaxGuests());
                roomsNeeded = Math.max(roomsNeeded, requestedRooms);
                
                int totalCapacity = roomsNeeded * r.getMaxGuests();
                int diff = totalCapacity - guests;
                
                boolean isBetter = false;
                if (bestRoom == null) {
                    isBetter = true;
                } else if (roomsNeeded < bestRoomsNeeded) {
                    isBetter = true;
                } else if (roomsNeeded == bestRoomsNeeded && diff < minDiff) {
                    isBetter = true;
                }

                if (isBetter) {
                    minDiff = diff;
                    bestRoom = r;
                    bestRoomsNeeded = roomsNeeded;
                }
            }
            
            if (bestRoom != null) {
                validRooms.add(bestRoom);
                recommendedRoomCountForHomestay.put(bestRoom.getHomestay().getId(), bestRoomsNeeded);
            }
        }
        
        foundRooms = validRooms.stream().limit(5).collect(Collectors.toList());
        
        if (foundRooms.isEmpty()) {
            response.setSystemPrompt("Hiện chưa có phòng phù hợp với bộ lọc tùy chỉnh của bạn.");
        } else {
            response.setSystemPrompt("Đây là các gợi ý phòng dựa trên bộ lọc tùy chỉnh của bạn.");
            
            List<RoomSuggestionDto> dtos = foundRooms.stream().map(room -> {
                RoomSuggestionDto dto = new RoomSuggestionDto();
                dto.setHomestayId(room.getHomestay().getId());
                dto.setRoomId(room.getId());
                dto.setHomestayName(room.getHomestay().getName());
                dto.setRoomName(room.getName());
                dto.setPrice(room.getPricePerNight());
                dto.setCheckInDate(manualFilters.getCheckInDate());
                dto.setCheckOutDate(manualFilters.getCheckOutDate());
                dto.setGuestCount(manualFilters.getGuestCount());
                
                int recommendedRooms = recommendedRoomCountForHomestay.getOrDefault(room.getHomestay().getId(), 1);
                dto.setRoomCount(recommendedRooms);
                
                List<String> roomAmenities = room.getHomestay().getAmenities().stream()
                        .map(Amenity::getName).toList();
                List<String> matched = new ArrayList<>();
                if (manualFilters.getAmenityGroups() != null && !manualFilters.getAmenityGroups().isEmpty()) {
                    for (List<String> group : manualFilters.getAmenityGroups()) {
                        for (String synonym : group) {
                            if (roomAmenities.stream().anyMatch(a -> a.toLowerCase().contains(synonym.toLowerCase()))) {
                                matched.add(synonym);
                                break;
                            }
                        }
                    }
                }
                dto.setMatchedAmenities(matched);
                return dto;
            }).collect(Collectors.toList());
            response.setSuggestions(dtos);
        }
        return response;
    }

    private List<Room> searchRooms(BookingFilterExtracted filters, List<String> relaxedFields) {
        // Try strict
        Specification<Room> spec = HomestayRoomSpecification.buildSpecification(filters, relaxedFields);
        List<Room> rooms = roomRepository.findAll(spec);
        if (!rooms.isEmpty()) return rooms;

        // Relax Price
        if (filters.getPriceMin() != null || filters.getPriceMax() != null) {
            relaxedFields.add("price");
            spec = HomestayRoomSpecification.buildSpecification(filters, relaxedFields);
            rooms = roomRepository.findAll(spec);
            if (!rooms.isEmpty()) return rooms;
        }

        // Relax Amenities
        if (filters.getAmenityGroups() != null && !filters.getAmenityGroups().isEmpty()) {
            relaxedFields.add("amenities");
            spec = HomestayRoomSpecification.buildSpecification(filters, relaxedFields);
            rooms = roomRepository.findAll(spec);
        }

        return rooms;
    }

    @Override
    public boolean checkRoomAvailability(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        if (checkInDate == null || checkOutDate == null) {
            return true; // Cannot check without dates, assume true to let them proceed to checkout where real validation happens
        }
        List<BookingDetail> overlaps = bookingDetailRepository.findOverlapping(roomId, checkInDate, checkOutDate);
        return overlaps.isEmpty();
    }
}
