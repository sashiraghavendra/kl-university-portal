package com.kluniversity.repository;

import com.kluniversity.entity.HostelRoom;
import com.kluniversity.entity.RoomAvailabilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HostelRoomRepository extends JpaRepository<HostelRoom, Long> {
    List<HostelRoom> findByAvailabilityStatus(RoomAvailabilityStatus status);
    List<HostelRoom> findByHostelHostelIdAndAvailabilityStatus(Long hostelId, RoomAvailabilityStatus status);
    List<HostelRoom> findByHostelHostelIdAndRoomType(Long hostelId, String roomType);
    long countByHostelHostelIdAndRoomType(Long hostelId, String roomType);
    List<HostelRoom> findByHostelHostelIdAndRoomTypeAndAvailabilityStatus(
            Long hostelId, String roomType, RoomAvailabilityStatus status);
    List<HostelRoom> findByHostelHostelIdAndRoomTypeAndSharingTypeAndAvailabilityStatus(
            Long hostelId, String roomType, String sharingType, RoomAvailabilityStatus status);
}
