# Azure Storage Emulator example with Spring Boot and GRPC

This is a simple example of how to use the Azure Storage Emulator with Spring Boot and GRPC.

## Build and Run the Application

Build the project with Maven:

```
./mvnw clean package
```

Run the application:

```
./mvnw spring-boot:run
```

## Usage

### Upload File

```
grpcurl -plaintext -d '{"file_name": "sample1.txt", "file_content": "hello azure storage"}' \
     localhost:9090 storage.v1.StorageService/Upload
```

Response:

```json
{
  "message": "File uploaded successfully"
}
```

### Download File

```
grpcurl -plaintext -d '{"file_name": "sample1.txt"}' \
     localhost:9090 storage.v1.StorageService/Download
```

Response:

```json
{
  "file_content": "hello azure storage"
}
```

### List Files

```
grpcurl -plaintext localhost:9090 storage.v1.StorageService/ListFiles     
```

Response:

```json
{
  "file_names": [
    "sample1.txt"
  ]
}
```