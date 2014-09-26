
package com.yt.reader.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.yt.reader.R;
import com.yt.reader.format.cool.CoolMainActivity;
import com.yt.reader.format.fb.FBMainActivity;
import com.yt.reader.format.pdf.PDFActivity;
import com.yt.reader.format.txt.TXTActivity;
import com.yt.reader.model.Book;
import com.yt.reader.optionmenu.TextSearch;

public class FileUtils {

    private List<Book> fileslist;

    public FileUtils(List<Book> list) {
        this.fileslist = list;
    }

    /***
     * 获取路径下面的文件
     * 
     * @param path
     * @return
     */
    public List<Book> getFiles(String path) {
        File file = new File(path);
        File[] array = file.listFiles();
        Book book = null;
        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                book = new Book();
                File subFile = array[i];
                // 如果是文件
                if (subFile.isFile()) {
                    String name = subFile.getName();
                    if (name.indexOf(".") < 0) {
                        continue;
                    }
                    int suffixLoc = name.lastIndexOf(".");
                    String suffix = name.substring(suffixLoc);
                    // 不区分大小写
                    if (suffix.equalsIgnoreCase(".txt") || suffix.equalsIgnoreCase(".epub")
                            || suffix.equalsIgnoreCase(".pdf") || suffix.equalsIgnoreCase(".rtf")
                            || suffix.equalsIgnoreCase(".fb2") || suffix.equalsIgnoreCase(".mobi")
                            || suffix.equalsIgnoreCase(".htm") || suffix.equalsIgnoreCase(".html")
                            || suffix.equalsIgnoreCase(".pdb") || suffix.equalsIgnoreCase(".doc")) {
                        name = subFile.getName();
                    }
                    String fileType = getFileType(name);
                    if (fileType.equals("TXT") || fileType.equals("EPUB") || fileType.equals("PDF")
                            || fileType.equals("RTF") || fileType.equals("FB2")
                            || fileType.equals("MOBI") || fileType.equals("HTM")
                            || fileType.equals("HTML") || fileType.equals("PDB")
                            || fileType.equals("DOC")) {
                        book.setName(name);
                        book.setLastModifyTime(DateUtils.getGreenwichDate(
                                new Date(subFile.lastModified())).getTime());
                        book.setAddedTime(DateUtils.getGreenwichDate(
                                new Date(subFile.lastModified())).getTime());
                        book.setPath(subFile.getPath().substring(0,
                                subFile.getPath().lastIndexOf("/")));
                        book.setFileType(fileType);
                        book.setSize(getFilesize(subFile));
                        book.setCurrentLocation(0);
                        book.setTotalPage(getTotalPage(subFile.getPath()));
                        fileslist.add(book);
                    }
                } else if (subFile.isDirectory() && !subFile.getName().contains(".")) { // 文件夹格式
                    getFiles(subFile.getPath());
                }

            }
        }
        return fileslist;
    }

    /**
     * 读取文件创建时间
     */
    public String getCreateTime(String filePath) {
        
        String strTime = null;
        try {
            Process p = Runtime.getRuntime().exec("cmd /C dir " + filePath + "/tc");
            InputStream is = p.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.endsWith(".txt")) {
                    strTime = line.substring(0, 17);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
       return strTime;
    }

    /****
     * 根据地址获取改目录下的文件和文件夹
     * 
     * @param path
     * @return
     */
    public static Map<String, List<Book>> getFilesAndFolder(String path) {
        Map map = null;
        File file = new File(path);
        File[] array = file.listFiles();
        Book book = null;
        List<Book> filelist = new ArrayList<Book>();
        List<Book> folderList = new ArrayList<Book>();
        if (array != null) {
            map = new HashMap();
            for (int i = 0; i < array.length; i++) {
                book = new Book();
                File subFile = array[i];
                // 如果是文件
                if (subFile.isFile()) {
                    String name = subFile.getName();
                    if (name.indexOf(".") < 0)
                        continue;
                    String fileType = getFileType(name);
                    if (fileType.equals("TXT") || fileType.equals("EPUB") || fileType.equals("PDF")
                            || fileType.equals("RTF") || fileType.equals("FB2")
                            || fileType.equals("MOBI") || fileType.equals("HTM")
                            || fileType.equals("HTML") || fileType.equals("PDB")
                            || fileType.equals("DOC")) {
                        book.setName(name);
                        book.setLastModifyTime(DateUtils.getGreenwichDate(
                                new Date(subFile.lastModified())).getTime());
                        book.setPath(subFile.getPath().substring(0,
                                subFile.getPath().lastIndexOf("/")));
                        book.setFileType(fileType);
                        book.setSize(getFilesize(subFile));
                        book.setAddedTime(DateUtils.getGreenwichDate(
                                new Date(subFile.lastModified())).getTime());
                        book.setCurrentLocation(0);
                        book.setTotalPage(getTotalPage(subFile.getPath()));
                        filelist.add(book);
                    }
                } else if (subFile.isDirectory()) { // 文件夹格式
                    book.setName(subFile.getName());
                    book.setAddedTime(DateUtils.getGreenwichDate(
                            new Date(subFile.lastModified())).getTime());
                    book.setLastModifyTime(DateUtils.getGreenwichDate(
                            new Date(subFile.lastModified())).getTime());
                    book.setPath(subFile.getPath().substring(0, subFile.getPath().lastIndexOf("/")));
                    //book.setSize(getFilesize(subFile));
                    folderList.add(book);

                }

            }
            map.put("file", filelist);
            map.put("folder", folderList);
        }

        return map;

    }

    /***
     * 获取SD卡的路径
     * 
     * @return
     */
    public static File getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取根目录
        }
        return sdDir;
    }

    /**
     * 得到文件大小
     * 
     * @param f
     * @return
     */
    public static String getFilesize(File f) {
        String s;
        double length = f.length();
        if (length < 1024) {
            s = "1KB";
        } else {
            length = length / 1024;
            NumberFormat nf = new DecimalFormat("0.0 ");
            if (length < 1024) {
                s = Double.parseDouble(nf.format(length)) + "KB";
            } else {
                s = Double.parseDouble(nf.format(length / 1024)) + "MB";
            }
        }
        return s;
    }

    /**
     * 得到文件类型。 合理的方案应该是解析文件得到真正的文件类型，如果得不到，再使用文件的后缀名。 本代码直接返回文件后缀名的大写。
     * 
     * @param path
     * @return
     */
    public static String getFileType(String path) {// TODO
        if (path.indexOf(".") == -1)
            return null;
        return path.substring(path.lastIndexOf(".") + 1).toUpperCase();
    }

    public static String getBookName(File file) {
        String name = file.getName();
        return name.substring(0, name.lastIndexOf("."));
    }

    /**
     * 根据书的类型分别调用对应的Activity，以打开该书。
     * 
     * @param activity
     * @param book
     */
    public static void openBook(Activity activity, Book book) {
        //判断文件是否存防止sd卡未加载成功找不到文件
        File file=new File(book.getPath());
        if(!file.exists()){//不存在
            Toast.makeText(activity, activity.getResources().getString(R.string.scanningsd), Toast.LENGTH_SHORT).show();
            return;
        }
        String fileType = book.getFileType();
        if (null == fileType)
            fileType = getFileType(book.getName());
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("name", book.getName());
        bundle.putString("path", book.getPath());
        intent.putExtra("book", bundle);
        Class cls = null;
        if (fileType.equals("PDF")) {// TODO
            cls = PDFActivity.class;
        } else if (fileType.equals("TXT")) {
            cls = TXTActivity.class;
        } else if (fileType.equals("DOC") || (fileType.equals("PDB"))) {
            cls = CoolMainActivity.class;
        } else if (fileType.equals("FB2") || fileType.equals("HTM") || fileType.equals("HTML")
                || fileType.equals("RTF") || fileType.equals("MOBI") || fileType.equals("EPUB")) {
            cls = FBMainActivity.class;
        }
        if (null != cls) {
            intent.setClass(activity, cls);
            activity.startActivity(intent);
        }
    }

    /**
     * 得到书名。 合理的方案应该是解析文件得到真正的书名，如果得不到，再使用文件名。 本代码直接返回去掉后缀的文件名。
     * 
     * @param path
     * @return
     */
    public static String getRealName(File file) {// TODO
        return file.getName().substring(0, file.getName().lastIndexOf("."));
    }

    /**
     * 返回书的总页数或总偏移。
     * 
     * @param path
     * @return
     */
    public static long getTotalPage(String path) {
        File f = new File(path);
        String fileType = getFileType(path);
        if ("TXT".equals(fileType)) {
            return f.length();
        } else {// TODO 其他格式需要自己完成

        }
        return -1;
    }

    /**
     * 得到TXT文件的编码。
     */
    public static String getTXTCharset(File file) {
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1)
                return charset;
            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                return "UTF-16LE";
            } else if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF) {
                return "UTF-16BE";
            } else if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1] == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF) {
                return "UTF-8";
            }
            bis.close();
            bis = new BufferedInputStream(new FileInputStream(file));
            if (!checked) {
                while ((read = bis.read()) != -1) {
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)
                                                          // (0x80
                                                          // - 0xBF),也可能在GB编码内
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) {// 也有可能出错，但是几率较小
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }

            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return charset;
    }

    public static void convertFileEncode(File file, String fromCharset, String toCharset) {
        File convertFile = new File(file.getPath() + ".convert");
        long t1 = new Date().getTime();
        new TextSearch().iconv(fromCharset, toCharset, file.getPath(),
                convertFile.getAbsolutePath());
        while (!convertFile.exists()) {
        }
        long t2 = new Date().getTime();
        Log.v("convert", file.getName() + " with " + fromCharset + " converting: " + (t2 - t1)
                / 1000 + "s");
        if (file.delete())
            Log.v("convert", file.getName() + " is deleted suceeded");
        else
            Log.v("convert", file.getName() + " is deleted failed");
        if (convertFile.renameTo(file)) {
            Log.v("convert", convertFile.getName() + " is renamed to " + file.getName()
                    + " suceeded");
        } else
            Log.v("convert", convertFile.getName() + " is renamed to " + file.getName() + " failed");
    }
}
