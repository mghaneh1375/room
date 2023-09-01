package bogen.studio.Room.Service;

import bogen.studio.Room.Models.PaginatedResponse;
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

    public String authorizedList(List<String> filters) {
        return null;
    }

    abstract String list(List<String> filters);

    public abstract String update(ObjectId id, ObjectId userId, D dto);

    public abstract String store(D dto, Object ...additionalFields);

    T populateEntity(T t, D d) {
        try {
            return (T) objectMapper.readValue(new JSONObject(d).toString(), t.getClass());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    abstract T findById(ObjectId id);

}
