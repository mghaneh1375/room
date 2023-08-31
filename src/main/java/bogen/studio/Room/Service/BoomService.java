package bogen.studio.Room.Service;

import bogen.studio.Room.DTO.BoomDTO;
import bogen.studio.Room.Models.Boom;
import bogen.studio.Room.Network.Network;
import bogen.studio.Room.Repository.BoomRepository;
import bogen.studio.Room.Repository.RoomRepository;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static bogen.studio.Room.Utility.StaticValues.*;
import static bogen.studio.Room.Utility.Utility.generateErr;
import static bogen.studio.Room.Utility.Utility.generateSuccessMsg;

@Service
public class BoomService extends AbstractService<Boom, BoomDTO> {

    @Autowired
    private BoomRepository boomRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Override
    public String list(List<String> filters) {

        int userId = Integer.parseInt(filters.get(0));
        List<Boom> all = boomRepository.findByUserIdIncludeEmbeddedFields(userId);

        JSONArray jsonArray = new JSONArray();

        for(Boom boom : all)
            jsonArray.put(boom.getPlaceId().toString());

        // todo: cache res
        JSONObject res = Network.sendPostReq("place/getSpecificPlaces",
                new JSONObject().put("ids", jsonArray)
        );

        if(res == null)
            return generateErr("خطایی در ارتباط بین سیستم ها رخ داده است (خطا ۱۰۱)");

        JSONArray places = res.getJSONArray("data");
        if(places.length() != all.size())
            return generateErr("خطایی در ارتباط بین سیستم ها رخ داده است (خطا ۱۰۲)");

        return generateSuccessMsg("data", all.stream()
                .map(x -> {

                            JSONObject place = null;
                            String id = x.get_id().toString();
                            String placeId = x.getPlaceId().toString();

                            for(int i = 0; i < places.length(); i++) {

                                if(places.getJSONObject(i).getString("id").equals(placeId)) {
                                    place = places.getJSONObject(i);
                                    break;
                                }

                            }

                            return new JSONObject()
                                    .put("id", id)
                                    .put("created_at", x.getCreatedAt().toString())
                                    .put("place", place)
                                    .put("rooms", roomRepository.countRoomByBoomId(x.get_id()))
                                    .put("availability", x.isAvailability());
                        }
                ).collect(Collectors.toList())
        );
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
        boomRepository.saveAll(Collections.singleton(boom));

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
