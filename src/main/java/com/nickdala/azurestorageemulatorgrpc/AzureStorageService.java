package com.nickdala.azurestorageemulatorgrpc;

import com.azure.spring.cloud.core.resource.AzureStorageBlobProtocolResolver;
import com.nickdala.protobuf.storage.v1.*;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.WritableResource;
import org.springframework.grpc.server.service.GrpcService;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Stream;

@GrpcService
class AzureStorageEmulatorGrpcService extends StorageServiceGrpc.StorageServiceImplBase {
    private static final String BLOB_RESOURCE_PATTERN = "azure-blob://%s/%s";

    private final ResourceLoader resourceLoader;
    private final String containerName;
    private final AzureStorageBlobProtocolResolver azureStorageBlobProtocolResolver;

    public AzureStorageEmulatorGrpcService(ResourceLoader resourceLoader,
                                           @Value("${spring.cloud.azure.storage.blob.container-name}") String containerName,
                                           AzureStorageBlobProtocolResolver azureStorageBlobProtocolResolver) {
        this.resourceLoader = resourceLoader;
        this.containerName = containerName;
        this.azureStorageBlobProtocolResolver = azureStorageBlobProtocolResolver;
    }

    @Override
    public void listFiles(ListFilesRequest request, StreamObserver<ListFilesResponse> responseObserver) {
        try {
            Resource[] resources = azureStorageBlobProtocolResolver.getResources(String.format(BLOB_RESOURCE_PATTERN, this.containerName, "*.txt"));
            List<String> fileNames = Stream.of(resources).map(Resource::getFilename).toList();
            ListFilesResponse response = ListFilesResponse.newBuilder()
                    .addAllFileNames(fileNames)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IOException e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void upload(UploadRequest request, StreamObserver<UploadResponse> responseObserver) {
        Resource resource = resourceLoader.getResource(String.format(BLOB_RESOURCE_PATTERN, this.containerName, request.getFileName()));

        try (OutputStream os = ((WritableResource) resource).getOutputStream()) {
            os.write(request.getFileContent().getBytes(Charset.defaultCharset()));

            UploadResponse response = UploadResponse.newBuilder()
                    .setMessage("File uploaded successfully")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IOException e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void download(DownloadRequest request, StreamObserver<DownloadResponse> responseObserver) {
        Resource resource = resourceLoader.getResource(String.format(BLOB_RESOURCE_PATTERN, this.containerName, request.getFileName()));
        try {
            String fileContent = StreamUtils.copyToString(resource.getInputStream(), Charset.defaultCharset());
            DownloadResponse response = DownloadResponse.newBuilder()
                    .setFileContent(fileContent)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IOException e) {
            responseObserver.onError(e);
        }
    }
}
