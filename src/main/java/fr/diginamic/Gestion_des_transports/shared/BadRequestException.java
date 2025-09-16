package fr.diginamic.Gestion_des_transports.shared;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
