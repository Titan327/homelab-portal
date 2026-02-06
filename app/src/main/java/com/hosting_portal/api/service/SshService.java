package com.hosting_portal.api.service;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class SshService {

    private static final Logger log = LoggerFactory.getLogger(SshService.class);

    @Value("${ssh.default.host}")
    private String host;

    @Value("${ssh.default.port}")
    private int port;

    @Value("${ssh.default.username}")
    private String username;

    public String executeCommand(String command) {

        JSch jsch = new JSch();
        Session session = null;
        ChannelExec channelExec = null;

        try {

            String knownHostsPath = "/app/config/known_hosts";
            String privateKeyPath = "/app/ssh/hosting-portal-key";

            File knownHostsFile = new File(knownHostsPath);

            if (!knownHostsFile.exists()) {
                log.error("ERREUR FATALE: Fichier known_hosts INTROUVABLE: {}", knownHostsPath);
                throw new RuntimeException("SÉCURITÉ: Le fichier known_hosts est OBLIGATOIRE: " + knownHostsPath);
            }

            jsch.setKnownHosts(knownHostsPath);

            File privateKeyFile = new File(privateKeyPath);

            if (!privateKeyFile.exists()) {
                throw new RuntimeException("Clé SSH privée introuvable: " + privateKeyPath);
            }

            jsch.addIdentity(privateKeyPath);

            log.info("Connexion SSH à {}@{}:{}", username, host, port);
            session = jsch.getSession(username, host, port);

            // Configuration pour forcer la vérification des clefs via le fichier know host
            session.setConfig("StrictHostKeyChecking", "yes");

            // Connexion avec timeout de 10 secondes
            session.connect(10000);
            log.info("Connexion SSH établie");

            // Ouvrir un canal pour exécuter la commande
            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);
            log.info(command);

            // Capturer TOUT dans le même flux (stdout + stderr)
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            channelExec.setOutputStream(outputStream);
            channelExec.setErrStream(outputStream);  // Combine stdout + stderr

            // Exécuter la commande
            channelExec.connect();

            while (!channelExec.isClosed()) {
                Thread.sleep(100);
            }

            // Récupérer le code de sortie
            int exitStatus = channelExec.getExitStatus();
            String output = outputStream.toString();

            log.info("Code de sortie: {}", exitStatus);
            log.info("SORTIE BASH (stdout + stderr):\n{}", output);

            if (exitStatus != 0) {
                log.error("Commande échouée avec le code: {}", exitStatus);
                throw new RuntimeException("Erreur SSH (code " + exitStatus + "): " + output);
            }

            return output;

        } catch (JSchException e) {
            log.error("Erreur de connexion SSH: {}", e.getMessage());
            throw new RuntimeException("Erreur SSH: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Erreur lors de l'exécution: {}", e.getMessage());
            throw new RuntimeException("Erreur: " + e.getMessage(), e);
        } finally {
            // Fermer proprement les connexions
            if (channelExec != null && channelExec.isConnected()) {
                channelExec.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
            log.info("Connexion SSH fermée");
        }
    }

    /**
     * Exécute plusieurs commandes SSH de manière séquentielle
     */
    public String executeMultipleCommands(String... commands) {

        StringBuilder results = new StringBuilder();

        for (String command : commands) {
            results.append("=== Commande: ").append(command).append(" ===\n");
            String result = executeCommand(command);
            results.append(result).append("\n\n");
        }

        return results.toString();
    }

    /**
     * Exécute un script avec des variables d'environnement
     */
    public String executeResourceScript(String scriptName, Map<String, String> variables) {
        try {
            log.info("Lecture du script: {}", scriptName);

            ClassPathResource resource = new ClassPathResource("scripts/" + scriptName);

            if (!resource.exists()) {
                throw new RuntimeException("Script introuvable: scripts/" + scriptName);
            }

            String scriptContent = new String(
                    resource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            StringBuilder envVars = new StringBuilder();
            if (variables != null && !variables.isEmpty()) {
                log.info("Variables injectées: {}", variables.keySet());
                for (Map.Entry<String, String> entry : variables.entrySet()) {
                    String escapedValue = entry.getValue().replace("'", "'\\''");
                    envVars.append(String.format("export %s='%s'\n",
                            entry.getKey(),
                            escapedValue));
                }
            }

            String command = String.format(
                    "bash << 'END_OF_SCRIPT'\n%s%s\nEND_OF_SCRIPT",
                    envVars,
                    scriptContent
            );

            log.info("Exécution du script via SSH...");
            log.info("Commande complète:\n{}", command);

            String result = executeCommand(command);

            log.info("Script exécuté avec succès");
            log.info("SORTIE COMPLÈTE DU SCRIPT:\n{}", result);

            return result;

        } catch (IOException e) {
            log.error("Erreur lecture du script {}: {}", scriptName, e.getMessage());
            throw new RuntimeException("Impossible de lire le script: " + scriptName, e);
        }
    }

    /**
     * Surcharge pour exécuter sans variables
     */
    public String executeResourceScript(String scriptName) {
        return executeResourceScript(scriptName, null);
    }
}