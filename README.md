#User management service
##Docker build locally
docker build user-service .<br>
docker run -d --name user-service -p 8080:8080 user-service

##Database
docker run -d --name pg-users -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=user -p 5432:5432 postgres:10.5
