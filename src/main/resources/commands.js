/**
 * This sets up the environement and gives example using Mongo shell
 */

use PgPerf;

// This creates indexes the same as Populate.createMongoIndexes
db.example.createIndex({"name": 1});
db.example.createIndex({"date": 1});
db.example.createIndex({"features.Ref": 1});
db.example.createIndex({"features.Ref": 1, "date":1});
