import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

public class Main {

    public static BufferedImage toGray(BufferedImage rawImg) {

        for (int x = 0; x < rawImg.getWidth(); x++) {
            for (int y = 0; y < rawImg.getHeight(); y++) {
                final Color color = new Color(rawImg.getRGB(x, y));
                final int grayColorValue = (color.getBlue() + color.getRed() + color.getGreen()) / 3;

                rawImg.setRGB(x, y,
                        new Color(grayColorValue, grayColorValue, grayColorValue).getRGB());
            }
        }

        return rawImg;
    }

    public static int[] histogram(BufferedImage grayImg) {
        final int[] hist = new int[256];
        for (int x = 0; x < grayImg.getWidth(); x++) {
            for (int y = 0; y < grayImg.getHeight(); y++) {
                final Color color = new Color(grayImg.getRGB(x, y));
                hist[color.getBlue()]++;
            }
        }

        return hist;
    }

    public static BufferedImage removedColorImage(BufferedImage img, Set<Integer> reserverColor,
            Color replaceColor) {
        if (null == replaceColor) {
            replaceColor = Color.BLACK;
        }
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                if (!reserverColor.contains(img.getRGB(x, y))) {
                    img.setRGB(x, y, replaceColor.getRGB());
                }
                else {
                    // img.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }
        return img;
    }

    static class GrayPix {
        int color;
        int volume;
    }

    static class GrayPixComparator implements Comparator<GrayPix> {

        @Override
        public int compare(GrayPix a, GrayPix b) {
            return a.volume - b.volume;
        }

    }
    
    public static BufferedImage createTextImage(int v,int size,Font font) {
        final BufferedImage img = new BufferedImage(size,size,BufferedImage.TYPE_BYTE_BINARY);
        final  Graphics2D g = img.createGraphics();

        g.setBackground(Color.BLACK);
        g.setColor(Color.WHITE);
        g.setFont(font);
        
        final String str = String.valueOf(v % 10);
        final int startY = ((size - g.getFontMetrics().getHeight()) >> 1) + font.getSize();
        final int startX = ((size - g.getFontMetrics().stringWidth(str)) >> 1);
        
        g.drawString(str, startX, startY);
        //g.drawLine(0, startY, size, startY); //debug, draw baseline
        //System.out.println("draw " + v + " startX=" + startX + " ,startY=" + startY + " h=" + g.getFontMetrics().getHeight());
        
        return img;
    }
    
    public static BufferedImage toBinaryImage(BufferedImage img) {
        final BufferedImage newimg = new BufferedImage(img.getWidth(),img.getHeight(),BufferedImage.TYPE_BYTE_BINARY);
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                if(img.getRGB(x, y) != Color.BLACK.getRGB()) {
                    newimg.setRGB(x, y, Color.WHITE.getRGB());
                }
                else {
                    newimg.setRGB(x, y, Color.BLACK.getRGB());
                }
            }
        }
        
        
        return newimg;
    }
    
    public static BufferedImage rotateTextImage(BufferedImage img,int theta) {
        final BufferedImage newimg = new BufferedImage(img.getWidth(),img.getHeight(),img.getType());
        final Graphics2D g = newimg.createGraphics();
        final int centerX = img.getWidth() >> 1;
        final int cetnerY = img.getHeight() >> 1;
        g.rotate(Math.toRadians(theta), centerX, cetnerY);
        g.drawImage(img, 0, 0, null);

        return newimg;
    }
    
    public static float score(BufferedImage verf_img,BufferedImage rotateImage,int x,int y) {
        float score = 0.0f;
        for(int rx = 0;rx < rotateImage.getWidth();rx++) {
            for(int ry = 0;ry < rotateImage.getHeight();ry++) {
                final int a = verf_img.getRGB(x + rx, y + ry);
                final int b = rotateImage.getRGB(rx, ry);
                //if(a == b) {
                //    if(a != Color.BLACK.getRed()) {
                //        score += 
                //    }
                //}
                score += a == b ? 1 : -1;
            }
        }
        return score / (float)(rotateImage.getWidth() * rotateImage.getHeight());
    }
    
    public static Shape getShape(BufferedImage clippedImage) {
    	
    	final Polygon polygon = new Polygon();
        for (int y = 0; y < clippedImage.getHeight(); y++) {
             if(clippedImage.getRGB(0, y) != Color.BLACK.getRGB()) {
                 	
             }
        }
       
       return polygon;
    	
    }
    
    public static BufferedImage clipEmpty(BufferedImage img) {
        //find MinX
        //MinX
        int minX = 0;
        for(;minX < img.getWidth();minX++) {
            boolean allBlack = true;
            for(int y = 0;y < img.getHeight();y++) {
                allBlack = img.getRGB(minX, y) == Color.BLACK.getRGB();
                if(!allBlack) {
                    break;
                }
            }
            if(!allBlack) {
                break;
            }
        }
        if(minX == img.getWidth()) {
            System.out.println("no minX");
            return img; //All Black
        }
        
        int maxX = img.getWidth() - 1;
        for(;maxX > minX;maxX--) {
            boolean allBlack = true;
            for(int y = 0;y < img.getHeight();y++) {
                allBlack = img.getRGB(maxX, y) == Color.BLACK.getRGB();
                if(!allBlack) {
                    break;
                }
            }
            if(!allBlack) {
                break;
            }
        }
        if(maxX < 0 || maxX <= minX) {
            System.out.println("no maxX");
            return img; //All Black
        }
        
        //minY
        int minY = 0;
        for(;minY < img.getHeight();minY++) {
            boolean allBlack = true;
            for(int x = minX;x <= maxX;x++) {
                allBlack = img.getRGB(x, minY) == Color.BLACK.getRGB();
                if(!allBlack) {
                    break;
                }
            }
            
            if(!allBlack) {
                break;
            }
        }
        if(minY == img.getHeight()) {
            System.out.println("no minY");
            return img; //All Black
        }
        
        //maxY
        int maxY = img.getHeight() - 1;
        for(;maxY > minY;maxY--) {
            boolean allBlack = true;
            for(int x = minX;x <= maxX;x++) {
                allBlack = img.getRGB(x, maxY) == Color.BLACK.getRGB();
                if(!allBlack) {
                    break;
                }
            }
            
            if(!allBlack) {
                break;
            }
        }
        if(maxY < 0 || maxY <= minY) {
            System.out.println("no maxY minX=" + minX + " maxX=" + maxX + " minY=" + minY);
            return img; //All Black
        }
        final Rectangle clipRectangle = new Rectangle(minX,minY,maxX - minX + 1,maxY - minY + 1);
        BufferedImage clippedImage = new BufferedImage(clipRectangle.width,clipRectangle.height,img.getType());
        clippedImage.createGraphics().drawImage(img, 0, 0, clippedImage.getWidth(), clippedImage.getHeight(), clipRectangle.x, clipRectangle.y, clipRectangle.x + clipRectangle.width, clipRectangle.y + clipRectangle.height, null);
        //final Graphics2D g = img.createGraphics();
        //g.drawRect(clipRectangle.x, clipRectangle.y, clipRectangle.width, clipRectangle.height);
        //System.out.println("clipRectangle=" + clipRectangle);
        
        return clippedImage;
    }
        
    public static boolean joinToRectangle(HashMap<Rectangle,Float> map,Rectangle r,float score,int maxNumbrOfElement) {
        Rectangle replaceKey = null;
        float minScore = Float.MAX_VALUE;
        Rectangle minScoreRectangle = null;
        
        for(Map.Entry<Rectangle,Float> entry : map.entrySet()) {
            if(entry.getValue() < minScore) {
                minScore = entry.getValue();
                minScoreRectangle = entry.getKey();
            }
            
            if(entry.getKey().intersects(r)) {
                if(score > entry.getValue()) {
                    replaceKey = entry.getKey();
                    break;
                }
                else {
                    return false;
                }
            }
        }
        if(null == replaceKey) {
            if(map.size() >= maxNumbrOfElement) {
                if(score > minScore) {
                    map.remove(minScoreRectangle);
                    map.put(r, score);
                    return true;
                }
                else {
                    return false;
                }
            }
            else {
                map.put(r, score);
                return true;
            }

        }
        else {
            map.remove(replaceKey);
            map.put(r, score);
            return true;
        }

        
    }
    
    private final static HashMap<String,BufferedImage> numberMap = new HashMap<String,BufferedImage>();
    private final static HashMap<String,int[][]> accMap = new HashMap<String,int[][]>();
    
    public static Map<Rectangle,Float> findNumber(int v,BufferedImage verf_img,int[][] verfImgAccArrays) throws IOException {
        final float threshold = 0.6f;
        final int RANGE = 60;
        final int fontSize = 26;
        final Font font = new Font("Times New Roman",Font.BOLD,fontSize);
        final BufferedImage rawNumberImage = createTextImage(v,(int)Math.sqrt(fontSize*fontSize + fontSize*fontSize),font);
        final HashMap<Rectangle,Float> mapOfRectangle = new HashMap<Rectangle,Float>();

        int debug_reduce = 0;
        int debug_total = 0;
        
        //final String tempdir = "/Users/bbkb/Documents/workspace/RAILWAY_CAPTCHA/numbers";

        for(int degree = -RANGE;degree <= RANGE;degree++) {
            //System.out.println("\t degree=" + degree);
        	//final File filename = new File(tempdir,String.valueOf(v) + "_" + String.valueOf(degree) + ".png");
            //final BufferedImage rotateImage = filename.exists() ? ImageIO.read(filename)  : clipEmpty(rotateTextImage(rawNumberImage,degree));
            /*
            if(!filename.exists()) {
            	ImageIO.write(rotateImage, "PNG", filename);
            }*/
        	final String key = String.valueOf(v) + "_" + String.valueOf(degree);
        	BufferedImage rotateImage = numberMap.get(key);
        	
        	if(null == rotateImage) {
        		rotateImage = clipEmpty(rotateTextImage(rawNumberImage,degree));
        		numberMap.put(key, rotateImage);
        	}
            
            int[][] rotateImagesAccArrays = accMap.get(key);
            if(null == rotateImagesAccArrays) {
            	rotateImagesAccArrays = toAccArrays(rotateImage);
            	accMap.put(key, rotateImagesAccArrays);
            }

            final int sumOfRawNumberPix = 1 + calculateSumPixles(rotateImagesAccArrays,new Rectangle(0,0,rotateImage.getWidth(),rotateImage.getHeight()));
            
            final int sumOfRawTopHalf = 1 + calculateSumPixles(rotateImagesAccArrays,new Rectangle(0,0,rotateImage.getWidth(),rotateImage.getHeight() >> 1));
            final int sumOfRawBottomHalf = 1 + calculateSumPixles(rotateImagesAccArrays,new Rectangle(0,rotateImage.getHeight() >> 1,rotateImage.getWidth(),rotateImage.getHeight() >> 1));
            final int sumOfRawLeftHalf = 1 + calculateSumPixles(rotateImagesAccArrays,new Rectangle(0,0,rotateImage.getWidth() >> 1,rotateImage.getHeight()));
            final int sumOfRawRightHalf = 1 + calculateSumPixles(rotateImagesAccArrays,new Rectangle(rotateImage.getWidth() >> 1,0,rotateImage.getWidth() >> 1,rotateImage.getHeight()));
            
            final int sumleftTopOfRawNumberPix = 1 +calculateSumPixles(rotateImagesAccArrays,new Rectangle(0,0,rotateImage.getWidth() >> 1,rotateImage.getHeight() >> 1));
            final int sumleftDownOfRawNumberPix = 1 + calculateSumPixles(rotateImagesAccArrays,new Rectangle(0,rotateImage.getHeight() >> 1,rotateImage.getWidth() >> 1,rotateImage.getHeight() >> 1));
            final int sumrightTopOfRawNumberPix = 1 + calculateSumPixles(rotateImagesAccArrays,new Rectangle(rotateImage.getWidth() >> 1,0,rotateImage.getWidth() >> 1,rotateImage.getHeight() >> 1));
            final int sumrightDownOfRawNumberPix = 1 + calculateSumPixles(rotateImagesAccArrays,new Rectangle(rotateImage.getWidth() >> 1,rotateImage.getHeight(),rotateImage.getWidth() >> 1,rotateImage.getHeight() >> 1));
            

            for (int x = 0; x < verf_img.getWidth() - rotateImage.getWidth(); x++) {
                for (int y = 0; y < verf_img.getHeight() - rotateImage.getHeight(); y++) {
                	debug_total++;
                	
                	final int sumOfRectangle = 1 + calculateSumPixles(verfImgAccArrays,new Rectangle(x,y,rotateImage.getWidth(),rotateImage.getHeight()));
                    
                	final int sumOfTopHalf =  1 + calculateSumPixles(verfImgAccArrays,new Rectangle(x,y,rotateImage.getWidth(),rotateImage.getHeight() >> 1));
                	final int sumOfBottomHalf = 1 + calculateSumPixles(verfImgAccArrays,new Rectangle(x,y + (rotateImage.getHeight() >> 1),rotateImage.getWidth(),rotateImage.getHeight() >> 1));
                	final int sumOfLeftHalf = 1 + calculateSumPixles(verfImgAccArrays,new Rectangle(x,y,rotateImage.getWidth() >> 1,rotateImage.getHeight()));
                	final int sumOfRightHalf = 1 + calculateSumPixles(verfImgAccArrays,new Rectangle(x + (rotateImage.getWidth() >> 1),y,rotateImage.getWidth() >> 1,rotateImage.getHeight()));
                	
                	final int sumleftTopRectangle = 1 + calculateSumPixles(verfImgAccArrays,new Rectangle(x,y,rotateImage.getWidth() >> 1,rotateImage.getHeight() >> 1));
                	final int sumleftDownRectangle = 1 + calculateSumPixles(verfImgAccArrays,new Rectangle(x,y + (rotateImage.getHeight() >> 1),rotateImage.getWidth() >> 1,rotateImage.getHeight() >> 1));
                	final int sumrightTopRectangle = 1 + calculateSumPixles(verfImgAccArrays,new Rectangle(x + (rotateImage.getWidth() >> 1),y,rotateImage.getWidth() >> 1,rotateImage.getHeight() >> 1));
                	final int sumrightDownRectangle = 1 + calculateSumPixles(verfImgAccArrays,new Rectangle(x + (rotateImage.getWidth() >> 1),y+ (rotateImage.getHeight() >> 1),rotateImage.getWidth() >> 1,rotateImage.getHeight() >> 1));
                    
                	
                	if((float) sumOfRectangle / (float) sumOfRawNumberPix > threshold  &&
                			(float) sumOfTopHalf/ (float) sumOfRawTopHalf > threshold &&
                			(float) sumOfBottomHalf / (float) sumOfRawBottomHalf > threshold &&
                			(float) sumOfLeftHalf / (float) sumOfRawLeftHalf > threshold &&
                			(float) sumOfRightHalf / (float) sumOfRawRightHalf > threshold &&
                    		(float) sumleftTopRectangle / (float) sumleftTopOfRawNumberPix > threshold &&
                    		(float) sumleftDownRectangle / (float) sumleftDownOfRawNumberPix > threshold &&
                    		(float) sumrightTopRectangle / (float) sumrightTopOfRawNumberPix > threshold &&
                    		(float) sumrightDownRectangle / (float) sumrightDownOfRawNumberPix > threshold) {
                	
	                    float score = score(verf_img,rotateImage,x,y);
                		/*
                		float score = 
                			((float) sumOfRectangle / (float) sumOfRawNumberPix) + 
                			((float) sumOfTopHalf/ (float) sumOfRawTopHalf) + 
                			((float) sumOfBottomHalf / (float) sumOfRawBottomHalf) + 
                			((float) sumOfLeftHalf / (float) sumOfRawLeftHalf) + 
                			((float) sumOfRightHalf / (float) sumOfRawRightHalf) +
                			((float) sumleftTopRectangle / (float) sumleftTopOfRawNumberPix) + 
                			((float) sumleftDownRectangle / (float) sumleftDownOfRawNumberPix) + 
                			((float) sumrightTopRectangle / (float) sumrightTopOfRawNumberPix) + 
                			((float) sumrightDownRectangle / (float) sumrightDownOfRawNumberPix);
                			
                		score /= 9.0f;	
                		*/
	                    if(score > threshold) {
	                        joinToRectangle(mapOfRectangle,new Rectangle(x,y,rotateImage.getWidth(),rotateImage.getHeight()),score,5);
	                    }
                	}
                	else {
                		debug_reduce++;
                	}
                    
                }
            }
            
            
            
        }
        //System.out.println("redeuce=" + (float)debug_reduce / (float) debug_total);
        
        return mapOfRectangle;
    }
    
    public static int[][] toAccArrays(BufferedImage img) {
        int[][] accArrays = new int[img.getWidth()][img.getHeight()];
            for (int y = 0; y < img.getHeight(); y++) {
            	for (int x = 0; x < img.getWidth(); x++) {
            		
                final int left = 0 == x ? 0 : accArrays[x - 1][y];
                final int top = 0 == y ? 0 : accArrays[x][y - 1];
                final int lefttop = 0 == x || 0 == y ? 0 : accArrays[x - 1][y - 1];
               
                accArrays[x][y] = left + top - lefttop + (img.getRGB(x, y) != Color.BLACK.getRGB() ? 1 : 0);
                //System.out.println("(" + x + "," + y + ")=" + img.getRGB(x, y));
                        
            }
        }
        return accArrays;
    }
    
    public static int calculateSumPixles(int[][] accArrays,Rectangle r) {
        int startX = r.x < 0 ? 0 : r.x;
        int startY = r.y < 0 ? 0 : r.y;
        int endX = r.x + r.width - 1 >= accArrays.length ? accArrays.length - 1 : r.x + r.width - 1;
        int endY = r.y + r.height - 1>= accArrays[0].length ? accArrays[0].length - 1 : r.y + r.height - 1;
        
        //System.out.println("startX=" + startX + " startY=" + startY + " endX=" + endX + " endY="+ endY);
        
        return accArrays[endX][endY] - (0 == startY ? 0 : accArrays[endX][startY - 1]) - (0 == startX ? 0 : accArrays[startX - 1][endY]) + (0 == startX || 0 == startY ? 0 : accArrays[startX -1][startY - 1]);
    }
    
    public static String getCaptcha(BufferedImage rawImg) throws Exception {
        final BufferedImage grayRawImage = toGray(rawImg);
        final int[] hist = histogram(grayRawImage);
        final GrayPix[] pixHist = new GrayPix[hist.length];
        for (int i = 0; i < hist.length; i++) {
            pixHist[i] = new GrayPix();
            pixHist[i].color = i;
            pixHist[i].volume = hist[i];
        }

        Arrays.sort(pixHist, new GrayPixComparator());
        final Set<Integer> reserverColor = new HashSet<Integer>();
        for (int i = 0; i < pixHist.length; i++) {
            if (pixHist[i].volume < 200 && pixHist[i].color < 128) {
                reserverColor.add(new Color(pixHist[i].color, pixHist[i].color,
                        pixHist[i].color).getRGB());
                //System.out.println("color reserve " + pixHist[i].color + " " + pixHist[i].volume);
            }
            //System.out.println("color " + pixHist[i].color + " " + pixHist[i].volume);
        }
        
        final BufferedImage binaryRawImage = toBinaryImage(removedColorImage(grayRawImage, reserverColor, Color.BLACK));
        final Graphics2D g = binaryRawImage.createGraphics();
        final HashMap<Rectangle,Float> totalEntry = new HashMap<Rectangle,Float>();
        final HashMap<Rectangle,Integer> totalIntegerEntry = new HashMap<Rectangle,Integer>();
        final int[][] verfImgAccArrays = toAccArrays(binaryRawImage); 
        
        g.setColor(Color.WHITE);
        /*
        final List<Future<Map<Rectangle,Float>>> featuresList = new ArrayList<Future<Map<Rectangle,Float>>>(10);
        for(int v = 0;v < 10;v++) {
            final int value = v;
            featuresList.add(THREADPOOL.submit(new Callable<Map<Rectangle,Float>>(){

                @Override
                public Map<Rectangle, Float> call() throws Exception {
                    return findNumber(value,binaryRawImage,verfImgAccArrays);
                }}));
        }*/
        for(int v = 0;v < 10;v++) {
            System.out.print(v);
            for(Map.Entry<Rectangle,Float> entry : findNumber(v,binaryRawImage,verfImgAccArrays).entrySet()) {
                totalIntegerEntry.put(entry.getKey(), v);
                joinToRectangle(totalEntry,entry.getKey(),entry.getValue(),5);
            }
        }
        System.out.println();
        
        final TreeMap<Integer,Rectangle> sortedRectangle = new TreeMap<Integer,Rectangle>();
        final StringBuffer answerSB = new StringBuffer();
        for(Map.Entry<Rectangle,Float> entry : totalEntry.entrySet()) {
            sortedRectangle.put(entry.getKey().x,entry.getKey());
            System.out.println("\tv=" + totalIntegerEntry.get(entry.getKey()) + " Rect " + entry.getKey() + "," + entry.getValue());
            g.drawRect(entry.getKey().x, entry.getKey().y, entry.getKey().width, entry.getKey().height);
        }
        
        for(Map.Entry<Integer,Rectangle> entry : sortedRectangle.entrySet()) {
            answerSB.append(totalIntegerEntry.get(entry.getValue()));
        }
        final String answer = answerSB.toString();
        if(5 == answer.length()) {
            return answer.toString();
        }
        else {
            return null;
        }
    }
    
    private final static ExecutorService THREADPOOL = Executors.newFixedThreadPool(4);

    public static void main(String[] args) throws Exception {
    	
        final File inputFolder = new File(args[0]);
        float success = 0.0f;
        float total = 0.0f;
        long alltimeTaken = 0L;
        for (File inputFile : inputFolder.listFiles()) {
            
            final String filename = inputFile.getName().toLowerCase();
            if(!filename.endsWith(".jpg") || filename.startsWith("out")) {
                continue;
            }
            total++;
            System.out.println("inputFile=" + inputFile);
            
            final long stime = System.currentTimeMillis();
            
            final BufferedImage rawImg = ImageIO.read(inputFile);

            final String answer = getCaptcha(rawImg);
            if(answer != null && 5 == answer.length() && inputFile.getName().indexOf(answer) >= 0) {
            	success++;
            	System.out.println("SUCCESS answer=" + answer);
            }
            else {
            	System.out.println("FAILURE answer=" + answer);
            }
            
            alltimeTaken += (System.currentTimeMillis() - stime);
            
            //ImageIO.write(binaryRawImage, "JPEG",new File(inputFile.getParent(), "out" + inputFile.getName()));
            

        }
        
        System.out.println("TP=" + success / total + " (" + success + "/" + total + ") avgtime=" + alltimeTaken / total);

        
    }
    
    
    

}
