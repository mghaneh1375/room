package bogen.studio.Room.Service;

import bogen.studio.Room.DTO.DatePrice;
import bogen.studio.Room.DTO.ReservationRequestDTO;
import bogen.studio.Room.DTO.RoomDTO;
import bogen.studio.Room.DTO.TripRequestDTO;
import bogen.studio.Room.Enums.*;
import bogen.studio.Room.Exception.InvalidFieldsException;
import bogen.studio.Room.Models.ReservationRequests;
import bogen.studio.Room.Models.Room;
import bogen.studio.Room.Network.Network;
import bogen.studio.Room.Repository.ReservationRequestsRepository;
import bogen.studio.Room.Repository.RoomRepository;
import bogen.studio.Room.Utility.FileUtils;
import my.common.commonkoochita.Utility.JalaliCalendar;
import my.common.commonkoochita.Utility.PairValue;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static bogen.studio.Room.Utility.StaticValues.*;
import static my.common.commonkoochita.Utility.Statics.*;
import static my.common.commonkoochita.Utility.Utility.*;

@Service
public class RoomService extends AbstractService<Room, RoomDTO> {

    public final static String FOLDER = "rooms";

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ReservationRequestsRepository reservationRequestsRepository;

    @Override
    public String list(List<String> filters) {

        ObjectId boomId = new ObjectId(filters.get(0));
        ObjectId userId = new ObjectId(filters.get(1));

        List<Room> rooms = roomRepository.findByUserIdAndBoomId(userId, boomId);

        return generateSuccessMsg("data", rooms.stream()
                .map(x -> {
                            String id = x.get_id().toString();

                            JSONObject jsonObject = new JSONObject()
                                    .put("id", id)
                                    .put("createdAt", convertDateToJalali(x.getCreatedAt()))
                                    .put("availability", x.isAvailability())
                                    .put("title", x.getTitle())
                                    .put("image", ASSET_URL + FOLDER + "/" + x.getImage())
                                    .put("cap", x.getCap())
                                    .put("no", x.getNo())
                                    .put("price", x.getPrice())
                                    .put("main", x.isMain())
                                    .put("maxCap", x.getMaxCap())
                                    .put("capPrice", x.getCapPrice())
                                    .put("onlineReservation", x.isOnlineReservation());

                            if (!x.isOnlineReservation())
                                jsonObject.put("pendingRequests", reservationRequestsRepository.countByRoomIdAndStatus(x.get_id(),
                                        ReservationStatus.PENDING.getName().toUpperCase())
                                );

                            return jsonObject;
                        }
                ).collect(Collectors.toList())
        );

    }

    private void translateFeatures(JSONObject jsonObject, Room x) {

        jsonObject.put("foodFacilities", x.getFoodFacilities() == null ? new JSONArray() :
                x.getFoodFacilities().stream()
                        .map(FoodFacility::toFarsi)
                        .collect(Collectors.toList()));

        jsonObject.put("limitations", x.getLimitations() == null ? new JSONArray() :
                x.getLimitations().stream()
                        .map(Limitation::toFarsi)
                        .collect(Collectors.toList()));

        jsonObject.put("welfares", x.getWelfares() == null ? new JSONArray() :
                x.getWelfares().stream()
                        .map(Welfare::toFarsi)
                        .collect(Collectors.toList()));

        jsonObject.put("accessibilityFeatures", x.getAccessibilityFeatures() == null ? new JSONArray() :
                x.getAccessibilityFeatures().stream()
                        .map(AccessibilityFeature::toFarsi)
                        .collect(Collectors.toList()));

        jsonObject.put("additionalFacilities", x.getAdditionalFacilities() == null ? new JSONArray() :
                x.getAdditionalFacilities().stream()
                        .map(AdditionalFacility::toFarsi)
                        .collect(Collectors.toList()));

        jsonObject.put("sleepFeatures", x.getSleepFeatures() == null ? new JSONArray() :
                x.getSleepFeatures().stream()
                        .map(SleepFeature::toFarsi)
                        .collect(Collectors.toList()));

    }

