package bogen.studio.Room.Service;

import bogen.studio.Room.DTO.DatePrice;
import bogen.studio.Room.DTO.ReservationRequestDTO;
import bogen.studio.Room.DTO.RoomDTO;
import bogen.studio.Room.Enums.*;
import bogen.studio.Room.Exception.InvalidFieldsException;
import bogen.studio.Room.Models.PaginatedResponse;
import bogen.studio.Room.Models.ReservationRequests;
import bogen.studio.Room.Models.Room;
import bogen.studio.Room.Repository.FilteringFactory;
import bogen.studio.Room.Repository.ReservationRequestsRepository;
import bogen.studio.Room.Repository.RoomRepository;
import bogen.studio.Room.Utility.FileUtils;
import bogen.studio.Room.Utility.PairValue;
import bogen.studio.Room.Utility.Utility;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static bogen.studio.Room.Utility.StaticValues.*;
import static bogen.studio.Room.Utility.Utility.generateErr;
import static bogen.studio.Room.Utility.Utility.generateSuccessMsg;

@Service
public class RoomService extends AbstractService<Room, RoomDTO> {

    private final static String FOLDER = "rooms";

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ReservationRequestsRepository reservationRequestsRepository;

    @Override
    public PaginatedResponse<Room> list(List<String> filters) {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Room> all = roomRepository.findAllWithFilter(Room.class,
                FilteringFactory.parseFromParams(filters, Room.class), pageable
        );

        return returnPaginateResponse(all);
    }

    @Override
    public String update(ObjectId id, Object userId, RoomDTO dto) {

        Optional<Room> roomOptional = roomRepository.findById(id);

        Room room = roomOptional.orElse(null);

        if (room == null)
            return JSON_NOT_VALID_ID;

        if (!room.getUserId().equals(userId))
            return JSON_NOT_ACCESS;

        roomRepository.save(populateEntity(room, dto));
        return JSON_OK;
    }

    @Override
    public String store(RoomDTO dto, Object... additionalFields) {

        Room room = populateEntity(null, dto);
        if (room == null)
            return JSON_UNKNOWN_UPLOAD_FILE;

        room.setUserId((Integer) additionalFields[0]);
        room.setBoomId((ObjectId) additionalFields[1]);

        String filename = FileUtils.uploadFile((MultipartFile) additionalFields[2], FOLDER);
        if (filename == null)
            return JSON_UNKNOWN_UPLOAD_FILE;

        room.setImage(filename);

        roomRepository.insert(room);

        return generateSuccessMsg("id", room.get_id());
    }

    public String setPic(ObjectId id, MultipartFile file) {

        Optional<Room> roomOptional = roomRepository.findById(id);

        Room room = roomOptional.orElse(null);

        if (room == null)
            return JSON_NOT_VALID_ID;

        String filename = FileUtils.uploadFile(file, FOLDER);
        if (filename == null)
            return JSON_UNKNOWN_UPLOAD_FILE;

        FileUtils.removeFile(room.getImage(), FOLDER);
        room.setImage(filename);
        roomRepository.save(room);

        return JSON_OK;
    }

    public String addDatePrice(ObjectId id, DatePrice datePrice) {

        Room room = findById(id);
        if (room == null)
            return JSON_NOT_VALID_ID;

        List<DatePrice> datePrices = room.getDatePrices() == null ? new ArrayList<>() : room.getDatePrices();

        int idx = -1;
        int counter = 0;

        for (DatePrice itr : datePrices) {

            if (itr.getDate().equals(datePrice.getDate())) {
                idx = counter;
                break;
            }

            counter++;
        }

        if (idx != -1)
            datePrices.set(idx, datePrice);
        else
            datePrices.add(datePrice);

        room.setDatePrices(datePrices);

        roomRepository.save(room);
        return JSON_OK;
    }

