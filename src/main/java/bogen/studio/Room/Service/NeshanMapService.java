package bogen.studio.Room.Service;

import bogen.studio.Room.Exception.BackendErrorException;
import bogen.studio.Room.Exception.ExternalServiceCallException;
import bogen.studio.Room.Exception.InvalidInputException;
import bogen.studio.Room.Models.Boom;
import bogen.studio.Room.Models.Room;
import bogen.studio.Room.Repository.NeshanMapDataRepository;
import bogen.studio.Room.documents.BoomMapInfo;
import bogen.studio.Room.documents.NeshanMapData;
import bogen.studio.Room.documents.Place;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NeshanMapService {

    private final RoomService roomService;
    private final BoomService boomService;
    private final BoomMapInfoService boomMapInfoService;
    private final PlaceService placeService;
    private final NeshanMapDataRepository neshanMapDataRepository;

    private final String targetFolderPath = "./boom_maps/";


    public String fetchBoomMapPath(ObjectId roomId) {
        /* This method fetches map path of the boom by input roomId.
         * 1. Fetch boom
         * 2. Check if there is a saved map for the boom
         * 3. If there is no saved map: 0) Fetch place, 1) Download map, 2) Insert map data info to DB
         * 4. return the map path */

        // 1.
        Boom boom = fetchBoomByRoomId(roomId);

        // 2.
        BoomMapInfo boomMapInfo = boomMapInfoService.fetchByBoomId(boom.get_id());
        if (boomMapInfo == null) {
            // 3.0
            Place place = placeService.fetchById(boom.getPlaceId());

            // 3.1
            String mapPath = downloadAndSaveMapForBoom(place.getC(), place.getD(), boom.get_id());

            // 3.2
            insertBoomMapInfoToDb(boom.get_id(), mapPath, place.getC(), place.getD());

            // 4
            return mapPath;
        }

        // 4
        return boomMapInfo.getMapPath();
    }

    private void insertBoomMapInfoToDb(ObjectId boomId, String mapPath, Double latitude, Double longitude) {
        /* Insert boomMapInfo to database */

        BoomMapInfo boomMapInfo = new BoomMapInfo()
                .setBoomId(boomId)
                .setMapPath(mapPath)
                .setLatitude(latitude)
                .setLongitude(longitude);

        boomMapInfoService.insert(boomMapInfo);
    }

    private String downloadAndSaveMapForBoom(Double latitude, Double longitude, ObjectId boomId) {
        /* Download map for input coordinates from Neshan map */

        if (latitude == null || longitude == null) {
            throw new InvalidInputException("مختصات جغرافیایی بوم در دیتابیس موجود نیست");
        }

        // Load Neshan map data
        NeshanMapData neshanMapData = loadNeshanMapData();
        if (neshanMapData == null) {
            neshanMapData = insertNeshanMapDataToDb();
        }

        // Download and save the image of the boom map
        String mapName = String.format("%s.png", boomId.toString());
        InputStream mapStream = downloadMapFromNeshanWebsite(neshanMapData, latitude, longitude);
        return saveMap(mapStream, targetFolderPath, mapName);
    }

    private InputStream downloadMapFromNeshanWebsite(NeshanMapData data, Double latitude, Double longitude) {
        /* Download map from Neshan map website */

        try {
            // Center of the map
            String center = latitude + "," + longitude;

            // Http call
            HttpResponse<InputStream> response = Unirest
                    .get(data.getBaseUrl())
                    .queryString("key", data.getKey())
                    .queryString("type", data.getType())
                    .queryString("zoom", data.getZoom())
                    .queryString("center", center)
                    .queryString("width", data.getWidth())
                    .queryString("height", data.getHeight())
                    .queryString("markerToken", data.getMarkerToken())
                    .asBinary();

            return response.getBody();

        } catch (UnirestException e) {
            log.error(e.getMessage());
            throw new ExternalServiceCallException("خطا در سرویس کال سایت نقشه نشان");
        }
    }

    private String saveMap(InputStream inputStream, String mapsDirectoryPath, String mapName) {
        /* Save map in the maps directory and return the path of the saved map */

        CreateMapsDirectory(mapsDirectoryPath);

        try {
            File file = new File(mapsDirectoryPath + mapName);
            FileUtils.copyInputStreamToFile(inputStream, file);
            log.info("Downloaded and saved map: " + mapName);
            return mapsDirectoryPath + mapName;
        } catch (Exception e) {
            log.error("error in saving downloaded map: " + e.getMessage());
            throw new BackendErrorException("خطا در ذخیره نقشه دانلود شده");
        }
    }

    private void CreateMapsDirectory(String mapsDirectoryPath) {
        /* Create directory of maps */

        File mapsDirectory = new File(mapsDirectoryPath);
        if (!mapsDirectory.exists()) {
            mapsDirectory.mkdirs();
        }
    }

    private NeshanMapData insertNeshanMapDataToDb() {
        /* Insert data regarding Neshan map to DB */

        NeshanMapData data = new NeshanMapData()
                .setBaseUrl("https://api.neshan.org/v4/static")
                .setKey("service.dc6a6301c0f64c2eb3ec44ed743555df") // Registered key for service in Neshan map website
                .setType("standard-day")
                .setZoom(16)
                .setWidth(600) // width of map
                .setHeight(600) // height of map
                .setMarkerToken("241037.EnucX5rr"); // Registered marker token in Neshan map website

        return neshanMapDataRepository.insert(data);

    }

    private NeshanMapData loadNeshanMapData() {
        /* This method loads data regarding neshan map from database */

        List<NeshanMapData> list = neshanMapDataRepository.fetchAll();

        if (list.size() > 0) {
            return list.get(0);
        }

        return null;
    }

    private Boom fetchBoomByRoomId(ObjectId roomId) {
        /* Fetch Boom by roomId */

        Room room = roomService.findById(roomId);

        return boomService.findById(room.getBoomId());
    }


}