    public String publicList(ObjectId boomId, TripRequestDTO dto) {

        JSONArray jsonArray = new JSONArray();

        roomRepository.findByBoomId(boomId).forEach(x -> {

            for(int i = 0; i < jsonArray.length(); i++) {

                JSONObject tmp = jsonArray.getJSONObject(i);

                if(tmp.getString("title").equals(x.getTitle())) {

                    if(dto != null) {
                        try {

                            List<String> dates = canReserve(x, dto);
                            tmp.getJSONArray("freeRoomIds").put(x.get_id().toString());

                            if(tmp.getInt("totalPrice") == -1) {
                                tmp.put("totalPrice",
                                        (int) calcPrice(x, dates, dto.getAdults(), dto.getChildren()).getKey()
                                );
                            }
                        }
                        catch (Exception ignore) {}
                    }

                    return;
                }

            }

            JSONObject jsonObject = new JSONObject(x);

            jsonObject.put("id", x.get_id().toString());
            jsonObject.remove("_id");
            jsonObject.remove("no");

            jsonObject.put("image", ASSET_URL + FOLDER + "/" + x.getImage());

            translateFeatures(jsonObject, x);

            if(dto != null) {

                JSONArray freeRoomIds = new JSONArray();

                try {
                    jsonObject.put("totalPrice", calcPrice(x, canReserve(x, dto), dto.getAdults(), dto.getChildren()).getKey());
                    freeRoomIds.put(x.get_id().toString());
                }
                catch (Exception ex) {
                    jsonObject.put("totalPrice", -1);
                }

                jsonObject.put("freeRoomIds", freeRoomIds);
            }

            jsonArray.put(jsonObject);
        });

        return generateSuccessMsg("data", jsonArray);

    }

    private Room canModify(ObjectId id, ObjectId userId) throws InvalidFieldsException {

        Room room = findById(id);

        if (room == null)
            throw new InvalidFieldsException("id is not correct");

        if(room.getUserId() != userId)
            throw new InvalidFieldsException("access dined");

        if(!room.isMain())
            throw new InvalidFieldsException("تنها می توانید اتاق های اصلی را ویرایش کنید");

        return room;
    }

    private void saveAllSimilar(String oldTitle, Room main) {

        List<Room> similarRooms = roomRepository.findByNonMainByTitleAndBoomId(oldTitle, main.getBoomId());

        List<Room> modified = new ArrayList<>();
        modified.add(main);

        for(Room itr : similarRooms)
            copy(main, itr);

        modified.addAll(similarRooms);

        roomRepository.saveAll(modified);
    }

    @Override
    public String update(ObjectId id, ObjectId userId, RoomDTO dto) {

        Room room;
        try {
            room = canModify(id, userId);
        } catch (InvalidFieldsException e) {
            return generateErr(e.getMessage());
        }

        String oldTitle = room.getTitle();

        if (!oldTitle.equals(dto.getTitle()) &&
                roomRepository.countRoomByBoomIdAndTitle(room.getBoomId(), dto.getTitle()) > 0)
            return generateErr("اتاقی با این نام در بوم گردی شما موجود است.");

        saveAllSimilar(oldTitle, populateEntity(room, dto));

        return JSON_OK;
    }

    private void copy(Room main, Room room) {

        room.setTitle(main.getTitle());
        room.setDescription(main.getDescription());
        room.setImage(main.getImage());

        room.setMaxCap(main.getMaxCap());
        room.setCap(main.getCap());

        room.setDatePrices(main.getDatePrices());

        room.setCapPrice(main.getCapPrice());
        room.setWeekendCapPrice(main.getWeekendCapPrice());
        room.setVacationCapPrice(main.getVacationCapPrice());

        room.setPrice(main.getPrice());
        room.setWeekendPrice(main.getWeekendPrice());
        room.setVacationPrice(main.getVacationPrice());

        room.setLimitations(main.getLimitations());
        room.setFoodFacilities(main.getFoodFacilities());
        room.setWelfares(main.getWelfares());
        room.setSleepFeatures(main.getSleepFeatures());
        room.setAdditionalFacilities(main.getAdditionalFacilities());
        room.setAccessibilityFeatures(main.getAccessibilityFeatures());

        room.setAvailability(main.isAvailability());
        room.setOnlineReservation(main.isOnlineReservation());
    }

