/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pexbuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Bryce
 */
public final class Requests {

    public static String login(String username, String password) {
        String body = "null";
        try {
            URL url = new URL("https://shulkerbox.org/api/user/login?username=" + username + "&password=" + password);
            URLConnection con = url.openConnection();
            InputStream in = con.getInputStream();
            String encoding = con.getContentEncoding();
            encoding = encoding == null ? "UTF-8" : encoding;
            body = IOUtils.toString(in, encoding);
        } catch (IOException e) {
            System.out.println("Error logging in: " + e.getMessage());
        }
        return body;
    }
    
    public static String getOnline() {
        String body = "null";
        try {
            URL url = new URL("https://shulkerbox.org/api/user/get/online");
            URLConnection con = url.openConnection();
            InputStream in = con.getInputStream();
            String encoding = con.getContentEncoding();
            encoding = encoding == null ? "UTF-8" : encoding;
            body = IOUtils.toString(in, encoding);
        } catch (IOException e) {
            System.out.println("Error getting online users: " + e.getMessage());
        }
        return body;
    }

    public static String register(String username, String password, String first, String last, String email) {
        String body = "null";
        try {
            URL url = new URL("https://shulkerbox.org/api/user/register?username=" + username + "&password=" + password + "&fname=" + first + "&lname=" + last + "&email=" + email);
            URLConnection con = url.openConnection();
            InputStream in = con.getInputStream();
            String encoding = con.getContentEncoding();
            encoding = encoding == null ? "UTF-8" : encoding;
            body = IOUtils.toString(in, encoding);
        } catch (IOException e) {
            System.out.println("Error logging in: " + e.getMessage());
        }
        return body;
    }

    public static String verify(String token) {
        String body = "null";
        try {
            URL url = new URL("https://shulkerbox.org/api/user/verifylogin?_t=" + token);
            URLConnection con = url.openConnection();
            InputStream in = con.getInputStream();
            String encoding = con.getContentEncoding();
            encoding = encoding == null ? "UTF-8" : encoding;
            body = IOUtils.toString(in, encoding);
        } catch (IOException e) {
            System.out.println("Error logging in: " + e.getMessage());
        }
        return body;
    }

    public static String isUpgraded(String token) {
        String body = "null";
        try {
            URL url = new URL("https://shulkerbox.org/api/user/is/upgraded?token=" + token + "&scriptid=79");
            URLConnection con = url.openConnection();
            InputStream in = con.getInputStream();
            String encoding = con.getContentEncoding();
            encoding = encoding == null ? "UTF-8" : encoding;
            body = IOUtils.toString(in, encoding);
        } catch (IOException e) {
            System.out.println("Error logging in: " + e.getMessage());
        }
        return body;
    }

}
