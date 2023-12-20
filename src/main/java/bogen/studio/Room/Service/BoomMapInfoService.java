package bogen.studio.Room.Service;

import bogen.studio.Room.Repository.BoomMapInfoRepository;
import bogen.studio.Room.documents.BoomMapInfo;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoomMapInfoService {

    private final BoomMapInfoRepository boomMapInfoRepository;

    public BoomMapInfo fetchByBoomId(ObjectId boomId) {

        return boomMapInfoRepository.fetchByBoomId(boomId);
    }


    public BoomMapInfo insert(BoomMapInfo boomMapInfo) {
        /* Insert document to the database */

        return boomMapInfoRepository.insert(boomMapInfo);
    }
}
