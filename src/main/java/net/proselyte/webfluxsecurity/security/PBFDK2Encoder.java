package net.proselyte.webfluxsecurity.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * PasswordEncoder - используется для кодирования данных, в нашем примере,
 * для кодирования пароля, что бы он не хранился строкой.
 */
@Component
public class PBFDK2Encoder implements PasswordEncoder {

    /**
     * Секрет - на основании его будем кодировать.
     */
    @Value("${jwt.password.encoder.secret}")
    private String secret;

    /**
     * Количество итераций энкодинга.
     */
    @Value("${jwt.password.encoder.iteration}")
    private Integer iteration;
    /**
     * Длин ключа.
     */
    @Value("${jwt.password.encoder.keylength}")
    private Integer keyLength;

    /**
     * Это секретный ключ алгоритма с помощью, которого будет генериться
     * закодированный пароль.
     */
    private static final String SECRET_KEY_INSTANCE = "PBKDF2WithHmacSHA512";

    /**
     * Метод кодирует пароль в защищенную запись.
     *
     * @param rawPassword сырой пароль
     * @return строковое представление закодированного пароля
     */
    @Override
    public String encode(CharSequence rawPassword) {

        try {
            byte[] result = SecretKeyFactory.getInstance(SECRET_KEY_INSTANCE)
                    .generateSecret(new PBEKeySpec(rawPassword.toString().toCharArray(),
                            secret.getBytes(), iteration, keyLength))
                    .getEncoded();
            return Base64.getEncoder()
                    .encodeToString(result); //кодируем в строку массив байтов
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод принимает на вход сырой пароль и закодированных пароль и сравнивает их.
     *
     * @param rawPassword     the raw password to encode and match (передали из вне)
     * @param encodedPassword the encoded password from storage to compare with (взяли из БД закодированный)
     * @return булевый результат
     */
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return encode(rawPassword).equals(encodedPassword);
    }
}
