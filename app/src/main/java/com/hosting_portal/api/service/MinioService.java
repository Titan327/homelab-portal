package com.hosting_portal.api.service;

import io.minio.*;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Slf4j
@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public void initBucket() throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build()
        );

        if (!exists) {
            log.info("Création du bucket: {}", bucketName);
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
            log.info("Bucket créé avec succès");
        } else {
            log.info("Le bucket {} existe déjà", bucketName);
        }
    }

    /**
     * Upload un script depuis un String
     */
    public void uploadScript(String scriptName, String scriptContent) throws Exception {
        initBucket();

        byte[] content = scriptContent.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(content);

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(scriptName)
                        .stream(stream, content.length, -1)
                        .contentType("text/x-shellscript")
                        .build()
        );

        log.info("Script uploadé: {}", scriptName);
    }

    /**
     * Upload un script depuis un fichier
     */
    public void uploadScriptFile(MultipartFile file) throws Exception {
        initBucket();

        String fileName = file.getOriginalFilename();

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType("text/x-shellscript")
                        .build()
        );

        log.info("Fichier script uploadé: {}", fileName);
    }

    /**
     * Télécharger un script
     */
    public InputStream downloadScript(String scriptName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(scriptName)
                        .build()
        );
    }

    /**
     * Lister tous les scripts
     */
    public Iterable<Result<Item>> listScripts() throws Exception {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .build()
        );
    }

    /**
     * Supprimer un script
     */
    public void deleteScript(String scriptName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(scriptName)
                        .build()
        );

        log.info("Script supprimé: {}", scriptName);
    }

    /**
     * Vérifier si un script existe
     */
    public boolean scriptExists(String scriptName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(scriptName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}