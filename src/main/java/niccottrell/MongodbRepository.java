package niccottrell;

import niccottrell.model.ExampleMongo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface MongodbRepository extends MongoRepository<ExampleMongo, String>,
        QuerydslPredicateExecutor<ExampleMongo> {

  List<ExampleMongo> findByName(String name);

  @Query("{date: { $gt: ?0 } }")
  List<ExampleMongo> findByDateAfter(Date minDate);

}

