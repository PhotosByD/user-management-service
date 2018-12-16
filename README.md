# User management service
## Docker build locally
docker build user-service .<br>
docker run -d --name user-service -p 8080:8080 user-service

## Database
docker run -d --name pg-users -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=user -p 5432:5432 postgres:10.5

## ETCD
export HostIP="192.168.99.100"
docker run -d -p 2379:2379 \
   --name etcd \
   --volume=/tmp/etcd-data:/etcd-data \
   quay.io/coreos/etcd:latest \
   /usr/local/bin/etcd \
   --name my-etcd-1 \
   --data-dir /etcd-data \
   --listen-client-urls http://0.0.0.0:2379 \
   --advertise-client-urls http://0.0.0.0:2379 \
   --listen-peer-urls http://0.0.0.0:2380 \
   --initial-advertise-peer-urls http://0.0.0.0:2380 \
   --initial-cluster my-etcd-1=http://0.0.0.0:2380 \
   --initial-cluster-token my-etcd-token \
   --initial-cluster-state new \
   --auto-compaction-retention 1 \
   -cors="*"
