# erp

#client 
ng serve --open


#server
./gradlew bootRun

## db

curl -X POST "http://localhost:8080/api/debug/reset-database?mode=empty"


curl -X POST "http://localhost:8080/api/debug/reset-database?mode=mock"

curl -i GET "http://localhost:8080/api/debug/database-overview"
