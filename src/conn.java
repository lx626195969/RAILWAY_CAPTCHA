import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;


public class conn {
    
    public static String toPostData(Map<String,String> formData) throws UnsupportedEncodingException {
        final StringBuffer dataSB = new StringBuffer();
        int c = 0;
        for(Map.Entry<String, String> entry : formData.entrySet()) {
            dataSB.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "utf8"));
            if(c++ < formData.size() - 1) {
                dataSB.append("&");
            }
        }
        
        
        return dataSB.toString();
    }
    
    public static List<String> findAllowBookedDate() throws Exception{
        //
        //final Pattern pattern = Pattern.compile("(\\d{4}/\\d{2}/\\d{2}-\\d{2})>\\d{4}/\\d{2}/\\d{2}\\s");
        final Pattern pattern = Pattern.compile("(\\d{4}/\\d{2}/\\d{2}-\\d{2})>\\d{4}/\\d{2}/\\d{2}");
        List<String> retStrings = new ArrayList<String>(14);
        
        
        URLConnection conn = new URL("http://railway.hinet.net/ctno1.htm").openConnection();
        conn.setDoOutput(true);
        conn.connect();
        
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line = null;
        
        do {
            
            line = br.readLine();
            if(line != null) {
                Matcher matcher = pattern.matcher(line);
                if(matcher.find()) {
                    retStrings.add(matcher.group(1));
                }
            }
        }while(line != null);
        
        conn.getInputStream().close();
        
        return retStrings;
    }
    
    public static void putCookies(URLConnection conn,Map<String,String> cookies) {
        
        if(cookies.size() > 0) {
            StringBuffer cookieSB = new StringBuffer();
            int count = 0;
            for(Map.Entry<String,String> entry : cookies.entrySet()) {
                cookieSB.append(entry.getKey()).append("=").append(entry.getValue());
                if(count++ < cookies.size() - 1) {
                    cookieSB.append(";");
                }
            }
        }
        
    }
    
    public static String getJSESSIONID(Map<String,String> cookies) {
        for(Map.Entry<String,String> entry : cookies.entrySet()) {
            if("jsessionid".equals(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }
        return null;
    }
    
    public static Map<String,String> getCookies(Map<String,String> formData) throws Exception {
        final Map<String,String> cookies = new HashMap<String,String>();
        URLConnection conn = new URL("http://railway.hinet.net/check_ctno1.jsp").openConnection();
        conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_4) AppleWebKit/536.11 (KHTML, like Gecko) Chrome/20.0.1132.57 Safari/536.11");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.addRequestProperty("Content-Length", String.valueOf(toPostData(formData).length()));
        conn.addRequestProperty("Referer", "http://railway.hinet.net/ctno1.htm");
        OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
        osw.write(toPostData(formData));
        System.out.println("post data=" + toPostData(formData));
        osw.flush();
        conn.getOutputStream().close();
        conn.connect();

        for(Map.Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
            //final String sessionKeyword = "jsessionid=";
            if("Set-Cookie".equals(entry.getKey())) {
                for(String v : entry.getValue()) {
                    for(String token : v.split(";")) {
                        final int eqpos = token.indexOf("=");
                        if(eqpos > 0) {
                            String key = token.substring(0, eqpos);
                            String value = token.substring(eqpos + 1);
                            if(!"expires".equals(key.toLowerCase())) {
                                cookies.put(key, value);
                            }
                        }
                    }
                }
            }
            
            System.out.print(entry.getKey()+"=");
            for(String v : entry.getValue()) {
                System.out.print(v + "||");
            }
            System.out.println();
        }
        
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line = null;
        
        do {
            
            line = br.readLine();
            if(line != null) {
                System.out.println(">>>" + line);
            }
        }while(line != null);
        
        conn.getInputStream().close();
        
       
        return cookies;
    }
    
    public static String resolveRandom(Map<String,String> cookies) throws Exception {
        String jsessionid = getJSESSIONID(cookies);
        String url = "http://railway.hinet.net/ImageOut.jsp;jsessionid=" + jsessionid;
        System.out.println("resolveRandom url=" + url);
        
        URLConnection conn = new URL(url).openConnection();
        putCookies(conn,cookies);
        conn.addRequestProperty("Referer", "http://railway.hinet.net/check_ctno1.jsp");
        conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_4) AppleWebKit/536.11 (KHTML, like Gecko) Chrome/20.0.1132.57 Safari/536.11");
        conn.setDoInput(true);
        conn.connect();
        
        BufferedImage rawImg = ImageIO.read(conn.getInputStream());
        String answer = Main.getCaptcha(rawImg);
        
        //debug
        //ImageIO.write(rawImg, "JPEG", new File("/Users/elixirbb/Documents/workspace/RAILWAY_CAPTCHA/testimg/out" + answer + ".jpg"));
        
        return answer;
    }
    
    public static String bookTicket(Map<String,String> formData,Map<String,String> cookies) throws Exception{
        final Pattern pattern = Pattern.compile("(\\d{5,7})");
       
        final String jsessionid = getJSESSIONID(cookies);
        final String sgetData = "?" + toPostData(formData);
        final String url = "http://railway.hinet.net/order_no1.jsp;jsessionid=" + jsessionid + sgetData;
        
        String ticketno = null;
        
        System.out.println("url=" + url);
        
        URLConnection conn = new URL(url).openConnection();
        conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_4) AppleWebKit/536.11 (KHTML, like Gecko) Chrome/20.0.1132.57 Safari/536.11");
        putCookies(conn,cookies);
        conn.addRequestProperty("Referer", "http://railway.hinet.net/check_ctno1.jsp");
        
        conn.setDoInput(true);
        conn.connect();
        
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line = null;
        
        do {
            
            line = br.readLine();
            if(line != null) {
                System.out.println(">>>" + line);
                int startpos = line.indexOf("電腦代碼：");
                if(startpos > 0) {
                    final Matcher matcher = pattern.matcher(line.substring(startpos + "電腦代碼：".length()));
                    if(matcher.find()) {
                        ticketno = matcher.group(1);
                    }
                }
            }
        }while(line != null);
        
        conn.getInputStream().close();
        
        return ticketno;
    }
    
    public static boolean cancelTicket(String person_id,String ticketno,Map<String,String> cookies) throws Exception {
        String url = "http://railway.hinet.net/ccancel_rt.jsp?personId=" + URLEncoder.encode(person_id, "utf8") + "&orderCode=" + URLEncoder.encode(ticketno, "utf8");
        System.out.println("cancelTicket url=" + url);
        
        URLConnection conn = new URL(url).openConnection();
        putCookies(conn,cookies);
        conn.addRequestProperty("Referer", "http://railway.hinet.net/ccancel.jsp?personId=" + person_id + "&orderCode=" + ticketno);
        conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_4) AppleWebKit/536.11 (KHTML, like Gecko) Chrome/20.0.1132.57 Safari/536.11");
        conn.setDoInput(true);
        conn.connect();
        
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line = null;
        try{
            do {
                
                line = br.readLine();
                if(line != null) {
                    System.out.println(">>>" + line);
                    int startpos = line.indexOf("取消成功");
                    if(startpos > 0) {
                        return true;
                    }
                }
            }while(line != null);
        }
        finally {
            conn.getInputStream().close();
        }
        
        return false;
    }

    public static void main(String[] argus) throws Exception {
        
        final Map<String,String> formData = new HashMap<String,String>();
        final List<String> allowBookingDateList = findAllowBookedDate();
        //Get into first page
        System.out.println("allowBookingDateList=" + allowBookingDateList);
        
        if(allowBookingDateList.size() < 2) {
            return;
        }
        final String person_id = "A127535236";
        formData.put("person_id", person_id);
        formData.put("from_station", "100");
        formData.put("to_station", "004");
        formData.put("getin_date", allowBookingDateList.get(3));
        formData.put("train_no", "242");
        formData.put("order_qty_str", "1");
        formData.put("n_order_qty_str", "0");
        formData.put("d_order_qty_str", "0");
        formData.put("b_order_qty_str", "0");
        formData.put("returnTicket", "0");
        
        final Map<String,String> cookies = getCookies(formData);
        System.out.println("cookies=" + cookies);
        
        if(null == cookies || cookies.isEmpty()) {
            return;
        }
        
        String randomNumber = resolveRandom(cookies);
        System.out.println("randomNumber=" + randomNumber);

        if(null == randomNumber || randomNumber.length() != 5) {
            return;
        }
        formData.remove("n_order_qty_str");
        formData.remove("d_order_qty_str");
        formData.remove("b_order_qty_str");
        
        formData.put("randInput", randomNumber);
        
        final String ticketno = bookTicket(formData,cookies);
        System.out.println("person_id=" + person_id + " ticketno=" + ticketno);
        
        final boolean isCancel = cancelTicket(person_id, ticketno, cookies);
        
        System.out.print("isCancel=" + isCancel);
    }
}
