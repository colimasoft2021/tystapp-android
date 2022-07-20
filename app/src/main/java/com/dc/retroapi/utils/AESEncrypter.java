/*
 * Copyright (c) 2015. Configure.IT, All Rights Reserved.
 */

package com.dc.retroapi.utils;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * PreviewIT
 *
 * @author <a href="http://www.configure.it/">Configure.IT</a><br />
 * </a><br />
 * <img src=
 * "http://cdn-site.configure.it/cit-site/console/wp-content/uploads/2014/07/logo-beta.png"
 * />
 * @version PreviewIT 1.1
 */
public class AESEncrypter {

  // private static final byte[] SALT = {
  // (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32,
  // (byte) 0x56, (byte) 0x35, (byte) 0xE3, (byte) 0x03
  // };
  // private static final int ITERATION_COUNT = 65536;
  // private static final int KEY_LENGTH = 128;
  private Cipher ecipher;
  private Cipher dcipher;

  private static byte[] ivBytes = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

  public AESEncrypter(String passPhrase) throws Exception {
    // SecretKeyFactory factory =
    // SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
    // KeySpec spec = new PBEKeySpec(passPhrase.toCharArray(), SALT,
    // ITERATION_COUNT, KEY_LENGTH);
    // SecretKey tmp = factory.generateSecret(spec);
    // SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
    passPhrase = getMd5(passPhrase).substring(0, 16);
    SecretKeySpec newKey = new SecretKeySpec(passPhrase.getBytes("UTF-8"),
            "AES");
    AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
    ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    ecipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);

    dcipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    // byte[] iv =
    // ecipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
    dcipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
  }

  public String encrypt(String encrypt) throws Exception {
    byte[] bytes = encrypt.getBytes("UTF8");
    byte[] encrypted = encrypt(bytes);
    String text = Base64.encodeToString(encrypted, 0);
    if (text != null) {
      text = URLEncoder.encode(text.trim(), "utf-8");
    }
    return text;
  }

  public String encryptPlain(String encrypt) throws Exception {
    byte[] bytes = encrypt.getBytes("UTF8");
    byte[] encrypted = encrypt(bytes);
    //        if (text != null) {
//            text = URLEncoder.encode(text.trim(), "utf-8");
//        }
    return Base64.encodeToString(encrypted, 0);
  }

  public byte[] encrypt(byte[] plain) throws Exception {
    return ecipher.doFinal(plain);
  }

  public String decrypt(String encrypt) throws Exception {
    byte[] bytes = Base64.decode(encrypt, 0);
    byte[] decrypted = decrypt(bytes);
    String text = new String(decrypted, "UTF8");
    return text.trim();
  }

  public byte[] decrypt(byte[] encrypt) throws Exception {
    return dcipher.doFinal(encrypt);
  }

  private static String convertToHex(byte[] data) {
    StringBuilder buf = new StringBuilder();
    for (byte b : data) {
      int halfbyte = (b >>> 4) & 0x0F;
      int two_halfs = 0;
      do {
        buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte)
                : (char) ('a' + (halfbyte - 10)));
        halfbyte = b & 0x0F;
      } while (two_halfs++ < 1);
    }
    return buf.toString();
  }

  public static String getSHA1(String text) throws NoSuchAlgorithmException,
          UnsupportedEncodingException {
//        MessageDigest md = MessageDigest.getInstance("SHA-1");
//        md.update(text.getBytes("iso-8859-1"), 0, text.length());
//        byte[] sha1hash = md.digest();
//        return convertToHex(sha1hash);

    return sha1(text);
  }

  public static String getSHA256(String text)
          throws NoSuchAlgorithmException, UnsupportedEncodingException {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    md.update(text.getBytes("iso-8859-1"), 0, text.length());
    byte[] sha1hash = md.digest();
    return convertToHex(sha1hash);
  }

  public static String getMd5(String text) throws NoSuchAlgorithmException,
          UnsupportedEncodingException {
    MessageDigest md = MessageDigest.getInstance("MD5");
    md.update(text.getBytes("iso-8859-1"), 0, text.length());
    byte[] sha1hash = md.digest();
    return convertToHex(sha1hash);
  }


  public static String sha1(String input) throws NoSuchAlgorithmException {
    MessageDigest mDigest = MessageDigest.getInstance("SHA1");
    byte[] result = mDigest.digest(input.getBytes());
    StringBuilder sb = new StringBuilder();
    for (byte aResult : result) {
      sb.append(Integer.toString((aResult & 0xff) + 0x100, 16).substring(1));
    }

    return sb.toString();
  }

  public String generateChecksumFromSortedMap(TreeMap<String, String> map)
          throws NoSuchAlgorithmException {
    StringBuilder stringBuilder = new StringBuilder();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      stringBuilder.append(entry.getKey()).append("=").append(entry.getValue());
    }
    return sha1(stringBuilder.toString());
  }
}