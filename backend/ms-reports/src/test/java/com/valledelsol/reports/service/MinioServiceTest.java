package com.valledelsol.reports.service;

import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import io.minio.PutObjectArgs;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MinioServiceTest {

    private MinioService minioService;
    private MinioClient minioClient;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        minioService = new MinioService();
        minioClient = mock(MinioClient.class);

        Field clientField = MinioService.class.getDeclaredField("minioClient");
        clientField.setAccessible(true);
        clientField.set(minioService, minioClient);

        Field bucketField = MinioService.class.getDeclaredField("bucketName");
        bucketField.setAccessible(true);
        bucketField.set(minioService, "reports");

        Field externalUrlField = MinioService.class.getDeclaredField("externalUrl");
        externalUrlField.setAccessible(true);
        externalUrlField.set(minioService, "https://minio");
    }

    @Test
    void testUploadFileReturnsUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", "content".getBytes());
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        String url = minioService.uploadFile(file);

        assertEquals(true, url.startsWith("https://minio/reports/"));
    }

    @Test
    void testUploadFileWithEmptyFileReturnsNull() {
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", new byte[0]);

        String url = minioService.uploadFile(file);

        assertEquals(null, url);
    }
}
