package com.hp.securedocdisk.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.hp.securedocdisk.config.AppProperties;
import com.hp.securedocdisk.model.FileDoc;
import com.hp.securedocdisk.model.Page;
import com.hp.securedocdisk.model.PageBean;
import com.hp.securedocdisk.service.FileService;
import com.hp.securedocdisk.service.UserService;
import com.hp.securedocdisk.utils.CommonUtil;
import com.hp.securedocdisk.utils.FileUtil;
import com.hp.securedocdisk.utils.MD5Mapper;
import com.hp.securedocdisk.utils.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URLEncoder;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.System.out;

@Controller
public class FileController {
    @Autowired
    private FileService fileService;
    @Autowired
    private UserService userService;
    @Autowired
    private SecurityUtil securityUtil;
    @Autowired
    private AppProperties appProperties;
    @Autowired
    private ElasticsearchClient client;
    @Autowired
    private FileUtil fileUtil;

    public static final String storePath = "D:" + File.separator + "projects" + File.separator
        + "SecureDocDisk" + File.separator + "files" + File.separator + "upload"; //存储目录
    private static final int normality = 20 * 1000 * 1000; //普通用户上传单个文件的最大体积 20mb
    private static final int vipLimit = 50 * 1000 * 1000; //普通用户上传单个文件的最大体积 50mb
    private static final int factor = 1000000;  //Mb到字节的转换因子

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String upload(@RequestParam("file") MultipartFile file, HttpSession Session, HttpServletRequest req, String MD5) throws IOException {
        //session域存的username和传进来的username一致，说明用户名没有造假
        String user_name = (String) Session.getAttribute("user_name");
        if (user_name == null || "".equals(user_name))
            return "login";

        String tmpPath = appProperties.getTempPath();
        //从数据库查询该用户是否为vip
        Integer isVip = null;
        try {
            isVip = userService.isVip(user_name);
            //把是否是vip的信息带到userhome主页，用于在客户端限制文件上传大小
            req.setAttribute("isvip", isVip);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("message", "未知错误，请重试");
            return "message";
        }

        File store = null;  //目的文件
        try {
            //存在每个用户有一个自己名字命名的文件夹
            store = new File(tmpPath + File.separator + user_name, Objects.requireNonNull(file.getOriginalFilename()));
        } catch (Exception e) {
            req.setAttribute("message", "请先选择文件！");
            return "userhome";
        }

        long size = file.getSize();  //上传文件的大小

        if (!checkFile(store, isVip, size, req)){
            req.setAttribute("message", "检查文件大小等是否符合要求！");
            return "userhome";
        }

        com.hp.securedocdisk.model.File f = new com.hp.securedocdisk.model.File();
        //把文件信息存入数据库
        f.setCreatetime(new Date(new java.util.Date().getTime()));
        f.setFilename(file.getOriginalFilename());
        f.setFilepath(user_name);
        f.setFilesize(String.valueOf(size / 1024 + 1));
        f.setCanshare(0);
        f.setMD5(MD5);
        MD5Mapper.MAP.put(MD5, f);
        Integer flag = null;
        try {
            f.setUser_id(userService.findUserID(user_name));
            flag = fileService.insertFile(f);
            file.transferTo(store);
        } catch (Exception e) {
            e.printStackTrace();
        }

        tmpPath = tmpPath + File.separator + user_name + File.separator + file.getOriginalFilename();
        String content = fileUtil.readContent(tmpPath);
        FileDoc fileDoc = new FileDoc();
        fileDoc.setFilename(file.getOriginalFilename());
        fileDoc.setFilesize(String.valueOf(size / 1024 + 1));
        fileDoc.setContent(content);
        fileDoc.setCreatetime(new Date(new java.util.Date().getTime()));
        IndexResponse indexResponse = client.index(i -> i
                .index("docdisk")
                //传入user对象
                .document(fileDoc));

        //加密上传文件
        String uploadPath = appProperties.getUploadPath() + File.separator + file.getOriginalFilename();
        try {
            securityUtil.encodeFile(tmpPath, uploadPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        log.info("上传文件: " + file.getOriginalFilename() + "完成");
        if (flag != null) {
            req.setAttribute("message", "上传成功！");
        }
        return "redirect:/searchUserfile";
    }

    @RequestMapping("/FastUpload")
    public String fastUpload(HttpSession session, String MD5) {
        String username = (String) session.getAttribute("user_name");
        com.hp.securedocdisk.model.File source = MD5Mapper.MAP.get(MD5);
//        String SourcePath = storePath + File.separator + source.getFilepath() + File.separator + source.getFilename();
//        String userPath = storePath + File.separator + username + File.separator + source.getFilename();
//        fileService.copyFile(SourcePath, userPath);
        source.setFilepath(username);
        try {
            source.setUser_id(userService.findUserID(username));
            fileService.insertFile(source);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/searchUserfile";

    }

    //
    private boolean checkFile(File store, int isVip, long size, HttpServletRequest req) {

        if (store.exists()) {
            req.setAttribute("message", "文件已存在");
            return false;
        }

        if (size == 0) {
            req.setAttribute("message", "文件大小不能为0");
            return false;
        } else if (isVip == 0 && size > normality) {
            req.setAttribute("message", "普通用户最大只能上传" + normality / factor + "Mb的文件");
            return false;
        } else if (isVip == 1 && size > vipLimit) {
            req.setAttribute("message", "VIP用户最大只能上传" + vipLimit / factor + "Mb的文件");
            return false;
        } else return true;
    }

    @RequestMapping("/searchFile")
    public String Search(@RequestParam("searchcontent") String searchContent, Page page, Model mv) {

        if (page.getPageSize() == 0)
            page.setPageSize(5);
        if (page.getCurrentpage() == 0)
            page.setCurrentpage(1);
        page.setSearchcontent(searchContent);
        PageBean pageBean = new PageBean();
//        List<com.hp.securedocdisk.model.File> list;
        List<FileDoc> list;
        try {
//            list = fileService.getAllFiles(page);
            list = fileService.getFiles(searchContent, page);
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/index";
        }
        log.info("检索关键词:" + searchContent + " 已完成");
//        log.info("ES检索结果:" + list);
        //拿到每页的数据，每个元素就是一条记录
        List<com.hp.securedocdisk.model.File> fileList = new ArrayList<>();
        pageBean.setCurrentpage(page.getCurrentpage());
        pageBean.setPagesize(page.getPageSize());
        try {
            int count = 0;
            for(FileDoc doc : list){
                count += fileService.countShareFiles(doc.getFilename());
                page.setSearchcontent(doc.getFilename());
                List<com.hp.securedocdisk.model.File> files = fileService.getAllFiles(page);
//                log.info("Mysql检索结果: " + files);
                fileList.addAll(files);
            }
            pageBean.setTotalrecord(count);
            pageBean.setList(fileList);
        } catch (Exception e2) {
            e2.printStackTrace();
        }

        mv.addAttribute("pagebean", pageBean);
        mv.addAttribute("searchcontent", searchContent);

        return "showsearchfiles";
    }


    @RequestMapping("/download")
    public String download(int id, String filename, HttpSession Session, HttpServletRequest req, HttpServletResponse rep) {
        log.info("执行下载任务: " + filename);
        FileInputStream in = null;

        try {
            String path = fileService.findFilepathById(id); //相对于/upload的路径
            if (path == null || "".equals(path)) {
                req.setAttribute("message", "对不起，您要下载的资源已被删除");
                return "userhome";
            }
//            log.info("获取下载路径: " + path);
            //解密下载文件
            String username = (String) Session.getAttribute("user_name");
            securityUtil.decodeFile(filename, username);

            path = appProperties.getTempPath() + File.separator + path;
//            path = "C:" + File.separator + "upload" + File.separator + path;

            File file = new File(path + File.separator + filename);
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
            byte[] b = new byte[1024];//暂存容器
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
                log.info("不是从开始进行下载！服务器即将开始断点续传...");
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
//                log.info("是从开始进行下载！");
            }
            try {
                rep.addHeader("Content-Disposition", "attachment; filename=" +URLEncoder.encode(filename,"UTF-8"));
                rep.setContentType(CommonUtil.setContentType(file.getName())); // set the MIME type.
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
                    /**
                     * 在写数据的时候， 对于 ClientAbortException 之类的异常， 是因为客户端取消了下载，而服务器端继续向浏览器写入数据时， 抛出这个异常，这个是正常的。 尤其是对于迅雷这种吸血的客户端软件， 明明已经有一个线程在读取 bytes=1275856879-1275877358， 如果短时间内没有读取完毕，迅雷会再启第二个、第三个。。。线程来读取相同的字节段， 直到有一个线程读取完毕，迅雷会 KILL 掉其他正在下载同一字节段的线程， 强行中止字节读出，造成服务器抛 ClientAbortException。 所以，我们忽略这种异常
                     */
                    log.debug("向客户端传输时出现IO异常，但此异常是允许的的。");
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
                String tmpPath = appProperties.getTempPath() + File.separator + login_user + File.separator + filename;
                String encPath = appProperties.getUploadPath() + File.separator + login_user + "_" + filename;
                out.println(tmpPath);
                File file = new File(tmpPath);
                File encFile = new File(encPath);
                if (file.exists()) {
                    file.delete();
                    encFile.delete();
                } else {
                    req.setAttribute("globalmessage", "文件已不存在");
                    return "redirect:/searchUserfile";
                }
                return "redirect:searchUserfile";
            } else { //不通过，可能是人为篡改数据，转发至全局消息页面
                req.setAttribute("globalmessage", "该文件可能不属于你");
                return "redirect:/searchUserfile";
            }
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("globalmessage", "该文件可能不属于你");
            return "redirect:/searchUserfile";
        }
    }
}