    @Override
    public String store(RoomDTO dto, Object... additionalFields) {

        ObjectId boomId = (ObjectId) additionalFields[1];
        ObjectId userId = (ObjectId) additionalFields[0];

        if (roomRepository.countRoomByBoomIdAndTitle(boomId, dto.getTitle()) > 0)
            return generateErr("اتاقی با این نام در بوم گردی شما موجود است.");

        List<Room> rooms = storePopulateEntity(dto);

        String filename = "";

        if(rooms.size() > 0) {
            filename = FileUtils.uploadFile((MultipartFile) additionalFields[2], FOLDER);
            if (filename == null)
                return JSON_UNKNOWN_UPLOAD_FILE;
        }

        JSONArray jsonArray = new JSONArray();

        for(Room room : rooms) {
            room.setUserId(userId);
            room.setBoomId(boomId);
            room.setImage(filename);
            roomRepository.insert(room);

            jsonArray.put(room.get_id().toString());
        }

        return generateSuccessMsg("ids", jsonArray);
    }

    public String setPic(ObjectId id, ObjectId userId, MultipartFile file) {

        Room room;
        try {
            room = canModify(id, userId);
        } catch (InvalidFieldsException e) {
            return generateErr(e.getMessage());
        }

        String filename = FileUtils.uploadFile(file, FOLDER);
        if (filename == null)
            return JSON_UNKNOWN_UPLOAD_FILE;

        FileUtils.removeFile(room.getImage(), FOLDER);
        room.setImage(filename);

        roomRepository.saveAll(Collections.singletonList(room));

        saveAllSimilar(room.getTitle(), room);
        return JSON_OK;
    }

    public String removeDatePrice(ObjectId id, ObjectId userId, String date) {

        Room room;
        try {
            room = canModify(id, userId);
        } catch (InvalidFieldsException e) {
            return generateErr(e.getMessage());
        }

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
        saveAllSimilar(room.getTitle(), room);

        return JSON_OK;
    }

    public String addDatePrice(ObjectId id, ObjectId userId, DatePrice datePrice) {

        if (!isLargerThanToday(datePrice.getDate()))
            return generateErr("تاریخ باید بزرگ تر از امروز باشد");

        Room room;
        try {
            room = canModify(id, userId);
        } catch (InvalidFieldsException e) {
            return generateErr(e.getMessage());
        }

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
        saveAllSimilar(room.getTitle(), room);

        return JSON_OK;
    }

