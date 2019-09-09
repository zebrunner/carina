/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.carina.core.foundation.crypto;

import java.io.File;
import java.util.regex.Pattern;

import javax.crypto.SecretKey;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;

public class CryptoConsole {
    private static final Logger LOG = Logger.getLogger(CryptoConsole.class);

    private static Pattern CRYPTO_PATTERN = Pattern.compile(SpecialKeywords.CRYPT);
    private static final String HELP_ARG = "help";
    private static final String ENCRYPT_ARG = "encrypt";
    private static final String DECRYPT_ARG = "decrypt";
    private static final String GENERATE_KEY_ARG = "generate";
    private static final String FILE_ARG = "file";
    private static final String STRING_ARG = "string";
    private static final String KEY_ARG = "key_file";

    @SuppressWarnings("static-access")
    public static void main(String[] args) {
        CommandLineParser parser = new GnuParser();

        Options options = new Options();
        options.addOption(HELP_ARG, false, "usage information");
        options.addOption(OptionBuilder.withArgName(ENCRYPT_ARG).withDescription("action for encrypt").create(ENCRYPT_ARG));
        options.addOption(OptionBuilder.withArgName(DECRYPT_ARG).withDescription("action for decrypt").create(DECRYPT_ARG));
        options.addOption(OptionBuilder.withArgName(GENERATE_KEY_ARG).withDescription("action to generate key").create(GENERATE_KEY_ARG));
        options.addOption(OptionBuilder.withArgName(FILE_ARG).hasArg().withDescription("file to encrypt/decrypt").create(FILE_ARG));
        options.addOption(OptionBuilder.withArgName(STRING_ARG).hasArg().withDescription("string to encrypt/decrypt").create(STRING_ARG));
        options.addOption(OptionBuilder.withArgName(KEY_ARG).hasArg().withDescription("secret key file path").create(KEY_ARG));

        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption(HELP_ARG)) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("CryptoConsole", options);
            } else if (line.hasOption(GENERATE_KEY_ARG) && line.hasOption(KEY_ARG)) {
                // TODO: provide command line arguments to use any key type/size
                SecretKey secretKey = SecretKeyManager.generateKey(SpecialKeywords.CRYPTO_KEY_TYPE, SpecialKeywords.CRYPTO_KEY_SIZE);
                File file = new File(line.getOptionValue(KEY_ARG));
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
                SecretKeyManager.saveKey(secretKey, file);
                LOG.info("Secret key was successfully generated and saved in: " + line.getOptionValue(KEY_ARG));
            } else if (line.hasOption(ENCRYPT_ARG) && line.hasOption(KEY_ARG)) {
                // TODO: adjust command line options to be able to generate key using any algorithm/size etc
                CryptoTool crypto = new CryptoTool(SpecialKeywords.CRYPTO_ALGORITHM, SpecialKeywords.CRYPTO_KEY_TYPE, line.getOptionValue(KEY_ARG));
                if (line.hasOption(FILE_ARG)) {
                    File inFile = new File(line.getOptionValue(FILE_ARG));
                    if (!inFile.exists()) {
                        throw new Exception("Input file not found: " + line.getOptionValue(FILE_ARG));
                    }
                    File outFile = new File(StringUtils.replace(inFile.getAbsolutePath(), ".", "_encrypted."));
                    if (outFile.exists()) {
                        outFile.delete();
                    }
                    outFile.createNewFile();
                    FileUtils.writeByteArrayToFile(outFile, crypto
                            .encryptByPatternAndWrap(new String(FileUtils.readFileToByteArray(inFile)), CRYPTO_PATTERN, "{crypt:%s}").getBytes());
                    LOG.info("Encrypted file saved: " + outFile.getAbsolutePath());
                } else if (line.hasOption(STRING_ARG)) {
                    LOG.info("Encrypted string: " + crypto.encrypt(line.getOptionValue(STRING_ARG)));
                } else {
                    throw new Exception(String.format("Invalid usage: -%s or -%s and -%s should be set", FILE_ARG, STRING_ARG, KEY_ARG));
                }
            } else if (line.hasOption(DECRYPT_ARG) && line.hasOption(KEY_ARG)) {
                // TODO: adjust command line options to be able to generate key using any algorithm/size etc
                CryptoTool crypto = new CryptoTool(SpecialKeywords.CRYPTO_ALGORITHM, SpecialKeywords.CRYPTO_KEY_TYPE, line.getOptionValue(KEY_ARG));

                if (line.hasOption(FILE_ARG)) {
                    File inFile = new File(line.getOptionValue(FILE_ARG));
                    if (!inFile.exists()) {
                        throw new Exception("Input file not found: " + line.getOptionValue(FILE_ARG));
                    }
                    File outFile = new File(StringUtils.replace(inFile.getAbsolutePath(), ".", "_decrypted."));
                    if (outFile.exists()) {
                        outFile.delete();
                    }
                    outFile.createNewFile();
                    FileUtils.writeByteArrayToFile(outFile, crypto
                            .decryptByPatternAndWrap(new String(FileUtils.readFileToByteArray(inFile)), CRYPTO_PATTERN, "{crypt:%s}").getBytes());
                    LOG.info("Decrypted file saved: " + outFile.getAbsolutePath());
                } else if (line.hasOption(STRING_ARG)) {
                    LOG.info("Decrypted string: " + crypto.decrypt(line.getOptionValue(STRING_ARG)));
                } else {
                    throw new Exception(String.format("Invalid usage: -%s or -%s and -%s should be set", FILE_ARG, STRING_ARG, KEY_ARG));
                }
            } else {
                throw new Exception(String.format("Invalid usage: -%s,-%s or -%s should be set", GENERATE_KEY_ARG, ENCRYPT_ARG, DECRYPT_ARG));
            }
        } catch (Exception e) {

            LOG.error(e.getMessage());
            LOG.info("Usage examples: \n"
                    + "com.qaprosoft.carina.core.foundation.crypto.CryptoConsole -generate -key_file \"file_path_to_save_key\" \n"
                    + "com.qaprosoft.carina.core.foundation.crypto.CryptoConsole -encrypt -string \"string_to_encrypt\" -key_file \"key_file_path\" \n"
                    + "com.qaprosoft.carina.core.foundation.crypto.CryptoConsole -decrypt -string \"string_to_decrypt\" -key_file \"key_file_path\" \n"
                    + "com.qaprosoft.carina.core.foundation.crypto.CryptoConsole -encrypt -file \"csv_file_to_encrypt\" -key_file \"key_file_path\" \n"
                    + "com.qaprosoft.carina.core.foundation.crypto.CryptoConsole -decrypt -file \"csv_file_to_decrypt\" -key_file \"key_file_path\" \n");
        }
    }
}
