package bogen.studio.Room.Service;

import bogen.studio.Room.Models.Boom;
import bogen.studio.Room.Models.PaginatedResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

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

    abstract PaginatedResponse<T> list(List<String> filters);

    public abstract String update(ObjectId id, Object userId, D dto);

    public abstract String store(D dto, Object ...additionalFields);

    T populateEntity(T t, D d) {
        try {
            String json = new JSONObject(d).toString();
            System.out.println(json);
            return (T) objectMapper.readValue(json, t.getClass());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    abstract T findById(ObjectId id);

}
