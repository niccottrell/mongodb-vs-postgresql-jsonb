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


select * from example
where features @>  '{"Ref": "ABC15"}'::jsonb;
