function cleanup() {
    docker rm postgres
}

trap cleanup EXIT

docker run --name postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres:14.7-bullseye
