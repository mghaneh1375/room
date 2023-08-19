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
import bogen.studio.Room.Utility.JalaliCalendar;
import bogen.studio.Room.Utility.PairValue;
import bogen.studio.Room.Utility.Utility;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
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
    public String list(List<String> filters) {

        ObjectId boomId = new ObjectId(filters.get(0));
        int userId = Integer.parseInt(filters.get(1));

        List<Room> rooms = roomRepository.findByUserIdAndBoomId(userId, boomId);

        return generateSuccessMsg("data", rooms.stream()
                .map(x -> {

                            String id = x.get_id().toString();

                            JSONObject jsonObject = new JSONObject()
                                    .put("id", id)
                                    .put("created_at", x.getCreatedAt().toString())
                                    .put("availability", x.isAvailability())
                                    .put("title", x.getTitle())
                                    .put("image", x.getImage())
                                    .put("cap", x.getCap())
                                    .put("price", x.getPrice())
                                    .put("maxCap", x.getMaxCap())
                                    .put("capPrice", x.getCapPrice())
                                    .put("onlineReservation", x.isOnlineReservation());

                            if(x.isOnlineReservation())
                                jsonObject.put("pendingRequests", reservationRequestsRepository.countByRoomIdAndStatus(x.get_id(), "RESERVED"));

                            return jsonObject;
                        }
                ).collect(Collectors.toList())
        );

    }


    public String publicList(ObjectId boomId) {

//        List<Room> rooms = roomRepository.findByBoomId(boomId).stream().map(x -> {
//
//            JSONObject jsonObject = new JSONObject(x);
////            roomDTO.getLimitations().stream()
////                    .map(String::toUpperCase)
////                    .map(Limitation::valueOf)
////                    .collect(Collectors.toList()
//
////            jsonObject.put("foodFacilities", );
//
//        });

        JSONArray jsonArray = new JSONArray();

        for(Room room : rooms) {
            JSONObject jsonObject = new JSONObject(room);
            jsonObject.put("id", room.get_id().toString());
            jsonObject.put("foodFacilities", jsonObject.getJSONArray("foodFacilities"))
            jsonObject.remove("_id");
            jsonArray.put(jsonObject);
        }

        return generateSuccessMsg("data", jsonArray);

    }

    @Override
    public String update(ObjectId id, Object userId, RoomDTO dto) {

        Optional<Room> roomOptional = roomRepository.findById(id);

        Room room = roomOptional.orElse(null);

        if (room == null)
            return JSON_NOT_VALID_ID;

        if (!room.getUserId().equals(userId))
            return JSON_NOT_ACCESS;

        String oldTitle = room.getTitle();

        room = populateEntity(room, dto);

        if(!oldTitle.equals(room.getTitle()) &&
                roomRepository.countRoomByBoomIdAndTitle(room.getBoomId(), room.getTitle()) > 0)
                return generateErr("اتاقی با این نام در بوم گردی شما موجود است.");

        roomRepository.save(room);
        return JSON_OK;
    }

    @Override
    public String store(RoomDTO dto, Object... additionalFields) {

        Room room = populateEntity(null, dto);

        room.setUserId((Integer) additionalFields[0]);
        room.setBoomId((ObjectId) additionalFields[1]);

        if(roomRepository.countRoomByBoomIdAndTitle(room.getBoomId(), room.getTitle()) > 0)
            return generateErr("اتاقی با این نام در بوم گردی شما موجود است.");

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

    public String removeDatePrice(ObjectId id, String date) {

        Room room = findById(id);
        if (room == null)
            return JSON_NOT_VALID_ID;

        if (room.getDatePrices() == null)
            return JSON_NOT_ACCESS;

        List<DatePrice> datePrices = room.getDatePrices();

        int idx = -1;
        int counter = 0;

        for (DatePrice itr : datePrices) {

            if (itr.getDate().equals(date)) {
                idx = counter;
                break;
            }

            counter++;
        }

        if (idx == -1)
            return JSON_NOT_ACCESS;

        datePrices.remove(idx);
        roomRepository.save(room);

        return JSON_OK;
    }

    public String addDatePrice(ObjectId id, DatePrice datePrice) {

        if (!datePrice.isLargerThanToday())
            return generateErr("تاریخ باید بزرگ تر از امروز باشد");

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

        if (room == null)
            room = new Room();

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

        return room;

    }

    @Override
    public Room findById(ObjectId id) {
        Optional<Room> room = roomRepository.findById(id);
        return room.orElse(null);
    }


    public String get(ObjectId id, int userId) {

        Room room = findById(id);

        if(room == null)
            return JSON_NOT_VALID_ID;

        if(room.getUserId() != userId)
            return JSON_NOT_ACCESS;

        JSONObject jsonObject = new JSONObject(room);
        jsonObject.put("boomId", room.getBoomId().toString());
        jsonObject.put("id", room.get_id().toString());
        jsonObject.remove("_id");

        return generateSuccessMsg("data", jsonObject);
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
            PairValue p = calcPrice(room, dates, dto.getPassengers());

            return generateSuccessMsg("data", new JSONObject()
                    .put("total", p.getKey())
                    .put("prices", p.getValue())
            );
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

            String[] splited = date.split("\\/");
            int dayOfWeek = JalaliCalendar.dayOfWeek(new JalaliCalendar.YearMonthDate(splited[0], splited[1], splited[2]));


            if (room.getWeekendPrice() != null && weekends.contains(dayOfWeek)) {
                prices.add(additionalPassengerPrice > 0 ? room.getWeekendPrice() + additionalPassengerPrice : room.getWeekendPrice());
                totalPrice += additionalPassengerPrice > 0 ? room.getWeekendPrice() + additionalPassengerPrice : room.getWeekendPrice();
            }
            // todo: check is vacation
            else {

                boolean isHoliday = false;

                if (room.getVacationPrice() != null) {
                    if (fetchedHolidays.containsKey(date)) {
                        isHoliday = fetchedHolidays.get(date);
                    } else {
                        try {
                            HttpResponse<JsonNode> res = Unirest.get("https://holidayapi.ir/jalali/" + date).asJson();
                            if (res.getStatus() == 200 && res.getBody().getObject().has("is_holiday")) {
                                isHoliday = res.getBody().getObject().getBoolean("is_holiday");
                                fetchedHolidays.put(date, isHoliday);
                            }
                        } catch (UnirestException e) {
                            e.printStackTrace();
                        }
                    }
                }

                int p;

                if (isHoliday)
                    p = additionalPassengerPrice > 0 ? room.getVacationPrice() + additionalPassengerPrice : room.getVacationPrice();
                else
                    p = additionalPassengerPrice > 0 ? room.getPrice() + additionalPassengerPrice : room.getPrice();

                prices.add(p);
                totalPrice += p;
            }

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
            reservationRequests.setStatus(ReservationStatus.RESERVED);
            reservationRequests.setReserveExpireAt(room.isOnlineReservation() ?
                    System.currentTimeMillis() + BANK_WAIT_MSEC :
                    System.currentTimeMillis() + ACCEPT_PENDING_WAIT_MSEC
            );
            reservationRequests.setUserId(userId);
            reservationRequests.setRoomId(room.get_id());

            reservationRequestsRepository.insert(reservationRequests);

            if (room.isOnlineReservation())
                //todo: go to bank
                ;

            return generateSuccessMsg("data", reservationRequests.get_id());
        } catch (Exception x) {
            return generateErr(x.getMessage());
        }

    }

    public String toggleAccessibility(ObjectId id) {

        Room room = findById(id);
        if (room == null)
            return JSON_NOT_VALID_ID;

        room.setAvailability(!room.isAvailability());
        roomRepository.save(room);

        return JSON_OK;
    }
}
