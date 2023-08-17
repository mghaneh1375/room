package bogen.studio.Room.Service;

import bogen.studio.Room.Models.PaginatedResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

import static bogen.studio.Room.Utility.Utility.generateSuccessMsg;

public abstract class AbstractService <T, D> {

    @Autowired
    ObjectMapper objectMapper;

    public PaginatedResponse<T> returnPaginateResponse(Page<T> all) {
        return PaginatedResponse.<T>builder()
                .currentPage(all.getNumber())
                .totalItems(all.getTotalElements())
                .totalPages(all.getTotalPages())
                .items(all.getContent())
                .hasNext(all.hasNext())
                .build();
    }

    public String authorizedList(List<String> filters) {
        return null;
    }

    abstract PaginatedResponse<T> list(List<String> filters);

    public abstract String update(ObjectId id, Object userId, D dto);

    public abstract String store(D dto, Object ...additionalFields);

    T populateEntity(T t, D d) {
        try {
            return (T) objectMapper.readValue(new JSONObject(d).toString(), t.getClass());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    abstract T findById(ObjectId id);

    public String toJSON(List<Object> objects) {

        JSONArray jsonArray = new JSONArray();

        for(Object o : objects) {
//            jsonArray.put(new JSONObject(new Gson().toJson(o)));
            jsonArray.put(new Gson().toJson(o));
        }

        return generateSuccessMsg("data", jsonArray);
    }

}
