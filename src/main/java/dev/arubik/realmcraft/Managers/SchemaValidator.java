package dev.arubik.realmcraft.Managers;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Logger;

import com.google.gson.JsonElement;

import dev.arubik.realmcraft.FileManagement.InteractiveFile;
import lombok.Setter;

public class SchemaValidator {
    InteractiveFile schemaFile;
    Boolean closed;

    public static class ClosedSchemaValidator extends Exception {
        private static final long serialVersionUID = 1L;
    }

    public static class InvalidSchema extends Throwable {
        private static final long serialVersionUID = 1L;
        String noFounded;

        public InvalidSchema(String noFounded) {
            this.noFounded = noFounded;
        }

        public void printStackTrace() {
            super.printStackTrace();
            internalPrint("Schema parts no founded: " + noFounded);
        }

        private void internalPrint(String print) {
            StringWriter sw = new StringWriter();
            Logger logger = Logger.getLogger("exceptions");
            sw.write(print);
            logger.warning(sw.toString());
        }
    }

    public void close() {
        closed = true;
        this.schemaFile = null;
    }

    public SchemaValidator(InteractiveFile schemaFile) {
        this.schemaFile = schemaFile;
        closed = false;
    }

    @Setter
    private List<String> schema;

    public Boolean validate() {
        if (closed) {
            try {
                throw new ClosedSchemaValidator();
            } catch (ClosedSchemaValidator e) {
                e.printStackTrace();
            }
        }
        Boolean valid = true;
        for (String key : schema) {
            valid = schemaFile.has(key);
        }
        return valid;
    }
}
