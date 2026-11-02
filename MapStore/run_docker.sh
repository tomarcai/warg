docker build -t map-store .
docker stop map-store
docker rm map-store
docker run -dit --name map-store -p 8080:80 -v "$PWD/database":/var/www/risk/database -v "$PWD/storage":/var/www/risk/storage map-store
