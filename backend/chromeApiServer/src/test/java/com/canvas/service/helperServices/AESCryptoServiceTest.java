package com.canvas.service.helperServices;
import com.canvas.exceptions.CanvasAPIException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.core.env.Environment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;


public class AESCryptoServiceTest {

    @Mock
    Environment env;

    @Before
    public void init(){
        env = mock(Environment.class);
        when(env.getProperty(any()))
                .thenReturn("someSecret");
    }

    @Test
    public void testEncryptAndDecrypt() {
        String originalString = "This is a test string";
        String secretKey = "This is a secret key";
        AESCryptoService aesCryptoService = new AESCryptoService(env);
        try {
            String encryptedString = aesCryptoService.encrypt(originalString, secretKey);
            String decryptedString = aesCryptoService.decrypt(encryptedString, secretKey);

            assertEquals("The original and decrypted strings should match", originalString, decryptedString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testEncryptWithNullInput() {
        String originalString = null;
        String secretKey = "This is a secret key";
        AESCryptoService aesCryptoService = new AESCryptoService(env);

        assertThrows(CanvasAPIException.class, () -> aesCryptoService.encrypt(originalString, secretKey));

    }

    @Test
    public void testDecryptWithNullInput() {
        String originalString = "This is a test string";
        String secretKey = null;
        AESCryptoService aesCryptoService = new AESCryptoService(env);

        assertThrows(CanvasAPIException.class, () -> aesCryptoService.decrypt(originalString, secretKey));

    }
}
