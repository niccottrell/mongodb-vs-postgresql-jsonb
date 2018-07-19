select post_id,jsonb_pretty(json) from json_stack
where json_displayname(json) = 'anayrat' limit 1;

-- Create an index on the data.date field
CREATE INDEX ON example ((data ->> 'date'));

-- Create an index on the whole JSON structure (each top-level key?)
CREATE INDEX ON example USING GIN (data);

-- Query a field that IS indexed (note that date is written as a string)
explain (analyze,buffers)  select * from example
where data @>  '{"date": "Aug 7, 1907, 12:00:00 AM"}'::jsonb;
-- QUERY PLAN should show "Bitmap Index Scan"

-- Query a field that is NOT explicity indexed
explain (analyze,buffers)  select * from example
where data @>  '{"stock": 19993}'::jsonb;


SELECT * FROM example
where features @>  '{"Ref": "ABC15"}'::jsonb;

-- The following indexes don't seem to help range queries - QUERY PLAN shows "Rows Removed by Filter": 5000
CREATE INDEX ON example USING BTREE ((data->>'stock'));
CREATE INDEX ON example USING HASH ((data->>'stock'));
CREATE INDEX ON example USING GIN ((data->>'stock'));
CREATE INDEX ON example USING GIN ((data));
-- CREATE INDEX ON example ((data->>'stock')::int); -- Gives syntax error
CREATE INDEX ON example USING BTREE (cast (data->>'stock' as int)); -- This index can support cast range queries on int

EXPLAIN (analyze,buffers)  SELECT * FROM example
WHERE ((data->>'stock')::integer)  > 15000;

--- Uses bitmap index
EXPLAIN ANALYZE SELECT * FROM example
WHERE data @> '{"stock": 15000}'::JSONB;

--- No index used!
EXPLAIN ANALYZE SELECT * FROM example
WHERE ((data->>'stock')::integer)  = 15000;

SELECT * FROM example WHERE (data #>> '{stock}')::int > 15000
