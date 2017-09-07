package at.gadermaier.argon2;

import at.gadermaier.argon2.exception.Argon2InvalidParameterException;

import static at.gadermaier.argon2.Constants.Constraints.*;
import static at.gadermaier.argon2.Constants.Messages.*;

class Validation {

    static void validateInput(Argon2 argon2){
        String message = null;

        if (argon2.getLanes() < MIN_PARALLELISM)
            message = P_MIN_MSG;
        else if (argon2.getLanes() > MAX_PARALLELISM)
            message = P_MAX_MSG;
        else if(argon2.getMemory() < 2 * argon2.getLanes())
            message = M_MIN_MSG;
        else if(argon2.getIterations() < MIN_ITERATIONS)
            message = T_MIN_MSG;
        else if(argon2.getIterations() > MAX_ITERATIONS)
            message = T_MAX_MSG;
        else if(argon2.getPasswordLength() < MIN_PWD_LENGTH)
            message = PWD_MIN_MSG;
        else if(argon2.getPasswordLength() > MAX_PWD_LENGTH)
            message = PWD_MAX_MSG;
        else if(argon2.getSaltLength() < MIN_SALT_LENGTH)
            message = SALT_MIN_MSG;
        else if(argon2.getSaltLength() > MAX_SALT_LENGTH)
            message = SALT_MAX_MSG;
        else if(argon2.getSecretLength() > MAX_SECRET_LENGTH)
            message = SECRET_MAX_MSG;
        else if(argon2.getAdditionalLength() > MAX_AD_LENGTH)
            message = ADDITIONAL_MAX_MSG;

            if(message != null)
                throw new Argon2InvalidParameterException(message);
    }
}
