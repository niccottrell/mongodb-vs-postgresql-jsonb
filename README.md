# mongodb-vs-postgresql-jsonb

The goal of this POC was to test the implementation and performance of storing JSON data in Postgrsql 9.4+ vs using native JSON structures in MongoDB.

We generate 10k documents in both MongoDB and Postgresql using Spring Boot + JPA + Hibernate and add appropriate indexes. 

## Getting started

1. Install local Postgres (with database "postgres")
1. Install local MongoDB (with database "PgPerf")
1. Install [DynamoDB Local](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.html#DynamoDBLocal.DownloadingAndRunning) 
1. Run `mvn compile` to build QueryDSL sources
1. Run `mvn test` to run performance tests

### sql4lite dependencies for Mac

```bash
wget https://d1.almworks.com/.files/sqlite4java/sqlite4java-392.zip
unzip sqlite4java-392.zip
sudo cp sqlite4java-392/*.dylib /Library/Java/Extensions/
```

## Results

### We test the following indexes on the JSONB columns:

```sql
CREATE INDEX ON example USING BTREE ((data->>'stock'));
CREATE INDEX ON example USING HASH ((data->>'stock'));
CREATE INDEX ON example USING GIN ((data));
CREATE INDEX ON example USING BTREE (cast (data->>'stock' as int));
```

### Time taken

Test| Time taken|
------------- |:-------------:|
testPerfMongo | 23.519s |
testPerfPg | 54.798s |

### Example data

In MongoDB, documents look like:

![MongoDB example](example_mongo.png)

In Postgresql, rows look like:

![Postgresql example](example_jsonb.png)

# Conclusions

## Disadvantages of Postgres/JSONB vs MongoDB

### Constraints/Validation

While PostgreSQL JSONB type provides flexibility, it should be used just when appropriate. The only check being performed is that stored data is actually in a valid JSON format. You cannot impose any other constraints as with regular columns - such as not null or enforce a particular Data Type (Integer, VarChar, Date). Therefore it is best suited for providing an additional optional set of data to an entity, where you cannot be sure before which data is would contain. And such data would differ a lot among each of the rows. Such example can be a user-provided set of additional data. You should always carefully consider which data is better suited as regular columns and which should be stored as JSON.

### Lack of stats

`jsonb` columns have a flat 1% statistics rate causing poor lookup strategies (unlike MongoDB)

### Range queries

Consider
```sql
EXPLAIN ANALYZE SELECT *
FROM example
WHERE to_date(data->>'date', 'YYYY-MM-DD') 
       BETWEEN '2018-02-01' 
       AND     '2020-03-01'
AND data->>'name' = 'Name 7';
```
or even
```sql
EXPLAIN analyze  SELECT * FROM example
WHERE ((data->>'stock')::integer)  > 15000;
```
   
The results should that neither approach can use indexes for range queries - a filter need to be run.   
   
### GIN Index is based on string format of value

Consider 
```sql
EXPLAIN ANALYZE SELECT * FROM example
WHERE ((data->>'stock')::integer)  = 15000;
```   
The casting as `integer` means that a table scan and filter is performed - no index.   
   
However, you can do:
```sql
CREATE INDEX ON example USING BTREE (cast (data->>'stock' as int));
```   
which does seem to create a usable index for the above query.
### Compound queries

`WHERE DATE > ? AND FEATURES.Ref = ?`

# Acknowledgements

Some code forked from https://www.vojtechruzicka.com/postgresqls-jsonb-type-mapping-using-hibernate/

Official docs:
* [Spring Data MongoDB](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/)
* Details on Postgres's [GIN Indexes](https://www.postgresql.org/docs/current/static/gin.html)
* [JSON Types](https://www.postgresql.org/docs/current/static/datatype-json.html)
* [JSON Functions and Operators](https://www.postgresql.org/docs/9.5/static/functions-json.html)
* [GiST and GIN Index Types](https://www.postgresql.org/docs/9.1/static/textsearch-indexes.html)

Postgresql articles:
* [About GIN internals](http://bitnine.net/blog-postgresql/postgresql-internals-jsonb-type-and-its-indexes/)
* [About index sizes](https://blog.2ndquadrant.com/jsonb-type-performance-postgresql-9-4/)
* [About index statistics](https://blog.anayrat.info/en/2017/11/26/postgresql---jsonb-and-statistics/)
* [About indexes as text](https://stackoverflow.com/questions/36075918/postgresql-index-on-json)
* [Checking existence of a key](https://stackoverflow.com/questions/28921355/how-do-i-check-if-a-json-key-exists-in-postgres)
* [Compares Posgresql JSON to JSONB data types](https://blog.2ndquadrant.com/processing-json/)
* [When To Avoid JSONB In A PostgreSQL Schema](https://heapanalytics.com/blog/engineering/when-to-avoid-jsonb-in-a-postgresql-schema)

DynamoDB articles:
* [General Guidelines for Secondary Indexes in DynamoDB](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/bp-indexes-general.html)