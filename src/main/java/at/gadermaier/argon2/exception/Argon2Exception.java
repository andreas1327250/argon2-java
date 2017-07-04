package at.gadermaier.argon2.exception;

/* dislike checked exceptions */
class Argon2Exception extends RuntimeException {
    Argon2Exception(String message) {
        super(message);
    }
}
