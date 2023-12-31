package bogen.studio.Room.Repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FilterableRepositoryImpl<T, D> implements FilterableRepository<T, D> {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<T> findAllWithFilter(Class<T> typeParameterClass, Filtering filtering, Pageable pageable) {
        Query query = constructQueryFromFiltering(filtering).with(pageable);
        return mongoTemplate.find(query, typeParameterClass);
//        return PageableExecutionUtils.getPage(ts, pageable, () -> mongoTemplate.count(query, typeParameterClass));
    }

    @Override
    public List<T> findAllWithFilter(Class<T> typeParameterClass, Filtering filtering) {
        Query query = constructQueryFromFiltering(filtering);
        return mongoTemplate.find(query, typeParameterClass);
    }

    @Override
    public List<D> findAllDigestWithFilter(Class<D> typeParameterClass, Filtering filtering) {
        Query query = constructQueryFromFiltering(filtering);
        return mongoTemplate.find(query, typeParameterClass);
    }


    @Override
    public List<Object> getAllPossibleValuesForFilter(Class<T> typeParameterClass, Filtering filtering, String filterKey) {
        Query query = constructQueryFromFiltering(filtering);
        return mongoTemplate.query(typeParameterClass).distinct(filterKey).matching(query).all();
    }
}

