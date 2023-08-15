package bogen.studio.Room.Service;

import bogen.studio.Room.DTO.BoomData;
import bogen.studio.Room.Models.Boom;
import bogen.studio.Room.Models.PaginatedResponse;
import bogen.studio.Room.Repository.BoomRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static bogen.studio.Room.Utility.StaticValues.JSON_NOT_VALID_PARAMS;
import static bogen.studio.Room.Utility.Utility.generateSuccessMsg;

@Service
public class BoomService extends AbstractService<Boom, BoomData> {

    @Autowired
    private BoomRepository boomRepository;

    @Override
    public PaginatedResponse<Boom> list(List<String> filters) {
        return null;
    }

    @Override
    public String update(ObjectId id, Object userId, BoomData dto) {
        return null;
    }

    @Override
    public String store(BoomData dto, Object... additionalFields) {

        Boom boom = populateEntity(new Boom(), dto);
        if(boom == null)
            return JSON_NOT_VALID_PARAMS;

        boomRepository.insert(boom);

        return generateSuccessMsg("data", boom.get_id());
    }

    @Override
    Boom findById(ObjectId id) {
        return null;
    }
}
