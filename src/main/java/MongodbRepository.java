import java.util.Date;
import java.util.List;

import model.ExampleMongo;
import model.ExamplePg;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MongodbRepository extends MongoRepository<ExampleMongo, String> {

  @Query("{'features.k': ?0 }")
  public List<ExampleMongo> findByFeature(String key);

  @Query("{'features': {'$elemMatch': {k: ?0, v: ?1 }}}")
  public List<ExampleMongo> findByFeature(String key, String value);

  List<ExampleMongo> findByName(String name);

  @Query("{domain: { $regex: ?0 } }")
  List<ExampleMongo> findByAfter(Date minDate);

}

