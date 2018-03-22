Security functionality in the Carina framework is required for sensitive data that should be hidden in test configurations and logging. It uses a symmetric key encryption algorithm for security implementation, which means that anyone may encrypt/decrypt the data in the same way, using the same key. In the Carina framework, AES encryption is used by default with a 128-bit security key.

![Security Algorithm](img/security-alg.png)

Anyone may generate their own security key and encrypt the data using that key, so that decrypted valid data may be used in the test only if the valid key is specified in test configuration. Also, one may use the default common key located in the test resources, giving access to all other users for secured data decryption.

## Secured data preparation
For secured data preparation, we implemented a special tool that helps to generate crypto keys and encrypt/decrypt test data files. Here is a usage tip:
```
com.qaprosoft.carina.core.foundation.crypto.CryptoConsole -generate -key_file "file_path_to_save_key"
com.qaprosoft.carina.core.foundation.crypto.CryptoConsole -encrypt -string "string_to_encrypt" -key_file "key_file_path"
com.qaprosoft.carina.core.foundation.crypto.CryptoConsole -decrypt -string "string_to_decrypt" -key_file "key_file_path"
com.qaprosoft.carina.core.foundation.crypto.CryptoConsole -encrypt -file "csv_file_to_encrypt" -key_file "key_file_path"
com.qaprosoft.carina.core.foundation.crypto.CryptoConsole -decrypt -file "csv_file_to_decrypt" -key_file "key_file_path"
```

**To generate an individual crypto key:**

1. Go to "Run configuration" in Eclipse
2. Create new Java application configuration selecting project and main class **com.qaprosoft.cariana.core.foundation.crypto.CryptoConsole**:
![Security Config 1](img/security-config-1.png)
3. Set arguments
![Security Config 2](img/security-config-2.png)
4. Press Apply and then Run
5. Crypto key will be generated:
![Security Config 3](img/security-config-3.png)

**To prepare test data file with secured data:**

1. Prepare input file  using  pattern {crypt:str_to_encrypt} for secured values
![Security Config 4](img/security-config4.png)
2. Prepare input file  using  pattern {crypt:str_to_encrypt} for secured values
Go to “Run configuration”, navigate to arguments tab and execute encryption command specifying key path and input file:
![Security Config 5](img/security-config5.png)
3. File with encrypted data will be generated:
![Security Config 6](img/security-config6.png)
4. To encrypt single string use the following config:
![Security Config 7](img/security-config7.png)
5. Encrypted string will be shown in console:
![Security Config 8](img/security-config-8.png)
6. To decrypt encrypted file execute:
![Security Config 9](img/security-config-9.png)
7. Decrypted file will be generated:
![Security Config 4](img/security-config4.png)

## Secured data usage
You may use encrypted values, both in test configuration and test data files; pay attention to the fact that there is no explicit before test listener for data decryption, so there is no way to find out later in the test if the data is sensitive or not. All the decryption logic is located in WebDriverHelper that wraps Selenium WebDriver methods for interaction with UI and encapsulates action logging logic. Every method that receives text tests if text contains {crypt:...} pattern and if it does, decrypts it and passes it to UI- logging and screenshots are populated with hidden characters:
![Security Config 11](img/security-config-11.png)
