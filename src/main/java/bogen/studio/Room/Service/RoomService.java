package bogen.studio.Room.Service;

import bogen.studio.Room.DTO.RoomData;
import bogen.studio.Room.Enums.Limitation;
import bogen.studio.Room.Models.PaginatedResponse;
import bogen.studio.Room.Models.Room;
import bogen.studio.Room.Repository.FilteringFactory;
import bogen.studio.Room.Repository.RoomRepository;
import bogen.studio.Room.Utility.FileUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static bogen.studio.Room.Utility.StaticValues.*;
import static bogen.studio.Room.Utility.Utility.generateSuccessMsg;

@Service
public class RoomService extends AbstractService<Room, RoomData> {

    private final static String FOLDER = "rooms";

    @Autowired
    private RoomRepository roomRepository;

    @Override
    public PaginatedResponse<Room> list(List<String> filters) {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Room> all = roomRepository.findAllWithFilter(Room.class,
                FilteringFactory.parseFromParams(filters, Room.class), pageable
        );

        return returnPaginateResponse(all);
    }

    @Override
    public String update(ObjectId id, Object userId, RoomData dto) {

        Optional<Room> roomOptional = roomRepository.findById(id);

        Room room = roomOptional.orElse(null);

        if(room == null)
            return JSON_NOT_VALID_ID;

        if(!room.getUserId().equals(userId))
            return JSON_NOT_ACCESS;

        roomRepository.save(populateEntity(room, dto));
        return JSON_OK;
    }

    @Override
    public String store(RoomData dto, Object ...additionalFields) {

        Room room = populateEntity(null, dto);
        if(room == null)
            return JSON_UNKNOWN_UPLOAD_FILE;

        room.setUserId((Integer) additionalFields[0]);
        room.setBoomId((ObjectId) additionalFields[1]);

        String filename = FileUtils.uploadFile((MultipartFile) additionalFields[2], FOLDER);
        if(filename == null)
            return JSON_UNKNOWN_UPLOAD_FILE;

        room.setImage(filename);

        roomRepository.insert(room);

        return generateSuccessMsg("id", room.get_id());
    }

    public String setPic(ObjectId id, MultipartFile file) {

        Optional<Room> roomOptional = roomRepository.findById(id);

        Room room = roomOptional.orElse(null);

        if(room == null)
            return JSON_NOT_VALID_ID;

        String filename = FileUtils.uploadFile(file, FOLDER);
        if(filename == null)
            return JSON_UNKNOWN_UPLOAD_FILE;

        FileUtils.removeFile(room.getImage(), FOLDER);
        room.setImage(filename);
        roomRepository.save(room);

        return JSON_OK;
    }

    @Override
    Room populateEntity(Room room, RoomData roomData) {

        boolean isNew = false;

        if(room == null) {
            room = new Room();
            isNew = true;
        }

        room.setTitle(roomData.getTitle());
        room.setDescription(roomData.getDescription());
        room.setMaxCap(roomData.getMaxCap());

        if(roomData.getLimitations() != null)
            room.setLimitations(roomData.getLimitations().stream()
                    .map(String::toUpperCase)
                    .map(Limitation::valueOf)
                    .collect(Collectors.toList())
            );

        //todo: uniqueness of name in one boomgardy for a person
        if(isNew);

        return room;

    }

    @Override
    public Room findById(ObjectId id) {
        Optional<Room> room = roomRepository.findById(id);
        return room.orElse(null);
    }


    public void remove(ObjectId id) {
        roomRepository.deleteById(id);
    }
}
