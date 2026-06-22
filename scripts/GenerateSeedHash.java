import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Standalone utility for regenerating BCrypt password hashes for the seed users
 * defined in {@code scripts/seed-admin-user.sql}.
 *
 * <p>Usage (when Maven + spring-security-crypto are available):
 * <pre>
 *   mvn -q -pl . dependency:copy -Dartifact=org.springframework.security:spring-security-crypto:6.2.4
 *   javac -cp "target/dependency/*" scripts/GenerateSeedHash.java
 *   java  -cp ".:target/dependency/*" GenerateSeedHash
 * </pre>
 *
 * <p>If Maven/Java is not available, the equivalent hash can be generated with
 * Node.js using {@code bcryptjs}:
 * <pre>
 *   node -e "const b=require('bcryptjs'); console.log(b.hashSync('admin123',10));"
 * </pre>
 *
 * <p>Output: prints the hash for both {@code admin123} and {@code pharma123} at
 * BCrypt strength 10, matching the cost factor previously used in the seed SQL.
 */
public class GenerateSeedHash {

    private static final int STRENGTH = 10;

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(STRENGTH);

        String adminHash = encoder.encode("admin123");
        String pharmaHash = encoder.encode("pharma123");

        System.out.println("admin@pcms.vn        (admin123)  -> " + adminHash);
        System.out.println("pharmacist01@pcms.vn (pharma123) -> " + pharmaHash);

        // Self-check (in case anyone tampers with the password literals above).
        if (!encoder.matches("admin123", adminHash)) {
            throw new IllegalStateException("Self-check failed: admin123 hash mismatch");
        }
        if (!encoder.matches("pharma123", pharmaHash)) {
            throw new IllegalStateException("Self-check failed: pharma123 hash mismatch");
        }
    }
}