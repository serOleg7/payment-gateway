package com.example.paymentgateway.service;

import com.example.paymentgateway.domain.Payment;
import com.google.gson.GsonBuilder;

import java.io.*;

public class StorageUtils {


    public static synchronized void saveToStorage(Payment payment, String pathFile) {
        //TODO Check for concurrency issues
        String jsonPayment = new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(payment);
        StringBuilder res = getLastLine(pathFile);
        if (res.length() == 0) {
            res.append("[\n").append(jsonPayment).append("]");
        } else {
            eraseLast(pathFile);
            res.deleteCharAt(res.length() - 1);
            res.append(",\n").append(jsonPayment).append("]");
        }

        try (PrintStream out = new PrintStream(new FileOutputStream(pathFile, true))) {
            out.print(res);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static StringBuilder getLastLine(String pathFile) {
        try (BufferedReader input = new BufferedReader(new FileReader(pathFile))) {
            String last = null, line;
            while ((line = input.readLine()) != null)
                last = line;
            return last == null ? new StringBuilder() : new StringBuilder(last);
        } catch (Exception e) {
            File file = new File(pathFile);
            return new StringBuilder();
        }
    }

    private static void eraseLast(String pathFile) {
        try (RandomAccessFile f = new RandomAccessFile(pathFile, "rw")) {
            if (f.length() != 0) {
                long length = f.length() - 1;
                byte b;
                do {
                    length -= 1;
                    f.seek(length);
                    b = f.readByte();
                } while (b != 10);
                f.setLength(length + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