    @Override
    Room populateEntity(Room room, RoomDTO roomDTO) {

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

    List<Room> storePopulateEntity(RoomDTO roomDTO) {

        List<Room> rooms = new ArrayList<>();

        for(int i = 0; i < roomDTO.getCount(); i++) {

            Room room = new Room();

            if(i == 0)
                room.setMain(true);

            room.setTitle(roomDTO.getTitle());
            room.setDescription(roomDTO.getDescription());
            room.setNo(i + 1);

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

            rooms.add(room);
        }

        return rooms;

    }

    @Override
    public Room findById(ObjectId id) {
        Optional<Room> room = roomRepository.findById(id);
        return room.orElse(null);
    }

    public String get(ObjectId id, ObjectId userId) {

        Room room = findById(id);

        if (room == null)
            return JSON_NOT_VALID_ID;

        if (room.getUserId() != userId)
            return JSON_NOT_ACCESS;

        JSONObject jsonObject = new JSONObject(room);
        jsonObject.put("boomId", room.getBoomId().toString());
        jsonObject.put("id", room.get_id().toString());
        jsonObject.remove("userId");
        jsonObject.put("createdAt", convertDateToJalali(room.getCreatedAt()));
        jsonObject.put("image", ASSET_URL + FOLDER + "/" + room.getImage());
        jsonObject.remove("_id");

        translateFeatures(jsonObject, room);

        return generateSuccessMsg("data", jsonObject);
    }

    public String remove(ObjectId id, ObjectId userId) {


        Room room = findById(id);

        if (room == null)
            return JSON_NOT_VALID_ID;

        if(room.getUserId() != userId)
            return JSON_NOT_ACCESS;

        if(reservationRequestsRepository.countAllActiveReservationsByRoomId(id) > 0)
            return generateErr("امکان حذف این اتاق به دلیل وجود اقامت فعال وجود ندارد");

        if(room.isMain())
            roomRepository.deleteByTitleAndBoomId(room.getTitle(), room.getBoomId());
        else
            roomRepository.delete(room);

        return JSON_OK;
    }

    private PairValue canReserve(ObjectId id, TripRequestDTO dto) throws InvalidFieldsException {

        Room room = findById(id);
        if (room == null)
            throw new InvalidFieldsException("id is not valid");

        return new PairValue(room, canReserve(room, dto));
    }

    private List<String> canReserve(Room room, TripRequestDTO dto) throws InvalidFieldsException {

        if (!room.isAvailability())
            throw new InvalidFieldsException("has not access");

        if (room.getMaxCap() < (dto.getAdults() + dto.getChildren()))
            throw new InvalidFieldsException("حداکثر ظرفیت برای این اتاق " + room.getMaxCap() + " می باشد.");

        List<String> dates = new ArrayList<>();
        dates.add(dto.getStartDate());

        for (int i = 1; i < dto.getNights(); i++)
            dates.add(getPast("/", dto.getStartDate(), -1 * i));

        if (reservationRequestsRepository.findActiveReservations(room.get_id(), dates) > 0)
            throw new InvalidFieldsException("در زمان خواسته شده، اقامتگاه مدنظر پر می باشد.");

        return dates;
    }

    public String calcPrice(ObjectId id, TripRequestDTO dto) {

        try {

            PairValue pairValue = canReserve(id, dto);

            Room room = (Room) pairValue.getKey();
            List<String> dates = (List<String>) pairValue.getValue();

            PairValue p = calcPrice(room, dates, dto.getAdults(), dto.getChildren());

            JSONArray jsonArray = new JSONArray();

            ((List<DatePrice>) p.getValue()).forEach(x -> jsonArray.put(new JSONObject()
                    .put("additionalCapPrice", x.getCapPrice())
                    .put("price", x.getPrice())
                    .put("date", x.getDate())
            ));

            return generateSuccessMsg("data", new JSONObject()
                    .put("total", p.getKey())
                    .put("prices", jsonArray)
            );
        } catch (Exception x) {
            return generateErr(x.getMessage());
        }
    }

    public PairValue calcPrice(Room room, List<String> dates, int adults, int children) {

        // todo: consider children independent
        adults += children;

        int totalPrice = 0;

        List<DatePrice> datePrices = room.getDatePrices();
        List<DatePrice> pricesDetail = new ArrayList<>();

        int exceedPassenger = Math.max(0, adults - room.getCap());

        for (String date : dates) {

            int nightPrice = -1;

            int additionalPassengerPrice = 0;

            if(datePrices != null) {
                for (DatePrice datePrice : datePrices) {
                    if (datePrice.getDate().equals(date)) {
                        nightPrice = datePrice.getPrice();
                        if (exceedPassenger > 0)
                            additionalPassengerPrice = datePrice.getCapPrice() != null ?
                                    exceedPassenger * datePrice.getCapPrice() :
                                    exceedPassenger * room.getCapPrice();
                        break;
                    }
                }
            }

            if (nightPrice != -1) {
                pricesDetail.add(new DatePrice(nightPrice, additionalPassengerPrice, date, "custom"));
                totalPrice += nightPrice + additionalPassengerPrice;
                continue;
            }

            String[] splited = date.split("\\/");
            int dayOfWeek = JalaliCalendar.dayOfWeek(new JalaliCalendar.YearMonthDate(splited[0], splited[1], splited[2]));

            if (room.getWeekendPrice() != null && weekends.contains(dayOfWeek)) {

                if(exceedPassenger > 0)
                    additionalPassengerPrice = room.getWeekendCapPrice() != null ?
                            exceedPassenger * room.getWeekendCapPrice() :
                            exceedPassenger * room.getCapPrice();

                pricesDetail.add(new DatePrice(room.getWeekendPrice(), additionalPassengerPrice, date, "weekend"));
                totalPrice += room.getWeekendPrice() + additionalPassengerPrice;
            }
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

                if(exceedPassenger > 0)
                    additionalPassengerPrice = isHoliday && room.getVacationCapPrice() != null ?
                            exceedPassenger * room.getVacationCapPrice() :
                            exceedPassenger * room.getCapPrice();

                if (isHoliday)
                    nightPrice = room.getVacationPrice();
                else
                    nightPrice = room.getPrice();

                pricesDetail.add(new DatePrice(nightPrice, additionalPassengerPrice, date, isHoliday ? "vacation" : "regular"));
                totalPrice += nightPrice + additionalPassengerPrice;
            }

        }

        return new PairValue(totalPrice, pricesDetail);
    }

