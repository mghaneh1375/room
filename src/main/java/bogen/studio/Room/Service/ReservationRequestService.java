package bogen.studio.Room.Service;

import bogen.studio.Room.DTO.ReservationRequestDTO;
import bogen.studio.Room.Models.PaginatedResponse;
import bogen.studio.Room.Models.ReservationRequests;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ReservationRequestService extends AbstractService<ReservationRequests, ReservationRequestDTO> {

    @Autowired
    private ReservationRequests reservationRequests;

    @Override
    PaginatedResponse<ReservationRequests> list(List<String> filters) {
        return null;
    }

    @Override
    public String update(ObjectId id, Object userId, ReservationRequestDTO dto) {
        return null;
    }

    @Override
    public String store(ReservationRequestDTO dto, Object... additionalFields) {
        return null;
    }

    @Override
    ReservationRequests findById(ObjectId id) {
        return null;
    }

}
