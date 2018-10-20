#User management service
##Docker build locally
docker build .

##Database
docker run -d --name pg-orders -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=user -p 5432:5432 postgres:10.5