    public String reserve(ObjectId id, ReservationRequestDTO dto, ObjectId userId) {

        try {

            ObjectId passengersId = dto.getPassengersId();

            JSONObject passengerServiceResponse = Network.sendGetReq(PASSENGER_URL + "system/trip/getTripPassengers/" + passengersId + "/" + userId);
            if(passengerServiceResponse == null)
                return generateErr("passengersId is not valid");

            int children = 0, adults = 0, infants = 0;

            JSONArray passengersJSON = passengerServiceResponse.getJSONObject("data").getJSONArray("passengers");
            List<Document> passengers = new ArrayList<>();

            for(int j = 0; j < passengersJSON.length(); j++) {
                Document doc = Document.parse(passengersJSON.getJSONObject(j).toString());
                switch (doc.getString("ageType")) {
                    case "بزرگسال":
                    default:
                        adults++;
                        break;
                    case "خردسال":
                        children++;
                        break;
                    case "نوزاد":
                        infants++;
                        break;
                }
                passengers.add(doc);
            }

            if(adults == 0)
                return generateErr("تعداد بزرگسال نمی تواند 0 باشد");

            TripRequestDTO tripRequestDTO = new TripRequestDTO(
                    adults, children, infants,
                    dto.getStartDate(), dto.getNights()
            );

            PairValue pairValue = canReserve(id, tripRequestDTO);

            Room room = (Room) pairValue.getKey();
            List<String> dates = (List<String>) pairValue.getValue();

            PairValue p = calcPrice(room, dates, adults, children);
            int totalAmount = (int) p.getKey();

            ReservationRequests reservationRequests = new ReservationRequests();

            reservationRequests.setCreator(Document.parse(passengerServiceResponse.getJSONObject("data").getJSONObject("creator").toString()));
            reservationRequests.setPassengers(passengers);

            reservationRequests.setAdults(adults);
            reservationRequests.setChildren(children);
            reservationRequests.setInfants(infants);

            reservationRequests.setPassengersId(passengersId);
            reservationRequests.setPrices((List<DatePrice>) p.getValue());
            reservationRequests.setTotalAmount(totalAmount);
            reservationRequests.setOwnerId(room.getUserId());
            reservationRequests.setStatus(room.isOnlineReservation() ?
                    ReservationStatus.RESERVED : ReservationStatus.PENDING
            );
            reservationRequests.setReserveExpireAt(room.isOnlineReservation() ?
                    System.currentTimeMillis() + BANK_WAIT_MSEC :
                    System.currentTimeMillis() + ACCEPT_PENDING_WAIT_MSEC
            );
            reservationRequests.setUserId(userId);
            reservationRequests.setRoomId(room.get_id());

            String trackingCode = randomString(6);

            reservationRequests.setTrackingCode(trackingCode);

            reservationRequestsRepository.insert(reservationRequests);

            if (room.isOnlineReservation())
                //todo: go to bank
                ;

            return generateSuccessMsg("data", new JSONObject()
                    .put("trackingCode", trackingCode)
                    .put("reservationId", reservationRequests.get_id())
            );

        } catch (Exception x) {
            return generateErr(x.getMessage());
        }

    }

    public String toggleAccessibility(ObjectId id, ObjectId userId) {

        Room room;
        try {
            room = canModify(id, userId);
        } catch (InvalidFieldsException e) {
            return generateErr(e.getMessage());
        }

        room.setAvailability(!room.isAvailability());
        saveAllSimilar(room.getTitle(), room);

        return JSON_OK;
    }

}
