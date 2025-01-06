# Azure Storage Emulator example with Spring Boot and gRPC

This is a simple example of how to use the [Azure Storage Emulator](https://learn.microsoft.com/en-us/azure/storage/common/storage-use-azurite?tabs=visual-studio%2Cblob-storage) with [Spring Boot](https://docs.spring.io/spring-boot/index.html) and [gRPC](https://grpc.io/).

## Architecture

The application uses the `Spring Cloud Azure Storage Starter`to interact with the Azure Storage Emulator. The Azure Storage Emulator is a local emulator that provides a local development environment for Azure Storage. In this sample, we use the Blob Storage service exposed by the docker compose file `compose.yaml`.

```yaml
services:
  azurite:
    image: 'mcr.microsoft.com/azure-storage/azurite:latest'
    ports:
      - '10000'
      - '10001'
      - '10002'
```

To interact with Azure Storage, add the following dependency to your `pom.xml` file:

```xml
<dependency>
  <groupId>com.azure.spring</groupId>
  <artifactId>spring-cloud-azure-starter-storage</artifactId>
</dependency>
```

Configure the application to use the Azure Storage Emulator by setting the following properties in the `application.properties` file:

```yaml
spring.cloud.azure.storage.blob.container-name=contoso
spring.cloud.azure.storage.blob.connection-string=DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://127.0.0.1:10000/devstoreaccount1;
```

In production, these properties should be set to the values of your Azure Storage account and not hardcoded in the `application.properties` file. Connection strings in production should be replaced with Microsoft Managed Identity when possible. For more information on Azure Storage support using Microsoft Entra ID, check out the link [here]( https://learn.microsoft.com/en-us/azure/storage/blobs/authorize-access-azure-active-directory).

The application exposes a gRPC service with the following methods:

- `Upload`: Uploads a file to the Azure Storage Emulator.
- `Download`: Downloads a file from the Azure Storage Emulator.
- `ListFiles`: Lists all files in the Azure Storage Emulator.

The gRPC service is defined in the `proto` folder and is generated by the `protobuf-maven-plugin` plugin. The `storage.proto` file defines the following messages and service that correspond to the methods described above.

```proto
message UploadRequest {
  string file_name = 1;
  string file_content = 2;
}

message UploadResponse {
  string message = 1;
}

message DownloadRequest {
  string file_name = 1;
}

message DownloadResponse {
  string file_content = 1;
}

message ListFilesRequest {
}

message ListFilesResponse {
  repeated string file_names = 1;
}
```

```proto
service StorageService {
  rpc Upload (UploadRequest) returns (UploadResponse);
  rpc Download (DownloadRequest) returns (DownloadResponse);
  rpc ListFiles (ListFilesRequest) returns (ListFilesResponse);
}
```

## Build and Run the Application

### Prerequisites

- [Java 21](https://www.oracle.com/java/technologies/downloads/#java21)
- [Docker](https://www.docker.com/)
- [grpcurl](https://github.com/fullstorydev/grpcurl)

There is a [DevContainer](https://code.visualstudio.com/docs/remote/containers) that can be used to build and run the project in a container. This is useful if you don't have the prerequisites installed on your machine. You can open the project in Visual Studio Code and select the "Reopen in Container" option. Or you can use [CodeSpaces](https://github.com/features/codespaces) to build the project in the cloud.

### Build

Build the project with Maven:

```
./mvnw clean package
```

### Run the Application

Run the application:

```
./mvnw spring-boot:run
```

## Testing the gRPC Service

The following should be executed in a terminal after the application is running using `./mvnw spring-boot:run`. The application will be running and the gRPC service is exposed on port 9090.

### Verify Azurite is running

First verify that the Azurite container is running by running the following command:

```
docker ps
```

You should see the following output:

```
CONTAINER ID  IMAGE  COMMAND  CREATED  STATUS  PORTS  NAMES

7f6caeb1e3f4  mcr.microsoft.com/azure-storage/azurite:latest  "docker-entrypoint.s…"  6 hours ago  Up 5 seconds  0.0.0.0:32768->10000/tcp, [::]:32768->10000/tcp, 0.0.0.0:32769->10001/tcp, [::]:32769->10001/tcp, 0.0.0.0:32770->10002/tcp, [::]:32770->10002/tcp spring-boot-grpc-azure-storage-emulator-azurite-1 
```

### Test the gRPC Service

You can test the gRPC service with `grpcurl`.

#### Upload File

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

#### Download File

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

#### List Files

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

## Demo

![Demo](./images/demo.gif)