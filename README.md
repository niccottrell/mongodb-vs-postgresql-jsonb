

## Disadvantages

While PostgreSQL JSONB type provides flexibility, it should be used just when appropriate. The only check being performed is that stored data is actually in a valid JSON format. You cannot impose any other constraints as with regular columns - such as not null or enforce a particular Data Type (Integer, VarChar, Date). Therefore it is best suited for providing an additional optional set of data to an entity, where you cannot be sure before which data is would contain. And such data would differ a lot among each of the rows. Such example can be a user-provided set of additional data. You should always carefully consider which data is better suited as regular columns and which should be stored as JSON.

# Acknowledgements

Some code forked from https://www.vojtechruzicka.com/postgresqls-jsonb-type-mapping-using-hibernate/