    @Override
    Room populateEntity(Room room, RoomDTO roomDTO) {

        boolean isNew = false;

        if (room == null) {
            room = new Room();
            isNew = true;
        }

        room.setTitle(roomDTO.getTitle());
        room.setDescription(roomDTO.getDescription());

        room.setMaxCap(roomDTO.getMaxCap());
        room.setCap(roomDTO.getCap());

        room.setCapPrice(roomDTO.getCapPrice());
        room.setPrice(roomDTO.getPrice());
        room.setWeekendPrice(roomDTO.getWeekendPrice());
        room.setVacationPrice(roomDTO.getVacationPrice());

        if (roomDTO.getLimitations() != null)
            room.setLimitations(roomDTO.getLimitations().stream()
                    .map(String::toUpperCase)
                    .map(Limitation::valueOf)
                    .collect(Collectors.toList())
            );

        if (roomDTO.getFoodFacilities() != null)
            room.setFoodFacilities(roomDTO.getFoodFacilities().stream()
                    .map(String::toUpperCase)
                    .map(FoodFacility::valueOf)
                    .collect(Collectors.toList())
            );

        if (roomDTO.getWelfares() != null)
            room.setWelfares(roomDTO.getWelfares().stream()
                    .map(String::toUpperCase)
                    .map(Welfare::valueOf)
                    .collect(Collectors.toList())
            );

        if (roomDTO.getSleepFeatures() != null)
            room.setSleepFeatures(roomDTO.getSleepFeatures().stream()
                    .map(String::toUpperCase)
                    .map(SleepFeature::valueOf)
                    .collect(Collectors.toList())
            );

        if (roomDTO.getAdditionalFacilities() != null)
            room.setAdditionalFacilities(roomDTO.getAdditionalFacilities().stream()
                    .map(String::toUpperCase)
                    .map(AdditionalFacility::valueOf)
                    .collect(Collectors.toList())
            );

        if (roomDTO.getAccessibilityFeatures() != null)
            room.setAccessibilityFeatures(roomDTO.getAccessibilityFeatures().stream()
                    .map(String::toUpperCase)
                    .map(AccessibilityFeature::valueOf)
                    .collect(Collectors.toList())
            );

        //todo: uniqueness of name in one boomgardy for a person
        if (isNew) ;

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

    private PairValue canReserve(ObjectId id, ReservationRequestDTO dto) throws InvalidFieldsException {

        Room room = findById(id);
        if (room == null)
            throw new InvalidFieldsException("id is not valid");

        if (!room.isAvailability())
            throw new InvalidFieldsException("has not access");

        if (room.getMaxCap() < dto.getPassengers())
            throw new InvalidFieldsException("حداکثر ظرفیت برای این اتاق " + room.getMaxCap() + " می باشد.");

        List<String> dates = new ArrayList<>();
        dates.add(dto.getStartDate());

        for (int i = 1; i < dto.getNights(); i++)
            dates.add(Utility.getPast("/", dto.getStartDate(), -1 * i));

        if (reservationRequestsRepository.findActiveReservations(room.get_id(), dates).size() > 0)
            throw new InvalidFieldsException("در زمان خواسته شده، اقامتگاه مدنظر پر می باشد.");

        return new PairValue(room, dates);
    }

    public String calcPrice(ObjectId id, ReservationRequestDTO dto) {

        try {

            PairValue pairValue = canReserve(id, dto);

            Room room = (Room) pairValue.getKey();
            List<String> dates = (List<String>) pairValue.getValue();

            return generateSuccessMsg("data", calcPrice(room, dates, dto.getPassengers()).getKey());
        } catch (Exception x) {
            return generateErr(x.getMessage());
        }
    }


    public PairValue calcPrice(Room room, List<String> dates, int passengers) {

        int totalPrice = 0;
        int additionalPassengerPrice = passengers > room.getCap() ?
                (passengers - room.getCap()) * room.getCapPrice() : 0;

        List<DatePrice> datePrices = room.getDatePrices();
        List<Integer> prices = new ArrayList<>();

        for (String date : dates) {

            int nP = -1;

            for (DatePrice datePrice : datePrices) {
                if (datePrice.getDate().equals(date)) {
                    nP = datePrice.getPrice();
                    break;
                }
            }

            if (nP != -1) {
                prices.add(additionalPassengerPrice > 0 ? nP + additionalPassengerPrice : nP);
                totalPrice += additionalPassengerPrice > 0 ? nP + additionalPassengerPrice : nP;
                continue;
            }

            // todo: check is vacation
            // todo: check is weekend

            prices.add(additionalPassengerPrice > 0 ? room.getPrice() + additionalPassengerPrice : room.getPrice());
            totalPrice += additionalPassengerPrice > 0 ? room.getPrice() + additionalPassengerPrice : room.getPrice();
        }

        return new PairValue(totalPrice, prices);
    }

    public String reserve(ObjectId id, ReservationRequestDTO dto, ObjectId userId) {

        try {

            PairValue pairValue = canReserve(id, dto);

            Room room = (Room) pairValue.getKey();
            List<String> dates = (List<String>) pairValue.getValue();

            PairValue p = calcPrice(room, dates, dto.getPassengers());
            int totalAmount = (int) p.getKey();
            List<Integer> prices = (List<Integer>) p.getValue();

            ReservationRequests reservationRequests = new ReservationRequests();
            reservationRequests.setNights(dates);
            reservationRequests.setPassengers(dto.getPassengers());
            reservationRequests.setPrices(prices);
            reservationRequests.setTotalAmount(totalAmount);
            reservationRequests.setStatus(room.isOnlineReservation() ?
                    ReservationStatus.RESERVED :
                    ReservationStatus.PENDING
            );
            reservationRequests.setUserId(userId);
            reservationRequests.setRoomId(room.get_id());

            reservationRequestsRepository.insert(reservationRequests);

            if(room.isOnlineReservation())
                //todo: go to bank
             ;

            return generateSuccessMsg("data", reservationRequests.get_id());
        } catch (Exception x) {
            return generateErr(x.getMessage());
        }

    }
}
