package bogen.studio.Room.Service;

import bogen.studio.Room.DTO.BoomDTO;
import bogen.studio.Room.Models.Boom;
import bogen.studio.Room.Models.PaginatedResponse;
import bogen.studio.Room.Models.Room;
import bogen.studio.Room.Network.Network;
import bogen.studio.Room.Repository.BoomRepository;
import bogen.studio.Room.Repository.FilteringFactory;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static bogen.studio.Room.Utility.StaticValues.*;
import static bogen.studio.Room.Utility.Utility.generateErr;
import static bogen.studio.Room.Utility.Utility.generateSuccessMsg;

@Service
public class BoomService extends AbstractService<Boom, BoomDTO> {

    @Autowired
    private BoomRepository boomRepository;

    public String myList(Integer userId) {
        List<Boom> all = boomRepository.findByUserIdIncludeEmbeddedFields(userId);
        return toJSON(Collections.singletonList(all));
    }

    @Override
    public PaginatedResponse<Boom> list(List<String> filters) {

        Pageable pageable = PageRequest.of(0, 10);
        Page<Boom> all = boomRepository.findAllWithFilter(Boom.class,
                FilteringFactory.parseFromParams(filters, Boom.class), pageable
        );

        return returnPaginateResponse(all);

    }

    public String toggleAccessibility(ObjectId id) {

        Optional<Boom> roomOptional = boomRepository.findById(id);

        Boom boom = roomOptional.orElse(null);

        if(boom == null)
            return JSON_NOT_VALID_ID;

        boolean newStatus = !boom.isAvailability();

        JSONObject result = Network.sendPutReq("place/setReservable/" + boom.getPlaceId() + "/" + newStatus);
        if(result == null)
            return generateErr("خطا در اتصال به سیستم کوچیتا");

        if(result.getString("status").equals("nok"))
            return generateErr(result.getString("msg"));

        boom.setAvailability(newStatus);
        boomRepository.save(boom);

        return JSON_OK;
    }

    @Override
    public String update(ObjectId id, Object userId, BoomDTO dto) {
        return null;
    }

    @Override
    public String store(BoomDTO dto, Object... additionalFields) {

        Boom boom = populateEntity(null, dto);
        if(boom == null)
            return JSON_NOT_VALID_PARAMS;

        boomRepository.insert(boom);

        return generateSuccessMsg("data", boom.get_id());
    }

    @Override
    Boom populateEntity(Boom boom, BoomDTO dto) {

        Boom b = new Boom();

        b.setPlaceId(dto.getPlaceId());
        b.setBusinessId(dto.getBusinessId());
        b.setUserId(dto.getUserId());

        return b;
    }

    @Override
    Boom findById(ObjectId id) {
        return null;
    }
}
