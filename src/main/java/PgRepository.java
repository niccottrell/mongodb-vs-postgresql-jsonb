import model.ExamplePg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
@Component
public interface PgRepository extends JpaRepository<ExamplePg, Long> {

  List<ExamplePg> findByFeature(String key);

  List<ExamplePg> findByFeature(String key, String value);

  List<ExamplePg> findByAfter(Date minDate);

  List<ExamplePg> findByName(String name);

}