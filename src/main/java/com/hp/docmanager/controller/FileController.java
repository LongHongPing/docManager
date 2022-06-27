package com.hp.docmanager.controller;

import com.hp.docmanager.config.AppProperties;
import com.hp.docmanager.model.Page;
import com.hp.docmanager.service.FileService;
import com.hp.docmanager.service.UserService;
import com.hp.docmanager.utils.GeneralUtil;
import com.hp.docmanager.utils.MD5Util;
import com.hp.docmanager.utils.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URLEncoder;
import java.sql.Date;
import java.util.List;

import static java.lang.System.out;

/**
 * Description:
 *
 * @Author hp long
 * @Date 2022/4/6 11:48
 */

@Slf4j
@Controller
@RequestMapping("/file")
public class FileController {

    private static final int normallimit = 20 * 1000 * 1000; //普通用户上传单个文件的最大体积 20mb
    private static final int viplimit = 50 * 1000 * 1000; //vip用户上传单个文件的最大体积 50mb
    private static final int factor = 1000000;  //Mb到字节的转换因子

    @Autowired
    private FileService fileService;
    @Autowired
    private UserService userService;
    @Autowired
    private AppProperties appProperties;

    @RequestMapping("/upload")
    public String upload(@RequestParam("file") CommonsMultipartFile file, HttpSession Session, HttpServletRequest req, String MD5) {
        //session域存的username和传进来的username一致，说明用户名没有造假
        String userName = (String) Session.getAttribute("user_name");
        if (userName == null || "".equals(userName)) {
            return "login";
        }

        String storePath = appProperties.getUploadPath();
        String srcPath = appProperties.getTempPath() + File.separator + userName;
        File store = null;  //目的文件
        try {
            //存在每个用户有一个自己名字命名的文件夹
            store = new File(srcPath,file.getOriginalFilename());
        } catch (Exception e) {
            req.setAttribute("message", "请先选择文件！");
            return "redirect:/searchUserfile";
        }

        long size = file.getSize();  //上传文件的大小

        if (!checkFile(store, size, req)) {
            req.setAttribute("message", "请检查文件！");
            return "redirect:/searchUserfile";
        }

        com.hp.docmanager.model.File fileModel = new com.hp.docmanager.model.File();
        //把文件信息存入数据库
        fileModel.setCreatetime(new Date(new java.util.Date().getTime()));
        fileModel.setFilename(file.getOriginalFilename());
        fileModel.setFilepath(userName);
        fileModel.setFilesize(String.valueOf(size / 1024 + 1));
        fileModel.setCanshare(0);
        fileModel.setMD5(MD5);
        MD5Util.MAP.put(MD5, fileModel);

        String encPath = storePath + File.separator + file.getOriginalFilename();
        Integer flag = null;
        try {
            fileModel.setUser_id(userService.findUserID(userName));
            flag = fileService.insertFile(fileModel);
            file.transferTo(store);
            srcPath = srcPath + File.separator + userName + File.separator + file.getOriginalFilename();
            SecurityUtil.encode(srcPath, encPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (flag != null) {
            req.setAttribute("message", "上传成功！");
        }
        return "redirect:/searchUserfile";
    }

    private boolean checkFile(File store, long size, HttpServletRequest req) {
        if (store.exists()) {
            req.setAttribute("message", "文件已存在");
            return false;
        }
        if (size == 0) {
            req.setAttribute("message", "文件大小不能为0");
            return false;
        } else {
            return true;
        }
    }

    @RequestMapping("/searchFile")
    public String Search(String searchcontent, Page page, Model mv) {
        if(page.getPageSize() == 0){
            page.setPageSize((5));
        }
        if (page.getCurrentPage() == 0) {
            page.setCurrentPage(1);
        }
        page.setSearchContent(searchcontent);
//        PageBean pageBean = new PageBean();
        List<com.hp.docmanager.model.File> fileList;
        try {
            fileList = fileService.getAllFiles(page);
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/index";
        }
        //拿到每页的数据，每个元素就是一条记录
        page.setList(fileList);
        page.setCurrentPage(page.getCurrentPage());
        page.setPageSize(page.getPageSize());
        try {
            page.setTotalRecord(fileService.countShareFiles(searchcontent));
        } catch (Exception e2) {
            e2.printStackTrace();
        }

        mv.addAttribute("pagebean", page);
        mv.addAttribute("searchcontent", searchcontent);

        return "showsearchfiles";
    }

    @RequestMapping("/download")
    public String download(int id, String filename, HttpServletRequest req, HttpServletResponse rep) {
        FileInputStream in = null;
        try {
            String path = fileService.findFilepathById(id); //相对于/upload的路径
            if (path == null || "".equals(path)) {
                req.setAttribute("message", "对不起，您要下载的资源已被删除");
                return "help";
            }
//            path = "C:" + File.separator + "upload" + File.separator + path;
            //解密
            path = appProperties.getUploadPath() + File.separator + filename;
            String deFile = appProperties.getTempPath() + File.separator + filename;
            SecurityUtil.decode(path, deFile);

            //下载
            File file = new File(deFile);
            long fileLength = file.length(); // 记录文件大小
            long pastLength = 0; // 记录已下载文件大小
            int rangeSwitch = 0; // 0：从头开始的全文下载；1：从某字节开始的下载（bytes=27000-）；2：从某字节开始到某字节结束的下载（bytes=27000-39000）
            long toLength = 0; // 记录客户端需要下载的字节段的最后一个字节偏移量（比如bytes=27000-39000，则这个值是为39000）
            long contentLength = 0; // 客户端请求的字节总量
            String rangeBytes = ""; // 记录客户端传来的形如“bytes=27000-”或者“bytes=27000-39000”的内容
            BufferedOutputStream out=null;
            RandomAccessFile raf=null;
            OutputStream os;
            int len = 0;
            byte b[] = new byte[1024];//暂存容器
            if (req.getHeader("Range") != null) { // 客户端请求的下载的文件块的开始字节
                rep.setStatus(javax.servlet.http.HttpServletResponse.SC_PARTIAL_CONTENT);
                rangeBytes = req.getHeader("Range").replaceAll("bytes=", "");
                if (rangeBytes.indexOf('-') == rangeBytes.length() - 1) {// bytes=969998336-
                    rangeSwitch = 1;
                    rangeBytes = rangeBytes.substring(0, rangeBytes.indexOf('-'));
                    pastLength = Long.parseLong(rangeBytes.trim());
                    contentLength = fileLength - pastLength; // 客户端请求的是 969998336 之后的字节
                } else { // bytes=1275856879-1275877358
                    rangeSwitch = 2;
                    String temp0 = rangeBytes.substring(0, rangeBytes.indexOf('-'));
                    String temp2 = rangeBytes.substring(rangeBytes.indexOf('-') + 1, rangeBytes.length());
                    pastLength = Long.parseLong(temp0.trim()); // bytes=1275856879-1275877358，从第 1275856879 个字节开始下载
                    toLength = Long.parseLong(temp2); // bytes=1275856879-1275877358，到第 1275877358 个字节结束
                    contentLength = toLength - pastLength; // 客户端请求的是 1275856879-1275877358 之间的字节
                }
            } else { // 从开始进行下载
                contentLength = fileLength; // 客户端要求全文下载
            }
            rep.reset(); // 告诉客户端允许断点续传多线程连接下载,响应的格式是:Accept-Ranges: bytes
            rep.setHeader("Accept-Ranges", "bytes");
            if (pastLength != 0) {
                // 不是从最开始下载,
                // 响应的格式是:
                // Content-Range: bytes [文件块的开始字节]-[文件的总大小 - 1]/[文件的总大小]
                log.info("不是从开始进行下载,服务器即将开始断点续传...");
                switch (rangeSwitch) {
                    case 1: { // 针对 bytes=27000- 的请求
                        String contentRange = new StringBuffer("bytes ").append(new Long(pastLength).toString()).append("-").append(new Long(fileLength - 1).toString()).append("/").append(new Long(fileLength).toString()).toString();
                        rep.setHeader("Content-Range", contentRange);
                        break;
                    }
                    case 2: { // 针对 bytes=27000-39000 的请求
                        String contentRange = rangeBytes + "/" + new Long(fileLength).toString();
                        rep.setHeader("Content-Range", contentRange);
                        break;
                    }
                    default: {
                        break;
                    }
                }
            } else {
                // 是从开始下载
                log.info("是从开始进行下载");
            }
            try {
                rep.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename,"UTF-8"));
                rep.setContentType(GeneralUtil.setContentType(file.getName())); // set the MIME type.
                rep.addHeader("Content-Length", String.valueOf(contentLength));
                os = rep.getOutputStream();
                out = new BufferedOutputStream(os);
                raf = new RandomAccessFile(file, "r");
                try {
                    switch (rangeSwitch) {
                        case 0: { // 普通下载，或者从头开始的下载
                            // 同1
                        }
                        case 1: { // 针对 bytes=27000- 的请求
                            raf.seek(pastLength); // 形如 bytes=969998336- 的客户端请求，跳过 969998336 个字节
                            int n = 0;
                            while ((n = raf.read(b, 0, 1024)) != -1) {
                                out.write(b, 0, n);
                            }
                            break;
                        }
                        case 2: { // 针对 bytes=27000-39000 的请求
                            raf.seek(pastLength); // 形如 bytes=1275856879-1275877358 的客户端请求，找到第 1275856879 个字节
                            int n = 0;
                            long readLength = 0; // 记录已读字节数
                            while (readLength <= contentLength - 1024) {// 大部分字节在这里读取
                                n = raf.read(b, 0, 1024);
                                readLength += 1024;
                                out.write(b, 0, n);
                            }
                            if (readLength <= contentLength) { // 余下的不足 1024 个字节在这里读取
                                n = raf.read(b, 0, (int) (contentLength - readLength));
                                out.write(b, 0, n);
                            }
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                    out.flush();
                } catch (IOException ie) {
                    log.info("向客户端传输时出现IO异常，但此异常是允许的的，有可能客户端取消了下载，导致此异常，不用关心！");
                }
            } catch (Exception ignored) {
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException ignored) {

                    }
                }
                if (raf != null) {
                    try {
                        raf.close();
                    } catch (IOException ignored) {
                    }
                }
            }
            return null;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping("/Share")
    public String share(Page page, int id, int canshare, HttpSession Session, HttpServletRequest req) {
        //把canshare修改进数据库
        try {
            //检查该文件是否属于该用户,否则不允许修改文件状态
            String username = fileService.findFilepathById(id);
            String login_user = (String) Session.getAttribute("user_name");
            if (login_user.equals(username)) {
                fileService.updateFileById(canshare, id);
            } else { //不通过，可能是人为篡改数据，转发至首页
                req.setAttribute("globalmessage", "该文件可能不属于你");
                return "redirect:/searchUserfile";
            }
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("globalmessage", "未知错误，可能是参数异常");
            return "redirect:/searchUserfile";
        }

        //转发到searchUserFile显示用户的文件
        return "redirect:/searchUserfile";

    }

    @RequestMapping("/deleteFile")
    public String Del(int id, Page page, HttpSession Session, HttpServletRequest req) {
        //判断该用户是否拥有此文件
        try {
            String username = fileService.findFilepathById(id);
            String login_user = (String) Session.getAttribute("user_name");
            String filename = fileService.findFilenameById(id); //查出文件名
            if (login_user.equals(username)) {
                fileService.deleteFileById(id); //删除数据库的该文件记录
                //从硬盘上删除文件
                String storepath = new String("C:" + File.separator + "upload" + File.separator + login_user + File.separator);
                storepath = storepath + filename;
                out.println(storepath);
                File file = new File(storepath);
                if (file.exists()) {
                    file.delete();
                } else {
                    req.setAttribute("globalmessage", "文件已不存在");
                    return "help";
                }
                return "redirect:searchUserfile";
            } else { //不通过，可能是人为篡改数据，转发至全局消息页面
                req.setAttribute("globalmessage", "该文件可能不属于你");
                return "help";
            }
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("globalmessage", "该文件可能不属于你");
            return "redirect:searchUserfile";
        }
    }
}
