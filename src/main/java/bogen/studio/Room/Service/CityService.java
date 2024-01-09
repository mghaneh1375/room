package bogen.studio.Room.Service;

import bogen.studio.Room.Repository.CityRepository;
import bogen.studio.Room.documents.City;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;

    public City fetchCityById(String id) {

        return cityRepository.fetchById(id);
    }


}
