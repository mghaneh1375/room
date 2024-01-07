package bogen.studio.Room.Service;

import bogen.studio.Room.DTO.*;
import bogen.studio.Room.Enums.*;
import bogen.studio.Room.Exception.*;
import bogen.studio.Room.Models.*;
import bogen.studio.Room.Network.Network;
import bogen.studio.Room.Repository.ReservationRequestRepository;
import bogen.studio.Room.Repository.ReservationRequestRepository2;
import bogen.studio.Room.Repository.RoomRepository;
import bogen.studio.Room.Repository.RoomRepository2;
import bogen.studio.Room.Utility.FileUtils;
import bogen.studio.Room.Utility.TimeUtility;
import bogen.studio.Room.documents.RoomDateReservationState;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.common.commonkoochita.Utility.JalaliCalendar;
import my.common.commonkoochita.Utility.PairValue;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static bogen.studio.Room.Enums.ReservationStatus.*;
import static bogen.studio.Room.Enums.RoomStatus.FREE;
import static bogen.studio.Room.Enums.RoomStatus.RESERVED;
import static bogen.studio.Room.Utility.StaticValues.*;
import static my.common.commonkoochita.Utility.Statics.*;
import static my.common.commonkoochita.Utility.Utility.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService extends AbstractService<Room, RoomDTO> {

    public final static String FOLDER = "rooms";

    //@Autowired
    private final RoomRepository roomRepository;

    //@Autowired
    private final ReservationRequestRepository reservationRequestRepository;

    private final RoomDateReservationStateService roomDateReservationStateService;
    private final ReservationRequestService reservationRequestService;
    private final ReservationRequestRepository2 reservationRequestRepository2;
    private final RoomRepository2 roomRepository2;
    private final DiscountService discountService;

    @Value("${tracking.code.length}")
    private int trackingCodeLength;

    @Value("${payment1.timeout}")
    private int payment1Timeout;

    @Value("${owner.response.timeout}")
    private int ownerResponseTimeout;

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

                            JSONArray galleryJSON = new JSONArray();

                            if (x.getGalleries() != null) {
                                for (String pic : x.getGalleries())
                                    galleryJSON.put(ASSET_URL + FOLDER + "/" + pic);
                            }

                            jsonObject.put("gallery", galleryJSON);

                            if (!x.isOnlineReservation())
                                jsonObject.put(
                                        "pendingRequests",
                                        reservationRequestRepository.countByRoomIdAndStatus(
                                                x.get_id(),
                                                WAIT_FOR_OWNER_RESPONSE.toString()
                                        )
                                );

                            return jsonObject;
                        }
                ).collect(Collectors.toList())
        );

    }

    private void translateFeatures(JSONObject jsonObject, Room x) {

        jsonObject.put("foodFacilities", x.getFoodFacilities() == null ? new JSONArray() :
                x.getFoodFacilities().stream()
                        .map(item -> new JSONObject()
                                .put("name", item.toFarsi())
                                .put("value", item.getName()))
                        .collect(Collectors.toList()));

        jsonObject.put("limitations", x.getLimitations() == null ? new JSONArray() :
                x.getLimitations().stream()
                        .map(item -> new JSONObject()
                                .put("name", item.toFarsi())
                                .put("value", item.getName()))
                        .collect(Collectors.toList()));

        jsonObject.put("welfares", x.getWelfares() == null ? new JSONArray() :
                x.getWelfares().stream()
                        .map(item -> new JSONObject()
                                .put("name", item.toFarsi())
                                .put("value", item.getName()))
                        .collect(Collectors.toList()));

        jsonObject.put("accessibilityFeatures", x.getAccessibilityFeatures() == null ? new JSONArray() :
                x.getAccessibilityFeatures().stream()
                        .map(item -> new JSONObject()
                                .put("name", item.toFarsi())
                                .put("value", item.getName()))
                        .collect(Collectors.toList()));

        jsonObject.put("additionalFacilities", x.getAdditionalFacilities() == null ? new JSONArray() :
                x.getAdditionalFacilities().stream()
                        .map(item -> new JSONObject()
                                .put("name", item.toFarsi())
                                .put("value", item.getName()))
                        .collect(Collectors.toList()));

        jsonObject.put("sleepFeatures", x.getSleepFeatures() == null ? new JSONArray() :
                x.getSleepFeatures().stream()
                        .map(item -> new JSONObject()
                                .put("name", item.toFarsi())
                                .put("value", item.getName()))
                        .collect(Collectors.toList()));

    }

    public String publicList(ObjectId boomId, TripInfo dto) {

        JSONArray jsonArray = new JSONArray();

        roomRepository.findByBoomId(boomId).forEach(x -> {

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject tmp = jsonArray.getJSONObject(i);

                if (tmp.getString("title").equals(x.getTitle())) {

                    if (dto != null) {
                        try {

                            List<String> dates = canReserve(x, dto);
                            tmp.getJSONArray("freeRoomIds").put(x.get_id().toString());

                            if (tmp.getInt("totalPrice") == -1) {
                                tmp.put("totalPrice",
                                        (long) calcPrice(x, dates, dto.getAdults(), dto.getChildren()).getTotalPrice()
                                );
                            }
                        } catch (Exception ignore) {
                        }
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

            if (dto != null) {

                JSONArray freeRoomIds = new JSONArray();

                try {
                    jsonObject.put("totalPrice", calcPrice(x, canReserve(x, dto), dto.getAdults(), dto.getChildren()).getTotalPrice());
                    freeRoomIds.put(x.get_id().toString());
                } catch (Exception ex) {
                    jsonObject.put("totalPrice", -1);
                }

                jsonObject.put("freeRoomIds", freeRoomIds);
            }

            jsonArray.put(jsonObject);
        });

        // Remove Rooms with no FreeIds
        JSONArray modifiedSearchResult = removeRoomsWithNoFreeIds(jsonArray);

        // Add Discount info
        discountService.addDiscountInfoToRoomSearchResult(modifiedSearchResult, boomId, dto, null);

        return generateSuccessMsg("data", modifiedSearchResult);

    }

    private JSONArray removeRoomsWithNoFreeIds(JSONArray searchResult) {
        /* Remove rooms, which do not have freeIds */

        JSONArray modifiedSearchResult = new JSONArray();

        for (int i = 0; i < searchResult.length(); i++) {

            var roomSearchObject = searchResult.getJSONObject(i);

            if (roomSearchObject.has("totalPrice")) {

                if (roomSearchObject.getInt("totalPrice") != -1) {
                    modifiedSearchResult.put(roomSearchObject);
                }
            } else {
                modifiedSearchResult.put(roomSearchObject);
            }


        }

        return modifiedSearchResult;

    }

    private Room canModify(ObjectId id, ObjectId userId, boolean justMain) throws InvalidFieldsException {

        Room room = findById(id);

        if (room == null)
            throw new InvalidFieldsException("id is not correct");

        if (!room.getUserId().equals(userId))
            throw new InvalidFieldsException("access dined");

        if (justMain && !room.isMain())
            throw new InvalidFieldsException("تنها می توانید اتاق های اصلی را ویرایش کنید");

        return room;
    }

    private void saveAllSimilar(String oldTitle, Room main) {

        List<Room> similarRooms = roomRepository.findByNonMainByTitleAndBoomId(oldTitle, main.getBoomId());

        List<Room> modified = new ArrayList<>();
        modified.add(main);

        for (Room itr : similarRooms)
            copy(main, itr);

        modified.addAll(similarRooms);

        roomRepository.saveAll(modified);
    }

    @Override
    public String update(ObjectId id, ObjectId userId, RoomDTO dto) {

        Room room;
        try {
            room = canModify(id, userId, true);
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

    public String getDatePrice(ObjectId id, ObjectId userId) {

        Room room;
        try {
            room = canModify(id, userId, false);
        } catch (InvalidFieldsException e) {
            return generateErr(e.getMessage());
        }

        List<DatePrice> datePrices = room.getDatePrices();
        JSONArray jsonArray = new JSONArray();

        for (DatePrice datePrice : datePrices) {
            jsonArray.put(new JSONObject()
                    .put("date", datePrice.getDate())
                    .put("capPrice", datePrice.getCapPrice())
                    .put("price", datePrice.getPrice())
                    .put("tag", datePrice.getTag())
            );
        }

        return generateSuccessMsg("data", jsonArray);
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
    @Transactional
    public String store(RoomDTO dto, Object... additionalFields) {

        ObjectId boomId = (ObjectId) additionalFields[1];
        ObjectId userId = (ObjectId) additionalFields[0];

        if (roomRepository.countRoomByBoomIdAndTitle(boomId, dto.getTitle()) > 0)
            return generateErr("اتاقی با این نام در بوم گردی شما موجود است.");

        List<Room> rooms = storePopulateEntity(dto);

        String filename = "";

        if (rooms.size() > 0) {
            filename = FileUtils.uploadFile((MultipartFile) additionalFields[2], FOLDER);
            if (filename == null)
                return JSON_UNKNOWN_UPLOAD_FILE;
        }

        JSONArray jsonArray = new JSONArray();

        for (Room room : rooms) {
            room.setUserId(userId);
            room.setBoomId(boomId);
            room.setImage(filename);
            roomRepository.insert(room);

            jsonArray.put(room.get_id().toString());
        }

        // Build RoomDateReservationState Documents for newly created rooms
        roomDateReservationStateService.createRoomDateReservationStateDocuments();

        return generateSuccessMsg("ids", jsonArray);
    }

    public String setPic(ObjectId id, ObjectId userId, MultipartFile file) {

        Room room;
        try {
            room = canModify(id, userId, true);
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

    public String getGalleries(ObjectId id, ObjectId userId) {

        Room room;
        try {
            room = canModify(id, userId, true);
        } catch (InvalidFieldsException e) {
            return generateErr(e.getMessage());
        }

        return generateSuccessMsg("data", (room.getGalleries() == null ? new ArrayList<>() : room.getGalleries())
                .stream().map(e -> ASSET_URL + FOLDER + "/" + e)
                .collect(Collectors.toList())
        );
    }

    public String addToGallery(ObjectId id, ObjectId userId, MultipartFile file) {

        Room room;
        try {
            room = canModify(id, userId, true);
        } catch (InvalidFieldsException e) {
            return generateErr(e.getMessage());
        }

        List<String> galleries = room.getGalleries() == null ? new ArrayList<>() : room.getGalleries();

        if (galleries.size() == 5)
            return generateErr("تنها 5 فایل به عنوان گالری می توان افزود");

        String filename = FileUtils.uploadFile(file, FOLDER);
        if (filename == null)
            return JSON_UNKNOWN_UPLOAD_FILE;

        galleries.add(filename);
        room.setGalleries(galleries);

        roomRepository.saveAll(Collections.singletonList(room));

        saveAllSimilar(room.getTitle(), room);
        return generateSuccessMsg("url", ASSET_URL + FOLDER + "/" + filename);
    }

    public String removeFromGallery(ObjectId id, ObjectId userId, String filename) {

        Room room;
        try {
            room = canModify(id, userId, true);
        } catch (InvalidFieldsException e) {
            return generateErr(e.getMessage());
        }

        List<String> galleries = room.getGalleries();

        if (galleries == null || !galleries.contains(filename))
            return JSON_NOT_ACCESS;

        FileUtils.removeFile(filename, FOLDER);

        galleries.remove(filename);
        room.setGalleries(galleries);

        roomRepository.saveAll(Collections.singletonList(room));

        saveAllSimilar(room.getTitle(), room);
        return JSON_OK;
    }

    public String removeDatePrice(ObjectId id, ObjectId userId, String date) {

        Room room;
        try {
            room = canModify(id, userId, true);
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
            room = canModify(id, userId, true);
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

        if (roomDTO.getOnlineReservation() != null)
            room.setOnlineReservation(roomDTO.getOnlineReservation());

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

        for (int i = 0; i < roomDTO.getCount(); i++) {

            Room room = new Room();

            if (i == 0)
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

        if (!room.getUserId().equals(userId))
            return JSON_NOT_ACCESS;

        JSONObject jsonObject = new JSONObject(room);
        jsonObject.put("boomId", room.getBoomId().toString());
        jsonObject.put("id", room.get_id().toString());
        jsonObject.put("count", roomRepository.countByNonMainByTitleAndBoomId(room.getTitle(), room.getBoomId()));
        jsonObject.remove("userId");
        jsonObject.put("createdAt", convertDateToJalali(room.getCreatedAt()));
        jsonObject.put("clean", room.isClean());
        jsonObject.put("image", ASSET_URL + FOLDER + "/" + room.getImage());
        jsonObject.remove("_id");

        JSONArray galleryJSON = new JSONArray();

        if (room.getGalleries() != null) {
            for (String pic : room.getGalleries())
                galleryJSON.put(ASSET_URL + FOLDER + "/" + pic);
        }

        jsonObject.put("gallery", galleryJSON);

        translateFeatures(jsonObject, room);

        return generateSuccessMsg("data", jsonObject);
    }

    public String remove(ObjectId roomId, ObjectId userId) {


        Room room = findById(roomId);

        if (room == null)
            return JSON_NOT_VALID_ID;

        if (!room.getUserId().equals(userId))
            return JSON_NOT_ACCESS;

        if (reservationRequestRepository2.countAllActiveReservationsByRoomId(roomId) > 0)
            return generateErr("امکان حذف این اتاق به دلیل وجود اقامت فعال وجود ندارد");

        if (room.isMain())
            roomRepository.deleteByTitleAndBoomId(room.getTitle(), room.getBoomId());
        else
            roomRepository.delete(room);

        return JSON_OK;
    }


    private PairValue canReserve(ObjectId roomId, TripInfo tripInfo, boolean calculatePriceUsage, List<RoomDateReservationState> roomDateSafetyList) throws InvalidFieldsException {

        Room room = findById(roomId);
        if (room == null)
            throw new InvalidIdException("آیدی اتاق نامعتبر است");

        if (!room.isAvailability())
            throw new RoomUnavailableByOwnerException("اتاق توسط مالک غیر قابل دسترس تنظیم شده است");

        if (room.getMaxCap() < (tripInfo.getAdults() + tripInfo.getChildren()))
            throw new RoomExceedCapacityException("حداکثر ظرفیت اتاق: ", room.getMaxCap());

        List<String> jalaliDates = new ArrayList<>();
        jalaliDates.add(tripInfo.getStartDate());

        for (int i = 1; i < tripInfo.getNights(); i++)
            jalaliDates.add(getPast("/", tripInfo.getStartDate(), -1 * i));


        /* This method is replaced with the following method */
//        if (reservationRequestsRepository.findActiveReservations(room.get_id(), jalaliDates) > 0)
//            throw new InvalidFieldsException("در زمان خواسته شده، اقامتگاه مدنظر پر می باشد.");

        // Check room date reserve status collection to see whether room is free or not, then make the RoomStatus to RESERVED
        checkRoomDateStatusAndMakeItReservedInDemand(roomId, jalaliDates, calculatePriceUsage, roomDateSafetyList);

        return new PairValue(room, jalaliDates);
    }

    private void checkRoomDateStatusAndMakeItReservedInDemand(ObjectId roomId, List<String> jalaliDates, boolean doNotSetRoomReserved, List<RoomDateReservationState> roomDateSafetyList) {
        /* Check room date reserve status collection to see whether room is free or not, then make the RoomStatus to
         * RESERVED */

        // Convert input jalali dates to gregorian dates
        List<LocalDateTime> gregorianDates = TimeUtility.convertJalaliDatesListToGregorian(jalaliDates);

        // According to input roomId and gregorian dates find the list of target RoomDateReservationState documents
        List<RoomDateReservationState> roomDateReservationStateList = roomDateReservationStateService.findRoomDateReservationStateForTargetDates(roomId, gregorianDates);

        // Validate fetched RoomDateReservationState docs
        validateFetchedRoomDateStateDocs(roomId, roomDateReservationStateList, gregorianDates);

        // Throw exception if room is not free in target dates
        throwExceptionIfRoomIsNotFreeForAnyTargetDate(roomDateReservationStateList);

        // In database change the RoomStatus to RESERVED
        if (!doNotSetRoomReserved) { // Do not change the RoomStatus in DB if the process in calculating the room price
            changeRoomDataStatusesToReserved(roomDateReservationStateList, roomDateSafetyList);
        }


    }

    private void validateFetchedRoomDateStateDocs(ObjectId roomId, List<RoomDateReservationState> fetchedDocsList, List<LocalDateTime> gregorianResidenceDates) {
        /* Number of fetched RoomDateReservationState docs should be the same as the size of list of requested residence dates */

        if (fetchedDocsList.size() != gregorianResidenceDates.size()) {
            throw new BackendErrorException(String.format("There is not enough RoomDateReservationState for roomId: %s, and target dates: %s. For developers attention", roomId, gregorianResidenceDates));
        }

    }

    private void changeRoomDataStatusesToReserved(List<RoomDateReservationState> roomDateReservationStateList, List<RoomDateReservationState> roomDateSafetyList) {

        // Set RoomStatus of input items to RESERVED
        for (RoomDateReservationState roomDateReservationState : roomDateReservationStateList) {
            roomDateReservationState.setRoomStatus(RESERVED);
        }


        for (RoomDateReservationState roomDateReservationState : roomDateReservationStateList) {

            try {
                roomDateReservationStateService.save(roomDateReservationState);
                roomDateSafetyList.add(roomDateReservationState);

                log.info(String.format("Status of room: %s, in date: %s, changed to: %s",
                        roomDateReservationState.getRoomObjectId(),
                        roomDateReservationState.getTargetDate(),
                        RESERVED));

            } catch (OptimisticLockingFailureException e) {

                log.warn("Roll back to room status: FREE, initiated");
                // Rollback edited documents, Since @Transactional needs Replica set and we do not have it yet
                for (RoomDateReservationState roomDateReservationState1 : roomDateSafetyList) {
                    roomDateReservationState1.setRoomStatus(FREE);
                    roomDateReservationStateService.save(roomDateReservationState1);

                    log.info(String.format("Status of room: %s, in date: %s, changed to: %s",
                            roomDateReservationState.getRoomObjectId(),
                            roomDateReservationState.getTargetDate(),
                            FREE));
                }

                throw new RoomNotFreeException("اتاق در تاریخ های انتخاب شده قابل رزرو نیست");
            }
        }
    }

    private void throwExceptionIfRoomIsNotFreeForAnyTargetDate(List<RoomDateReservationState> roomDateReservationStateList) {

        // Extract list of RoomStatus
        List<RoomStatus> roomStatusList = new ArrayList<>();
        roomDateReservationStateList.stream().forEach(item -> roomStatusList.add(item.getRoomStatus()));

        if (roomStatusList.contains(RoomStatus.BOOKED) || roomStatusList.contains(RESERVED)) {
            throw new RoomNotFreeException("اتاق در تاریخ های انتخاب شده خالی نیست");
        }
    }


    private List<String> canReserve(Room room, TripInfo tripInfo) throws InvalidFieldsException {

        if (!room.isAvailability())
            throw new InvalidFieldsException("اتاق توسط مالک غیر قابل دسترس تنظیم شده است");

        if (room.getMaxCap() < (tripInfo.getAdults() + tripInfo.getChildren()))
            throw new InvalidFieldsException("حداکثر ظرفیت برای این اتاق " + room.getMaxCap() + " می باشد.");

        List<String> dates = new ArrayList<>();
        dates.add(tripInfo.getStartDate());

        for (int i = 1; i < tripInfo.getNights(); i++)
            dates.add(getPast("/", tripInfo.getStartDate(), -1 * i));

        //====================================
        checkRoomDateStatusAndMakeItReservedInDemand(room.get_id(), dates, true, null);
//        if (reservationRequestRepository.findActiveReservations(room.get_id(), dates) > 0)
//            throw new InvalidFieldsException("در زمان خواسته شده، اقامتگاه مدنظر پر می باشد.");

        return dates;
    }

    public String calcPrice(ObjectId roomId, TripInfo dto, String discountCode) {

        try {

            JSONObject output = new JSONObject();

            PairValue pairValue = canReserve(roomId, dto, true, null);

            Room room = (Room) pairValue.getKey();
            List<String> dates = (List<String>) pairValue.getValue();

            // Get total price and put it in output Json
            CalculatePriceResult calculatePriceResult = calcPrice(room, dates, dto.getAdults(), dto.getChildren());
            output.put("total", calculatePriceResult.getTotalPrice());

            // Get details of prices and put it in the output json
            JSONArray jsonArray = new JSONArray();
            ((List<DatePrice>) calculatePriceResult.getDatePriceList()).forEach(x -> jsonArray.put(new JSONObject()
                    .put("additionalCapPrice", x.getCapPrice())
                    .put("price", x.getPrice())
                    .put("date", TimeUtility.convertJalaliDatesListToGregorian(List.of(x.getDate())).get(0))
            ));
            output.put("prices", jsonArray);

            // Add Discount info
            JSONObject discountInfo = discountService.buildRoomDiscountInfoJsonObject(
                    room.getTitle(),
                    room.getPrice().longValue(),
                    calculatePriceResult.getTotalPrice(),
                    room.getBoomId(),
                    TimeUtility.convertJalaliDatesListToGregorian(dates),
                    discountCode
            );
            output.put("discountInfo", discountInfo);

            return generateSuccessMsg("data", output);
        } catch (Exception x) {
            return generateErr(x.getMessage());
        }
    }

    public CalculatePriceResult calcPrice(Room room, List<String> dates, int adults, int children) {

        // todo: consider children independent
        adults += children;

        long totalPrice = 0;

        List<DatePrice> datePrices = room.getDatePrices();
        List<DatePrice> pricesDetail = new ArrayList<>();

        int exceedPassenger = Math.max(0, adults - room.getCap());

        for (String date : dates) {

            int nightPrice = -1;

            int additionalPassengerPrice = 0;

            if (datePrices != null) {
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

                if (exceedPassenger > 0)
                    additionalPassengerPrice = room.getWeekendCapPrice() != null ?
                            exceedPassenger * room.getWeekendCapPrice() :
                            exceedPassenger * room.getCapPrice();

                pricesDetail.add(new DatePrice(room.getWeekendPrice(), additionalPassengerPrice, date, "weekend"));
                totalPrice += room.getWeekendPrice() + additionalPassengerPrice;
            } else {

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

                if (exceedPassenger > 0)
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

        //return new PairValue(totalPrice, pricesDetail);
        return new CalculatePriceResult()
                .setTotalPrice(totalPrice)
                .setDatePriceList(pricesDetail);
    }

    @Transactional //Todo: This annotation needs replica set to work
    public String reserve(ObjectId roomId, ReservationRequestDTO reservationRequestDTO, ObjectId userId, String discountCode) {

        List<RoomDateReservationState> roomDateSafetyList = new ArrayList<>();

        try {

            ObjectId tripId = reservationRequestDTO.getTripId();

            // Get passengers and creator data from Passenger backend
            JSONObject passengerServiceResponse = getPassengersAndCreatorData(tripId, userId);

            // Extract Passengers data
            PassengersExtractedData passengersExtractedData = getPassengersData(passengerServiceResponse);


            TripInfo tripInfo = new TripInfo(
                    passengersExtractedData.getAdults(),
                    passengersExtractedData.getChildren(),
                    passengersExtractedData.getInfants(),
                    reservationRequestDTO.getStartDate(),
                    reservationRequestDTO.getNights()
            );

            PairValue canReservePairValue = canReserve(roomId, tripInfo, false, roomDateSafetyList);

            Room room = (Room) canReservePairValue.getKey();
            List<String> jalaliDates = (List<String>) canReservePairValue.getValue();

            CalculatePriceResult calculatePriceResult = calcPrice(room, jalaliDates, passengersExtractedData.getAdults(), passengersExtractedData.getChildren());
            Long totalAmount = calculatePriceResult.getTotalPrice();

            List<LocalDateTime> residenceDatesInGregorian = TimeUtility.convertJalaliDatesListToGregorian(jalaliDates);

            DiscountInfo discountInfo = getDiscountInfoAndHandleRollBack(room, totalAmount, roomDateSafetyList, residenceDatesInGregorian, discountCode);

            ReservationRequest reservationRequest = createReservationRequest(
                    passengerServiceResponse,
                    passengersExtractedData,
                    reservationRequestDTO,
                    tripId,
                    userId,
                    calculatePriceResult,
                    room,
                    totalAmount,
                    residenceDatesInGregorian.get(0),
                    tripInfo.getNights(),
                    residenceDatesInGregorian,
                    discountInfo);
            reservationRequestRepository.insert(reservationRequest);

            // Set initial state of reserve request
            setInitialReserveRequestState(roomDateSafetyList, room.isOnlineReservation(), reservationRequest);

            if (room.isOnlineReservation())
                // Todo: Inform the owner: Your room has received a request, Please set your respond
                //todo: go to bank
                ;

            return generateSuccessMsg("data", new JSONObject()
                    .put("trackingCode", reservationRequest.getTrackingCode())
                    .put("reservationId", reservationRequest.get_id())
                    .put("TotalAmount", reservationRequest.getTotalAmount())
                    .put("TotalDiscount", reservationRequest.getDiscountInfo().getTotalDiscount())
            );

        } catch (RoomNotFreeException | RoomExceedCapacityException | RoomUnavailableByOwnerException |
                 InvalidIdException | BackendErrorException | InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error: " + e.getMessage());
            throw new RuntimeException("Unexpected error while reservation process");
        }

    }

    private DiscountInfo getDiscountInfoAndHandleRollBack(
            Room room,
            long totalAmount,
            List<RoomDateReservationState> roomDateSafetyList,
            List<LocalDateTime> residenceDatesInGregorian,
            String discountCode
    ) {

        try {
            return discountService.buildDiscountInfo(
                    room.getTitle(),
                    room.getPrice().longValue(),
                    totalAmount, room.getBoomId(),
                    residenceDatesInGregorian,
                    discountCode);
        } catch (Exception e) {

            String exceptionClassName = e.getClass().getSimpleName();
            String invalidInputExceptionClassName = InvalidInputException.class.getSimpleName();
            if (exceptionClassName.equals(invalidInputExceptionClassName)) {

                for (RoomDateReservationState roomDateReservationState : roomDateSafetyList) {
                    roomDateReservationState.setRoomStatus(FREE);
                    roomDateReservationStateService.save(roomDateReservationState);

                    log.info(String.format("Status of room: %s, in date: %s, changed to: %s",
                            roomDateReservationState.getRoomObjectId(),
                            roomDateReservationState.getTargetDate(),
                            FREE));
                }
                throw e;
            }
        }
        return null;
    }

    private void setInitialReserveRequestState(List<RoomDateReservationState> roomDateSafetyList, boolean isRoomDirectReservable, ReservationRequest reservationRequest) {
        if (isRoomDirectReservable) {
            try {
                reservationRequestService.changeReservationRequestStatus(reservationRequest.get_id(), WAIT_FOR_PAYMENT_1);
            } catch (OptimisticLockingFailureException e) {
                rollBackRoomStatusToFree(roomDateSafetyList);
                reservationRequestService.changeReservationRequestStatus(reservationRequest.get_id(), SYSTEM_ERROR);
                log.error("Unexpected optimistic lock: " + e.getMessage());
                throw new RuntimeException("Unexpected optimistic lock: ");
            }
        } else {
            try {

                reservationRequestService.changeReservationRequestStatus(reservationRequest.get_id(), WAIT_FOR_OWNER_RESPONSE);
            } catch (OptimisticLockingFailureException e) {
                rollBackRoomStatusToFree(roomDateSafetyList);
                reservationRequestService.changeReservationRequestStatus(reservationRequest.get_id(), SYSTEM_ERROR);
                log.error("Unexpected optimistic lock: " + e.getMessage());
                throw new RuntimeException("Unexpected optimistic lock: ");
            }
        }
    }

    private void rollBackRoomStatusToFree(List<RoomDateReservationState> roomDateReservationStateList) {
        /* Rollback to Free */

        for (RoomDateReservationState roomDateReservationState : roomDateReservationStateList) {
            roomDateReservationState.setRoomStatus(FREE);
            roomDateReservationStateService.save(roomDateReservationState);
        }

    }

    private JSONObject getPassengersAndCreatorData(ObjectId tripId, ObjectId userId) {
        /* This function gets data regarding passengers and the creator fro Passenger backend, by a HTTP call */

        JSONObject passengerServiceResponse = Network.sendGetReq(PASSENGER_URL + "system/trip/getTripPassengers/" + tripId + "/" + userId);
        if (passengerServiceResponse == null)
            throw new InvalidIdException("Trip id is not valid");

        return passengerServiceResponse;
    }

    private PassengersExtractedData getPassengersData(JSONObject passengerServiceResponse) {

        int children = 0, adults = 0, infants = 0;

        JSONArray passengersJSON = passengerServiceResponse.getJSONObject("data").getJSONArray("passengers");
        List<Document> passengers = new ArrayList<>();

        for (int j = 0; j < passengersJSON.length(); j++) {
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

        if (adults == 0)
            throw new NoAdultsInPassengersException("تعداد بزرگسال نمی تواند صفر باشد");

        return PassengersExtractedData.builder()
                .adults(adults)
                .children(children)
                .infants(infants)
                .passengersInfo(passengers)
                .build();
    }

    private ReservationRequest createReservationRequest(JSONObject passengerServiceResponse,
                                                        PassengersExtractedData passengersExtractedData,
                                                        ReservationRequestDTO reservationRequestDTO,
                                                        ObjectId tripId,
                                                        ObjectId userId,
                                                        CalculatePriceResult calculatePriceResult,
                                                        Room room,
                                                        Long totalAmount,
                                                        LocalDateTime residenceStartDate,
                                                        int numberOfStayingNights,
                                                        List<LocalDateTime> gregorianResidenceDates,
                                                        DiscountInfo discountInfo) {

        ReservationRequest reservationRequest = new ReservationRequest();

        reservationRequest.setCreator(Document.parse(passengerServiceResponse.getJSONObject("data").getJSONObject("creator").toString()));
        reservationRequest.setPassengers(passengersExtractedData.getPassengersInfo());

        reservationRequest.setAdults(passengersExtractedData.getAdults());
        reservationRequest.setChildren(passengersExtractedData.getChildren());
        reservationRequest.setInfants(passengersExtractedData.getInfants());

        if (reservationRequestDTO.getDescription() != null)
            reservationRequest.setDescription(reservationRequestDTO.getDescription());

        reservationRequest.setPassengersId(tripId);
        reservationRequest.setPrices((List<DatePrice>) calculatePriceResult.getDatePriceList());
        reservationRequest.setTotalAmount(totalAmount);
        reservationRequest.setOwnerId(room.getUserId());

        reservationRequest.setStatus(
                //room.isOnlineReservation() ? ReservationStatus.RESERVED : ReservationStatus.PENDING
                ReservationStatus.REGISTERED_RESERVE_REQUEST
        );

        reservationRequest.addToReservationStatusHistory(new ReservationStatusDate(LocalDateTime.now(), ReservationStatus.REGISTERED_RESERVE_REQUEST));

        reservationRequest.setReserveExpireAt(room.isOnlineReservation() ?
                System.currentTimeMillis() + ((long) payment1Timeout * 60 * 1000) :
                System.currentTimeMillis() + ((long) ownerResponseTimeout * 60 * 1000)
        );
        reservationRequest.setUserId(userId);
        reservationRequest.setRoomId(room.get_id());

        reservationRequest.setTrackingCode(generateTrackingCode());

        reservationRequest.setResidenceStartDate(residenceStartDate);

        reservationRequest.setNumberOfStayingNights(numberOfStayingNights);

        reservationRequest.setGregorianResidenceDates(gregorianResidenceDates);

        reservationRequest.setDiscountInfo(discountInfo);

        return reservationRequest;

    }

    private String generateTrackingCode() {

        for (int i = 0; i < 100; i++) {
            String trackingCode = randomString(trackingCodeLength);
            if (reservationRequestRepository2.countTrackingCode(trackingCode) == 0) {
                return trackingCode;
            }
        }

        throw new BackendErrorException("Tracking code generation is duplicate for 100 times! Change your tracking code generation algorithm");
    }

    public String toggleAccessibility(ObjectId id, ObjectId userId) {

        Room room;
        try {
            room = canModify(id, userId, true);
        } catch (InvalidFieldsException e) {
            return generateErr(e.getMessage());
        }

        room.setAvailability(!room.isAvailability());
        saveAllSimilar(room.getTitle(), room);

        return JSON_OK;
    }

    public String toggleClean(ObjectId id, ObjectId userId) {

        Room room;
        try {
            room = canModify(id, userId, false);
        } catch (InvalidFieldsException e) {
            return generateErr(e.getMessage());
        }

        room.setClean(!room.isClean());
        roomRepository.save(room);

        return JSON_OK;
    }

    public List<RoomStatusDate> getRoomStatusForNext5days(ObjectId roomId) {

        return roomRepository2.getRoomStatusForNext5days(roomId);
    }

    public GuestCountGetDto getNumberOfGuestsByRoomIdAndDate(ObjectId roomId, LocalDateTime targetDate) {

        return roomRepository2.getNumberOfGuestsByRoomIdAndDate(roomId, targetDate);
    }

    public List<String> fetchDistinctRoomNamesOfBoom(ObjectId boomId) {

        return roomRepository2.fetchDistinctRoomNamesOfBoom(boomId);
    }

}
