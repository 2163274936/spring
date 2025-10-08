package top.villion.www;

import java.util.List;
import java.util.Optional;

public interface RoomService {
    Room createRoom(Room room, Long userId);
    List<Room> getAllRooms();
    Optional<Room> getRoomById(Long id);
    Room joinRoom(Long roomId, Long userId);
    Room leaveRoom(Long roomId, Long userId);
}
