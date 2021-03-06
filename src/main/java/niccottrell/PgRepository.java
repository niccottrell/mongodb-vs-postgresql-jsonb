package niccottrell;

import niccottrell.model.ExamplePg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface PgRepository extends JpaRepository<ExamplePg, Long> {

  @Query(nativeQuery = true, value = "SELECT * FROM example WHERE features->>?1 IS NOT NULL")
  List<ExamplePg> findByFeatures(@Param("key") String key);

  @Query(nativeQuery = true, value = "SELECT * FROM example WHERE features @> jsonb_build_object(:key, :val)")
  List<ExamplePg> findByFeatures(@Param("key") String key, @Param("val") String value);

  @Query(nativeQuery = true, value = "SELECT * FROM example WHERE (data->>'date') > :d")
  List<ExamplePg> findByDateAfter(@Param("d") Date minDate);

  @Query(nativeQuery = true, value = "SELECT * FROM example WHERE (data->>'date') > :d AND features @> jsonb_build_object(:key, :val)")
  List<ExamplePg> findByDateAfterAndFeatures(@Param("d") Date minDate, @Param("key") String key, @Param("val") String value);

  @Query(nativeQuery = true, value = "SELECT * FROM example WHERE (data->>'name') = :name")
  List<ExamplePg> findByName(@Param("name") String name);

  @Query(nativeQuery = true, value = "SELECT * FROM example WHERE (cast (data->>'stock' as int)) > :d LIMIT 100")
  List<ExamplePg> findByStockGreaterThan(@Param("d") int stock);

}