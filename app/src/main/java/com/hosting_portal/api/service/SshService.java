package com.hosting_portal.api.service;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class SshService {

    private static final Logger log = LoggerFactory.getLogger(SshService.class);

    @Value("${ssh.default.host}")
    private String host;

    @Value("${ssh.default.port}")
    private int port;

    @Value("${ssh.default.username}")
    private String username;

    @Value("${ssh.default.password}")
    private String password;


    public String executeCommand(String command) {

        JSch jsch = new JSch();
        Session session = null;
        ChannelExec channelExec = null;

        try {
            log.info("Connexion SSH à {}@{}:{}", username, host, port);

            // Créer la session SSH
            session = jsch.getSession(username, host, port);
            session.setPassword(password);

            // Configuration pour éviter la vérification stricte des clés
            // ATTENTION: À utiliser uniquement en dev/test
            session.setConfig("StrictHostKeyChecking", "no");

            // Connexion avec timeout de 10 secondes
            session.connect(10000);
            log.info("Connexion SSH établie");

            // Ouvrir un canal pour exécuter la commande
            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);

            // Capturer la sortie standard et les erreurs
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

            channelExec.setOutputStream(outputStream);
            channelExec.setErrStream(errorStream);

            // Exécuter la commande
            channelExec.connect();
            log.info("Commande exécutée: {}", command);

            // Attendre la fin de l'exécution
            while (!channelExec.isClosed()) {
                Thread.sleep(100);
            }

            // Récupérer le code de sortie
            int exitStatus = channelExec.getExitStatus();
            log.info("Code de sortie: {}", exitStatus);

            // Récupérer les résultats
            String output = outputStream.toString();
            String error = errorStream.toString();

            if (exitStatus != 0 && !error.isEmpty()) {
                log.error("Erreur d'exécution: {}", error);
                return "ERREUR: " + error;
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
     * Connexion avec clé privée SSH
     */
    public String executeCommandWithKey(String host, int port, String username,
                                        String privateKeyPath, String command) {

        JSch jsch = new JSch();
        Session session = null;
        ChannelExec channelExec = null;

        try {
            log.info("Connexion SSH avec clé privée à {}@{}:{}", username, host, port);

            // Ajouter la clé privée
            jsch.addIdentity(privateKeyPath);

            session = jsch.getSession(username, host, port);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(10000);

            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            channelExec.setOutputStream(outputStream);
            channelExec.connect();

            while (!channelExec.isClosed()) {
                Thread.sleep(100);
            }

            return outputStream.toString();

        } catch (Exception e) {
            log.error("Erreur SSH avec clé: {}", e.getMessage());
            throw new RuntimeException("Erreur SSH: " + e.getMessage(), e);
        } finally {
            if (channelExec != null) channelExec.disconnect();
            if (session != null) session.disconnect();
        }
    }
}